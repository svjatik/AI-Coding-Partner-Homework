# Banking Pipeline Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a 3-agent Python banking transaction pipeline (Validator → Fraud Detector → Reporting Agent) with TDD, skills, hooks, MCP server, and full documentation.

**Architecture:** Sequential orchestrator — `integrator.py` loops over transactions and calls each agent's `process_message()` in-process. Agents communicate via a JSON envelope format; results are written to `shared/results/`. Tests use `tmp_path` to avoid touching real filesystem.

**Tech Stack:** Python 3.11+, pytest, pytest-cov, fastmcp

---

## File Map

| File | Responsibility |
|---|---|
| `integrator.py` | Orchestrates pipeline: sets up dirs, loads transactions, chains agents |
| `agents/__init__.py` | Package marker (empty) |
| `agents/transaction_validator.py` | Validates fields, amount, currency; supports `--dry-run` CLI |
| `agents/fraud_detector.py` | Scores fraud risk; returns LOW/MEDIUM/HIGH |
| `agents/reporting_agent.py` | Writes result files; generates pipeline summary |
| `mcp/server.py` | FastMCP server: `get_transaction_status`, `list_pipeline_results`, `pipeline://summary` |
| `tests/test_transaction_validator.py` | Unit tests for validator |
| `tests/test_fraud_detector.py` | Unit tests for fraud detector |
| `tests/test_reporting_agent.py` | Unit tests for reporting agent (uses `tmp_path`) |
| `tests/test_integration.py` | Full pipeline integration test (uses `tmp_path`, `_skip_shared_setup=True`) |
| `conftest.py` | Adds `homework-6/` to `sys.path` so imports work from any working directory |
| `pytest.ini` | Sets `testpaths = tests` |
| `.claude/commands/write-spec.md` | Skill: generate specification from template |
| `.claude/commands/run-pipeline.md` | Skill: run full pipeline |
| `.claude/commands/validate-transactions.md` | Skill: dry-run validation only |
| `.claude/settings.json` | Coverage gate hook — blocks push if coverage < 80% |
| `mcp.json` | MCP server configuration (context7 + pipeline-status) |
| `specification.md` | 5-section project specification |
| `agents.md` | Pipeline-specific agent guidelines |
| `research-notes.md` | 2+ context7 queries documented |
| `requirements.txt` | Python dependencies |
| `README.md` | Author name, ASCII diagram, agent table, tech stack |
| `HOWTORUN.md` | Step-by-step setup and demo |

---

### Task 1: Project Scaffold

**Files:**
- Create: `homework-6/requirements.txt`
- Create: `homework-6/agents/__init__.py`
- Create: `homework-6/shared/input/.gitkeep`
- Create: `homework-6/shared/processing/.gitkeep`
- Create: `homework-6/shared/output/.gitkeep`
- Create: `homework-6/shared/results/.gitkeep`
- Create: `homework-6/mcp/__init__.py`
- Create: `homework-6/docs/screenshots/.gitkeep`
- Create: `homework-6/specification.md`
- Create: `homework-6/agents.md`
- Create: `homework-6/.claude/commands/write-spec.md`

- [ ] **Step 1.1: Create requirements.txt**

```
fastmcp>=2.0.0
pytest>=7.4.0
pytest-cov>=4.1.0
```

File: `homework-6/requirements.txt`

- [ ] **Step 1.2: Create directory structure and test infrastructure**

Run in `homework-6/`:
```bash
mkdir -p agents mcp tests shared/input shared/processing shared/output shared/results docs/screenshots .claude/commands
touch agents/__init__.py mcp/__init__.py tests/__init__.py
touch shared/input/.gitkeep shared/processing/.gitkeep shared/output/.gitkeep shared/results/.gitkeep docs/screenshots/.gitkeep
```

Create `homework-6/conftest.py`:
```python
import sys
from pathlib import Path

# Ensure agents/ and integrator.py are importable regardless of working directory
sys.path.insert(0, str(Path(__file__).parent))
```

Create `homework-6/pytest.ini`:
```ini
[pytest]
testpaths = tests
```

