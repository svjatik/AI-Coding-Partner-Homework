# Homework 3: Specification-Driven Design

## Student & Task Summary

**Student**: Sviatoslav Glushchenko

**Task**: Design a comprehensive specification package for a Virtual Card Lifecycle Management System, a finance-oriented application that enables users to create, manage, and monitor virtual payment cards in a regulated banking environment. This homework focuses on specification-driven design without implementation—producing detailed documentation that an AI coding partner could use to build a compliant, secure, and production-ready system.

## Rationale

### Why This Specification Approach

This specification was designed following a **top-down, compliance-first methodology** that prioritizes regulatory requirements and security constraints before technical implementation details. The rationale for this approach includes:

#### 1. **Regulatory Compliance as Foundation**
Banking applications operate in heavily regulated environments. Rather than treating compliance as an afterthought, this specification embeds **PCI-DSS Level 1** and **GDPR** requirements from the outset. Every component—from data models to API endpoints—was designed with compliance in mind.

**Why**: Retrofitting compliance into an existing system is costly and error-prone. By making it foundational, AI agents implementing this spec will naturally produce compliant code.

#### 2. **State Machine-Driven Design**
The card lifecycle is modeled as a strict state machine (PENDING → ACTIVE ↔ FROZEN → CANCELLED) with explicitly defined valid transitions. This prevents invalid operations and ensures data integrity.

**Why**: State machines eliminate ambiguity. AI agents can generate validation logic that rejects invalid state transitions, preventing bugs and ensuring auditability.

#### 3. **Security by Default**
Security requirements (field-level encryption, RBAC, audit logging) are specified as mandatory implementation notes rather than optional considerations. The specification prohibits insecure patterns (e.g., using `float` for money, logging sensitive data).

**Why**: AI agents trained on security best practices need explicit guidance on what to avoid. "Never do X" rules prevent common vulnerabilities.

#### 4. **Granular Task Decomposition**
The specification breaks down the system into 10 low-level tasks, each with:
- **What prompt to run**: Clear instruction for AI agent
- **What file to create/update**: Concrete deliverable
- **What function to create/update**: Specific code artifact
- **What details to add**: Business rules and constraints

**Why**: This structure maps directly to AI coding workflow. Each task is self-contained and executable without requiring interpretation or additional research.

#### 5. **Audit-First Architecture**
Every state-changing operation requires audit logging with before/after state. This is not optional—it's baked into the service layer specification.

**Why**: Financial regulators require immutable audit trails. By specifying audit logging at the service layer (not infrastructure layer), we ensure business logic and audit logic are coupled.

#### 6. **Type Safety and Monetary Precision**
The specification mandates `Decimal` type for all monetary amounts and requires type hints on all functions. This prevents floating-point precision errors and enables static type checking.

**Why**: Financial calculations demand precision. A single floating-point error can cause regulatory violations or financial loss. Type safety catches errors at development time, not runtime.

#### 7. **Multi-Layered Guidance**
The specification package includes three levels of guidance:
- **specification.md**: What to build (product requirements)
- **agents.md**: How to build it (technical standards, domain rules)
- **.claude/project-rules.md**: What to avoid (common mistakes, anti-patterns)

**Why**: Different AI agents (code generators, reviewers, testers) need different information. Separating concerns ensures each agent has the right context without information overload.

## Industry Best Practices

This specification incorporates established FinTech and banking industry best practices. Below is a comprehensive list of best practices and their specific locations in the specification documents.

### 1. PCI-DSS Compliance (Payment Card Industry Data Security Standard)

**Practice**: Never store unencrypted cardholder data; use strong encryption (AES-256) for PAN, CVV, and sensitive fields.

**Where in Spec**:
- **specification.md**:
  - Implementation Notes → "Data Privacy: Implement field-level encryption for PAN, CVV, and cardholder data per PCI-DSS requirements"
  - Task 6 (Security Implementation) → "Use AES-256-GCM for encryption; store IV with ciphertext"
  - Task 6 → "Encrypt fields: card_number, cvv, cardholder_name, billing_address"
- **agents.md**:
  - Domain Rules: Banking & FinTech → Compliance Requirements → PCI-DSS Level 1 Compliance (lines 10-16)
  - Security Constraints → Data Protection → "Encrypt all sensitive data at rest and in transit"
- **.claude/project-rules.md**:
  - Critical Rules → "NEVER store unencrypted card data"
  - Compliance Checklist → "PCI-DSS: Card data encrypted?"

### 2. Audit Trail and Immutability

**Practice**: Maintain immutable, tamper-proof audit logs for all financial operations with 10-year retention.

