# Claude Code Project Rules: Virtual Card Lifecycle Management System

## Project Context
This is a **FinTech/banking application** handling sensitive financial data. All code must comply with **PCI-DSS Level 1**, **GDPR**, and financial industry regulations. Security, compliance, and auditability are paramount.

## Critical Rules

### 🚨 NEVER Do These
1. **NEVER use `float` for money** - Always use `decimal.Decimal` for all monetary amounts and calculations
2. **NEVER log sensitive data** - No PAN (card numbers), CVV, passwords, encryption keys, or JWT tokens in logs
3. **NEVER expose internal errors** - Return generic error messages to users; log details internally
4. **NEVER skip input validation** - Validate and sanitize all user inputs
5. **NEVER hardcode secrets** - Use environment variables loaded from secure vault
6. **NEVER store unencrypted card data** - Use AES-256-GCM for PAN, CVV, and sensitive fields
7. **NEVER skip audit logging** - Log all state-changing operations with before/after state
8. **NEVER allow invalid state transitions** - Enforce card state machine strictly

### ✅ ALWAYS Do These
1. **ALWAYS use type hints** - Include type hints on all function signatures and enable `mypy --strict`
2. **ALWAYS validate state transitions** - Check current state before allowing operations
3. **ALWAYS implement idempotency** - Accept and validate Idempotency-Key header for state-changing operations
4. **ALWAYS mask card numbers** - Show only last 4 digits in API responses and logs (e.g., `****1234`)
5. **ALWAYS use parameterized queries** - Never build SQL with string concatenation
6. **ALWAYS write tests** - Include unit tests for all new functions; aim for 90% coverage on critical paths
7. **ALWAYS log with correlation ID** - Include trace_id in all logs for request tracing
8. **ALWAYS check authorization** - Verify user has permission before allowing access to resources

## Code Patterns to Follow

### Monetary Calculations
```python
# ✅ CORRECT
from decimal import Decimal, ROUND_HALF_UP

amount = Decimal("123.45")
tax = amount * Decimal("0.05")
total = amount + tax  # Decimal('129.8225')
total_rounded = total.quantize(Decimal("0.01"), ROUND_HALF_UP)

# ❌ WRONG - NEVER DO THIS
amount = 123.45  # float causes precision errors
tax = amount * 0.05
total = amount + tax  # 129.82249999999999
```

### Card Number Masking
```python
# ✅ CORRECT
def mask_card_number(pan: str) -> str:
    """Mask all but last 4 digits of card number."""
    if len(pan) < 4:
        return "****"
    return "*" * (len(pan) - 4) + pan[-4:]

# Usage: "1234567890123456" -> "************3456"

# ❌ WRONG - NEVER log or return full PAN
logger.info(f"Card created: {pan}")  # NEVER DO THIS
return {"card_number": pan}  # NEVER DO THIS
```

### Audit Logging Pattern
```python
# ✅ CORRECT - Always log before/after state
before_state = {"status": card.status.value, "limit": str(card.daily_limit)}

# ... perform operation ...

after_state = {"status": card.status.value, "limit": str(card.daily_limit)}
await audit_service.log(
    entity_type="card",
    entity_id=card.id,
    action="UPDATE_LIMIT",
    user_id=current_user.id,
    before_state=before_state,
    after_state=after_state,
    ip_address=request.client.host
)
```

### Error Handling Pattern
```python
# ✅ CORRECT - Custom exceptions with standardized responses
class InvalidStateTransitionError(ValidationError):
    def __init__(self, current_state: str, target_state: str):
        super().__init__(
            error_code="INVALID_STATE_TRANSITION",
            message=f"Cannot transition from {current_state} to {target_state}",
            details={"current_state": current_state, "target_state": target_state}
        )

# Usage
if card.status == CardStatus.CANCELLED:
    raise InvalidStateTransitionError(card.status, "FROZEN")

# ❌ WRONG - Generic exceptions and exposed details
raise Exception(f"Database error: {db_error}")  # NEVER DO THIS
```

### Idempotency Pattern
```python
# ✅ CORRECT - Check and store idempotency key
@router.post("/cards/{card_id}/freeze")
async def freeze_card(
    card_id: UUID,
    request: FreezeCardRequest,
    idempotency_key: str = Header(..., alias="Idempotency-Key")
):
    # Check if already processed
    cache_key = f"idempotency:{current_user.id}:{idempotency_key}"
    cached_response = await redis.get(cache_key)
    if cached_response:
        return cached_response

    # Process request
    result = await card_service.freeze_card(card_id, current_user.id, request.reason)

    # Cache response for 24 hours
    await redis.setex(cache_key, 86400, result.json())
    return result
```