- [ ] **Step 1.3: Install dependencies**

Run in `homework-6/`:
```bash
pip install -r requirements.txt
```

Expected: packages install without error.

- [ ] **Step 1.4: Create specification.md**

Create `homework-6/specification.md` with this content:

```markdown
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
```

- [ ] **Step 1.5: Create agents.md**

Create `homework-6/agents.md`:

```markdown
# AI Agent Guidelines for Banking Transaction Pipeline

## Tech Stack
- **Language**: Python 3.11+
- **Testing**: pytest 7.4+, pytest-cov 4.1+
- **MCP**: fastmcp 2.0+
- **No external dependencies for agents** (stdlib only: json, logging, decimal, datetime, pathlib, uuid, argparse)

## Agent Responsibilities

| Agent | File | Input | Output |
|---|---|---|---|
| Transaction Validator | `agents/transaction_validator.py` | Raw transaction envelope | Envelope with `status: validated/rejected` |
| Fraud Detector | `agents/fraud_detector.py` | Validated envelope | Envelope with `fraud_risk_score`, `fraud_risk_level` |
| Reporting Agent | `agents/reporting_agent.py` | Final envelope | Written to `shared/results/{txn_id}.json` |

## Message Envelope Format

```json
{
  "message_id": "uuid4-string",
  "timestamp": "ISO-8601",
  "source_agent": "transaction_validator",
  "target_agent": "fraud_detector",
  "message_type": "transaction",
  "data": { ...transaction fields + agent results... }
}
```

## Domain Rules
- **NEVER** use `float` for monetary values — always `decimal.Decimal`
- **NEVER** log full account numbers — mask to last 4 characters
- **ALWAYS** validate ISO 4217 currency codes: `{USD, EUR, GBP, JPY, CAD, AUD, CHF, CNY, SEK, NZD}`
- Decimal amounts are stored as **strings** in JSON; parse to Decimal for computation only
- Missing `metadata.country` treated as domestic (no cross-border penalty)

## Audit Log Format
```
TIMESTAMP | AGENT_NAME | TRANSACTION_ID | OUTCOME
```
Written to: stdout + `shared/audit.log`

## File-Based Communication Protocol
```
shared/
├── input/       ← initial message drop
├── processing/  ← in-flight
├── output/      ← staged output
└── results/     ← final outcomes
```
```

- [ ] **Step 1.6: Create write-spec.md skill**

Create `homework-6/.claude/commands/write-spec.md`:

```markdown
Generate a complete specification.md for the banking transaction pipeline.

Follow this template structure exactly:

1. **High-Level Objective** — one sentence describing what the pipeline does
2. **Mid-Level Objectives** — 4-5 testable requirements (things you can write a test for)
3. **Implementation Notes** — include: decimal.Decimal only, ISO 4217 currency whitelist, audit logging format, PII masking rules
4. **Context** — beginning state: sample-transactions.json exists with 8 records; ending state: all results in shared/results/, test coverage ≥ 90%
5. **Low-Level Tasks** — one entry per agent using this exact format:
   ```
   Task: [Agent Name]
   Prompt: "Context: [...] Task: [...] Rules: [...] Output: [...]"
   File to CREATE: agents/[name].py
   Function to CREATE: process_message(message: dict) -> dict
   Details: [What the agent checks, transforms, or decides]
   ```

Save the result to specification.md in the homework-6 directory.
```

- [ ] **Step 1.7: Commit scaffold**

Run in repo root:
```bash
git add homework-6/requirements.txt homework-6/agents/ homework-6/mcp/ homework-6/tests/ homework-6/shared/ homework-6/docs/ homework-6/.claude/ homework-6/specification.md homework-6/agents.md homework-6/conftest.py homework-6/pytest.ini
git commit -m "feat(hw6): add project scaffold, specification, and write-spec skill"
```

- [ ] **Step 1.8: Capture spec-produced.png**

Invoke the `/write-spec` skill in Claude Code and screenshot the output showing `specification.md` being generated.

