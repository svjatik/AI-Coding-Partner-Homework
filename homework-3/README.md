# Homework 3: Specification-Driven Design

## Student & Task Summary

**Student**: Sviatoslav Glushchenko

**Task**: Design a specification package for a **Virtual Card Lifecycle Management System** — a finance-oriented application that enables users to create, manage, and monitor virtual payment cards in a regulated banking environment. No implementation required; only specification documents, agent rules, and a rationale README.

### Deliverables

| File | Purpose | Audience |
|---|---|---|
| `specification.md` | Full product spec: objectives, architecture diagrams, state machine, API endpoints, RBAC matrix, and 10 low-level implementation tasks | AI coding agent (implementer) |
| `agents.md` | Tech stack, banking domain rules, code style, testing expectations, security/compliance constraints, common patterns with code examples | AI coding agent (reviewer/generator) |
| `.claude/project-rules.md` | Critical rules (NEVER/ALWAYS), anti-patterns, code examples for correct vs. incorrect approaches, compliance checklist | Claude Code (real-time guidance) |
| `README.md` | Rationale, design decisions, and industry best practices mapping | Human reviewer (instructor) |

---

## Rationale

### Design Decisions

This specification follows a **top-down, compliance-first methodology** where regulatory and security constraints shape the architecture before technical details are filled in. Below are the key design decisions and their reasoning.

| # | Decision | Rationale |
|---|---|---|
| 1 | **Compliance as foundation** | PCI-DSS and GDPR requirements are embedded from the outset — in data models, services, and API design. Retrofitting compliance is costly; baking it in ensures AI agents naturally produce compliant code. |
| 2 | **State machine for card lifecycle** | The card lifecycle (PENDING → ACTIVE ↔ FROZEN → CANCELLED/EXPIRED) is modeled as a strict state machine with a Mermaid diagram and transition table. This eliminates ambiguity — AI agents generate validation logic that rejects invalid transitions, preventing bugs. |
| 3 | **Security by default (prohibitive rules)** | Security requirements are mandatory, not optional. The spec explicitly prohibits insecure patterns (`float` for money, logging PAN, exposing stack traces). "Never do X" rules in `.claude/project-rules.md` prevent common FinTech vulnerabilities at generation time. |
| 4 | **Granular task decomposition** | 10 low-level tasks, each with: prompt, target files, target functions, and detailed constraints. This maps directly to an AI coding workflow — each task is self-contained and executable without interpretation. |
| 5 | **Audit-first architecture** | Every state-changing operation requires audit logging with before/after state JSON. Specified at the service layer (not infrastructure) so business logic and audit logic are coupled. Financial regulators require immutable trails — this makes it non-optional. |
| 6 | **Decimal precision mandate** | `Decimal(19,4)` with `ROUND_HALF_UP` for all monetary amounts. A single float precision error can cause regulatory violations or financial loss. Enforced in all three documents. |
| 7 | **Three-layer documentation** | `specification.md` = **what** to build, `agents.md` = **how** to build it, `.claude/project-rules.md` = **what to avoid**. Different AI agents (generators, reviewers, testers) need different context; separating concerns prevents information overload. |

### Why This Structure Works for AI

The specification is designed to maximize AI agent effectiveness:

- **Mermaid diagrams** in `specification.md` provide visual architecture and state machine context that AI agents can parse
- **RBAC permission matrix** and **API endpoint table** provide unambiguous reference for authorization checks
- **Code examples** in `agents.md` and `.claude/project-rules.md` show correct vs. incorrect patterns (e.g., `Decimal` vs. `float`, masked vs. raw PAN logging)
- **Idempotency-Key patterns** are specified with Redis TTL, cache key format, and response caching — no room for interpretation
- **Test naming conventions** (`test_<function>_<scenario>_<expected_result>`) guide AI to produce well-structured test suites

---

## Industry Best Practices

The table below maps each incorporated FinTech/banking best practice to its specific location(s) in the specification documents.

### Quick Reference: Practice → Location Matrix

