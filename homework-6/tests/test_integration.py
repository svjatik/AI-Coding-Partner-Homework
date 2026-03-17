import json
from pathlib import Path
import pytest
from integrator import run_pipeline

# Absolute path — works regardless of working directory
SAMPLE_TRANSACTIONS = Path(__file__).parent.parent / "sample-transactions.json"


def test_full_pipeline_processes_all_8_transactions(tmp_path):
    summary = run_pipeline(
        transactions_file=str(SAMPLE_TRANSACTIONS),
        results_dir=str(tmp_path / "results"),
        _skip_shared_setup=True,
    )
    assert summary["total"] == 8


def test_6_transactions_validated_2_rejected(tmp_path):
    summary = run_pipeline(
        transactions_file=str(SAMPLE_TRANSACTIONS),
        results_dir=str(tmp_path / "results"),
        _skip_shared_setup=True,
    )
    assert summary["validated"] == 6
    assert summary["rejected"] == 2


def test_risk_breakdown_matches_expected(tmp_path):
    summary = run_pipeline(
        transactions_file=str(SAMPLE_TRANSACTIONS),
        results_dir=str(tmp_path / "results"),
        _skip_shared_setup=True,
    )
    # TXN001=LOW, TXN003=LOW, TXN008=LOW
    assert summary["risk_breakdown"]["LOW"] == 3
    # TXN002=MEDIUM, TXN004=MEDIUM
    assert summary["risk_breakdown"]["MEDIUM"] == 2
    # TXN005=HIGH
    assert summary["risk_breakdown"]["HIGH"] == 1


def test_result_files_written_for_all_transactions(tmp_path):
    results_dir = tmp_path / "results"
    run_pipeline(
        transactions_file=str(SAMPLE_TRANSACTIONS),
        results_dir=str(results_dir),
        _skip_shared_setup=True,
    )
    txn_files = list(results_dir.glob("TXN*.json"))
    assert len(txn_files) == 8


def test_pipeline_summary_json_exists(tmp_path):
    results_dir = tmp_path / "results"
    run_pipeline(
        transactions_file=str(SAMPLE_TRANSACTIONS),
        results_dir=str(results_dir),
        _skip_shared_setup=True,
    )
    assert (results_dir / "pipeline_summary.json").exists()


def test_txn006_rejected_invalid_currency(tmp_path):
    results_dir = tmp_path / "results"
    run_pipeline(
        transactions_file=str(SAMPLE_TRANSACTIONS),
        results_dir=str(results_dir),
        _skip_shared_setup=True,
    )
    txn006 = json.loads((results_dir / "TXN006.json").read_text())
    assert txn006["data"]["status"] == "rejected"
    assert "XYZ" in txn006["data"]["rejection_reason"]


def test_txn007_rejected_negative_amount(tmp_path):
    results_dir = tmp_path / "results"
    run_pipeline(
        transactions_file=str(SAMPLE_TRANSACTIONS),
        results_dir=str(results_dir),
        _skip_shared_setup=True,
    )
    txn007 = json.loads((results_dir / "TXN007.json").read_text())
    assert txn007["data"]["status"] == "rejected"


def test_txn005_high_risk(tmp_path):
    results_dir = tmp_path / "results"
    run_pipeline(
        transactions_file=str(SAMPLE_TRANSACTIONS),
        results_dir=str(results_dir),
        _skip_shared_setup=True,
    )
    txn005 = json.loads((results_dir / "TXN005.json").read_text())
    assert txn005["data"]["fraud_risk_level"] == "HIGH"
    assert txn005["data"]["fraud_risk_score"] == 7