```bash
# Screenshot the Claude Code terminal while /write-spec runs
# Save to: homework-6/docs/screenshots/spec-produced.png
```

---

### Task 2: Transaction Validator (TDD)

**Files:**
- Create: `homework-6/tests/test_transaction_validator.py`
- Create: `homework-6/agents/transaction_validator.py`

- [ ] **Step 2.1: Write the failing tests**

Create `homework-6/tests/test_transaction_validator.py`:

```python
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
```

- [ ] **Step 2.2: Run tests to verify they fail**

```bash
cd homework-6 && python -m pytest tests/test_transaction_validator.py -v
```

Expected: `ImportError` or `ModuleNotFoundError` — validator doesn't exist yet.

- [ ] **Step 2.3: Implement transaction_validator.py**

Create `homework-6/agents/transaction_validator.py`:

```python
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
```

- [ ] **Step 2.4: Run tests to verify they pass**

```bash
cd homework-6 && python -m pytest tests/test_transaction_validator.py -v
```

Expected: All 11 tests PASS.

- [ ] **Step 2.5: Verify dry-run mode works**

```bash
cd homework-6 && python agents/transaction_validator.py --dry-run
```

Expected: Table showing TXN001–TXN008, with TXN006 (XYZ) and TXN007 (-100.00) as rejected.

- [ ] **Step 2.6: Commit**

```bash
git add homework-6/agents/transaction_validator.py homework-6/tests/test_transaction_validator.py
git commit -m "feat(hw6): add Transaction Validator with TDD (11 tests)"
```

---

### Task 3: Fraud Detector (TDD)

**Files:**
- Create: `homework-6/tests/test_fraud_detector.py`
- Create: `homework-6/agents/fraud_detector.py`

- [ ] **Step 3.1: Write the failing tests**

Create `homework-6/tests/test_fraud_detector.py`:

```python
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
```

- [ ] **Step 3.2: Run tests to verify they fail**

```bash
cd homework-6 && python -m pytest tests/test_fraud_detector.py -v
```

Expected: `ImportError` — fraud_detector doesn't exist yet.

- [ ] **Step 3.3: Implement fraud_detector.py**

Create `homework-6/agents/fraud_detector.py`:

```python
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
```

- [ ] **Step 3.4: Run tests to verify they pass**

```bash
cd homework-6 && python -m pytest tests/test_fraud_detector.py -v
```

Expected: All 16 tests PASS.

- [ ] **Step 3.5: Commit**

```bash
git add homework-6/agents/fraud_detector.py homework-6/tests/test_fraud_detector.py
git commit -m "feat(hw6): add Fraud Detector with TDD (16 tests)"
```

---

### Task 4: Reporting Agent (TDD)

**Files:**
- Create: `homework-6/tests/test_reporting_agent.py`
- Create: `homework-6/agents/reporting_agent.py`

- [ ] **Step 4.1: Write the failing tests**

Create `homework-6/tests/test_reporting_agent.py`:

```python
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
```

- [ ] **Step 4.2: Run tests to verify they fail**

```bash
cd homework-6 && python -m pytest tests/test_reporting_agent.py -v
```

Expected: `ImportError` — reporting_agent doesn't exist yet.

- [ ] **Step 4.3: Implement reporting_agent.py**

Create `homework-6/agents/reporting_agent.py`:

