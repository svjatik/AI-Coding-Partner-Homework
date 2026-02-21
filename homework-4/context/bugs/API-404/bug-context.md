# Bug: API-404

**Title**: GET /api/users/:id returns 404 for valid user IDs
**Priority**: High
**Status**: Fixed
**Reporter**: qa-team@company.com

## Description

The user API endpoint was returning 404 errors even when user IDs exist in the database. Multiple users reported being unable to retrieve user profiles via the API.

## Steps to Reproduce

1. Start the API server: `npm start`
2. Verify user with ID `123` exists in the users array
3. Call `GET /api/users/123`
4. Observe 404 response

```bash
curl http://localhost:3000/api/users/123
# Expected: User object
# Actual: {"error": "User not found"} with 404 status
```

## Root Cause

In `src/controllers/userController.js` line 23, the `find` used strict equality (`===`) to compare `req.params.id` (a **string**) with `user.id` (a **number**). Because `"123" !== 123` in JavaScript strict mode, no user was ever found.

## Fix Applied

Convert `req.params.id` to an integer using `parseInt(userId, 10)` before comparison.

## Related Files

- `demo-bug-fix/src/controllers/userController.js` (fixed)
- `demo-bug-fix/src/routes/users.js` (not changed)
- `demo-bug-fix/server.js` (not changed)