| # | Practice | Standard/Source | specification.md | agents.md | .claude/project-rules.md |
|---|---|---|---|---|---|
| 1 | **PCI-DSS card data encryption** | PCI-DSS Req. 3 | Implementation Notes; Task 6 (AES-256-GCM, encrypt PAN/CVV, key hierarchy) | Compliance Requirements → PCI-DSS Level 1; Data Protection | NEVER store unencrypted card data; Compliance Checklist |
| 2 | **Immutable audit trails** | PCI-DSS Req. 10; SOX | Mid-Level Objectives; Task 7 (write-once storage, hash chain, 10-year retention) | Regulatory Requirements; Service Layer with Audit Example | ALWAYS log with correlation ID; Audit Logging Pattern |
| 3 | **RBAC / Principle of Least Privilege** | NIST AC-6; PCI-DSS Req. 7 | RBAC Permission Matrix; Task 4 (4 roles, ownership validation) | Security Constraints → Auth & Authz; Secure Coding Principles | Authorization Checks code example |
| 4 | **GDPR data minimization & subject rights** | GDPR Art. 5, 15-20 | Mid-Level Objectives; Task 1 (soft delete); Task 7 (DSAR, erasure, portability) | GDPR Compliance section (5 requirements) | Compliance Checklist → GDPR items |
| 5 | **Idempotency for financial operations** | Industry standard | Implementation Notes; Task 2 (Redis, 24h TTL); Task 5 (Idempotency-Key header) | Business Logic Rules → Idempotency | ALWAYS implement idempotency; Idempotency Pattern |
| 6 | **Decimal precision for money** | IEEE 854; Industry | Implementation Notes (Decimal(19,4)); Task 1 (DB column type) | Monetary Calculations (CRITICAL rule + code example) | NEVER use float for money; Monetary Calculations example |
| 7 | **Input validation & parameterized queries** | OWASP Top 10 (A03) | Implementation Notes; Task 5 (Pydantic validation rules) | Security Constraints → Data Protection; Secure Coding Principles | ALWAYS validate; Input Validation Pydantic example |
| 8 | **Rate limiting** | OWASP (API4) | Implementation Notes; Task 3 (60/min reads); Task 5 (rate limit headers) | Rate Limiting & DDoS Protection (sliding window) | HTTP 429 in Quick Reference |
| 9 | **MFA for sensitive operations** | PCI-DSS Req. 8; NIST 800-63B | Mid-Level Objectives; Task 4 (cancel, limit increase) | Auth & Authz → MFA requirement | Authentication Flow |
| 10 | **Data masking (PAN tokenization)** | PCI-DSS Req. 3.4 | Task 3 (`mask_card_number`); Task 6 (never log decrypted data) | PCI-DSS → mask in logs/responses; Logging Standards | NEVER log sensitive data; Masking code example |
| 11 | **Separation of duties** | PCI-DSS Req. 6; SOX | RBAC Matrix (SUPPORT_AGENT masked, COMPLIANCE_OFFICER audit-only) | KYC verification; Role definitions | Authorization Checks |
| 12 | **Error handling without info disclosure** | OWASP (A01) | Implementation Notes; Task 5 (standardized error response schema) | Error Handling → no stack traces; Fail Securely | NEVER expose internal errors; Error Handler example |
| 13 | **Defense in depth** | NIST SP 800-53 | Multiple security tasks (4: Auth, 6: Encryption, 7: Audit) | Secure Coding Principles → Defense in Depth | ALWAYS rules cover multiple security layers |
| 14 | **Concurrency control** | Industry standard | Task 2 (`SELECT FOR UPDATE`, row-level locking) | Repository Pattern example | Concurrent Operations (wrong vs. correct) |
| 15 | **Multi-layered testing** | Industry standard | Implementation Notes (90% cov); Task 8 (unit/integration/security/compliance) | Testing Expectations (4 test types, coverage thresholds) | Test naming convention; Test file organization |
| 16 | **Encryption key rotation** | PCI-DSS Req. 3.6 | Task 6 (quarterly rotation, dual-key transition, HSM hierarchy) | Secrets Management → rotation schedule | Environment Variables reference |
| 17 | **Structured logging with correlation IDs** | 12-Factor App | Task 5 (request logging fields); Task 7 (audit context fields) | Logging Standards (JSON, levels, what to log/not log) | ALWAYS log with correlation ID |
| 18 | **API versioning & OpenAPI docs** | REST best practices | API Endpoints table (`/api/v1/`); Task 9 (OpenAPI 3.0) | Tech Stack | N/A |
| 19 | **Database design** (UUIDs, indexes, soft delete) | Industry standard | Task 1 (UUIDs, composite indexes, check constraints, soft delete) | Naming Conventions → DB Models; Performance → indexes | Database Models code example |
| 20 | **Monitoring & alerting** | SRE principles | Task 10 (Prometheus, alert thresholds); Task 7 (compliance alerts) | Performance SLAs (200ms reads, 500ms writes) | N/A |