## Naming Conventions

### Variables & Functions
- Use descriptive names: `daily_spending_limit` not `dsl`
- Boolean variables: prefix with `is_`, `has_`, `can_`, `should_`
- Money amounts: suffix with `_amount` (e.g., `transaction_amount`)
- Timestamps: suffix with `_at` for datetime (e.g., `created_at`)

### Constants
```python
# ✅ Financial limits
MAX_DAILY_LIMIT = Decimal("10000.00")
MAX_MONTHLY_LIMIT = Decimal("50000.00")
MIN_TRANSACTION_AMOUNT = Decimal("0.01")

# ✅ Compliance periods
AUDIT_LOG_RETENTION_YEARS = 10
TRANSACTION_RETENTION_YEARS = 7
PCI_KEY_ROTATION_DAYS = 90
```

### Database Models
```python
# ✅ CORRECT - Clear field names with constraints
class Card(Base):
    __tablename__ = "cards"

    id: Mapped[UUID] = mapped_column(primary_key=True, default=uuid4)
    user_id: Mapped[UUID] = mapped_column(ForeignKey("users.id"), nullable=False, index=True)
    card_number_encrypted: Mapped[str] = mapped_column(String(500), nullable=False, unique=True)
    cvv_encrypted: Mapped[str] = mapped_column(String(500), nullable=False)
    status: Mapped[CardStatus] = mapped_column(Enum(CardStatus), nullable=False, index=True)
    daily_limit: Mapped[Decimal] = mapped_column(Numeric(19, 4), nullable=False)
    monthly_limit: Mapped[Decimal] = mapped_column(Numeric(19, 4), nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc))
    deleted_at: Mapped[Optional[datetime]] = mapped_column(DateTime(timezone=True), nullable=True)
```

## Security Requirements

### Authentication Flow
1. Extract JWT from `Authorization: Bearer <token>` header
2. Validate JWT signature and expiration
3. Extract `user_id` and `roles` from claims
4. Attach to request context for downstream use
5. Log authentication attempt (success/failure)

### Authorization Checks
```python
# ✅ CORRECT - Always verify ownership
async def get_card(card_id: UUID, current_user: User):
    card = await card_repo.get_by_id(card_id)
    if not card:
        raise NotFoundError("Card not found")

    # Critical: Verify user owns this card
    if card.user_id != current_user.id and not current_user.has_role("ADMIN"):
        raise ForbiddenError("Access denied")

    return card
```

### Input Validation
```python
# ✅ CORRECT - Use Pydantic for validation
class CreateCardRequest(BaseModel):
    card_type: Literal["VIRTUAL_SINGLE_USE", "VIRTUAL_MULTI_USE"]
    daily_limit: Decimal = Field(gt=0, le=Decimal("10000.00"))
    monthly_limit: Decimal = Field(gt=0, le=Decimal("50000.00"))

    @model_validator(mode='after')
    def validate_limits(self) -> 'CreateCardRequest':
        if self.daily_limit > self.monthly_limit:
            raise ValueError("daily_limit cannot exceed monthly_limit")
        return self
```

## Testing Requirements

### Test File Organization
```
tests/
├── unit/
│   ├── services/
│   │   ├── test_card_service.py          # Business logic tests
│   │   └── test_transaction_service.py
│   └── validators/
│       └── test_card_validators.py
├── integration/
│   ├── test_card_lifecycle_flow.py       # End-to-end workflows
│   └── test_concurrent_operations.py
├── security/
│   ├── test_authentication.py            # Auth/authz tests
│   ├── test_encryption.py                # Crypto tests
│   └── test_sql_injection.py             # OWASP tests
└── compliance/
    ├── test_audit_logging.py             # Audit completeness
    └── test_pci_compliance.py            # PCI-DSS validation
```

### Test Naming Convention
```python
# ✅ Pattern: test_<function>_<scenario>_<expected_result>

def test_freeze_card_when_active_returns_success():
    """Freezing an active card should succeed."""

def test_freeze_card_when_already_frozen_returns_conflict_error():
    """Freezing an already frozen card should return 409 Conflict."""

def test_freeze_card_when_cancelled_returns_validation_error():
    """Freezing a cancelled card should return 400 Bad Request."""

def test_create_card_without_idempotency_key_returns_error():
    """Creating a card without Idempotency-Key header should fail."""
```

