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
