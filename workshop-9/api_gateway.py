"""REST API Gateway — wraps the banking pipeline behind HTTP endpoints."""
import json
import logging
import sys
import uuid
from datetime import datetime, timezone
from pathlib import Path

from flask import Flask, jsonify, request

# Ensure workshop-9 root is on sys.path so agents/ imports work
sys.path.insert(0, str(Path(__file__).parent))

import integrator  # noqa: E402

app = Flask(__name__)
logging.basicConfig(level=logging.INFO, format="%(message)s")

RESULTS_DIR = Path("shared/results")
INPUT_DIR = Path("shared/input")

REQUIRED_FIELDS = {"transaction_id", "amount", "currency", "source_account", "destination_account", "timestamp"}


def _setup_dirs() -> None:
    for d in ["shared/input", "shared/processing", "shared/output", "shared/results"]:
        Path(d).mkdir(parents=True, exist_ok=True)


# ---------------------------------------------------------------------------
# POST /api/transactions
# ---------------------------------------------------------------------------
@app.route("/api/transactions", methods=["POST"])
def submit_transaction():
    body = request.get_json(silent=True)

    if not body:
        return jsonify({"error": "Request body must be valid JSON"}), 400

    # Validate required fields
    missing = REQUIRED_FIELDS - set(body.keys())
    if missing:
        return jsonify({"error": f"Missing required fields: {', '.join(sorted(missing))}"}), 400

    # Assign a tracking ID if not provided
    tracking_id = body.get("transaction_id") or str(uuid.uuid4())
    body["transaction_id"] = tracking_id

    # Add timestamp if missing
    if not body.get("timestamp"):
        body["timestamp"] = datetime.now(timezone.utc).isoformat()

    # Write raw transaction to shared/input/
    INPUT_DIR.mkdir(parents=True, exist_ok=True)
    input_path = INPUT_DIR / f"{tracking_id}.json"
    input_path.write_text(json.dumps(body, indent=2))

    # Process synchronously through the pipeline
    _setup_dirs()
    integrator.process_transaction(body, results_dir=str(RESULTS_DIR))

    return jsonify({"tracking_id": tracking_id, "status": "accepted"}), 201


# ---------------------------------------------------------------------------
# GET /api/transactions/<id>/status
# ---------------------------------------------------------------------------
@app.route("/api/transactions/<txn_id>/status", methods=["GET"])
def get_transaction_status(txn_id: str):
    result_file = RESULTS_DIR / f"{txn_id}.json"

    if not result_file.exists():
        return jsonify({"error": "Transaction not found", "transaction_id": txn_id}), 404

    message = json.loads(result_file.read_text())
    data = message.get("data", {})
    status = data.get("status", "unknown")
    if status == "validated":
        display_status = "approved"
    else:
        display_status = status

    return jsonify({
        "transaction_id": txn_id,
        "status": display_status,
        "details": data,
    }), 200


# ---------------------------------------------------------------------------
# GET /api/results
# ---------------------------------------------------------------------------
@app.route("/api/results", methods=["GET"])
def list_results():
    if not RESULTS_DIR.exists():
        return jsonify([]), 200

    results = []
    for result_file in sorted(RESULTS_DIR.glob("*.json")):
        if result_file.name == "pipeline_summary.json":
            continue
        message = json.loads(result_file.read_text())
        data = message.get("data", {})
        txn_id = data.get("transaction_id", result_file.stem)
        status = data.get("status", "unknown")
        if status == "validated":
            display_status = "approved"
        else:
            display_status = status

        entry: dict = {
            "transaction_id": txn_id,
            "status": display_status,
            "amount": data.get("amount"),
            "currency": data.get("currency"),
        }
        if data.get("fraud_risk_level"):
            entry["risk_level"] = data["fraud_risk_level"]
        if data.get("rejection_reason"):
            entry["rejection_reason"] = data["rejection_reason"]
        if data.get("notification_sent"):
            entry["flagged"] = True
        results.append(entry)

    return jsonify(results), 200


# ---------------------------------------------------------------------------
# GET /api/health
# ---------------------------------------------------------------------------
@app.route("/api/health", methods=["GET"])
def health():
    return jsonify({"status": "ok"}), 200


if __name__ == "__main__":
    _setup_dirs()
    app.run(host="0.0.0.0", port=5001, debug=False)
