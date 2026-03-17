import pytest
from agents.fraud_detector import process_message


def make_validated_envelope(data: dict) -> dict:
    return {
        "message_id": "test-uuid",
        "timestamp": "2026-03-17T10:00:00Z",
        "source_agent": "transaction_validator",
        "target_agent": "fraud_detector",
        "message_type": "transaction",
        "data": {**data, "status": "validated"},
    }


BASE_TXN = {
    "transaction_id": "TXN001",
    "timestamp": "2026-03-16T09:00:00Z",
    "source_account": "ACC-1001",
    "destination_account": "ACC-2001",
    "amount": "1500.00",
    "currency": "USD",
    "transaction_type": "transfer",
    "status": "validated",
    "metadata": {"channel": "online", "country": "US"},
}


def test_normal_transaction_is_low_risk():
    result = process_message(make_validated_envelope(BASE_TXN))
    assert result["data"]["fraud_risk_score"] == 0
    assert result["data"]["fraud_risk_level"] == "LOW"


def test_high_value_over_10k_adds_3_points():
    data = {**BASE_TXN, "amount": "25000.00"}
    result = process_message(make_validated_envelope(data))
    assert result["data"]["fraud_risk_score"] == 3
    assert result["data"]["fraud_risk_level"] == "MEDIUM"


def test_very_high_value_over_50k_adds_7_points_total():
    data = {**BASE_TXN, "amount": "75000.00"}
    result = process_message(make_validated_envelope(data))
    assert result["data"]["fraud_risk_score"] == 7
    assert result["data"]["fraud_risk_level"] == "HIGH"


def test_amount_exactly_10000_not_flagged():
    data = {**BASE_TXN, "amount": "10000.00"}
    result = process_message(make_validated_envelope(data))
    assert result["data"]["fraud_risk_score"] == 0


def test_amount_just_over_10000_adds_3():
    data = {**BASE_TXN, "amount": "10000.01"}
    result = process_message(make_validated_envelope(data))
    assert result["data"]["fraud_risk_score"] == 3


def test_unusual_hour_2_adds_2_points():
    data = {**BASE_TXN, "timestamp": "2026-03-16T02:47:00Z"}
    result = process_message(make_validated_envelope(data))
    assert result["data"]["fraud_risk_score"] == 2


def test_unusual_hour_3_adds_2_points():
    data = {**BASE_TXN, "timestamp": "2026-03-16T03:00:00Z"}
    result = process_message(make_validated_envelope(data))
    assert result["data"]["fraud_risk_score"] == 2


def test_unusual_hour_4_adds_2_points():
    data = {**BASE_TXN, "timestamp": "2026-03-16T04:59:00Z"}
    result = process_message(make_validated_envelope(data))
    assert result["data"]["fraud_risk_score"] == 2


def test_hour_5_is_not_unusual():
    data = {**BASE_TXN, "timestamp": "2026-03-16T05:00:00Z"}
    result = process_message(make_validated_envelope(data))
    assert result["data"]["fraud_risk_score"] == 0


def test_cross_border_adds_1_point():
    data = {**BASE_TXN, "metadata": {"channel": "api", "country": "DE"}}
    result = process_message(make_validated_envelope(data))
    assert result["data"]["fraud_risk_score"] == 1
    assert result["data"]["fraud_risk_level"] == "LOW"


def test_missing_metadata_no_error_zero_cross_border_score():
    data = {k: v for k, v in BASE_TXN.items() if k != "metadata"}
    result = process_message(make_validated_envelope(data))
    assert result["data"]["fraud_risk_score"] == 0


def test_missing_metadata_country_no_error():
    data = {**BASE_TXN, "metadata": {"channel": "online"}}
    result = process_message(make_validated_envelope(data))
    assert result["data"]["fraud_risk_score"] == 0


def test_txn004_sample_medium_risk():
    # TXN004: EUR 500, 02:47 UTC, country=DE → score=3 (hour+2, cross-border+1)
    data = {
        **BASE_TXN,
        "transaction_id": "TXN004",
        "amount": "500.00",
        "currency": "EUR",
        "timestamp": "2026-03-16T02:47:00Z",
        "metadata": {"channel": "api", "country": "DE"},
    }
    result = process_message(make_validated_envelope(data))
    assert result["data"]["fraud_risk_score"] == 3
    assert result["data"]["fraud_risk_level"] == "MEDIUM"


def test_output_message_agents_updated():
    result = process_message(make_validated_envelope(BASE_TXN))
    assert result["source_agent"] == "fraud_detector"
    assert result["target_agent"] == "reporting_agent"


def test_score_7_is_high():
    # Boundary: exactly 7 should be HIGH
    data = {**BASE_TXN, "amount": "75000.00"}
    result = process_message(make_validated_envelope(data))
    assert result["data"]["fraud_risk_level"] == "HIGH"


def test_score_6_is_medium():
    # Cross-border (1) + unusual hour (2) + >10k (3) = 6 → MEDIUM
    data = {
        **BASE_TXN,
        "amount": "15000.00",
        "timestamp": "2026-03-16T02:00:00Z",
        "metadata": {"channel": "api", "country": "DE"},
    }
    result = process_message(make_validated_envelope(data))
    assert result["data"]["fraud_risk_score"] == 6
    assert result["data"]["fraud_risk_level"] == "MEDIUM"
