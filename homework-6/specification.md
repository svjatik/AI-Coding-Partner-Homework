# Specification: AI-Powered Multi-Agent Banking Transaction Pipeline

## 1. High-Level Objective
Build a 3-agent Python pipeline that validates, scores for fraud risk, and reports on banking transactions using file-based JSON message passing.

## 2. Mid-Level Objectives
- Transactions with invalid currency codes (not in ISO 4217 whitelist) are rejected with `rejection_reason: "invalid currency: XYZ"`
- Transactions with non-positive amounts are rejected with `rejection_reason: "amount must be positive"`
- Transactions above $10,000 are assigned `fraud_risk_level: "MEDIUM"` or higher with a risk score of at least 3
- Transactions above $50,000 receive a total fraud score of 7 and are assigned `fraud_risk_level: "HIGH"`
- All 8 sample transactions appear in `shared/results/` after running `python integrator.py`
- Test coverage for `agents/` is ≥ 90%; the coverage gate blocks push if below 80%

## 3. Implementation Notes
- **Monetary calculations**: `decimal.Decimal` only — never `float`. Parse string amounts from JSON to `Decimal`; serialize back to string for output.
- **Currency validation**: ISO 4217 whitelist: `{"USD", "EUR", "GBP", "JPY", "CAD", "AUD", "CHF", "CNY", "SEK", "NZD"}`
- **Logging**: audit trail written to stdout and `shared/audit.log`. Format: `TIMESTAMP | AGENT_NAME | TRANSACTION_ID | OUTCOME`
- **PII**: account numbers masked in all logs — show only last 4 characters (`ACC-****`)
- **Decimal serialization**: amounts stored as strings in JSON envelopes; convert to `Decimal` for computation only

## 4. Context
- **Beginning state**: `sample-transactions.json` exists in project root with 8 raw transaction records. No `agents/` code exists. No `shared/` directories populated.
- **Ending state**: All 8 transactions processed. Results in `shared/results/TXN001.json` through `TXN008.json` plus `pipeline_summary.json`. Test coverage ≥ 90%. `README.md` and `HOWTORUN.md` complete.

## 5. Low-Level Tasks

### Task: Transaction Validator
**Prompt**: "Context: Python 3.11 project with file-based JSON message passing. Messages are dicts with `message_id`, `timestamp`, `source_agent`, `target_agent`, `message_type`, and `data` fields.
Task: Implement `agents/transaction_validator.py` with a `process_message(message: dict) -> dict` function that validates banking transactions.
Rules: Use `decimal.Decimal` for amounts (never float). Reject if missing required fields, amount ≤ 0, or currency not in ISO 4217 whitelist {USD, EUR, GBP, JPY, CAD, AUD, CHF, CNY, SEK, NZD}. Log with format: TIMESTAMP | transaction_validator | TXN_ID | OUTCOME. Mask account numbers in logs (last 4 chars only). Also add a `if __name__ == '__main__'` block with `--dry-run` argparse support that loads sample-transactions.json and prints a validation table.
Output: Returns the message dict with `data.status` set to 'validated' or 'rejected', and `data.rejection_reason` added on rejection. Updates `source_agent` and `target_agent` fields."
**File to CREATE**: `agents/transaction_validator.py`
**Function to CREATE**: `process_message(message: dict) -> dict`
**Details**:
- Check required fields: `transaction_id`, `amount`, `currency`, `source_account`, `destination_account`, `timestamp`
- Parse `amount` to `decimal.Decimal`; reject if ≤ 0 or invalid format
- Validate `currency` against ISO 4217 whitelist
- Return updated message with `status: "validated"` or `status: "rejected"` + `rejection_reason`
- `--dry-run` mode: load `sample-transactions.json`, print table of results, no file I/O

### Task: Fraud Detector
**Prompt**: "Context: Python 3.11 pipeline agent. Receives validated transaction messages from `transaction_validator`.
Task: Implement `agents/fraud_detector.py` with `process_message(message: dict) -> dict` that scores transactions for fraud risk.
Rules: Use `decimal.Decimal` for amount comparison. Score: amount > $10,000 = +3pts, amount > $50,000 = +4pts additional (cumulative total +7). Unusual UTC hour (2, 3, or 4 only — NOT 5) = +2pts. Cross-border (metadata.country != 'US') = +1pt. Missing metadata.country treated as domestic (0 pts, no KeyError). Risk: LOW (0-2), MEDIUM (3-6), HIGH (7-10). Log: TIMESTAMP | fraud_detector | TXN_ID | risk=LEVEL score=N.
Output: Returns message with `data.fraud_risk_score` (int) and `data.fraud_risk_level` (LOW/MEDIUM/HIGH) added."
**File to CREATE**: `agents/fraud_detector.py`
**Function to CREATE**: `process_message(message: dict) -> dict`
**Details**:
- Cumulative scoring: both >$10k (+3) and >$50k (+4) fire for $75k transaction (total = 7)
- Hour 5 UTC is NOT unusual — only hours 2, 3, 4
- Gracefully handle missing `metadata` or `metadata.country` (treat as US domestic)

### Task: Reporting Agent
**Prompt**: "Context: Python 3.11 pipeline agent. Receives final processed messages (validated+fraud-scored or rejected).
Task: Implement `agents/reporting_agent.py` with `process_message(message, results_dir='shared/results') -> dict` that writes result files, and `generate_summary(results_dir='shared/results') -> dict` that produces a pipeline summary.
Rules: Use `json.dumps` with `default=str` for Decimal serialization. Create `results_dir` if it doesn't exist. Log: TIMESTAMP | reporting_agent | TXN_ID | OUTCOME. `generate_summary` reads all `*.json` files in results_dir (excluding pipeline_summary.json) to compute totals.
Output: `process_message` writes `{results_dir}/{transaction_id}.json` and returns the message. `generate_summary` writes `{results_dir}/pipeline_summary.json` with: total, validated, rejected, risk_breakdown {LOW,MEDIUM,HIGH}, rejection_reasons [{transaction_id, reason}]."
**File to CREATE**: `agents/reporting_agent.py`
**Functions to CREATE**: `process_message(message: dict, results_dir: str = "shared/results") -> dict`, `generate_summary(results_dir: str = "shared/results") -> dict`
**Details**:
- `process_message` creates `results_dir` if needed, writes full message as JSON
- `generate_summary` scans all result files (not pipeline_summary.json), computes counts
