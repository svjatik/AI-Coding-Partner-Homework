# Task 1: Multi-Format Ticket Import API - COSTAR Prompt

## 📋 Context

**Project:** Customer Support Ticket Management System
**Current State:** Starting fresh - need to build the foundation
**Tech Stack:** Java 17 + Spring Boot 3.2.2 + Maven
**Database:** PostgreSQL (production) + H2 (testing)

**Dependencies Already Available:**
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Validation
- Apache Commons CSV 1.10.0
- Jackson (JSON/XML support)
- Lombok (optional for reducing boilerplate)

**Project Structure:**
```
src/main/java/com/workshop/ticketsystem/
├── entity/           # JPA entities
├── repository/       # Spring Data repositories
├── dto/             # Data transfer objects
├── service/         # Business logic
├── controller/      # REST controllers
├── parser/          # File parsers (CSV, JSON, XML)
├── exception/       # Custom exceptions
└── validator/       # Custom validators
```

---

## 🎯 Objective

**Primary Goal:**
Build a complete REST API for customer support ticket management with multi-format file import capabilities (CSV, JSON, XML).

**Success Criteria:**
- ✅ All 6 CRUD endpoints functional
- ✅ Successful parsing of CSV, JSON, and XML files
- ✅ Comprehensive validation with meaningful error messages
- ✅ Proper HTTP status codes (201, 200, 400, 404, 500)
- ✅ Bulk import returns detailed summary (total, success, failed, errors)
- ✅ Handle malformed files gracefully
- ✅ All endpoints tested and working

**Required Endpoints:**

| Method | Endpoint | Description | Status Code |
|--------|----------|-------------|-------------|
| POST | `/tickets` | Create new ticket | 201 Created |
| POST | `/tickets/import` | Bulk import from file | 200 OK |
| GET | `/tickets` | List all tickets (with filters) | 200 OK |
| GET | `/tickets/:id` | Get specific ticket | 200 OK / 404 |
| PUT | `/tickets/:id` | Update ticket | 200 OK / 404 |
| DELETE | `/tickets/:id` | Delete ticket | 204 No Content / 404 |

---

## 💻 Style

**Code Style:**
- Follow Spring Boot conventions and best practices
- Use dependency injection (@Autowired or constructor injection)
- Separate concerns: Controller → Service → Repository
- Use DTOs for API requests/responses (not entities directly)
- Implement proper exception handling (@RestControllerAdvice)

**Architecture Patterns:**
- **Factory Pattern**: ParserFactory to select appropriate parser (CSV/JSON/XML)
- **Strategy Pattern**: FileParser interface with concrete implementations
- **Repository Pattern**: Spring Data JPA repositories
- **DTO Pattern**: Separate request/response models from entities

**Naming Conventions:**
- Entities: `Ticket`, `TicketMetadata`
- DTOs: `CreateTicketRequest`, `TicketResponse`, `ImportSummaryResponse`
- Services: `TicketService`, `ImportService`
- Controllers: `TicketController`
- Parsers: `CsvFileParser`, `JsonFileParser`, `XmlFileParser`

---

## 🗣️ Tone

**Code Comments:**
- Minimal inline comments - prefer self-documenting code
- Javadoc for public methods and complex logic
- Comment "why", not "what"

**Error Messages:**
- Clear and actionable for developers
- User-friendly for API consumers
- Include field names in validation errors
- Example: "Invalid email format for field 'customer_email'"

**Logging:**
- DEBUG: Method entry/exit, parsed data
- INFO: Successful operations, import summaries
- ERROR: Exceptions, validation failures, parsing errors

---

## 👥 Audience

**Primary:** Mid-level Java/Spring Boot developers
**Secondary:** Frontend developers consuming the API
**Skill Level:** Familiar with REST APIs, Spring Boot basics, file I/O

**Assumptions:**
- Developers understand REST principles
- Familiar with Spring annotations (@RestController, @Service, etc.)
- Know basic validation concepts
- Understand file upload/multipart handling

---

## 📤 Response

### Expected Deliverables:

#### 1. Entity Layer
**File:** `src/main/java/com/workshop/ticketsystem/entity/Ticket.java`
```java
@Entity
@Table(name = "tickets")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @Email(message = "Invalid email format")
    private String customerEmail;

    // ... other fields with validation annotations

    @Embedded
    private TicketMetadata metadata;

    // Getters, setters, lifecycle hooks
}
```

**File:** `src/main/java/com/workshop/ticketsystem/entity/TicketMetadata.java`
```java
@Embeddable
public class TicketMetadata {
    @Enumerated(EnumType.STRING)
    private TicketSource source;

    private String browser;

    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;
}
```

#### 2. DTOs
**Files needed:**
- `CreateTicketRequest.java` - with @Valid annotations
- `UpdateTicketRequest.java` - all fields optional
- `TicketResponse.java` - complete ticket data
- `ImportSummaryResponse.java` - bulk import results
- `ErrorResponse.java` - standardized error format

#### 3. File Parsers
**File:** `src/main/java/com/workshop/ticketsystem/parser/FileParser.java`
```java
public interface FileParser {
    List<CreateTicketRequest> parse(MultipartFile file) throws IOException;
    String getSupportedFormat(); // "csv", "json", "xml"
}
```

**Concrete Implementations:**
- `CsvFileParser.java` - Apache Commons CSV
- `JsonFileParser.java` - Jackson ObjectMapper
- `XmlFileParser.java` - Jackson XmlMapper

**File:** `src/main/java/com/workshop/ticketsystem/parser/ParserFactory.java`
```java
@Component
public class ParserFactory {
    public FileParser getParser(String format) {
        // Return appropriate parser based on format
    }
}
```