```python
import json
import logging
from datetime import datetime, timezone
from pathlib import Path

logger = logging.getLogger(__name__)


def _log(txn_id: str, outcome: str) -> None:
    logger.info(
        "%s | reporting_agent | %s | %s",
        datetime.now(timezone.utc).isoformat(), txn_id, outcome,
    )


def process_message(message: dict, results_dir: str = "shared/results") -> dict:
    data = message["data"]
    txn_id = data.get("transaction_id", "UNKNOWN")

    Path(results_dir).mkdir(parents=True, exist_ok=True)
    result_path = Path(results_dir) / f"{txn_id}.json"
    result_path.write_text(json.dumps(message, indent=2, default=str))

    _log(txn_id, f"written to {result_path}")
    return message


def generate_summary(results_dir: str = "shared/results") -> dict:
    results_path = Path(results_dir)
    result_files = [
        f for f in results_path.glob("*.json")
        if f.name != "pipeline_summary.json"
    ]

    total = len(result_files)
    validated = 0
    rejected = 0
    risk_counts: dict[str, int] = {"LOW": 0, "MEDIUM": 0, "HIGH": 0}
    rejection_reasons: list[dict] = []

    for f in result_files:
        msg = json.loads(f.read_text())
        data = msg["data"]
        if data.get("status") == "validated":
            validated += 1
            level = data.get("fraud_risk_level", "LOW")
            if level in risk_counts:
                risk_counts[level] += 1
        else:
            rejected += 1
            rejection_reasons.append({
                "transaction_id": data.get("transaction_id"),
                "reason": data.get("rejection_reason", "unknown"),
            })

    summary = {
        "total": total,
        "validated": validated,
        "rejected": rejected,
        "risk_breakdown": risk_counts,
        "rejection_reasons": rejection_reasons,
        "generated_at": datetime.now(timezone.utc).isoformat(),
    }

    summary_path = results_path / "pipeline_summary.json"
    summary_path.write_text(json.dumps(summary, indent=2))

    logger.info(
        "%s | reporting_agent | summary | total=%d validated=%d rejected=%d",
        datetime.now(timezone.utc).isoformat(), total, validated, rejected,
    )
    return summary
```

- [ ] **Step 4.4: Run tests to verify they pass**

```bash
cd homework-6 && python -m pytest tests/test_reporting_agent.py -v
```

Expected: All 10 tests PASS.

- [ ] **Step 4.5: Commit**

```bash
git add homework-6/agents/reporting_agent.py homework-6/tests/test_reporting_agent.py
git commit -m "feat(hw6): add Reporting Agent with TDD (10 tests)"
```

---

### Task 5: Integrator + Integration Test

**Files:**
- Create: `homework-6/tests/test_integration.py`
- Create: `homework-6/integrator.py`

- [ ] **Step 5.1: Write the failing integration test**

Create `homework-6/tests/test_integration.py`:

```python
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
```

- [ ] **Step 5.2: Run integration test to verify it fails**

```bash
cd homework-6 && python -m pytest tests/test_integration.py -v
```

Expected: `ImportError` — integrator doesn't exist yet.

- [ ] **Step 5.3: Implement integrator.py**

Create `homework-6/integrator.py`:

```python
import json
import logging
import uuid
from datetime import datetime, timezone
from pathlib import Path

from agents import transaction_validator, fraud_detector, reporting_agent


def _setup_logging() -> None:
    root = logging.getLogger()
    if not root.handlers:
        root.setLevel(logging.INFO)
        fmt = logging.Formatter("%(message)s")
        sh = logging.StreamHandler()
        sh.setFormatter(fmt)
        root.addHandler(sh)
        Path("shared").mkdir(exist_ok=True)
        fh = logging.FileHandler("shared/audit.log")
        fh.setFormatter(fmt)
        root.addHandler(fh)


def _setup_dirs() -> None:
    for d in ["shared/input", "shared/processing", "shared/output", "shared/results"]:
        Path(d).mkdir(parents=True, exist_ok=True)


def _make_envelope(txn: dict) -> dict:
    return {
        "message_id": str(uuid.uuid4()),
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "source_agent": "integrator",
        "target_agent": "transaction_validator",
        "message_type": "transaction",
        "data": txn,
    }


def run_pipeline(
    transactions_file: str = "sample-transactions.json",
    results_dir: str = "shared/results",
    _skip_shared_setup: bool = False,
) -> dict:
    if not _skip_shared_setup:
        _setup_dirs()
        _setup_logging()

    txns = json.loads(Path(transactions_file).read_text())

    for txn in txns:
        envelope = _make_envelope(txn)

        # Stage 1: validate
        envelope = transaction_validator.process_message(envelope)

        # Stage 2: fraud detection (only if validated)
        if envelope["data"].get("status") == "validated":
            envelope = fraud_detector.process_message(envelope)

        # Stage 3: report (always)
        reporting_agent.process_message(envelope, results_dir=results_dir)

    summary = reporting_agent.generate_summary(results_dir=results_dir)

    print(f"\n{'=' * 40}")
    print(f"Pipeline Complete")
    print(f"{'=' * 40}")
    print(f"Total:      {summary['total']}")
    print(f"Validated:  {summary['validated']}")
    print(f"Rejected:   {summary['rejected']}")
    rb = summary["risk_breakdown"]
    print(f"Risk:       LOW={rb['LOW']} | MEDIUM={rb['MEDIUM']} | HIGH={rb['HIGH']}")
    if summary["rejection_reasons"]:
        print("\nRejected transactions:")
        for r in summary["rejection_reasons"]:
            print(f"  {r['transaction_id']}: {r['reason']}")
    print()

    return summary


if __name__ == "__main__":
    run_pipeline()
```