## Performance Guidelines

### Database Query Optimization
```python
# ✅ CORRECT - Use indexes and select specific fields
stmt = (
    select(Card.id, Card.status, Card.daily_limit)
    .where(Card.user_id == user_id, Card.deleted_at.is_(None))
    .order_by(Card.created_at.desc())
    .limit(100)
)

# ❌ WRONG - No indexes, loading full objects
cards = session.query(Card).filter(Card.user_id == user_id).all()
```

### Caching Strategy
```python
# ✅ CORRECT - Cache read-heavy operations
async def get_card_details(card_id: UUID) -> CardResponse:
    cache_key = f"card:{card_id}"
    cached = await redis.get(cache_key)
    if cached:
        return CardResponse.parse_raw(cached)

    card = await card_repo.get_by_id(card_id)
    response = CardResponse.from_orm(card)

    # Cache for 5 minutes
    await redis.setex(cache_key, 300, response.json())
    return response
```

## Compliance Checklist

When implementing any feature, verify:
- [ ] **PCI-DSS**: Card data encrypted? Audit logs created? Data masked in responses?
- [ ] **GDPR**: Personal data minimized? Retention policy applied? Export/delete supported?
- [ ] **Audit**: Operation logged with user_id, timestamp, before/after state?
- [ ] **Security**: Input validated? Authorization checked? Secure against OWASP Top 10?
- [ ] **Error Handling**: Errors caught? Generic messages returned? Details logged?
- [ ] **Testing**: Unit tests written? Edge cases covered? Security tests included?

## Common Mistakes to Avoid

### State Management
```python
# ❌ WRONG - No state validation
async def freeze_card(card_id: UUID):
    card.status = CardStatus.FROZEN
    await db.commit()

# ✅ CORRECT - Validate current state first
async def freeze_card(card_id: UUID):
    if card.status != CardStatus.ACTIVE:
        raise InvalidStateTransitionError(card.status, CardStatus.FROZEN)

    card.status = CardStatus.FROZEN
    card.frozen_at = datetime.now(timezone.utc)
    await db.commit()
```

### Concurrent Operations
```python
# ❌ WRONG - Race condition possible
async def update_card_limit(card_id: UUID, new_limit: Decimal):
    card = await card_repo.get_by_id(card_id)
    card.daily_limit = new_limit
    await db.commit()

# ✅ CORRECT - Use row-level locking
async def update_card_limit(card_id: UUID, new_limit: Decimal):
    stmt = select(Card).where(Card.id == card_id).with_for_update()
    card = await db.scalar(stmt)
    card.daily_limit = new_limit
    await db.commit()
```

### Logging Sensitive Data
```python
# ❌ WRONG - Logs contain PAN
logger.info(f"Created card {card.card_number} for user {user_id}")

# ✅ CORRECT - Use masked version
logger.info(
    f"Created card {mask_card_number(card.card_number)} for user {user_id}",
    extra={"card_id": str(card.id), "user_id": str(user_id)}
)
```

## Quick Reference

### HTTP Status Codes
- `200 OK` - Successful read operation
- `201 Created` - Successful resource creation
- `204 No Content` - Successful delete operation
- `400 Bad Request` - Invalid input/validation error
- `401 Unauthorized` - Missing or invalid authentication
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource does not exist
- `409 Conflict` - State conflict (e.g., duplicate operation)
- `429 Too Many Requests` - Rate limit exceeded
- `500 Internal Server Error` - Unexpected server error

### Environment Variables Required
```bash
# Database
DATABASE_URL=postgresql://user:pass@localhost:5432/cards_db

# Redis
REDIS_URL=redis://localhost:6379/0

# Security
JWT_SECRET_KEY=<from-vault>
JWT_ALGORITHM=RS256
ENCRYPTION_KEY_ID=<from-vault>

# External Services
CARD_PROCESSOR_API_URL=https://api.processor.com
CARD_PROCESSOR_API_KEY=<from-vault>

# Monitoring
LOG_LEVEL=INFO
SENTRY_DSN=<optional>
```

---

**Remember**: This is a financial application handling sensitive data. When in doubt, choose the more secure, more compliant, more auditable option. Never compromise on security or compliance for convenience.