**Where in Spec**:
- **specification.md**:
  - Mid-Level Objectives → "Audit & Monitoring: Maintain immutable audit logs for all operations"
  - Implementation Notes → "Audit Requirements: Log all operations with timestamp, user ID, IP address, action type, before/after state"
  - Task 7 (Audit Logging) → "Implement write-once storage: audit logs cannot be modified or deleted"
  - Task 7 → "Create hash chain: each log entry includes hash of previous entry to detect tampering"
- **agents.md**:
  - Domain Rules → Compliance Requirements → Regulatory Requirements → "Maintain immutable audit trails for 10 years"
  - Common Patterns → Service Layer with Audit Example (lines 213-243)
- **.claude/project-rules.md**:
  - Critical Rules → "ALWAYS log with correlation ID"
  - Audit Logging Pattern example

### 3. Principle of Least Privilege (PoLP)

**Practice**: Grant users minimum necessary permissions; implement role-based access control (RBAC).

**Where in Spec**:
- **specification.md**:
  - Mid-Level Objectives → "Security & Access Control: Enforce role-based access control (RBAC)"
  - Task 4 (Authentication and Authorization) → "Define roles: CARDHOLDER, ACCOUNT_ADMIN, SUPPORT_AGENT, COMPLIANCE_OFFICER"
  - Task 4 → "CARDHOLDER can only access their own cards; validate user_id matches card.user_id"
- **agents.md**:
  - Domain Rules → Security Constraints → Authentication & Authorization → "Enforce role-based access control (RBAC)"
  - Security Best Practices → Secure Coding Principles → "Least Privilege: Grant minimum necessary permissions"
- **.claude/project-rules.md**:
  - Security Requirements → Authorization Checks example (verify ownership)

### 4. Data Minimization and Privacy (GDPR)

**Practice**: Collect only necessary data; support data subject rights (access, erasure, portability); implement retention policies.

**Where in Spec**:
- **specification.md**:
  - Mid-Level Objectives → "Compliance & Regulatory: GDPR-compliant data retention"
  - Implementation Notes → "Data Privacy: Implement field-level encryption for PAN, CVV, cardholder data per PCI-DSS requirements"
  - Task 1 (Database Schema) → "Implement soft delete pattern (deleted_at field) for GDPR compliance"
  - Task 7 (Audit Logging) → "GDPR compliance: support data subject access requests (DSAR), right to erasure, data portability"
- **agents.md**:
  - Domain Rules → Compliance Requirements → GDPR Compliance (lines 18-23)
  - Compliance section → "Implement data minimization: collect only necessary data"
- **.claude/project-rules.md**:
  - Compliance Checklist → "GDPR: Personal data minimized? Retention policy applied?"

### 5. Idempotency for Financial Operations

**Practice**: Ensure all state-changing operations are idempotent to prevent duplicate transactions.

**Where in Spec**:
- **specification.md**:
  - Implementation Notes → "Idempotency: All state-changing operations must be idempotent using idempotency keys"
  - Task 2 (Card Management Service) → "Store idempotency keys with 24-hour expiration in Redis"
  - Task 5 (API Endpoints) → "Implement idempotency: Accept Idempotency-Key header for state-changing operations"
- **agents.md**:
  - Domain Rules → Business Logic Rules → Idempotency (lines 60-64)
  - Code example showing idempotency implementation
- **.claude/project-rules.md**:
  - Critical Rules → "ALWAYS implement idempotency"
  - Idempotency Pattern code example

### 6. Decimal Precision for Monetary Calculations

**Practice**: Never use floating-point arithmetic for money; use fixed-precision decimal types.

**Where in Spec**:
- **specification.md**:
  - Implementation Notes → "Monetary Calculations: Use Decimal type for all monetary amounts; never use floating-point arithmetic"
  - Task 1 (Database Schema) → "All monetary fields must use Decimal(19,4) type"
- **agents.md**:
  - Domain Rules → Business Logic Rules → Monetary Calculations (lines 50-58)
  - "CRITICAL: Always use `decimal.Decimal` for all monetary amounts"
  - Code example showing correct Decimal usage
- **.claude/project-rules.md**:
  - Critical Rules → "NEVER use float for money"
  - Monetary Calculations code example (correct vs. wrong)

### 7. Input Validation and Sanitization

**Practice**: Validate and sanitize all user inputs to prevent injection attacks; use parameterized queries.

**Where in Spec**:
- **specification.md**:
  - Implementation Notes → "Input Validation: Sanitize and validate all inputs; enforce strict type checking"
  - Task 5 (API Endpoints) → "Input validation rules" with specific constraints
- **agents.md**:
  - Domain Rules → Security Constraints → Data Protection → "Implement input validation and sanitization"
  - Security Best Practices → Secure Coding Principles → "Input Validation: Validate and sanitize all user inputs"
- **.claude/project-rules.md**:
  - Critical Rules → "ALWAYS validate state transitions"
  - Input Validation code example using Pydantic

