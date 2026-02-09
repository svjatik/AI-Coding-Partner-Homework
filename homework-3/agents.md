# AI Agent Guidelines for Virtual Card Lifecycle Management System

## Tech Stack

### Backend
- **Language**: Python 3.11+
- **Framework**: FastAPI 0.104+
- **ORM**: SQLAlchemy 2.0+ with Alembic for migrations
- **Database**: PostgreSQL 15+ with UUID extension
- **Cache**: Redis 7+ for caching and rate limiting
- **Task Queue**: Celery with Redis broker for async operations
- **Testing**: pytest, pytest-asyncio, pytest-cov, httpx for API testing
- **Security**: cryptography library for AES-256-GCM encryption, PyJWT for JWT handling
- **Validation**: Pydantic v2 for request/response validation

### Infrastructure
- **Containerization**: Docker with multi-stage builds
- **Orchestration**: Kubernetes (development) or Docker Compose (local)
- **Monitoring**: Prometheus + Grafana
- **Logging**: Structured JSON logging with python-json-logger
- **Secrets Management**: Environment variables from secure vault

## Domain Rules: Banking & FinTech

### Compliance Requirements
1. **PCI-DSS Level 1 Compliance**
   - Never store unencrypted card data (PAN, CVV, expiry)
   - Use field-level encryption with AES-256-GCM
   - Implement secure key management and rotation
   - Maintain audit logs for all card data access
   - Mask card numbers in logs and API responses (show only last 4 digits)
   - Enforce TLS 1.3 for all data transmission

2. **GDPR Compliance**
   - Implement data minimization: collect only necessary data
   - Support data subject rights: access, rectification, erasure, portability
   - Maintain data retention policies and automated deletion
   - Log all personal data access with purpose
   - Provide data breach notification mechanisms

3. **Regulatory Requirements**
   - Maintain immutable audit trails for 10 years
   - Implement transaction monitoring for AML/CFT compliance
   - Support regulatory reporting (SAR, CTR)
   - Enforce Know Your Customer (KYC) verification before card issuance

### Security Constraints
1. **Authentication & Authorization**
   - Use OAuth 2.0 with JWT tokens (RS256 algorithm)
   - Enforce role-based access control (RBAC)
   - Require MFA for sensitive operations (card cancellation, limit increases)
   - Implement session management with secure timeout (15 minutes)
   - Log all authentication and authorization attempts

2. **Data Protection**
   - Encrypt all sensitive data at rest and in transit
   - Use parameterized queries to prevent SQL injection
   - Implement input validation and sanitization
   - Apply principle of least privilege for database access
   - Never expose internal error details in API responses

3. **Rate Limiting & DDoS Protection**
   - Implement per-user rate limits: 60 requests/minute for reads, 10 requests/minute for writes
   - Implement per-IP rate limits to prevent abuse
   - Use sliding window algorithm for accurate rate limiting
   - Return 429 status with Retry-After header when limits exceeded

### Business Logic Rules
1. **Card State Machine** (enforce strictly)
   - PENDING → ACTIVE (after successful issuance)
   - ACTIVE ↔ FROZEN (user can freeze/unfreeze)
   - ACTIVE → CANCELLED (permanent, no reversal)
   - FROZEN → CANCELLED (permanent, no reversal)
   - CANCELLED and EXPIRED are terminal states
   - Reject any invalid state transitions with 400 error

2. **Spending Limits Validation**
   - daily_limit ≤ monthly_limit ≤ overall_limit (strict validation)
   - All limits must be positive Decimal values
   - Reject transactions exceeding configured limits before processing
   - Track spend in real-time; use Redis for performance

3. **Monetary Calculations**
   - **CRITICAL**: Always use `decimal.Decimal` for all monetary amounts
   - Never use `float` or `int` for money (causes precision errors)
   - Set decimal precision to 4 decimal places (Decimal(19,4))
   - Use `ROUND_HALF_UP` rounding mode for calculations
   - Validate currency codes (ISO 4217)

4. **Idempotency**
   - All state-changing operations must accept Idempotency-Key header
   - Store idempotency keys in Redis with 24-hour TTL
   - Return cached response if duplicate request detected
   - Use hash of (user_id + endpoint + idempotency_key) as Redis key

## Code Style & Conventions

