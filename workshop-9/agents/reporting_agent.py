import json
import logging
from datetime import datetime, timezone
from pathlib import Path

logger = logging.getLogger(__name__)


def _log(txn_id: str, outcome: str) -> None:
    logger.info(
        "%s | reporting_agent | %s | %s",
        datetime.now(timezone.utc).isoformat(), txn_id, outcome,
    )


def process_message(message: dict, results_dir: str = "shared/results") -> dict:
    data = message["data"]
    txn_id = data.get("transaction_id", "UNKNOWN")

    Path(results_dir).mkdir(parents=True, exist_ok=True)
    result_path = Path(results_dir) / f"{txn_id}.json"
    result_path.write_text(json.dumps(message, indent=2, default=str))

    _log(txn_id, f"written to {result_path}")
    return message


def generate_summary(results_dir: str = "shared/results") -> dict:
    results_path = Path(results_dir)
    result_files = [
        f for f in results_path.glob("*.json")
        if f.name != "pipeline_summary.json"
    ]

    total = len(result_files)
    validated = 0
    rejected = 0
    flagged = 0
    risk_counts: dict[str, int] = {"LOW": 0, "MEDIUM": 0, "HIGH": 0}
    rejection_reasons: list[dict] = []

    for f in result_files:
        msg = json.loads(f.read_text())
        data = msg["data"]
        if data.get("status") == "validated":
            validated += 1
            level = data.get("fraud_risk_level", "LOW")
            if level in risk_counts:
                risk_counts[level] += 1
            if data.get("notification_sent"):
                flagged += 1
        else:
            rejected += 1
            rejection_reasons.append({
                "transaction_id": data.get("transaction_id"),
                "reason": data.get("rejection_reason", "unknown"),
            })

    summary = {
        "total": total,
        "validated": validated,
        "rejected": rejected,
        "flagged": flagged,
        "risk_breakdown": risk_counts,
        "rejection_reasons": rejection_reasons,
        "generated_at": datetime.now(timezone.utc).isoformat(),
    }

    summary_path = results_path / "pipeline_summary.json"
    summary_path.write_text(json.dumps(summary, indent=2))

    logger.info(
        "%s | reporting_agent | summary | total=%d validated=%d rejected=%d flagged=%d",
        datetime.now(timezone.utc).isoformat(), total, validated, rejected, flagged,
    )
    return summary
