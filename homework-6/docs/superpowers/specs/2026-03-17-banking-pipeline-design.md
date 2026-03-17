# Design: AI-Powered Multi-Agent Banking Pipeline

**Date:** 2026-03-17
**Homework:** 6 — Final Capstone
**Language:** Python 3.11+

---

## Overview

Build a 3-agent Python pipeline that validates, scores for fraud risk, and reports on banking transactions using file-based JSON message passing. Four meta-agents (spec, code-gen, test, docs) produce the system. The deliverable is both the meta-agents and the resulting working pipeline.

---

## Architecture

### Pipeline Flow

```
sample-transactions.json  (provided in homework-6/ root — do NOT regenerate)
         │
    integrator.py  ← sets up dirs, loads JSON, loops over transactions
         │
    ┌────▼────────────────────────────────────────────────┐
    │  For each transaction:                               │
    │                                                      │
    │  1. transaction_validator.process_message()          │
    │     → writes envelope to shared/processing/          │
    │     → returns {status: validated|rejected, ...}      │
    │                                                      │
    │  2. fraud_detector.process_message()  (if validated) │
    │     → returns {fraud_risk_score, fraud_risk_level}   │
    │                                                      │
    │  3. reporting_agent.process_message()                │
    │     → writes final result to shared/results/         │
    │                                                      │
    └──────────────────────────────────────────────────────┘
         │
    shared/results/TXN001.json ... TXN008.json
    shared/results/pipeline_summary.json
```

### Approach

**Sequential orchestrator (Option A):** The integrator calls each agent's `process_message()` in-process, in sequence, for each transaction. Shared directories serve as the message store between pipeline stages.

### Message Envelope Format

```json
{
  "message_id": "uuid4-string",
  "timestamp": "2026-03-16T10:00:00Z",
  "source_agent": "transaction_validator",
  "target_agent": "fraud_detector",
  "message_type": "transaction",
  "data": {
    "transaction_id": "TXN001",
    "amount": "1500.00",
    "currency": "USD",
    "status": "validated"
  }
}
```

**Note on Decimal serialization:** Amounts are stored as strings in JSON envelopes (`"amount": "1500.00"`). Within each agent, parse to `decimal.Decimal(message["data"]["amount"])` for computation. Serialize back to string for output. Never store as `float`.

---

## Components

### Agent 1 — Transaction Validator (`agents/transaction_validator.py`)

**Function:** `process_message(message: dict) -> dict`

