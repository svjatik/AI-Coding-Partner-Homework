import logging
from datetime import datetime, timezone
from decimal import Decimal

logger = logging.getLogger(__name__)

_HIGH_VALUE_THRESHOLD = Decimal("10000")
_VERY_HIGH_VALUE_THRESHOLD = Decimal("50000")
_UNUSUAL_HOURS = {2, 3, 4}


def _log(txn_id: str, outcome: str) -> None:
    logger.info(
        "%s | fraud_detector | %s | %s",
        datetime.now(timezone.utc).isoformat(), txn_id, outcome,
    )


def _calculate_score(data: dict) -> int:
    score = 0
    amount = Decimal(str(data["amount"]))

    if amount > _HIGH_VALUE_THRESHOLD:
        score += 3
    if amount > _VERY_HIGH_VALUE_THRESHOLD:
        score += 4

    timestamp = data.get("timestamp", "")
    try:
        dt = datetime.fromisoformat(timestamp.replace("Z", "+00:00"))
        if dt.hour in _UNUSUAL_HOURS:
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

    score = _calculate_score(data)
    level = _risk_level(score)

    _log(txn_id, f"risk={level} score={score}")

    return {
        **message,
        "data": {**data, "fraud_risk_score": score, "fraud_risk_level": level},
        "source_agent": "fraud_detector",
        "target_agent": "reporting_agent",
    }
