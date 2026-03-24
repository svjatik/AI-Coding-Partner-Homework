"""Pipeline integrator — orchestrates all 4 agents in sequence."""
import json
import logging
import uuid
from datetime import datetime, timezone
from pathlib import Path

from agents import transaction_validator, fraud_detector, notification_agent, reporting_agent


def _setup_logging() -> None:
    root = logging.getLogger()
    if not root.handlers:
        root.setLevel(logging.INFO)
        fmt = logging.Formatter("%(message)s")
        sh = logging.StreamHandler()
        sh.setFormatter(fmt)
        root.addHandler(sh)
        Path("shared").mkdir(exist_ok=True)
        fh = logging.FileHandler("shared/audit.log")
        fh.setFormatter(fmt)
        root.addHandler(fh)


def _setup_dirs() -> None:
    for d in ["shared/input", "shared/processing", "shared/output", "shared/results"]:
        Path(d).mkdir(parents=True, exist_ok=True)


def _make_envelope(txn: dict) -> dict:
    return {
        "message_id": str(uuid.uuid4()),
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "source_agent": "integrator",
        "target_agent": "transaction_validator",
        "message_type": "transaction",
        "data": txn,
    }


def process_transaction(txn: dict, results_dir: str = "shared/results") -> dict:
    """Process a single transaction through the full pipeline. Returns final message."""
    envelope = _make_envelope(txn)

    # Stage 1: validate
    envelope = transaction_validator.process_message(envelope)

    # Stage 2: fraud detection (only if validated)
    if envelope["data"].get("status") == "validated":
        envelope = fraud_detector.process_message(envelope)

        # Stage 3: notification (flagged/high-risk)
        envelope = notification_agent.process_message(envelope)

    # Stage 4: report (always)
    reporting_agent.process_message(envelope, results_dir=results_dir)

    return envelope


def run_pipeline(
    transactions_file: str = "sample-transactions.json",
    results_dir: str = "shared/results",
    _skip_shared_setup: bool = False,
) -> dict:
    if not _skip_shared_setup:
        _setup_dirs()
        _setup_logging()

    txns = json.loads(Path(transactions_file).read_text())

    for txn in txns:
        process_transaction(txn, results_dir=results_dir)

    summary = reporting_agent.generate_summary(results_dir=results_dir)

    print(f"\n{'=' * 40}")
    print(f"Pipeline Complete")
    print(f"{'=' * 40}")
    print(f"Total:      {summary['total']}")
    print(f"Validated:  {summary['validated']}")
    print(f"Rejected:   {summary['rejected']}")
    print(f"Flagged:    {summary['flagged']}")
    rb = summary["risk_breakdown"]
    print(f"Risk:       LOW={rb['LOW']} | MEDIUM={rb['MEDIUM']} | HIGH={rb['HIGH']}")
    if summary["rejection_reasons"]:
        print("\nRejected transactions:")
        for r in summary["rejection_reasons"]:
            print(f"  {r['transaction_id']}: {r['reason']}")
    print()

    return summary


if __name__ == "__main__":
    run_pipeline()
