# AI Coding Partner Instructions

This repository is an AI-assisted development course with progressive assignments in multiple languages. These instructions guide AI agents in being immediately productive across the codebase.

## Repository Purpose & Structure

**Course Goal**: Students implement assignments using AI tools (GitHub Copilot, Claude, etc.) to compare assisted development across different technology stacks and architectural patterns.

**Key Structure**:
- `homework-1/` — Dual-stack banking API (Node.js + Python, identical endpoints)
- `homework-2/` — Spring Boot ticket system with multi-format import (CSV/JSON/XML)
- `homework-3/` — Specification-only virtual card lifecycle system (FastAPI + PostgreSQL)
- `homework-4/` — Multi-agent pipeline for automated bug fixing (Express.js)
- `homework-5/` — MCP server configuration (in progress)
- `practice/` — Workshop implementations and pattern examples

## Critical Domain Constraints (Banking)

**Never violate these rules** — They're based on PCI-DSS, GDPR, and financial regulations:

- **Monetary amounts**: Use `Decimal(19,4)`, `BigDecimal`, or fixed-point precision; NEVER float/double
- **Rounding**: Always use `ROUND_HALF_UP` for financial calculations  
- **Sensitive data**: Never log PAN (card numbers), CVV, private keys, or auth tokens
- **Field-level encryption**: PAN, CVV, cardholder names require AES-256-GCM encryption at rest
- **Soft deletes**: Cancelled/expired cards retain data for compliance, never hard delete
- **State machines**: Card lifecycle: `PENDING → ACTIVE ↔ FROZEN → CANCELLED|EXPIRED` (no other transitions)
- **Audit trails**: All operations must be immutable, timestamped, user-attributed, and JSON-logged
- **Rate limiting**: Per-user (60 reads/min, 10 writes/min) and per-IP limits using Redis sliding window
- **Idempotency**: All state-changing endpoints accept `Idempotency-Key` header (24h Redis TTL)

## Architecture Patterns

### Layered Architecture (Standard Pattern)
All implementations follow: **Routes/Controllers → Services → Repositories → Database**

**Why**: Clear separation of concerns, testability, and business logic isolation.

**Example flows**:
- `homework-1/` Node.js: `routes/transactions.js → transaction.js (service) → in-memory storage`
- `homework-2/` Java: `TicketController → TicketService → TicketRepository → JPA → PostgreSQL`
- `homework-3/` Python spec: `FastAPI routes → CardService → CardRepository → PostgreSQL + Redis`

### Factory Pattern for Multi-Format Handling
File parsers use factory pattern to support CSV, JSON, and XML:

```
ParserFactory.getParser(filename) → 
  .csv → CsvFileParser
  .json → JsonFileParser  
  .xml → XmlFileParser
```

**Location**: `homework-2/src/main/java/com/ticketmanagement/services/ParserFactory.java`  
**Apply this when**: Adding new file format support — extend `FileParser` interface, register in factory

### State Machine for Card Lifecycle
`homework-3/` defines card states with validation:
- Only specific state transitions allowed (e.g., FROZEN can only go to ACTIVE, CANCELLED, EXPIRED — not back to PENDING)
- Use SELECT FOR UPDATE row-level locking for concurrent access safety
- Transitions triggered by explicit operations (freeze, unfreeze) or system events (expiry cron)

## Build, Test & Run Commands

### Homework 1 — Banking Transactions API

**Node.js**:
```bash
cd homework-1/nodejs && npm install && npm start    # http://localhost:3000
npm test                                              # Jest tests
npm run test:coverage                                 # Coverage report
```

**Python**:
```bash
cd homework-1/python && pip install -r requirements.txt
uvicorn src.main:app --reload                        # http://localhost:8000
pytest                                                # Pytest tests
pytest --cov=src                                      # Coverage report
```

### Homework 2 — Spring Boot Ticket Management

```bash
cd homework-2
mvn clean install                        # Full build + all tests
mvn spring-boot:run                      # Start server (H2 by default, PostgreSQL available)
mvn test                                 # JUnit 5 tests with H2 in-memory database
mvn test -Dtest=TicketControllerTest    # Single test class
mvn jacoco:report                        # Coverage report (enforces 85% on business logic)
```

