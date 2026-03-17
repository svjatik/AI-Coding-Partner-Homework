import json
import sys
from pathlib import Path

from fastmcp import FastMCP

# Add project root to path so agents can be imported if needed
# Use append (not insert at 0) to avoid shadowing the installed `mcp` package
sys.path.append(str(Path(__file__).parent.parent))

RESULTS_DIR = Path(__file__).parent.parent / "shared" / "results"

mcp = FastMCP("pipeline-status")


@mcp.tool()
def get_transaction_status(transaction_id: str) -> dict:
    """Get the current status of a transaction by its ID."""
    result_file = RESULTS_DIR / f"{transaction_id}.json"
    if not result_file.exists():
        return {
            "error": f"Transaction {transaction_id} not found",
            "transaction_id": transaction_id,
        }
    msg = json.loads(result_file.read_text())
    data = msg["data"]
    return {
        "transaction_id": transaction_id,
        "status": data.get("status"),
        "fraud_risk_level": data.get("fraud_risk_level"),
        "fraud_risk_score": data.get("fraud_risk_score"),
        "rejection_reason": data.get("rejection_reason"),
    }


@mcp.tool()
def list_pipeline_results() -> list:
    """List all processed transactions with their status summary."""
    if not RESULTS_DIR.exists():
        return []
    results = []
    for f in sorted(RESULTS_DIR.glob("*.json")):
        if f.name == "pipeline_summary.json":
            continue
        msg = json.loads(f.read_text())
        data = msg["data"]
        results.append({
            "transaction_id": data.get("transaction_id"),
            "status": data.get("status"),
            "fraud_risk_level": data.get("fraud_risk_level"),
        })
    return results


@mcp.resource("pipeline://summary")
def pipeline_summary() -> str:
    """Returns the latest pipeline run summary as text."""
    summary_file = RESULTS_DIR / "pipeline_summary.json"
    if not summary_file.exists():
        return "No pipeline summary available. Run the pipeline first: python integrator.py"
    return summary_file.read_text()


if __name__ == "__main__":
    mcp.run()