#### 4. Service Layer
**File:** `src/main/java/com/workshop/ticketsystem/service/TicketService.java`
- `createTicket(CreateTicketRequest)` → TicketResponse
- `getTicketById(UUID)` → TicketResponse
- `getAllTickets()` → List<TicketResponse>
- `updateTicket(UUID, UpdateTicketRequest)` → TicketResponse
- `deleteTicket(UUID)` → void

**File:** `src/main/java/com/workshop/ticketsystem/service/ImportService.java`
- `importTickets(MultipartFile, String format)` → ImportSummaryResponse

#### 5. Controller Layer
**File:** `src/main/java/com/workshop/ticketsystem/controller/TicketController.java`
```java
@RestController
@RequestMapping("/tickets")
public class TicketController {

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(
        @Valid @RequestBody CreateTicketRequest request) {
        // Return 201 Created with ticket data
    }

    @PostMapping("/import")
    public ResponseEntity<ImportSummaryResponse> importTickets(
        @RequestParam("file") MultipartFile file,
        @RequestParam("format") String format) {
        // Return 200 OK with import summary
    }

    // ... other endpoints
}
```

**File:** `src/main/java/com/workshop/ticketsystem/controller/GlobalExceptionHandler.java`
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TicketNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(TicketNotFoundException ex) {
        // Return 404 with error details
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        // Return 400 with field-level errors
    }

    // ... other exception handlers
}
```

#### 6. Enums
Create enums for:
- `TicketCategory` (ACCOUNT_ACCESS, TECHNICAL_ISSUE, BILLING_QUESTION, FEATURE_REQUEST, BUG_REPORT, OTHER)
- `TicketPriority` (URGENT, HIGH, MEDIUM, LOW)
- `TicketStatus` (NEW, IN_PROGRESS, WAITING_CUSTOMER, RESOLVED, CLOSED)
- `TicketSource` (WEB_FORM, EMAIL, API, CHAT, PHONE)
- `DeviceType` (DESKTOP, MOBILE, TABLET)

#### 7. Validation Requirements

**Field Validations:**
- `customer_id`: @NotBlank
- `customer_email`: @NotBlank + @Email
- `customer_name`: @NotBlank
- `subject`: @NotBlank + @Size(min=1, max=200)
- `description`: @NotBlank + @Size(min=10, max=2000)
- `category`: Valid enum value
- `priority`: Valid enum value
- `status`: Valid enum value

**File Upload Validation:**
- Max file size: 10MB
- Allowed formats: CSV, JSON, XML
- Check file extension
- Validate file content structure

#### 8. Sample Request/Response Examples

**Create Ticket Request:**
```json
{
  "customer_id": "C001",
  "customer_email": "john@example.com",
  "customer_name": "John Doe",
  "subject": "Cannot login to account",
  "description": "I forgot my password and cannot reset it",
  "category": "account_access",
  "priority": "high",
  "metadata": {
    "source": "web_form",
    "browser": "Chrome 120",
    "device_type": "desktop"
  }
}
```

**Import Summary Response:**
```json
{
  "total_records": 50,
  "successful_imports": 48,
  "failed_imports": 2,
  "errors": [
    "Row 15: Invalid email format for 'customer_email'",
    "Row 32: Description too short (minimum 10 characters)"
  ]
}
```

**Error Response:**
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 400,
  "error": "Validation Error",
  "message": "Request validation failed",
  "path": "/tickets",
  "field_errors": {
    "customer_email": "Invalid email format",
    "description": "Description must be between 10 and 2000 characters"
  }
}
```

#### 9. CSV Format Example
```csv
customer_id,customer_email,customer_name,subject,description,category,priority
C001,john@example.com,John Doe,Login Issue,Cannot access my account,account_access,high
C002,jane@example.com,Jane Smith,Billing Question,Need invoice for last month,billing_question,medium
```

#### 10. Configuration
**File:** `src/main/resources/application.yml`
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

---

## 🔍 Additional Requirements

1. **Error Handling:**
   - Catch `IOException` for file reading errors
   - Catch `ValidationException` for data validation
   - Return appropriate HTTP status codes
   - Include helpful error messages

2. **Logging:**
   - Log successful operations (INFO level)
   - Log errors with stack traces (ERROR level)
   - Log file parsing details (DEBUG level)

3. **Testing Considerations:**
   - All services should be testable (use interfaces)
   - Controllers should handle both success and error cases
   - Parsers should handle malformed data gracefully

4. **Performance:**
   - Stream large files instead of loading into memory
   - Use batch inserts for bulk imports when possible
   - Set reasonable connection pool sizes

---

## ✅ Definition of Done

- [ ] All 6 REST endpoints implemented and functional
- [ ] CSV, JSON, and XML parsers working correctly
- [ ] Validation on all required fields
- [ ] Proper HTTP status codes for all scenarios
- [ ] Bulk import returns detailed summary
- [ ] Malformed files handled gracefully
- [ ] Global exception handler configured
- [ ] All enums defined
- [ ] DTOs separated from entities
- [ ] Code compiles without errors
- [ ] Manual testing via Postman/cURL successful

---

## 🚀 Implementation Approach

**Phase 1:** Set up entity layer (Ticket, TicketMetadata) and enums
**Phase 2:** Create repositories (TicketRepository)
**Phase 3:** Build DTOs (requests, responses, errors)
**Phase 4:** Implement file parsers (CSV, JSON, XML) + factory
**Phase 5:** Create service layer (TicketService, ImportService)
**Phase 6:** Build REST controllers (TicketController)
**Phase 7:** Add global exception handling
**Phase 8:** Test all endpoints manually

Start with Phase 1 and progress sequentially.