### Python Style
- Follow PEP 8 strictly; use `black` formatter (line length: 100)
- Use type hints for all function signatures (enforce with `mypy --strict`)
- Use dataclasses or Pydantic models for structured data
- Prefer explicit over implicit (avoid magic behavior)
- Use f-strings for string formatting
- Organize imports: standard library → third-party → local (use `isort`)

### Naming Conventions
- **Files**: snake_case (e.g., `card_service.py`)
- **Classes**: PascalCase (e.g., `CardService`)
- **Functions/Variables**: snake_case (e.g., `create_card`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_DAILY_LIMIT`)
- **Private members**: prefix with underscore (e.g., `_validate_state`)
- **Database tables**: plural snake_case (e.g., `cards`, `transactions`)

### Project Structure
```
src/
├── api/              # API route handlers
├── models/           # SQLAlchemy ORM models
├── schemas/          # Pydantic request/response schemas
├── services/         # Business logic layer
├── repositories/     # Data access layer
├── middleware/       # Auth, logging, error handling
├── security/         # Encryption, key management
├── validators/       # Custom validation logic
├── utils/            # Helper functions
└── config.py         # Configuration management
tests/
├── unit/             # Unit tests
├── integration/      # Integration tests
├── security/         # Security tests
└── compliance/       # Compliance validation tests
```

### Error Handling
- Use custom exception hierarchy: `BaseAPIException` → `ValidationError`, `AuthorizationError`, `NotFoundError`
- Catch specific exceptions; avoid bare `except`
- Never expose stack traces in production responses
- Log exceptions with correlation ID for tracing
- Return standardized error format:
  ```json
  {
    "error_code": "INVALID_CARD_STATE",
    "message": "Cannot freeze a cancelled card",
    "trace_id": "uuid-v4",
    "details": {"current_state": "CANCELLED"}
  }
  ```

### Logging Standards
- Use structured JSON logging (timestamp, level, message, context)
- Include correlation_id in all logs for request tracing
- Log levels:
  - DEBUG: detailed diagnostic info (not in production)
  - INFO: normal operations (card created, transaction processed)
  - WARNING: unexpected but handled situations
  - ERROR: errors requiring investigation
  - CRITICAL: system failures requiring immediate action
- **Never log**: PAN, CVV, passwords, encryption keys, JWT tokens
- **Always log**: user_id, ip_address, action, status, duration

## Testing Expectations

### Coverage Requirements
- **Minimum overall coverage**: 80%
- **Critical paths coverage**: 90% (card operations, transaction processing, security)
- Fail CI/CD pipeline if coverage drops below threshold

### Test Types
1. **Unit Tests** (`tests/unit/`)
   - Test individual functions in isolation
   - Mock all external dependencies (database, Redis, APIs)
   - Use pytest fixtures for test data
   - Validate both success and error paths

2. **Integration Tests** (`tests/integration/`)
   - Test complete workflows end-to-end
   - Use test database (reset before each test)
   - Validate API contracts and error responses
   - Test concurrent operations and race conditions

3. **Security Tests** (`tests/security/`)
   - Test OWASP Top 10 vulnerabilities
   - Validate authentication and authorization
   - Test encryption/decryption correctness
   - Test SQL injection, XSS, CSRF prevention

4. **Compliance Tests** (`tests/compliance/`)
   - Verify audit logs created for all operations
   - Validate data masking rules
   - Test GDPR data export/deletion
   - Verify PCI-DSS requirements met

### Test Data
- Use factories (e.g., `factory_boy`) for test data generation
- Create realistic test scenarios: various card states, user roles, edge cases
- Test boundary values: zero amounts, maximum limits, expired dates
- Test error conditions: invalid inputs, unauthorized access, timeout scenarios

### Test Naming
- Use descriptive names: `test_<function>_<scenario>_<expected_result>`
- Examples:
  - `test_create_card_with_valid_data_returns_201`
  - `test_freeze_cancelled_card_returns_400_error`
  - `test_unauthorized_user_cannot_view_other_cards`

## Performance Expectations

### Response Time SLAs
- Read operations: <200ms (p95)
- Write operations: <500ms (p95)
- Database queries: <100ms (p95)
- Cache operations: <10ms (p95)

### Optimization Guidelines
- Use database indexes on frequently queried columns
- Implement caching for read-heavy operations (Redis with TTL)
- Use connection pooling for database and Redis
- Paginate large result sets (max 100 items per page)
- Use background jobs (Celery) for non-critical async operations
- Implement database query optimization (avoid N+1 queries)

## Security Best Practices

### Secure Coding Principles
- **Input Validation**: Validate and sanitize all user inputs
- **Output Encoding**: Encode data before rendering to prevent XSS
- **Parameterized Queries**: Always use ORM or parameterized queries
- **Least Privilege**: Grant minimum necessary permissions
- **Defense in Depth**: Implement multiple layers of security
- **Fail Securely**: Default deny; fail closed, not open

### Secrets Management
- Never hardcode secrets in code
- Use environment variables loaded from secure vault
- Rotate secrets regularly (encryption keys quarterly, JWT keys monthly)
- Use different secrets for each environment (dev, staging, prod)

### Dependency Management
- Keep dependencies up to date; scan for vulnerabilities weekly
- Use `pip-audit` or `safety` to check for known CVEs
- Pin exact versions in `requirements.txt`
- Review security advisories for third-party libraries

## AI Agent Behavior Guidelines

### Code Generation
1. **Always**:
   - Include type hints and docstrings
   - Add error handling and input validation
   - Write corresponding unit tests
   - Follow the established project structure
   - Use existing patterns and conventions
   - Add audit logging for state-changing operations

2. **Never**:
   - Use float for monetary calculations (use Decimal)
   - Log sensitive data (PAN, CVV, passwords)
   - Expose internal errors in API responses
   - Skip input validation
   - Implement custom cryptography (use proven libraries)
   - Ignore compliance requirements

### Code Review Checklist
Before submitting code, verify:
- [ ] Type hints on all functions
- [ ] Input validation implemented
- [ ] Error handling with custom exceptions
- [ ] Audit logging for operations
- [ ] Tests written (unit + integration)
- [ ] Security considerations addressed
- [ ] Compliance requirements met (PCI-DSS, GDPR)
- [ ] No hardcoded secrets or credentials
- [ ] Performance optimizations (indexes, caching)
- [ ] Documentation updated (docstrings, API docs)

### When Uncertain
1. Ask clarifying questions about requirements
2. Propose multiple implementation approaches with trade-offs
3. Reference industry standards (PCI-DSS, OWASP, NIST)
4. Default to secure and compliant solutions
5. Document assumptions and decisions

## Common Patterns

### Repository Pattern Example
```python
class CardRepository:
    async def create(self, card: Card) -> Card:
        async with self.db.begin():
            self.db.add(card)
            await self.db.flush()
            return card

    async def get_by_id(self, card_id: UUID, user_id: UUID) -> Optional[Card]:
        stmt = select(Card).where(
            Card.id == card_id,
            Card.user_id == user_id,
            Card.deleted_at.is_(None)
        )
        result = await self.db.execute(stmt)
        return result.scalar_one_or_none()
```

### Service Layer with Audit Example
```python
class CardService:
    async def freeze_card(
        self,
        card_id: UUID,
        user_id: UUID,
        reason: str,
        idempotency_key: str
    ) -> CardResponse:
        # Check idempotency
        cached = await self.cache.get(idempotency_key)
        if cached:
            return cached

        # Validate authorization
        card = await self.repo.get_by_id(card_id, user_id)
        if not card:
            raise NotFoundError("Card not found")

        # Validate state transition
        if card.status != CardStatus.ACTIVE:
            raise ValidationError(f"Cannot freeze card in {card.status} state")

        # Audit before state
        before_state = {"status": card.status.value}

        # Update state
        card.status = CardStatus.FROZEN
        card.frozen_at = datetime.utcnow()
        card.freeze_reason = reason
        await self.repo.update(card)

        # Audit after state
        after_state = {"status": card.status.value, "frozen_at": card.frozen_at}
        await self.audit.log(
            entity_type="card",
            entity_id=card_id,
            action="FREEZE",
            user_id=user_id,
            before_state=before_state,
            after_state=after_state
        )

        # Cache response
        response = CardResponse.from_orm(card)
        await self.cache.set(idempotency_key, response, ttl=86400)

        return response
```

### Error Handler Example
```python
@app.exception_handler(ValidationError)
async def validation_error_handler(request: Request, exc: ValidationError):
    return JSONResponse(
        status_code=400,
        content={
            "error_code": exc.error_code,
            "message": exc.message,
            "trace_id": str(request.state.trace_id),
            "details": exc.details
        }
    )
```
