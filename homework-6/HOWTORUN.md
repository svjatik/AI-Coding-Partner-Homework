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
