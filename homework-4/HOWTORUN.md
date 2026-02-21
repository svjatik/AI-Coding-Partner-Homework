# How to Run

## Prerequisites

- Node.js 18+
- npm 9+

---

## Run the Application

```bash
cd homework-4/demo-bug-fix
npm install
npm start
```

The server starts at `http://localhost:3000`.

### Verify the fix

```bash
# Should return user object (200)
curl http://localhost:3000/api/users/123

# Should return all users (200)
curl http://localhost:3000/api/users

# Should return 404
curl http://localhost:3000/api/users/999
```

---

## Run the Tests

```bash
cd homework-4/demo-bug-fix
npm install   # if not already done
npm test
```

Expected output: **13 tests, 13 passed**.

---

## Run the 4-Agent Pipeline (Conceptual)

The agents are defined as markdown specifications in `agents/`. To run them with an AI assistant that supports agent files (e.g. Claude Code with sub-agents), execute them in the following order:

1. **Bug Researcher** — (pre-existing) produces `context/bugs/API-404/research/codebase-research.md`
2. **Bug Research Verifier** (`agents/research-verifier.agent.md`) — reads research, verifies references, writes `research/verified-research.md`
3. **Bug Planner** — (pre-existing) produces `context/bugs/API-404/implementation-plan.md`
4. **Bug Implementer** (`agents/bug-implementer.agent.md`) — applies changes, writes `fix-summary.md`
5. **Security Verifier** (`agents/security-verifier.agent.md`) — reviews changed code, writes `security-report.md`
6. **Unit Test Generator** (`agents/unit-test-generator.agent.md`) — generates and runs tests, writes `test-report.md`

All artifact outputs are pre-populated in `context/bugs/API-404/` for review.
