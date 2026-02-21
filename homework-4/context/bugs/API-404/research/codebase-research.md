# Codebase Research: API-404

**Researcher**: Bug Researcher Agent
**Date**: 2026-02-17
**Bug**: GET /api/users/:id returns 404 for valid user IDs

---

## Summary

The bug is a **type mismatch** in the user lookup logic. The route parameter `:id` is always a string in Express.js, but the in-memory `users` array stores IDs as integers. A strict equality check (`===`) between string and integer always evaluates to `false`, causing every lookup to fail with "User not found".

---

## Files Investigated

| File | Purpose |
|------|---------|
| `demo-bug-fix/src/controllers/userController.js` | User business logic — contains the bug |
| `demo-bug-fix/src/routes/users.js` | Route definitions — maps URL to controller |
| `demo-bug-fix/server.js` | App entry point — mounts routes |
| `demo-bug-fix/package.json` | Dependencies and scripts |

---

## Findings

### Finding 1 — Strict equality type mismatch

**File**: `demo-bug-fix/src/controllers/userController.js`
**Line**: 23

```js
const user = users.find(u => u.id === userId);
```

`userId` comes from `req.params.id` (line 19), which is always a `string` in Express. The `users` array stores numeric IDs (`123`, `456`, `789`). Strict equality between `"123"` and `123` returns `false` in JavaScript.

**Result**: `users.find()` never matches any user, so the `if (!user)` branch (line 25) always fires and returns `{ error: 'User not found' }` with status 404.

---

### Finding 2 — Working endpoint for comparison

**File**: `demo-bug-fix/src/controllers/userController.js`
**Lines**: 37–39

```js
async function getAllUsers(req, res) {
  res.json(users);
}
```

`GET /api/users` works correctly because it simply returns the whole array without any ID comparison.

---

### Finding 3 — Route registration

**File**: `demo-bug-fix/src/routes/users.js`
**Lines**: 11–14

```js
router.get('/api/users', userController.getAllUsers);
router.get('/api/users/:id', userController.getUserById);
```

Routes are correctly defined. The issue is solely in the controller logic, not in routing.

---

## Root Cause

`req.params.id` is a string; `user.id` values are numbers; strict equality always fails.

## Proposed Fix

Convert the route parameter to an integer before comparison:

```js
const user = users.find(u => u.id === parseInt(userId, 10));
```

## Evidence

- Bug affects **all** user IDs (not just specific ones) — consistent with a type comparison failure.
- `GET /api/users` works — confirms data exists and routing is correct.
- The comment in the source code confirms the bug: "BUG: req.params.id returns a string, but users array uses numeric IDs".