**Key**: Tests run with H2 in-memory DB by default (`application-test.yml`). Requires Java 17+, Maven.

### Homework 3 — Virtual Card Lifecycle (Specification Only)

**Files only** — No runnable implementation yet. Contains:
- `specification.md` — Full API spec, state machine, RBAC matrix, implementation notes
- `agents.md` — Agent roles and domain constraints (banking rules)

When implementing: Use FastAPI + PostgreSQL, follow all constraints in spec.

### Homework 4 — Multi-Agent Bug Fix Pipeline

```bash
cd homework-4/demo-bug-fix
npm install && npm test    # 13 passing tests (Jest + supertest)
npm start                  # Express server on port 3000
```

**Structure**: Bug context in `context/bugs/API-404/`, agent specs in `agents/`, skills in `skills/`

### Workshop 3 — Banking Transaction Parser (High Test Coverage Example)

```bash
cd practice/workshop3
npm install && npm test                  # 54 tests, 98.75% coverage
npm run test:coverage                    # Detailed coverage report
```

**Why valuable**: Shows best practices in test coverage for financial code.

## Code Organization Conventions

### File Naming & Locations
- **Routes/Controllers**: `src/routes/`, `src/controllers/`, `src/main/java/[package]/controller/`
- **Services**: `src/services/`, `src/main/java/[package]/service/`
- **Models/DTOs**: `src/models.py`, `src/main/java/[package]/dto/`
- **Validators**: `src/validators.js`, `src/main/java/[package]/validator/`
- **Tests**: Mirror source structure: `tests/`, `src/test/`, `__tests__/`
- **Docs**: `docs/`, `API_REFERENCE.md`, `ARCHITECTURE.md` in project root

### Validation Patterns
- **Node.js**: Custom validator functions in `validators.js`, return `{valid: boolean, errors: []}`
- **Python**: Pydantic models for automatic validation with descriptive error messages
- **Java**: Jakarta Bean Validation annotations (`@NotNull`, `@Email`, `@Size`) + custom validators

### Constants & Enums
- **Node.js**: Export const objects: `const TRANSACTION_TYPES = {DEBIT: 'debit', CREDIT: 'credit'}`
- **Python**: Use Python enums: `class TicketStatus(str, Enum): NEW = 'NEW'`
- **Java**: Dedicated enum classes in `enum/` directory with methods

## Testing Requirements & Patterns

### Coverage Requirements
- **Default target**: 80-90% overall, 85%+ for business logic (enforced in Java via JaCoCo)
- **Expected**: Unit tests for business logic, integration tests for API endpoints
- **Example success**: `workshop3/` achieved 98.75% coverage (54 tests, <1 second runtime)

### Test Organization
All implementations require:
1. **Unit tests** for isolated business logic (no database/network)
2. **Integration tests** for endpoints (with test database)
3. **Edge cases**: Empty inputs, invalid types, boundary values, concurrent access

### Language-Specific Test Frameworks
- **Node.js**: Jest (with supertest for HTTP testing)
- **Python**: Pytest with fixtures
- **Java**: JUnit 5 with Mockito for mocking

### FIRST Principles for Unit Tests
Referenced in `homework-4/skills/unit-tests-FIRST.md`:
- **Fast**: Tests run in milliseconds; no real I/O
- **Isolated**: No test dependencies, each test is independent
- **Repeatable**: Same result every run, no flaky timeouts
- **Self-checking**: Assert all observable behavior
- **Timely**: Write before or alongside implementation (TDD)

## Multi-Language Implementation Patterns

### Dual Implementations (Node.js ↔ Python)
`homework-1/` implements **identical banking API** in both stacks to compare approaches:

| Aspect | Node.js | Python |
|--------|---------|--------|
| Framework | Express.js | FastAPI |
| Validation | Custom validators | Pydantic models |
| Testing | Jest | Pytest |
| API Docs | Swagger/OpenAPI | FastAPI auto-docs |
| Storage | In-memory objects | In-memory dicts |

**Why**: Students can compare AI assistance and code clarity across languages at same complexity level.

### When Adding Features
If feature added to `homework-1/nodejs/src/routes/`, implement same functionality in `homework-1/python/src/` with:
- Same endpoint paths and methods
- Same request/response models
- Same business logic (not framework-specific shortcuts)
- Parallel test coverage in both stacks

