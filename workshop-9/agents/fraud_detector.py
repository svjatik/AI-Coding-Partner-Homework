"""Fraud Detector — scores transactions. Thresholds loaded from config/rules.json."""
import json
import logging
from datetime import datetime, timezone
from decimal import Decimal
from pathlib import Path

logger = logging.getLogger(__name__)

_RULES_PATH = Path(__file__).parent.parent / "config" / "rules.json"


def _load_rules() -> dict:
    return json.loads(_RULES_PATH.read_text())


def _log(txn_id: str, outcome: str) -> None:
    logger.info(
        "%s | fraud_detector | %s | %s",
        datetime.now(timezone.utc).isoformat(), txn_id, outcome,
    )


def _calculate_score(data: dict, rules: dict) -> int:
    fd = rules.get("fraud_detection", {})
    high_threshold = Decimal(str(fd.get("high_value_threshold", 10000)))
    very_high_threshold = Decimal(str(fd.get("very_high_value_threshold", 50000)))
    unusual_start = fd.get("unusual_hours_start", 2)
    unusual_end = fd.get("unusual_hours_end", 5)
    unusual_hours = set(range(unusual_start, unusual_end))

    score = 0
    amount = Decimal(str(data["amount"]))

    if amount > high_threshold:
        score += 3
    if amount > very_high_threshold:
        score += 4

    timestamp = data.get("timestamp", "")
    try:
        dt = datetime.fromisoformat(timestamp.replace("Z", "+00:00"))
        if dt.hour in unusual_hours:
            score += 2
    except (ValueError, AttributeError):
        pass

    country = data.get("metadata", {}).get("country", "US")
    if country != "US":
        score += 1

    return score


def _risk_level(score: int) -> str:
    if score <= 2:
        return "LOW"
    if score <= 6:
        return "MEDIUM"
    return "HIGH"


def process_message(message: dict) -> dict:
    data = dict(message["data"])
    txn_id = data.get("transaction_id", "UNKNOWN")
    rules = _load_rules()

    score = _calculate_score(data, rules)
    level = _risk_level(score)

    _log(txn_id, f"risk={level} score={score}")

    return {
        **message,
        "data": {**data, "fraud_risk_score": score, "fraud_risk_level": level},
        "source_agent": "fraud_detector",
        "target_agent": "notification_agent",
    }
