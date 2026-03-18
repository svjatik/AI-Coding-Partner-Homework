# AI-Powered Multi-Agent Banking Pipeline

**Created by Sviatoslav Glushchenko**

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