Rejects if:
- Missing required fields: `transaction_id`, `amount`, `currency`, `source_account`, `destination_account`, `timestamp`
- `amount` ≤ 0 when parsed as `decimal.Decimal` (catches TXN007's -100.00)
- `currency` not in ISO 4217 whitelist: `{"USD", "EUR", "GBP", "JPY", "CAD", "AUD", "CHF", "CNY", "SEK", "NZD"}` (catches TXN006's "XYZ")

Output: adds `status: "validated" | "rejected"` and optional `rejection_reason` to the message data.

**CLI entry point:** `transaction_validator.py` must also support `python agents/transaction_validator.py --dry-run` via a `if __name__ == "__main__"` block using `argparse`. In dry-run mode, it loads `sample-transactions.json` directly and prints validation results without writing any files.

### Agent 2 — Fraud Detector (`agents/fraud_detector.py`)

**Function:** `process_message(message: dict) -> dict`

Only runs on validated transactions. Scoring rules applied in order (cumulative):

| Rule | Condition | Points |
|---|---|---|
| High value | amount > $10,000 | +3 |
| Very high value | amount > $50,000 | +4 additional (total +7 if both fire) |
| Unusual hour | hour(timestamp) in [2, 3, 4] UTC | +2 |
| Cross-border | `metadata.country` ≠ "US" | +1 |

**Important:** Both threshold rules are evaluated independently and added together. An amount of $75,000 earns both +3 AND +4 = +7. An amount of $25,000 earns only +3.

**Unusual hour boundary:** The rule applies to hours 2, 3, and 4 UTC only — hour 5 is NOT included. This overrides the template hint's "2am–5am" phrasing.

**Missing metadata handling:** If `metadata` key is absent or `metadata.country` is missing, treat as domestic (no cross-border penalty, no KeyError).

Risk levels: LOW (0–2), MEDIUM (3–6), HIGH (7–10)

Output: adds `fraud_risk_score` (int) and `fraud_risk_level` ("LOW" | "MEDIUM" | "HIGH") to the message data.

### Agent 3 — Reporting Agent (`agents/reporting_agent.py`)

**Functions:**
- `process_message(message: dict) -> dict` — records one transaction result
- `generate_summary(results_dir: str) -> dict` — reads all result files and writes `pipeline_summary.json`

Runs on every transaction (including rejected). Writes final result JSON to `shared/results/{transaction_id}.json`. After all transactions processed, `generate_summary()` writes `shared/results/pipeline_summary.json` with:
- total, validated, rejected counts
- breakdown by fraud risk level (LOW/MEDIUM/HIGH counts)
- rejection reasons list

### Integrator (`integrator.py`)

- Creates `shared/{input,processing,output,results}/` directories (idempotent — use `exist_ok=True`)
- Loads `sample-transactions.json` from project root
- Loops: wraps each transaction in envelope → Validator → Fraud Detector → Reporting Agent
- Calls `reporting_agent.generate_summary()` after all transactions
- Prints a console summary table

---

## Expected Results for Sample Data

| TXN | Amount | Validator | Score Breakdown | Risk Level |
|---|---|---|---|---|
| TXN001 | $1,500 USD | validated | 0 | LOW |
| TXN002 | $25,000 USD | validated | +3 (>$10k) | MEDIUM |
| TXN003 | $9,999.99 USD | validated | 0 | LOW |
| TXN004 | €500 EUR, 02:47 UTC, DE | validated | +2 (unusual hour) +1 (country=DE) = 3 | MEDIUM |
| TXN005 | $75,000 USD | validated | +3 (>$10k) +4 (>$50k) = 7 | HIGH |
| TXN006 | XYZ currency | **rejected** | — | — (invalid currency: XYZ) |
| TXN007 | -£100 GBP | **rejected** | — | — (negative amount) |
| TXN008 | $3,200 USD | validated | 0 | LOW |

---

## File Structure

```
homework-6/
├── sample-transactions.json          ← provided input — do not modify
├── integrator.py
├── agents/
│   ├── __init__.py
│   ├── transaction_validator.py
│   ├── fraud_detector.py
│   └── reporting_agent.py
├── mcp/
│   └── server.py
├── tests/
│   ├── test_transaction_validator.py
│   ├── test_fraud_detector.py
│   ├── test_reporting_agent.py
│   └── test_integration.py
├── shared/
│   ├── input/
│   ├── processing/
│   ├── output/
│   └── results/
├── .claude/
│   ├── commands/
│   │   ├── write-spec.md
│   │   ├── run-pipeline.md
│   │   └── validate-transactions.md
│   └── settings.json
├── docs/
│   └── screenshots/
│       ├── spec-produced.png
│       ├── pipeline-run.png
│       ├── test-coverage.png
│       ├── skill-run-pipeline.png
│       ├── hook-trigger.png
│       ├── mcp-interaction.png       ← both context7 + custom MCP tool call
│       └── readme-author.png
├── mcp.json
├── specification.md
├── agents.md
├── research-notes.md
├── requirements.txt
├── README.md
└── HOWTORUN.md
```

---

## Specification Deliverables

### `specification.md` (Task 1 output)

This is a separate file from this design doc. It must follow the 5-section template from `specification-TEMPLATE-hint.md`:

1. **High-Level Objective** — one sentence
2. **Mid-Level Objectives** — 4–5 testable requirements
3. **Implementation Notes** — Decimal, ISO 4217, logging, PII rules
4. **Context** — beginning/ending state description
5. **Low-Level Tasks** — one entry per agent with exact AI prompt, file to create, function to create, and details

Each Low-Level Task entry must use this full wrapper format (required by TASKS.md):
```
Task: [Agent Name]
Prompt: "Context: [What exists — files, tech stack, constraints]
Task: [Exactly what to build]
Rules: [Non-negotiable requirements — Decimal, logging, error handling]
Output: [What format the result should take]"
File to CREATE: agents/[name].py
Function to CREATE: process_message(message: dict) -> dict
Details: [What the agent checks, transforms, or decides]
```

For Reporting Agent, also add:
```
Function to CREATE: generate_summary(results_dir: str) -> dict
```

### `agents.md`

Extend the homework-3 `agents.md` with project-specific context for this pipeline:
- Tech stack: Python 3.11+, pytest, pytest-cov, fastmcp
- Domain rules: decimal.Decimal only, ISO 4217, mask account numbers in logs
- Agent responsibilities table
- File-based communication protocol description

### `research-notes.md`

Each context7 query entry must use this exact format:
```markdown
## Query N: [topic]
- Search: "[exact search term used]"
- context7 library ID: [returned ID]
- Applied: [what insight or pattern was used]
```

---

## Skills & Hooks

### Custom Skills (`.claude/commands/`)

**`write-spec.md`** — generates a specification following the 5-section template:
```markdown
Generate a complete specification.md for the banking transaction pipeline.

Follow this template structure:
1. High-Level Objective (one sentence)
2. Mid-Level Objectives (4-5 testable requirements)
3. Implementation Notes (Decimal, ISO 4217, logging, PII)
4. Context (beginning state: sample-transactions.json; ending state: shared/results/)
5. Low-Level Tasks (one per agent with exact prompt, file, function, details)
```

**`run-pipeline.md`**:
```markdown
Run the multi-agent banking pipeline end-to-end.

Steps:
1. Check that sample-transactions.json exists
2. Clear shared/ directories
3. Run the pipeline: python integrator.py
4. Show a summary of results from shared/results/
5. Report any transactions that were rejected and why
```

**`validate-transactions.md`**:
```markdown
Validate all transactions in sample-transactions.json without processing them.

Steps:
1. Run the validator in dry-run mode: python agents/transaction_validator.py --dry-run
2. Report: total count, valid count, invalid count, reasons for rejection
3. Show a table of results
```

### Coverage Gate Hook

In `.claude/settings.json` — blocks `git push` if test coverage is below 80%. The hook must exit with a non-zero code to actually block the action. Use `$(git rev-parse --show-toplevel)` so the path is portable across machines:

```json
{
  "hooks": {
    "PreToolUse": [{
      "matcher": "Bash",
      "hooks": [{
        "type": "command",
        "command": "bash -c 'if echo \"$CLAUDE_TOOL_INPUT\" | grep -q \"git push\"; then REPO=$(git rev-parse --show-toplevel) && cd \"$REPO/homework-6\" && python -m pytest tests/ --cov=agents --cov-fail-under=80 -q 2>&1; exit $?; fi'"
      }]
    }]
  }
}
```

---

## MCP Integration

### `mcp.json`

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

### Custom MCP Server (`mcp/server.py`)

Uses FastMCP (`pip install fastmcp`). Exposes:
- **Tool `get_transaction_status(transaction_id: str)`** — reads `shared/results/{transaction_id}.json`, returns status or "not found"
- **Tool `list_pipeline_results()`** — scans all `.json` files in `shared/results/` (excluding `pipeline_summary.json`), returns list of `{transaction_id, status, fraud_risk_level}` objects
- **Resource `pipeline://summary`** — returns contents of `shared/results/pipeline_summary.json` as text

---

## Audit Logging

All agents write audit logs using Python's `logging` module, configured to write to both stdout and `shared/audit.log`. Format:

```
2026-03-17T10:00:00Z | transaction_validator | TXN001 | validated
2026-03-17T10:00:01Z | fraud_detector | TXN001 | risk=LOW score=0
```

**PII rules:**
- Account numbers masked: show only last 4 chars (`ACC-****`)
- Never log full account numbers in any output

---

## Testing Strategy

- `test_transaction_validator.py` — unit tests for each rejection rule (missing field, negative amount, invalid currency) + happy path
- `test_fraud_detector.py` — unit tests for each scoring trigger + risk level thresholds + missing metadata handling
- `test_reporting_agent.py` — unit tests for file writing + summary generation using `tmp_path`
- `test_integration.py` — full pipeline run using `tmp_path` fixture (patches `shared/` to temp dir, no real filesystem side effects)
- Target: ≥ 90% coverage; coverage gate blocks push at < 80%

---

## Documentation Requirements

- `README.md` — includes **author name** ("Created by [Name]"), ASCII pipeline diagram, agent responsibilities table, tech stack table
- `HOWTORUN.md` — numbered steps from setup (`pip install -r requirements.txt`) through demo (`python integrator.py`)
- Screenshots — required in `docs/screenshots/`:

| File | What to capture |
|---|---|
| `spec-produced.png` | AI generating `specification.md` |
| `pipeline-run.png` | Full terminal output of `python integrator.py` |
| `test-coverage.png` | Coverage report showing ≥ 80% |
| `skill-run-pipeline.png` | `/run-pipeline` skill executing |
| `hook-trigger.png` | Coverage gate hook firing or blocking push |
| `mcp-interaction.png` | Must show **both** a context7 query result **and** a custom MCP tool call (`get_transaction_status` or `list_pipeline_results`) in one screenshot |
| `readme-author.png` | README with author name visible |

- **PR description** must embed all screenshots inline, covering: spec produced, pipeline run, tests/coverage, skill/hook in action, MCP usage, and README with author name

---

## Implementation Constraints

- `decimal.Decimal` for all monetary amounts — never `float`
- Parse string amounts from JSON to Decimal; serialize back to string for output
- ISO 4217 currency whitelist: `{"USD", "EUR", "GBP", "JPY", "CAD", "AUD", "CHF", "CNY", "SEK", "NZD"}`
- Account numbers masked in all logs (show only last 4 chars)
- Missing `metadata.country` treated as domestic (no cross-border penalty)
- All files: snake_case naming; classes: PascalCase
