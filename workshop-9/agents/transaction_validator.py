"""Transaction Validator — validates fields, currency, amount. Rules loaded from config/rules.json."""
import json
import logging
from datetime import datetime, timezone
from decimal import Decimal, InvalidOperation
from pathlib import Path

logger = logging.getLogger(__name__)

_RULES_PATH = Path(__file__).parent.parent / "config" / "rules.json"

REQUIRED_FIELDS = {
    "transaction_id", "amount", "currency",
    "source_account", "destination_account", "timestamp",
}


def _load_rules() -> dict:
    return json.loads(_RULES_PATH.read_text())


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
    rules = _load_rules()

    compliance = rules.get("compliance", {})
    valid_currencies = set(compliance.get("valid_currencies", [
        "USD", "EUR", "GBP", "JPY", "CAD", "AUD", "CHF", "CNY", "SEK", "NZD"
    ]))
    blocked_currencies = set(compliance.get("blocked_currencies", []))
    limits = rules.get("limits", {})
    max_amount = Decimal(str(limits.get("max_transaction_amount", 1000000)))

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

    if amount > max_amount:
        return _reject(message, data, txn_id, f"amount {amount} exceeds max {max_amount}")

    # Currency validation
    currency = data["currency"]
    if currency in blocked_currencies:
        return _reject(message, data, txn_id, f"blocked currency: {currency}")

    if currency not in valid_currencies:
        return _reject(message, data, txn_id, f"invalid currency: {currency}")

    _log(txn_id, "validated")
    return {
        **message,
        "data": {**data, "status": "validated"},
        "source_agent": "transaction_validator",
        "target_agent": "fraud_detector",
    }