### Detailed Highlights

Below are the three most impactful practices with deeper explanation of how they are woven through the specification.

#### PCI-DSS Card Data Encryption (Practice #1)

This practice is the most pervasive constraint in the specification. It affects:
- **Data model design** (Task 1): `card_number_encrypted` and `cvv_encrypted` fields instead of plaintext; encrypted data format defined as `version:key_id:iv:ciphertext:tag`
- **Service layer** (Task 2): All card operations interact with `EncryptionService`; decrypted data never leaves the service boundary
- **API responses** (Task 3, 5): `mask_card_number()` function ensures only last 4 digits are ever returned
- **Logging** (Task 7): Audit logs capture card_id references but never decrypted PAN/CVV
- **Key management** (Task 6): HSM-backed master keys, quarterly rotation, dual-key transition period
- **Agent rules**: Both `agents.md` and `.claude/project-rules.md` include explicit "NEVER" rules with code examples showing correct vs. incorrect handling

#### State Machine Validation (Practice #2, #14)

The card lifecycle is the core business domain. Rather than relying on ad-hoc `if/else` checks:
- **specification.md** includes a **Mermaid state diagram** for visual clarity and an **allowed transitions table** for unambiguous reference
- **Task 2** mandates `validate_state_transition(current, target)` as a dedicated function
- **agents.md** includes a wrong-vs-correct code example for state management
- **.claude/project-rules.md** lists this in the "ALWAYS" rules with explicit anti-pattern (updating status without validation)
- **Task 8** requires testing invalid state transitions as specific test cases

#### Audit-First Architecture (Practice #2, #17)

Financial regulations (PCI-DSS Req. 10, SOX Section 302) require complete audit trails:
- **specification.md** Task 7 specifies a **hash chain** where each audit entry includes the hash of the previous entry, creating a tamper-evident log
- The `AuditLog` model (Task 1) captures before/after state as JSON, enabling forensic reconstruction
- Every service method in Task 2 includes audit logging as a mandatory step, not a decorator or afterthought
- **agents.md** provides a complete code example of the audit pattern in `CardService.freeze_card()`
- Retention policies are differentiated: transactions (7 years), audit logs (10 years), personal data (GDPR request-dependent)

---

## Summary

This specification package translates a broad requirement ("build a virtual card system for a regulated environment") into actionable, AI-executable guidance across three complementary documents. The approach prioritizes:

1. **Compliance by design** — PCI-DSS and GDPR constraints shape every layer
2. **Unambiguous references** — Mermaid diagrams, RBAC matrix, API table, state transition table
3. **Negative constraints** — explicit "NEVER do X" rules with code examples to prevent common FinTech pitfalls
4. **Testable specification** — each practice maps to specific test types in Task 8

The three-document structure ensures AI agents receive the right level of guidance whether they are generating code (`specification.md`), enforcing standards (`agents.md`), or avoiding anti-patterns (`.claude/project-rules.md`).