- [ ] **Step 5.4: Run all tests to verify they pass**

```bash
cd homework-6 && python -m pytest tests/ -v
```

Expected: All 45 tests PASS.

- [ ] **Step 5.5: Run pipeline end-to-end**

```bash
cd homework-6 && python integrator.py
```

Expected: Console summary showing 8 total, 6 validated, 2 rejected. Files appear in `shared/results/`.

- [ ] **Step 5.6: Check coverage**

```bash
cd homework-6 && python -m pytest tests/ --cov=agents --cov-report=term-missing
```

Expected: ≥ 90% coverage for `agents/`.

- [ ] **Step 5.7: Commit**

```bash
git add homework-6/integrator.py homework-6/tests/test_integration.py
git commit -m "feat(hw6): add integrator and integration tests (all 45 tests pass)"
```

---

### Task 6: Skills & Coverage Hook

**Files:**
- Create: `homework-6/.claude/commands/run-pipeline.md`
- Create: `homework-6/.claude/commands/validate-transactions.md`
- Create: `homework-6/.claude/settings.json`

- [ ] **Step 6.1: Create run-pipeline.md skill**

Create `homework-6/.claude/commands/run-pipeline.md`:

```markdown
Run the multi-agent banking pipeline end-to-end.

Steps:
1. Check that sample-transactions.json exists in homework-6/
2. Clear shared/ directories (remove and recreate shared/input, shared/processing, shared/output, shared/results)
3. Run the pipeline: cd homework-6 && python integrator.py
4. Show a summary of results from shared/results/pipeline_summary.json
5. Report any transactions that were rejected and why (look for "status": "rejected" in shared/results/*.json)
```

- [ ] **Step 6.2: Create validate-transactions.md skill**

Create `homework-6/.claude/commands/validate-transactions.md`:

```markdown
Validate all transactions in sample-transactions.json without processing them.

Steps:
1. Run the validator in dry-run mode: cd homework-6 && python agents/transaction_validator.py --dry-run
2. Report: total count, valid count, invalid count, reasons for rejection
3. Show a table of results
```

- [ ] **Step 6.3: Create .claude/settings.json with coverage gate**

Create `homework-6/.claude/settings.json`:

```json
{
  "hooks": {
    "PreToolUse": [
      {
        "matcher": "Bash",
        "hooks": [
          {
            "type": "command",
            "command": "bash -c 'if echo \"$CLAUDE_TOOL_INPUT\" | grep -q \"git push\"; then REPO=$(git rev-parse --show-toplevel) && cd \"$REPO/homework-6\" && python -m pytest tests/ --cov=agents --cov-fail-under=80 -q 2>&1; exit $?; fi'"
          }
        ]
      }
    ]
  }
}
```

- [ ] **Step 6.4: Verify hook syntax is valid JSON**

```bash
python -m json.tool homework-6/.claude/settings.json
```

Expected: Prints formatted JSON without errors.

- [ ] **Step 6.5: Commit**

```bash
git add homework-6/.claude/commands/run-pipeline.md homework-6/.claude/commands/validate-transactions.md homework-6/.claude/settings.json
git commit -m "feat(hw6): add run-pipeline and validate-transactions skills; add coverage gate hook"
```