## AI-Specific Guidance

### Documentation Expectations
Every assignment submission requires:
1. **README.md** — Overview, features, technology stack, AI tools used
2. **HOWTORUN.md** — Step-by-step build/test/run instructions
3. **IMPLEMENTATION_SUMMARY.md** — What was built, why, challenges
4. **docs/screenshots/** — Screenshots of:
   - AI tool interactions (prompts, suggestions, corrections)
   - Application running successfully
   - Test results and coverage reports
   - Architecture diagrams (if applicable)

### Dual-Stack Insights
When working with both Node.js and Python implementations:
1. Compare error handling approaches (try/catch vs try/except)
2. Note framework differences (Express middleware vs FastAPI dependencies)
3. Highlight where one language's idioms are clearer than the other
4. Document which AI tool was more effective for each language

### Architecture Documentation
For complex systems (homework-2, homework-3), create:
- **ARCHITECTURE.md** with diagrams (Mermaid flowcharts, sequence diagrams)
- **API_REFERENCE.md** with endpoint specifications and examples
- **TESTING_GUIDE.md** with test organization and coverage strategy

## Key Files to Read for Pattern Examples

1. **Multi-language patterns**: `homework-1/nodejs/src/` + `homework-1/python/src/`
2. **Layered architecture**: `homework-2/src/main/java/` (service, controller, repository layers)
3. **Factory pattern**: `homework-2/src/main/java/.../services/ParserFactory.java`
4. **Domain constraints**: `homework-3/specification.md` (state machines, RBAC, encryption)
5. **Test coverage**: `practice/workshop3/tests/` and `practice/workshop3/TEST_SUMMARY.md`
6. **Agent pipelines**: `homework-4/agents/` (research, implementation, security, testing)
7. **Banking transaction logic**: `homework-1/nodejs/src/transaction.js` (in-memory storage patterns)

## Common Tasks & How to Approach Them

**Adding a new API endpoint**:
1. Define route in appropriate `routes/` or `controller/` file
2. Implement service logic (business rules, validation)
3. Add repository method if data access needed
4. Write unit tests for service, integration tests for endpoint
5. Update API documentation (README.md or API_REFERENCE.md)
6. If dual-stack, implement in both Node.js and Python

**Adding validation**:
- Node.js: Extend `validators.js` function and call in route handler
- Python: Add Pydantic field validator or extend model
- Java: Add Jakarta Bean Validation annotation or custom validator class

**Supporting new file format** (beyond CSV/JSON/XML):
1. Create `YamlFileParser` implementing `FileParser` interface
2. Register in `ParserFactory.getParser()` method
3. Add tests mirroring existing parser tests
4. Update `ImportSummaryResponse` if format has unique metadata

**Multi-threaded/Concurrent access**:
- Java: Use `SELECT FOR UPDATE` row-level locking (card lifecycle)
- Node.js/Python: Use Redis locks or database transactions where needed
- Example: `homework-3/specification.md` concurrency section

## Banking Domain Reference

When implementing financial features, refer to:
- **`homework-3/specification.md`** — Complete RBAC matrix, API specs, state machine
- **`homework-1/`** — Transaction models (account, type, amount, currency)
- **`practice/workshop3/`** — Real transaction parsing and validation

Key banking concepts:
- Transactions are immutable after creation (only cancel, never modify)
- Accounts track running balance (sum of all transactions)
- Audit logs record all state changes with user and timestamp
- Sensitive PII is encrypted at field level (not just in transit)

## Getting Unblocked

1. **Run commands not clear**: Check `HOWTORUN.md` in relevant homework folder
2. **Test failures in Java**: Verify Java 17+ and Maven installed; check `mvn --version`
3. **Python environment issues**: Use `python -m venv` to create isolated environment
4. **Node dependencies**: Delete `node_modules/` and `package-lock.json`, then `npm install` clean
5. **Database not available**: Homework-2 tests use H2 in-memory by default (no PostgreSQL needed)
6. **Coverage gaps**: Check test output for uncovered lines, add edge case tests

---

*Last updated: March 4, 2026 | Source: CLAUDE.md, homework READMEs, architecture docs, test summaries*