### 8. Rate Limiting and DDoS Protection

**Practice**: Implement rate limiting to prevent abuse and denial-of-service attacks.

**Where in Spec**:
- **specification.md**:
  - Implementation Notes → "Rate Limiting: Implement per-user and per-IP rate limits to prevent abuse"
  - Task 3 (Transaction Service) → "Implement rate limiting: 60 requests/minute per user for transaction queries"
  - Task 5 (API Endpoints) → "Include rate limit headers: X-RateLimit-Limit, X-RateLimit-Remaining"
- **agents.md**:
  - Domain Rules → Security Constraints → Rate Limiting & DDoS Protection (lines 44-48)
  - "Implement per-user rate limits: 60 requests/minute for reads, 10 requests/minute for writes"
- **.claude/project-rules.md**:
  - Quick Reference → HTTP Status Codes → `429 Too Many Requests`

### 9. Multi-Factor Authentication (MFA) for Sensitive Operations

**Practice**: Require additional authentication for high-risk operations (card cancellation, limit increases).

**Where in Spec**:
- **specification.md**:
  - Mid-Level Objectives → "Security & Access Control: multi-factor authentication for sensitive operations"
  - Task 4 (Authentication and Authorization) → "Require MFA for: card.cancel, card.update_limits (if increasing limits)"
- **agents.md**:
  - Domain Rules → Security Constraints → Authentication & Authorization → "Require MFA for sensitive operations"
- **.claude/project-rules.md**:
  - Security Requirements → Authentication Flow

### 10. Data Masking and Tokenization

**Practice**: Mask sensitive data in logs and API responses; show only partial information (last 4 digits).

**Where in Spec**:
- **specification.md**:
  - Implementation Notes → "Audit Requirements: never expose internal system details or sensitive data in error messages"
  - Task 3 (Transaction Service) → "Mask full card number except last 4 digits in all API responses"
  - Task 3 → Function `mask_card_number(card_number)` - Masks PAN for display
  - Task 6 (Security Implementation) → "Never log or display decrypted card data; use masked versions only"
- **agents.md**:
  - Domain Rules → Compliance Requirements → PCI-DSS → "Mask card numbers in logs and API responses (show only last 4 digits)"
  - Logging Standards → "Never log: PAN, CVV, passwords, encryption keys"
- **.claude/project-rules.md**:
  - Critical Rules → "NEVER log sensitive data"
  - Card Number Masking code example

### 11. Separation of Duties and Access Control

**Practice**: Different roles have different access levels; support staff see masked data; compliance officers have full audit access.

**Where in Spec**:
- **specification.md**:
  - Task 4 (Authentication and Authorization) → "SUPPORT_AGENT can view cards but receives masked data (partial PAN, no CVV)"
  - Task 4 → "COMPLIANCE_OFFICER (full audit access)"
- **agents.md**:
  - Domain Rules → Compliance Requirements → Regulatory Requirements → "Enforce Know Your Customer (KYC) verification"
- **.claude/project-rules.md**:
  - Security Requirements → Authorization Checks

### 12. Error Handling Without Information Disclosure

**Practice**: Return generic error messages to users; log detailed errors internally; never expose stack traces or database errors.

**Where in Spec**:
- **specification.md**:
  - Implementation Notes → "Error Handling: Return standardized error codes; never expose internal system details or sensitive data in error messages"
  - Task 5 (API Endpoints) → "Standardized error responses: {error_code, message, details, trace_id}"
- **agents.md**:
  - Code Style & Conventions → Error Handling → "Never expose stack traces in production responses"
  - Security Best Practices → Secure Coding Principles → "Fail Securely: Default deny; fail closed"
- **.claude/project-rules.md**:
  - Critical Rules → "NEVER expose internal errors"
  - Error Handling Pattern code example

### 13. Defense in Depth

**Practice**: Implement multiple layers of security (authentication, authorization, encryption, audit logging).

**Where in Spec**:
- **specification.md**:
  - Implementation Notes spans multiple security layers: authentication, encryption, audit, input validation
  - Multiple tasks dedicated to different security aspects (Task 4: Auth, Task 6: Encryption, Task 7: Audit)
- **agents.md**:
  - Security Best Practices → Secure Coding Principles → "Defense in Depth: Implement multiple layers of security"
- **.claude/project-rules.md**:
  - ALWAYS Do These section lists multiple security layers

### 14. Concurrency Control and Race Condition Prevention

**Practice**: Use database row-level locking (SELECT FOR UPDATE) to prevent race conditions in concurrent operations.

**Where in Spec**:
- **specification.md**:
  - Task 2 (Card Management Service) → "Use database transactions with row-level locking (SELECT FOR UPDATE) to prevent race conditions"
  - Task 8 (Testing Suite) → "Test concurrent operations and race conditions"
