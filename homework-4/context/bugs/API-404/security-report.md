# Security Report: API-404

**Reviewer**: Security Verifier Agent
**Date**: 2026-02-17
**Source**: `fix-summary.md`

---

## Summary

- **Files reviewed**: `demo-bug-fix/src/controllers/userController.js`
- **Total findings**: 2
- **Critical**: 0 | High: 0 | Medium: 0 | Low: 1 | Info: 1
- **Deployment recommendation**: SAFE TO DEPLOY (with advisory noted)

---

## Findings

### Finding 1: Missing validation for non-numeric ID input

- **Severity**: LOW
- **File**: `demo-bug-fix/src/controllers/userController.js`
- **Line**: 19, 21
- **Category**: Missing Validation
- **Description**: `parseInt("abc", 10)` returns `NaN`. The current code passes `NaN` into the `find` callback. `NaN === NaN` is `false` in JavaScript, so the lookup correctly returns no match and responds 404. The behaviour is safe but relies on implicit JavaScript semantics rather than explicit validation. A future developer might add `isNaN` checks elsewhere and inadvertently change this path.
- **Vulnerable code**:
  ```js
  const userId = req.params.id;                              // "abc" from URL
  const user = users.find(u => u.id === parseInt(userId, 10)); // parseInt("abc") → NaN
  ```
- **Remediation**: Add explicit input validation before the lookup:
  ```js
  const userId = parseInt(req.params.id, 10);
  if (isNaN(userId)) {
    return res.status(400).json({ error: 'Invalid user ID' });
  }
  const user = users.find(u => u.id === userId);
  ```

---

### Finding 2: No authentication on user endpoints (informational)

- **Severity**: INFO
- **File**: `demo-bug-fix/src/routes/users.js`
- **Line**: 11–14
- **Category**: Broken Access Control
- **Description**: Both `GET /api/users` and `GET /api/users/:id` are publicly accessible without any authentication middleware. This is noted as an informational finding because the application is a demo/workshop project, not a production system. In a production context, exposing all user records without authentication would be a HIGH finding.
- **Vulnerable code**:
  ```js
  router.get('/api/users', userController.getAllUsers);
  router.get('/api/users/:id', userController.getUserById);
  ```
- **Remediation**: Add authentication middleware (e.g. JWT verification) before route handlers in a production context. Out of scope for this bug fix.

---

## Reviewed Changes

| File | Lines Changed | Notes |
|------|--------------|-------|
| `demo-bug-fix/src/controllers/userController.js` | 21–23 | `parseInt` conversion added; no injection vector introduced |

---

## Vulnerability Categories Assessed

| Category | Result |
|----------|--------|
| Injection (SQL/command/template) | Not applicable — in-memory data, no DB queries |
| Hardcoded secrets | Not found |
| Insecure comparisons | The original bug was an insecure comparison; **fixed** |
| Missing input validation | LOW finding raised (Finding 1) |
| Unsafe dependencies | Not applicable — no new dependencies added |
| XSS / CSRF | Not applicable — JSON API, no HTML output |
| Broken access control | INFO finding raised (Finding 2) |

---

## References

- Fix summary: `fix-summary.md`
- OWASP Top 10: https://owasp.org/www-project-top-ten/
