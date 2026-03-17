import json
from pathlib import Path
import pytest
from agents.reporting_agent import process_message, generate_summary


def make_validated_scored_envelope(txn_id: str, status: str = "validated",
                                   fraud_level: str = "LOW", score: int = 0,
                                   rejection_reason: str = None) -> dict:
    data = {
        "transaction_id": txn_id,
        "amount": "1500.00",
        "currency": "USD",
        "source_account": "ACC-1001",
        "destination_account": "ACC-2001",
        "timestamp": "2026-03-16T09:00:00Z",
        "status": status,
    }
    if status == "validated":
        data["fraud_risk_score"] = score
        data["fraud_risk_level"] = fraud_level
    if rejection_reason:
        data["rejection_reason"] = rejection_reason
    return {
        "message_id": "test-uuid",
        "timestamp": "2026-03-17T10:00:00Z",
        "source_agent": "fraud_detector",
        "target_agent": "reporting_agent",
        "message_type": "transaction",
        "data": data,
    }


def test_process_message_writes_result_file(tmp_path):
    envelope = make_validated_scored_envelope("TXN001")
    process_message(envelope, results_dir=str(tmp_path))
    result_file = tmp_path / "TXN001.json"
    assert result_file.exists()


def test_result_file_contains_valid_json(tmp_path):
    envelope = make_validated_scored_envelope("TXN001")
    process_message(envelope, results_dir=str(tmp_path))
    data = json.loads((tmp_path / "TXN001.json").read_text())
    assert data["data"]["transaction_id"] == "TXN001"


def test_process_message_returns_message(tmp_path):
    envelope = make_validated_scored_envelope("TXN001")
    result = process_message(envelope, results_dir=str(tmp_path))
    assert result["data"]["transaction_id"] == "TXN001"


def test_process_message_creates_results_dir(tmp_path):
    nested = tmp_path / "nested" / "results"
    envelope = make_validated_scored_envelope("TXN001")
    process_message(envelope, results_dir=str(nested))
    assert nested.exists()


def test_rejected_transaction_is_written(tmp_path):
    envelope = make_validated_scored_envelope(
        "TXN006", status="rejected", rejection_reason="invalid currency: XYZ"
    )
    process_message(envelope, results_dir=str(tmp_path))
    assert (tmp_path / "TXN006.json").exists()


def test_generate_summary_counts_totals(tmp_path):
    for i, (status, level) in enumerate([
        ("validated", "LOW"), ("validated", "MEDIUM"), ("rejected", None),
    ], 1):
        envelope = make_validated_scored_envelope(
            f"TXN00{i}", status=status, fraud_level=level or "LOW",
            rejection_reason="reason" if status == "rejected" else None,
        )
        process_message(envelope, results_dir=str(tmp_path))

    summary = generate_summary(results_dir=str(tmp_path))
    assert summary["total"] == 3
    assert summary["validated"] == 2
    assert summary["rejected"] == 1


def test_generate_summary_risk_breakdown(tmp_path):
    for txn_id, level in [("TXN001", "LOW"), ("TXN002", "MEDIUM"), ("TXN005", "HIGH")]:
        envelope = make_validated_scored_envelope(txn_id, fraud_level=level)
        process_message(envelope, results_dir=str(tmp_path))

    summary = generate_summary(results_dir=str(tmp_path))
    assert summary["risk_breakdown"]["LOW"] == 1
    assert summary["risk_breakdown"]["MEDIUM"] == 1
    assert summary["risk_breakdown"]["HIGH"] == 1


def test_generate_summary_writes_pipeline_summary_json(tmp_path):
    envelope = make_validated_scored_envelope("TXN001")
    process_message(envelope, results_dir=str(tmp_path))
    generate_summary(results_dir=str(tmp_path))
    assert (tmp_path / "pipeline_summary.json").exists()


def test_generate_summary_excludes_pipeline_summary_from_count(tmp_path):
    envelope = make_validated_scored_envelope("TXN001")
    process_message(envelope, results_dir=str(tmp_path))
    generate_summary(results_dir=str(tmp_path))
    # Run again — pipeline_summary.json should not be counted
    summary2 = generate_summary(results_dir=str(tmp_path))
    assert summary2["total"] == 1


def test_generate_summary_rejection_reasons(tmp_path):
    envelope = make_validated_scored_envelope(
        "TXN006", status="rejected", rejection_reason="invalid currency: XYZ"
    )
    process_message(envelope, results_dir=str(tmp_path))
    summary = generate_summary(results_dir=str(tmp_path))
    assert len(summary["rejection_reasons"]) == 1
    assert summary["rejection_reasons"][0]["transaction_id"] == "TXN006"
    assert "XYZ" in summary["rejection_reasons"][0]["reason"]