---

### Task 7: MCP Server + Research Notes

**Files:**
- Create: `homework-6/mcp/server.py`
- Create: `homework-6/mcp.json`
- Create: `homework-6/research-notes.md`

- [ ] **Step 7.1: Use context7 to research fastmcp**

Use the context7 MCP server to look up FastMCP documentation. In Claude Code, run:

```
Use context7 to look up the fastmcp library — specifically how to define tools and resources with @mcp.tool() and @mcp.resource() decorators.
```

Document the result in research-notes.md (see Step 7.3).

- [ ] **Step 7.2: Use context7 to research Python decimal module**

```
Use context7 to look up the Python decimal module — specifically ROUND_HALF_UP and how to set precision context for financial calculations.
```

Document the result in research-notes.md (see Step 7.3).

- [ ] **Step 7.2.5: Verify context7 queries returned results**

Before writing research-notes.md, confirm both queries returned library IDs and useful information. Fill in the actual library IDs returned by context7. Do NOT commit research-notes.md with placeholder text "(fill in after running the query)".

- [ ] **Step 7.3: Create research-notes.md**

Create `homework-6/research-notes.md` with the documented queries:

```markdown
# Research Notes — Context7 Queries

## Query 1: FastMCP tool and resource decorators
- Search: "fastmcp tools resources decorators"
- context7 library ID: (fill in after running the query)
- Applied: Used `@mcp.tool()` decorator to expose `get_transaction_status` and `list_pipeline_results` as callable tools. Used `@mcp.resource("pipeline://summary")` to expose the pipeline summary as a readable resource. FastMCP handles the MCP protocol automatically when `mcp.run()` is called.

## Query 2: Python decimal module for financial arithmetic
- Search: "Python decimal module ROUND_HALF_UP financial"
- context7 library ID: (fill in after running the query)
- Applied: Used `decimal.Decimal(str(amount))` to parse string amounts from JSON envelopes without floating-point precision loss. Applied `ROUND_HALF_UP` rounding mode awareness — all comparisons use Decimal arithmetic to avoid issues like `float("10000.01") > 10000` being imprecise.
```

- [ ] **Step 7.4: Implement mcp/server.py**

Create `homework-6/mcp/server.py`:

```python
import json
import sys
from pathlib import Path

# Add project root to path so agents can be imported if needed
sys.path.insert(0, str(Path(__file__).parent.parent))

from fastmcp import FastMCP

RESULTS_DIR = Path(__file__).parent.parent / "shared" / "results"

mcp = FastMCP("pipeline-status")


@mcp.tool()
def get_transaction_status(transaction_id: str) -> dict:
    """Get the current status of a transaction by its ID."""
    result_file = RESULTS_DIR / f"{transaction_id}.json"
    if not result_file.exists():
        return {
            "error": f"Transaction {transaction_id} not found",
            "transaction_id": transaction_id,
        }
    msg = json.loads(result_file.read_text())
    data = msg["data"]
    return {
        "transaction_id": transaction_id,
        "status": data.get("status"),
        "fraud_risk_level": data.get("fraud_risk_level"),
        "fraud_risk_score": data.get("fraud_risk_score"),
        "rejection_reason": data.get("rejection_reason"),
    }


@mcp.tool()
def list_pipeline_results() -> list:
    """List all processed transactions with their status summary."""
    if not RESULTS_DIR.exists():
        return []
    results = []
    for f in sorted(RESULTS_DIR.glob("*.json")):
        if f.name == "pipeline_summary.json":
            continue
        msg = json.loads(f.read_text())
        data = msg["data"]
        results.append({
            "transaction_id": data.get("transaction_id"),
            "status": data.get("status"),
            "fraud_risk_level": data.get("fraud_risk_level"),
        })
    return results


@mcp.resource("pipeline://summary")
def pipeline_summary() -> str:
    """Returns the latest pipeline run summary as text."""
    summary_file = RESULTS_DIR / "pipeline_summary.json"
    if not summary_file.exists():
        return "No pipeline summary available. Run the pipeline first: python integrator.py"
    return summary_file.read_text()


if __name__ == "__main__":
    mcp.run()
```

