import pytest
from agents.transaction_validator import process_message

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def make_envelope(data: dict) -> dict:
    return {
        "message_id": "test-uuid",
        "timestamp": "2026-03-17T10:00:00Z",
        "source_agent": "integrator",
        "target_agent": "transaction_validator",
        "message_type": "transaction",
        "data": data,
    }


VALID_TXN = {
    "transaction_id": "TXN001",
    "timestamp": "2026-03-16T09:00:00Z",
    "source_account": "ACC-1001",
    "destination_account": "ACC-2001",
    "amount": "1500.00",
    "currency": "USD",
    "transaction_type": "transfer",
    "description": "Test payment",
    "metadata": {"channel": "online", "country": "US"},
}

# ---------------------------------------------------------------------------
# Tests
# ---------------------------------------------------------------------------

def test_valid_transaction_is_validated():
    result = process_message(make_envelope(VALID_TXN))
    assert result["data"]["status"] == "validated"
    assert "rejection_reason" not in result["data"]


def test_missing_transaction_id_rejected():
    data = {k: v for k, v in VALID_TXN.items() if k != "transaction_id"}
    result = process_message(make_envelope(data))
    assert result["data"]["status"] == "rejected"
    assert "transaction_id" in result["data"]["rejection_reason"]


def test_missing_amount_rejected():
    data = {k: v for k, v in VALID_TXN.items() if k != "amount"}
    result = process_message(make_envelope(data))
    assert result["data"]["status"] == "rejected"


def test_missing_currency_rejected():
    data = {k: v for k, v in VALID_TXN.items() if k != "currency"}
    result = process_message(make_envelope(data))
    assert result["data"]["status"] == "rejected"


def test_negative_amount_rejected():
    result = process_message(make_envelope({**VALID_TXN, "amount": "-100.00"}))
    assert result["data"]["status"] == "rejected"
    assert "positive" in result["data"]["rejection_reason"]


def test_zero_amount_rejected():
    result = process_message(make_envelope({**VALID_TXN, "amount": "0.00"}))
    assert result["data"]["status"] == "rejected"


def test_invalid_currency_xyz_rejected():
    result = process_message(make_envelope({**VALID_TXN, "currency": "XYZ"}))
    assert result["data"]["status"] == "rejected"
    assert "XYZ" in result["data"]["rejection_reason"]


def test_valid_eur_currency_accepted():
    result = process_message(make_envelope({**VALID_TXN, "currency": "EUR"}))
    assert result["data"]["status"] == "validated"


def test_validated_message_has_correct_agents():
    result = process_message(make_envelope(VALID_TXN))
    assert result["source_agent"] == "transaction_validator"
    assert result["target_agent"] == "fraud_detector"


def test_rejected_message_targets_reporting_agent():
    data = {**VALID_TXN, "currency": "XYZ"}
    result = process_message(make_envelope(data))
    assert result["source_agent"] == "transaction_validator"
    assert result["target_agent"] == "reporting_agent"


def test_invalid_amount_format_rejected():
    result = process_message(make_envelope({**VALID_TXN, "amount": "not-a-number"}))
    assert result["data"]["status"] == "rejected"