- **agents.md**:
  - Common Patterns → Repository Pattern Example shows transaction usage
- **.claude/project-rules.md**:
  - Common Mistakes to Avoid → Concurrent Operations code example

### 15. Comprehensive Testing Strategy

**Practice**: Multi-layered testing including unit, integration, security, and compliance tests with high coverage requirements.

**Where in Spec**:
- **specification.md**:
  - Implementation Notes → "Testing: Include unit, integration, security, and compliance tests with minimum 90% code coverage"
  - Task 8 (Testing Suite) → Detailed breakdown of test types and requirements
- **agents.md**:
  - Testing Expectations (lines 127-181) → Comprehensive testing guidelines
  - Coverage Requirements → "Minimum overall coverage: 80%, Critical paths: 90%"
- **.claude/project-rules.md**:
  - Testing Requirements section with test organization and naming conventions

### 16. Key Rotation and Cryptographic Best Practices

**Practice**: Regular encryption key rotation with dual-key decryption during transition periods.

**Where in Spec**:
- **specification.md**:
  - Task 6 (Security Implementation) → "Key rotation: re-encrypt data in background process; maintain dual-key decryption during transition"
  - Task 6 → "Implement key hierarchy: Master Key (HSM-stored) → Data Encryption Keys (rotated quarterly)"
- **agents.md**:
  - Security Best Practices → Secrets Management → "Rotate secrets regularly (encryption keys quarterly, JWT keys monthly)"
- **.claude/project-rules.md**:
  - Quick Reference → Environment Variables section

### 17. Structured Logging with Correlation IDs

**Practice**: Use structured JSON logging with correlation IDs for request tracing; include context without sensitive data.

**Where in Spec**:
- **specification.md**:
  - Task 5 (API Endpoints) → "Log all requests: method, path, user_id, ip_address, request_id, response_status, duration"
  - Task 7 (Audit Logging) → "Capture context: user_id, session_id, ip_address, user_agent, timestamp, operation_duration"
- **agents.md**:
  - Code Style & Conventions → Logging Standards (lines 103-113)
  - "Include correlation_id in all logs for request tracing"
- **.claude/project-rules.md**:
  - Critical Rules → "ALWAYS log with correlation ID"
  - Logging Sensitive Data example

### 18. API Versioning and Documentation

**Practice**: Version APIs (e.g., /api/v1/); provide OpenAPI documentation with examples and security schemes.

**Where in Spec**:
- **specification.md**:
  - Task 5 (API Endpoints) → "POST /api/v1/cards" (versioned endpoints)
  - Task 5 → "OpenAPI documentation: include examples, schema definitions, security requirements"
  - Task 9 (Documentation) → "OpenAPI 3.0 specification: complete schema definitions, security schemes"
- **agents.md**:
  - Tech Stack → Testing → "httpx for API testing"

### 19. Database Design Best Practices

**Practice**: Use UUIDs for primary keys, composite indexes for performance, check constraints for data integrity, soft deletes for compliance.

**Where in Spec**:
- **specification.md**:
  - Task 1 (Database Schema) → "Use UUID for all primary keys"
  - Task 1 → "Add composite indexes on (user_id, status) and (card_id, timestamp) for performance"
  - Task 1 → "Implement check constraints for positive amounts and valid date ranges"
  - Task 1 → "Implement soft delete pattern (deleted_at field) for GDPR compliance"
- **agents.md**:
  - Naming Conventions → Database Models example shows best practices
  - Performance Expectations → Optimization Guidelines → "Use database indexes on frequently queried columns"
- **.claude/project-rules.md**:
  - Database Models code example

### 20. Monitoring and Alerting

**Practice**: Implement comprehensive monitoring with Prometheus metrics; set up alerts for critical thresholds.

**Where in Spec**:
- **specification.md**:
  - Task 10 (Deployment Configuration) → "Monitoring: Prometheus metrics (request rate, error rate, latency percentiles)"
  - Task 10 → "Alerts: API error rate >5%, database connection pool exhausted, memory usage >80%"
  - Task 7 (Audit Logging) → "Alert compliance team on: unusual data access patterns, bulk exports"
- **agents.md**:
  - Performance Expectations → Response Time SLAs with specific latency targets

---

## Summary

This specification package demonstrates how to translate high-level business requirements ("build a virtual card system") into detailed, actionable guidance that an AI coding partner can execute. By embedding industry best practices at every level—from architectural decisions to code-level anti-patterns—the specification ensures that the resulting implementation will be secure, compliant, maintainable, and production-ready.

The three-document structure (specification.md, agents.md, project-rules.md) provides complementary views: **what** to build, **how** to build it, and **what not to do**. This approach maximizes the likelihood that an AI agent will produce high-quality code that meets stringent FinTech standards without requiring extensive human review or refactoring.