- [ ] **Step 7.5: Create mcp.json**

Create `homework-6/mcp.json`:

```json
{
  "mcpServers": {
    "context7": {
      "command": "npx",
      "args": ["-y", "@upstash/context7-mcp@latest"]
    },
    "pipeline-status": {
      "command": "python",
      "args": ["mcp/server.py"]
    }
  }
}
```

- [ ] **Step 7.6: Verify MCP server starts**

```bash
cd homework-6 && python mcp/server.py &
sleep 2
kill %1
```

Expected: Server starts without errors (may print startup message).

- [ ] **Step 7.7: Commit**

```bash
git add homework-6/mcp/server.py homework-6/mcp.json homework-6/research-notes.md
git commit -m "feat(hw6): add FastMCP server, mcp.json, and research notes"
```

---

### Task 8: Documentation

**Files:**
- Create: `homework-6/README.md`
- Create: `homework-6/HOWTORUN.md`

- [ ] **Step 8.1: Create README.md**

Create `homework-6/README.md`:

```markdown
# AI-Powered Multi-Agent Banking Pipeline

**Created by [YOUR NAME HERE]**

A 3-agent Python pipeline that validates, scores for fraud risk, and reports on banking transactions using file-based JSON message passing.

## What It Does

The pipeline processes raw banking transaction records through three cooperating agents:

1. **Transaction Validator** checks required fields, positive amounts, and ISO 4217 currency codes
2. **Fraud Detector** scores transactions for risk using amount thresholds, unusual timing, and cross-border indicators
3. **Reporting Agent** writes result files and generates a pipeline summary

Rejected transactions are logged with a reason; validated transactions receive a fraud risk score (LOW / MEDIUM / HIGH) before final reporting.

## Pipeline Architecture

```
sample-transactions.json
         │
    integrator.py
         │
    ┌────▼──────────────────────────────────────┐
    │  For each transaction:                     │
    │                                            │
    │  transaction_validator.process_message()   │
    │       │ validated          │ rejected       │
    │       ▼                   │                │
    │  fraud_detector            │                │
    │  .process_message()        │                │
    │       │                   │                │
    │       └──────────────►────┘                │
    │                       │                    │
    │  reporting_agent.process_message()          │
    │       │                                    │
    └───────▼────────────────────────────────────┘
         │
    shared/results/TXN001.json ... TXN008.json
    shared/results/pipeline_summary.json
```

## Agent Responsibilities

| Agent | File | Input | Output |
|---|---|---|---|
| Transaction Validator | `agents/transaction_validator.py` | Raw transaction | `status: validated/rejected` |
| Fraud Detector | `agents/fraud_detector.py` | Validated transaction | `fraud_risk_score`, `fraud_risk_level` |
| Reporting Agent | `agents/reporting_agent.py` | Final transaction | Result JSON files + summary |

## Tech Stack

| Component | Technology |
|---|---|
| Language | Python 3.11+ |
| Testing | pytest 7.4+, pytest-cov 4.1+ |
| MCP Server | fastmcp 2.0+ |
| Message format | JSON envelopes via filesystem |
| Coverage gate | Claude Code hook (blocks push if < 80%) |

## Quick Start

```bash
pip install -r requirements.txt
python integrator.py
```

See [HOWTORUN.md](HOWTORUN.md) for full setup instructions.
```

- [ ] **Step 8.2: Create HOWTORUN.md**

Create `homework-6/HOWTORUN.md`:

```markdown
# How to Run the Banking Pipeline

## Prerequisites

- Python 3.11+
- pip

## Setup

1. Navigate to the homework-6 directory:
   ```bash
   cd homework-6
   ```

2. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```

## Run the Pipeline

3. Run all 8 transactions through the pipeline:
   ```bash
   python integrator.py
   ```

   Expected output:
   ```
   ========================================
   Pipeline Complete
   ========================================
   Total:      8
   Validated:  6
   Rejected:   2
   Risk:       LOW=3 | MEDIUM=2 | HIGH=1

   Rejected transactions:
     TXN006: invalid currency: XYZ
     TXN007: amount must be positive, got -100.00
   ```

