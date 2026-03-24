"""Notification Agent — generates alerts for flagged or rejected transactions."""
import json
import logging
from datetime import datetime, timezone
from pathlib import Path

logger = logging.getLogger(__name__)

_RULES_PATH = Path(__file__).parent.parent / "config" / "rules.json"


def _load_rules() -> dict:
    return json.loads(_RULES_PATH.read_text())


def _log(txn_id: str, outcome: str) -> None:
    logger.info(
        "%s | notification_agent | %s | %s",
        datetime.now(timezone.utc).isoformat(), txn_id, outcome,
    )


def _should_alert(data: dict, rules: dict) -> tuple[bool, str]:
    notif_cfg = rules.get("notification", {})
    status = data.get("status", "")
    risk_level = data.get("fraud_risk_level", "")

    if status == "rejected" and notif_cfg.get("alert_on_rejection", True):
        reason = data.get("rejection_reason", "unknown")
        return True, f"REJECTED: {reason}"

    min_level = notif_cfg.get("min_risk_level_for_alert", "HIGH")
    risk_order = {"LOW": 0, "MEDIUM": 1, "HIGH": 2}
    if (
        notif_cfg.get("alert_on_high_risk", True)
        and risk_level in risk_order
        and risk_order[risk_level] >= risk_order.get(min_level, 2)
    ):
        return True, f"HIGH RISK (score={data.get('fraud_risk_score', '?')}, level={risk_level})"

    return False, ""


def process_message(message: dict, alerts_dir: str = "shared/output") -> dict:
    data = dict(message["data"])
    txn_id = data.get("transaction_id", "UNKNOWN")
    rules = _load_rules()

    should_alert, alert_reason = _should_alert(data, rules)

    if should_alert:
        Path(alerts_dir).mkdir(parents=True, exist_ok=True)
        alert = {
            "message_id": message["message_id"],
            "timestamp": datetime.now(timezone.utc).isoformat(),
            "source_agent": "notification_agent",
            "target_agent": "ops_team",
            "message_type": "alert",
            "data": {
                "transaction_id": txn_id,
                "alert_reason": alert_reason,
                "amount": data.get("amount"),
                "currency": data.get("currency"),
            },
        }
        alert_path = Path(alerts_dir) / f"alert_{txn_id}.json"
        alert_path.write_text(json.dumps(alert, indent=2))
        _log(txn_id, f"alert generated: {alert_reason}")
    else:
        _log(txn_id, "no alert needed")

    return {
        **message,
        "data": {**data, "notification_sent": should_alert, "alert_reason": alert_reason if should_alert else None},
        "source_agent": "notification_agent",
        "target_agent": "reporting_agent",
    }
