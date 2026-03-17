import argparse
import json
import logging
import uuid
from datetime import datetime, timezone
from decimal import Decimal, InvalidOperation
from pathlib import Path

VALID_CURRENCIES = {"USD", "EUR", "GBP", "JPY", "CAD", "AUD", "CHF", "CNY", "SEK", "NZD"}
REQUIRED_FIELDS = {
    "transaction_id", "amount", "currency",
    "source_account", "destination_account", "timestamp",
}

logger = logging.getLogger(__name__)


def _mask_account(account: str) -> str:
    return f"****{account[-4:]}" if len(account) >= 4 else "****"


def _log(txn_id: str, outcome: str) -> None:
    logger.info(
        "%s | transaction_validator | %s | %s",
        datetime.now(timezone.utc).isoformat(), txn_id, outcome,
    )


def _reject(message: dict, data: dict, txn_id: str, reason: str) -> dict:
    _log(txn_id, f"rejected ({reason})")
    return {
        **message,
        "data": {**data, "status": "rejected", "rejection_reason": reason},
        "source_agent": "transaction_validator",
        "target_agent": "reporting_agent",
    }


def process_message(message: dict) -> dict:
    data = dict(message["data"])
    txn_id = data.get("transaction_id", "UNKNOWN")

    # Required fields
    missing = REQUIRED_FIELDS - set(data.keys())
    if missing:
        return _reject(message, data, txn_id, f"missing fields: {', '.join(sorted(missing))}")

    # Amount validation
    try:
        amount = Decimal(str(data["amount"]))
    except InvalidOperation:
        return _reject(message, data, txn_id, "invalid amount format")

    if amount <= 0:
        return _reject(message, data, txn_id, f"amount must be positive, got {data['amount']}")

    # Currency validation
    if data["currency"] not in VALID_CURRENCIES:
        return _reject(message, data, txn_id, f"invalid currency: {data['currency']}")

    _log(txn_id, "validated")
    return {
        **message,
        "data": {**data, "status": "validated"},
        "source_agent": "transaction_validator",
        "target_agent": "fraud_detector",
    }


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Transaction Validator")
    parser.add_argument("--dry-run", action="store_true", help="Validate without writing files")
    args = parser.parse_args()

    if args.dry_run:
        txns = json.loads(Path("sample-transactions.json").read_text())
        print(f"\n{'TXN ID':<10} {'STATUS':<12} REASON")
        print("-" * 60)
        total = valid = invalid = 0
        for txn in txns:
            total += 1
            envelope = {
                "message_id": str(uuid.uuid4()),
                "timestamp": datetime.now(timezone.utc).isoformat(),
                "source_agent": "integrator",
                "target_agent": "transaction_validator",
                "message_type": "transaction",
                "data": txn,
            }
            result = process_message(envelope)
            status = result["data"]["status"]
            reason = result["data"].get("rejection_reason", "")
            if status == "validated":
                valid += 1
            else:
                invalid += 1
            print(f"{txn.get('transaction_id', '?'):<10} {status:<12} {reason}")
        print(f"\nTotal: {total} | Valid: {valid} | Invalid: {invalid}\n")