4. Check results:
   ```bash
   ls shared/results/
   cat shared/results/pipeline_summary.json
   ```

## Validate Transactions (Dry Run)

5. Run validation only without processing:
   ```bash
   python agents/transaction_validator.py --dry-run
   ```

## Run Tests

6. Run the full test suite with coverage:
   ```bash
   python -m pytest tests/ --cov=agents --cov-report=term-missing -v
   ```

   Expected: ≥ 90% coverage, all tests pass.

## Start the MCP Server

7. Start the pipeline status MCP server:
   ```bash
   python mcp/server.py
   ```

   The server exposes:
   - Tool: `get_transaction_status(transaction_id)` — query a transaction's status
   - Tool: `list_pipeline_results()` — list all processed transactions
   - Resource: `pipeline://summary` — latest pipeline summary

## Slash Commands (Claude Code)

- `/run-pipeline` — runs the full pipeline end-to-end
- `/validate-transactions` — dry-run validation only
- `/write-spec` — generates a specification from the template

## Coverage Gate

A hook in `.claude/settings.json` blocks `git push` if test coverage drops below 80%.
```

- [ ] **Step 8.3: Run full test suite and check coverage one final time**

```bash
cd homework-6 && python -m pytest tests/ --cov=agents --cov-report=term-missing
```

Expected: ≥ 90% for all agent modules. Overall ≥ 80%.

- [ ] **Step 8.4: Run pipeline end-to-end one final time**

```bash
cd homework-6 && python integrator.py
```

Expected: All 8 transactions processed, 6 validated, 2 rejected.

- [ ] **Step 8.5: Commit**

```bash
git add homework-6/README.md homework-6/HOWTORUN.md
git commit -m "feat(hw6): add README with author name and HOWTORUN"
```

- [ ] **Step 8.6: Capture readme-author.png**

Open `README.md` in editor or `cat README.md` and screenshot the author line ("Created by [Your Name]") visible in the output. Save to `docs/screenshots/readme-author.png`.

- [ ] **Step 8.7: Create PR**

Push the branch and create a PR to `main`. The PR description must embed all 7 screenshots inline:

```bash
git push -u origin homework-6-submission
```

PR description must include inline screenshots for:
1. `spec-produced.png` — `/write-spec` skill generating specification.md
2. `pipeline-run.png` — full `python integrator.py` terminal output
3. `test-coverage.png` — coverage report ≥ 80%
4. `skill-run-pipeline.png` — `/run-pipeline` skill executing
5. `hook-trigger.png` — coverage gate hook firing
6. `mcp-interaction.png` — context7 query result AND custom MCP tool call
7. `readme-author.png` — README with author name visible

---

## Final Checklist

Before submitting:

- [ ] `python integrator.py` runs without errors, all 8 results in `shared/results/`
- [ ] `python -m pytest tests/ --cov=agents --cov-fail-under=80` passes
- [ ] `python agents/transaction_validator.py --dry-run` shows validation table
- [ ] `.claude/commands/run-pipeline.md` and `validate-transactions.md` exist
- [ ] `.claude/settings.json` has coverage gate hook
- [ ] `mcp.json` has both context7 and pipeline-status servers
- [ ] `python mcp/server.py` starts without errors
- [ ] `research-notes.md` has 2+ context7 queries documented
- [ ] `README.md` includes your name ("Created by [Name]") and ASCII diagram
- [ ] `HOWTORUN.md` has numbered steps
- [ ] 7 screenshots taken and saved to `docs/screenshots/`: spec-produced, pipeline-run, test-coverage, skill-run-pipeline, hook-trigger, mcp-interaction (both context7 + custom MCP), readme-author
- [ ] PR created to `main` from `homework-6-submission` branch
- [ ] PR description includes all 7 screenshots inline, covering: spec produced, pipeline run, tests/coverage, skill/hook in action, MCP usage, and README with author name
