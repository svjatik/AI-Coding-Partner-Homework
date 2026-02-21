# Skill: Research Quality Measurement

## Purpose

Define standard quality levels for bug research output. Any agent verifying research MUST assign one of these levels and justify the assignment in their output.

---

## Quality Levels

| Level | Label | Description |
|-------|-------|-------------|
| 5 | **EXCELLENT** | All file:line references verified; all code snippets match source exactly; root cause clearly identified with evidence; no discrepancies found; fully actionable for implementation. |
| 4 | **GOOD** | Most references verified (>80%); minor wording differences in snippets (not logic); root cause identified; 1-2 minor discrepancies that do not affect fix planning. |
| 3 | **ACCEPTABLE** | Core references verified; some snippets paraphrased but semantically correct; probable root cause stated; discrepancies present but workarounds documented. |
| 2 | **POOR** | <50% references verified; snippets contain inaccuracies that could mislead the implementer; root cause is a guess; significant discrepancies not documented. |
| 1 | **UNUSABLE** | References not verified or point to wrong locations; snippets wrong; root cause missing or clearly incorrect; research cannot be safely used for implementation. |

---

## Examples

### Level 5 — EXCELLENT

```
Claim: "In src/controllers/userController.js at line 23, the find uses strict equality"
Source at line 23: `const user = users.find(u => u.id === userId);`
Result: VERIFIED — exact match, line number correct, semantics accurately described.
```

### Level 3 — ACCEPTABLE

```
Claim: "Around line 20 of userController.js, the user lookup compares types incorrectly"
Source at line 23 (not 20): `const user = users.find(u => u.id === userId);`
Result: DISCREPANCY (LOW) — line number off by 3, but the function and logic are correct.
         Root cause claim is valid despite imprecise reference.
```

### Level 1 — UNUSABLE

```
Claim: "In src/models/User.js at line 45, the database query uses string comparison"
Source: File src/models/User.js does not exist in the project.
Result: DISCREPANCY (HIGH) — wrong file, wrong technology (no database, it's an in-memory array).
         Root cause claim is based on incorrect assumptions.
```

---

## Usage Instructions

When writing a research verification result (`verified-research.md`), include a **Research Quality Assessment** section with:

```
## Research Quality Assessment

**Level**: <1–5>
**Label**: <UNUSABLE | POOR | ACCEPTABLE | GOOD | EXCELLENT>

**Reasoning**:
- Reference accuracy: <observation>
- Snippet accuracy: <observation>
- Root cause clarity: <observation>
- Overall: <one-sentence summary>
```

### Decision rules

- Assign Level 5 only if zero discrepancies are found.
- Assign Level 4 if all discrepancies are cosmetic (LOW impact) and < 20% of references.
- Assign Level 3 if the root cause is correct despite some reference inaccuracies.
- Assign Level 1 or 2 if the Bug Implementer cannot safely act on the research.
- Levels 3–5 allow the pipeline to proceed; Levels 1–2 require re-research before continuing.
- When in doubt between two levels, choose the lower one — it's safer to ask for re-research than to proceed on bad data.
