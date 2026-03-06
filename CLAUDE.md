# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository Overview

Multi-language homework repository for an AI-assisted development course. Contains progressive assignments from simple APIs to multi-agent automation pipelines.

## Build, Test & Run Commands

### Homework 1 — Banking Transactions API (Node.js + Python)
```bash
# Node.js
cd homework-1/nodejs && npm install && npm start    # Express server
cd homework-1/nodejs && npm test                     # Jest tests

# Python
cd homework-1/python && pip install -r requirements.txt
cd homework-1/python && uvicorn src.main:app --reload
cd homework-1/python && pytest
```

### Homework 2 — Ticket Management System (Java/Spring Boot)
```bash
cd homework-2
mvn clean install          # Build + run all tests
mvn spring-boot:run        # Start server (needs PostgreSQL or use H2 profile)
mvn test                   # JUnit 5 tests (uses H2 in-memory DB)
mvn test -Dtest=ClassName  # Run a single test class
```
- Java 21, Spring Boot 3.2.2, Maven
- JaCoCo enforces 85% coverage on business logic

### Homework 4 — 4-Agent Bug Fix Pipeline (Node.js)
```bash
cd homework-4/demo-bug-fix
npm install && npm test    # 13 Jest + supertest tests
npm start                  # Express server on port 3000
```

### Workshop 3 — Banking Transaction Parser (Node.js)
```bash
cd practice/workshop3
npm install && npm test    # 54 Jest tests, 98.75% coverage
```

## Architecture

### Homework 1
Dual-stack (Node.js Express + Python FastAPI) implementing the same banking transactions REST API. Layered: routes -> validators -> business logic -> in-memory storage.

### Homework 2
Spring Boot enterprise app with layered architecture: Controller -> Service -> Repository -> JPA/PostgreSQL. Uses a parser factory pattern (CSV/JSON/XML) for multi-format ticket import and a keyword-matching classification service.

### Homework 3
Specification-only assignment (no runnable code). Contains `specification.md` (product spec with state machine, RBAC matrix, API specs), `agents.md` (tech stack and domain rules), and `.claude/project-rules.md` (critical NEVER/ALWAYS rules for banking domain — e.g., never use float for money, never log PAN/CVV).

### Homework 4
Multi-agent pipeline: Bug Researcher -> Research Verifier -> Bug Planner -> Bug Implementer -> Security Verifier + Unit Test Generator. Agent specs live in `homework-4/agents/`, shared skills in `homework-4/skills/`, and bug context/outputs in `homework-4/context/bugs/`.

## Submission Workflow

Each homework is submitted on a `homework-N-submission` branch with a PR to `main`. PRs require: README.md, HOWTORUN.md, screenshots in `docs/screenshots/`, and demo scripts.
