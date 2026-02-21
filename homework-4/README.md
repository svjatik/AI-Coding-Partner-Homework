# Homework 4 — 4-Agent Pipeline

**Author**: Sviatoslav Glushchenko | **Course**: AI Coding Partner

---

## Overview

This homework implements a **4-agent pipeline** for automated bug fixing:

```
Bug Researcher → Bug Research Verifier → Bug Planner → Bug Implementer
                                                           ├─→ Security Verifier
                                                           └─→ Unit Test Generator
```

The pipeline was applied to **Bug API-404**: `GET /api/users/:id` returning 404 for valid user IDs in a demo Express.js API.

---

## Bug Fixed

**Root cause**: In [`demo-bug-fix/src/controllers/userController.js`](demo-bug-fix/src/controllers/userController.js), `req.params.id` is a string but user IDs in the array are numbers. Strict equality (`===`) always failed.

**Fix**: `parseInt(userId, 10)` before comparison.

---

## Project Structure

```
homework-4/
├── README.md
├── HOWTORUN.md
├── STUDENT.md
├── agents/
│   ├── research-verifier.agent.md     # Agent 1: verifies bug research quality
│   ├── bug-implementer.agent.md       # Agent 2: applies implementation plan
│   ├── security-verifier.agent.md     # Agent 3: security review of changed code
│   └── unit-test-generator.agent.md   # Agent 4: generates and runs unit tests
├── skills/
│   ├── research-quality-measurement.md  # Defines research quality levels 1–5
│   └── unit-tests-FIRST.md              # Defines FIRST unit test principles
├── context/bugs/API-404/
│   ├── bug-context.md
│   ├── research/
│   │   ├── codebase-research.md        # Bug Researcher output
│   │   └── verified-research.md        # Bug Research Verifier output (Level 5 EXCELLENT)
│   ├── implementation-plan.md          # Bug Planner output
│   ├── fix-summary.md                  # Bug Implementer output
│   ├── security-report.md              # Security Verifier output
│   └── test-report.md                  # Unit Test Generator output
├── docs/screenshots/                    # Pipeline run screenshots
└── demo-bug-fix/
    ├── server.js
    ├── package.json
    ├── src/
    │   ├── controllers/userController.js  ← fixed file
    │   └── routes/users.js
    └── tests/
    └── userController.test.js         ← 13 passing tests

## Quick Start

```bash
cd demo-bug-fix && npm install && npm test   # run tests (13 pass)
npm start                                    # start server on :3000
```

See [HOWTORUN.md](HOWTORUN.md) for full instructions.

---

## Agent Outputs Summary

| Agent | Output file | Result |
|-------|-------------|--------|
| Bug Research Verifier | `research/verified-research.md` | Level 5 EXCELLENT, 0 discrepancies |
| Bug Implementer | `fix-summary.md` | SUCCESS, 2 files changed |
| Security Verifier | `security-report.md` | SAFE TO DEPLOY, 1 LOW + 1 INFO finding |
| Unit Test Generator | `test-report.md` | 13/13 tests pass, FIRST compliant |
