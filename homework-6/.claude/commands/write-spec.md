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
