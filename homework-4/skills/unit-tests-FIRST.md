# Skill: FIRST Unit Test Principles

## Purpose

Define the **FIRST** principles that every generated unit test MUST satisfy. The Unit Test Generator agent MUST apply this skill and confirm each principle is met before committing test output.

---

## The FIRST Principles

### F — Fast
Tests must execute in milliseconds. No real I/O (disk, network, DB). Mock all external dependencies. A slow test suite discourages frequent runs and defeats its purpose.

**Checklist**:
- [ ] No real HTTP calls (use mocks or supertest with in-process server)
- [ ] No sleep/setTimeout in tests
- [ ] Total suite runs in < 5 seconds for a small project

**Good example** (fast — in-process):
```js
const request = require('supertest');
const app = require('../server'); // no app.listen(), just the Express app
test('returns user', async () => {
  const res = await request(app).get('/api/users/123');
  expect(res.status).toBe(200);
});
```

**Bad example** (slow — real network call):
```js
test('returns user', async () => {
  const res = await fetch('http://localhost:3000/api/users/123'); // requires running server
  expect(res.status).toBe(200);
});
```

### I — Independent
Each test must be completely self-contained. No test should depend on the state left by another test. Any order of execution must produce the same result.

**Checklist**:
- [ ] No shared mutable state between tests
- [ ] `beforeEach` / `afterEach` resets state if needed
- [ ] No hard-coded sequence assumptions

**Good example** (independent — each test creates its own request):
```js
test('finds user 123', async () => {
  const res = await request(app).get('/api/users/123');
  expect(res.status).toBe(200);
});
test('returns 404 for missing user', async () => {
  const res = await request(app).get('/api/users/999');
  expect(res.status).toBe(404);
});
```

**Bad example** (dependent — second test relies on first test's side effect):
```js
let createdId;
test('creates user', async () => {
  const res = await request(app).post('/api/users').send({ name: 'Test' });
  createdId = res.body.id; // shared state!
});
test('gets created user', async () => {
  const res = await request(app).get(`/api/users/${createdId}`); // fails if run alone
  expect(res.status).toBe(200);
});
```

### R — Repeatable
Tests must produce the same result every time regardless of environment, date, or external service availability.

**Checklist**:
- [ ] No dependency on real-time values (use fixed timestamps or mock `Date`)
- [ ] No dependency on env-specific config (use test-specific env vars)
- [ ] Deterministic assertions (no random IDs without seeding)

**Good example** (repeatable — fixed seed):
```js
test('generates predictable output', () => {
  const result = formatDate(new Date('2024-01-15T00:00:00Z'));
  expect(result).toBe('Jan 15, 2024');
});
```

**Bad example** (not repeatable — depends on current time):
```js
test('shows today', () => {
  const result = formatDate(new Date()); // different every day
  expect(result).toContain('2024'); // fails in 2025
});
```

### S — Self-Validating
Tests must produce a clear binary pass/fail result. No manual inspection of log output to determine success.

**Checklist**:
- [ ] Every test has at least one `expect` assertion
- [ ] Assertions check the actual value against expected, not just truthiness
- [ ] No `console.log` as the only form of verification

**Good example** (self-validating — explicit assertions):
```js
test('returns correct user', async () => {
  const res = await request(app).get('/api/users/123');
  expect(res.status).toBe(200);
  expect(res.body.name).toBe('Alice Smith');
  expect(res.body.email).toBe('alice@example.com');
});
```

**Bad example** (not self-validating — requires human to read logs):
```js
test('returns user', async () => {
  const res = await request(app).get('/api/users/123');
  console.log(res.body); // "looks right" is not a passing test
});
```

### T — Timely
Tests should be written at the time of the fix, not weeks later. For an agent pipeline this means: generate tests immediately after the fix is applied and before the PR is merged.

**Checklist**:
- [ ] Tests cover the exact changed lines described in `fix-summary.md`
- [ ] Tests exist for the bug scenario (the old broken behaviour is a regression test)
- [ ] Tests exist for the fix (the new correct behaviour is a positive test)
- [ ] Edge cases for boundary inputs are included (not just the happy path)

**Good example** (timely — tests the exact fix):
```js
// Bug: string "123" !== number 123 caused 404
// Fix: parseInt(userId, 10) before comparison
test('REGRESSION: string route param resolves correctly after parseInt fix', async () => {
  const res = await request(app).get('/api/users/123'); // param arrives as string "123"
  expect(res.status).toBe(200);  // was 404 before fix
  expect(res.body.id).toBe(123);
});
```

**Bad example** (not timely — tests unrelated functionality):
```js
test('server starts on port 3000', () => {
  // Unrelated to the bug fix; doesn't test changed code
});
```

---

## Usage in Test Reports

When writing `test-report.md`, include a **FIRST Compliance** section:

```
## FIRST Compliance

| Principle | Status | Notes |
|-----------|--------|-------|
| Fast       | PASS / FAIL | <observation> |
| Independent | PASS / FAIL | <observation> |
| Repeatable  | PASS / FAIL | <observation> |
| Self-validating | PASS / FAIL | <observation> |
| Timely      | PASS / FAIL | <observation> |
```
