# Implementation Plan: API-404

**Planner**: Bug Planner Agent
**Date**: 2026-02-17
**Based on**: `research/verified-research.md` (Quality Level 5 — EXCELLENT)

---

## Plan Overview

Fix a single-line type mismatch in `getUserById`. The route parameter `:id` is a string; the users array uses numeric IDs. Converting the param to an integer before comparison resolves the bug.

**Risk**: Very low. Single-line change in one file. No schema changes, no dependency changes.

---

## Changes

### Change 1

**File**: `demo-bug-fix/src/controllers/userController.js`
**Location**: Line 21–23 (`getUserById` function body)

**Before**:
```js
const userId = req.params.id;

// BUG: req.params.id returns a string, but users array uses numeric IDs
// Strict equality (===) comparison will always fail: "123" !== 123
const user = users.find(u => u.id === userId);
```

**After**:
```js
const userId = req.params.id;

// FIX: Convert string param to integer for correct numeric comparison
const user = users.find(u => u.id === parseInt(userId, 10));
```

**Explanation**: `parseInt(userId, 10)` converts the string `"123"` to the number `123`, so strict equality against the numeric array ID succeeds.

---

## Test Command

```bash
cd demo-bug-fix && npm test
```

Expected: All tests pass. Specifically, `GET /api/users/123` must return status 200 and the Alice Smith user object.

---

## Rollback Plan

If tests fail, revert line 23 to the original `const user = users.find(u => u.id === userId);` — the original broken state is non-destructive (returns 404, not a crash).

---

## Files NOT to Change

- `demo-bug-fix/src/routes/users.js` — routing is correct
- `demo-bug-fix/server.js` — app setup is correct
- `demo-bug-fix/package.json` — no dependency changes required for the fix itself
