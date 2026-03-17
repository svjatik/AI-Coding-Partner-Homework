Run the multi-agent banking pipeline end-to-end.

Steps:
1. Check that sample-transactions.json exists in homework-6/
2. Clear shared/ directories (remove and recreate shared/input, shared/processing, shared/output, shared/results)
3. Run the pipeline: cd homework-6 && python integrator.py
4. Show a summary of results from shared/results/pipeline_summary.json
5. Report any transactions that were rejected and why (look for "status": "rejected" in shared/results/*.json)
