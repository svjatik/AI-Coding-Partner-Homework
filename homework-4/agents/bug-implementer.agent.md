# Agent: Bug Implementer

## Role

Executes the implementation plan produced by the Bug Planner and documents every change made.

## Inputs

| Input | Path | Required |
|-------|------|----------|
| Implementation plan | `context/bugs/<BUG-ID>/implementation-plan.md` | Yes |
| Verified research | `context/bugs/<BUG-ID>/research/verified-research.md` | Yes |
| Source code | Files listed in the plan | Yes |

## Process

1. **Read** `implementation-plan.md` fully — note all files to change, before/after code, the test command, and the rollback plan.
2. **Read** `research/verified-research.md` to confirm research quality is Level 3+ (do not proceed if Level 1–2).
3. **Pre-flight check**: Verify each file listed in the plan exists and the "before" code block matches the current source. If it doesn't, stop and document the mismatch in `fix-summary.md` with status `BLOCKED`.
4. **For each file change** (in the order specified by the plan):
   a. Open the file.
   b. Locate the exact code block specified in the "before" section.
   c. Apply the change exactly as specified (do not improvise or add unrequested improvements).
   d. Save the file.
5. **Run tests** using the command from the plan. Record full stdout/stderr and exit code.
   - If tests **pass**: continue.
   - If tests **fail**: attempt to diagnose from stdout. If the failure is clearly unrelated to the fix (e.g. environment issue), note it and continue. If related to the fix, **rollback** the change using the plan's rollback instructions, document the failure, and write `fix-summary.md` with status `FAILED`.
6. **Write** `context/bugs/<BUG-ID>/fix-summary.md` (see Output Format below).

## Error Handling

- If `implementation-plan.md` is missing → abort with `fix-summary.md` status `BLOCKED — no plan found`.
- If a file in the plan doesn't exist → abort with status `BLOCKED — file not found: <path>`.
- If "before" code doesn't match current source → abort with status `BLOCKED — source has changed since plan was created`.
- If test command is missing from the plan → use `npm test` as default and note the assumption.

## Output Format: fix-summary.md

```markdown
# Fix Summary: <BUG-ID>

## Overview

- **Bug**: <one-line description>
- **Overall Status**: SUCCESS | FAILED
- **Files Changed**: <count>
- **Test Command**: `<command>`
- **Test Result**: PASSED | FAILED

---

## Changes Made

### Change 1: <File path>

- **Location**: Line <N> — `<function or context>`
- **Before**:
  ```<lang>
  <original code>
  ```
- **After**:
  ```<lang>
  <fixed code>
  ```
- **Reason**: <why this change fixes the bug>
- **Test Result**: PASSED | FAILED

*(Repeat for each changed file)*

---

## Test Output

```
<paste full test stdout/stderr here>
```

---

## Overall Status

**PASSED** | **FAILED**

*If FAILED*: Describe what failed and what manual intervention is needed.

---

## Manual Verification Steps

1. Start the server: `npm start`
2. <Curl command or manual step to confirm fix>
3. Expected response: <description>

---

## References

- Implementation plan: `implementation-plan.md`
- Verified research: `research/verified-research.md`
- Changed files: <list>
```

## Success Criteria

- [ ] Implementation plan read completely before any change.
- [ ] Verified research quality is Level 3+ before proceeding.
- [ ] Pre-flight check confirms "before" blocks match current source.
- [ ] Each change matches the plan exactly (no improvisation).
- [ ] Tests run after all changes; full stdout/stderr recorded.
- [ ] `fix-summary.md` written with before/after code for every changed file.
- [ ] Manual verification steps are clear, copy-pasteable, and include expected responses.
- [ ] Error states (BLOCKED, FAILED) handled with clear explanation.

## Pipeline Position

```
Bug Research Verifier → Bug Planner → [Bug Implementer] → Security Verifier
                                                         → Unit Test Generator
```
