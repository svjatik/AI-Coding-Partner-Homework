# Task 3: AI-Generated Comprehensive Test Suite - COSTAR Prompt

## 📋 Context

**Project:** Customer Support Ticket Management System
**Current State:** Tasks 1 & 2 completed
- ✅ REST API with 6 CRUD endpoints + auto-classify
- ✅ Multi-format file import (CSV, JSON, XML)
- ✅ Auto-classification system
- ✅ All business logic implemented

**Tech Stack:** Java 17 + Spring Boot 3.2.2 + JUnit 5 + MockMvc + AssertJ
**Testing Framework:** JUnit 5 + Spring Boot Test
**Coverage Tool:** JaCoCo (target: >85%)

**Existing Codebase:**
```
src/main/java/com/workshop/ticketsystem/
├── controller/TicketController.java
├── service/{TicketService, ClassificationService, ImportService}
├── parser/{CsvFileParser, JsonFileParser, XmlFileParser}
├── entity/Ticket.java
├── repository/TicketRepository.java
└── dto/{various DTOs}
```

---

## 🎯 Objective

**Primary Goal:**
Generate a comprehensive test suite achieving **>85% code coverage** that validates all functionality of the ticket management system.

**Success Criteria:**
- ✅ Total of 46+ tests across 6 test files
- ✅ Code coverage >85% (verified via JaCoCo report)
- ✅ All API endpoints tested (success and error cases)
- ✅ All file parsers tested (valid and malformed data)
- ✅ Classification logic tested (all categories and priorities)
- ✅ Entity validation tested
- ✅ Integration tests for complete workflows
- ✅ All tests pass consistently

**Required Test Files (46 tests total):**

| Test File | Tests | Coverage Focus |
|-----------|-------|----------------|
| `TicketControllerTest` | 11 | API endpoints |
| `TicketModelTest` | 9 | Entity validation |
| `CsvFileParserTest` | 6 | CSV parsing |
| `JsonFileParserTest` | 5 | JSON parsing |
| `XmlFileParserTest` | 5 | XML parsing |
| `ClassificationServiceTest` | 10 | Auto-classification |

---

## 💻 Style

**Testing Philosophy:**
- **Arrange-Act-Assert (AAA)** pattern for all tests
- **Given-When-Then** comments for complex scenarios
- **Test behavior, not implementation** - focus on what, not how
- **One assertion per test** (when practical)
- **Descriptive test names**: `testCreateTicket_WithValidData_Returns201()`

**Test Structure:**
```java
@Test
void testMethodName_Scenario_ExpectedOutcome() {
    // Arrange
    // ... setup test data

    // Act
    // ... execute the operation

    // Assert
    // ... verify the results
}
```

**Naming Conventions:**
- Test classes: `{ClassName}Test.java`
- Test methods: `test{MethodName}_{Scenario}_{ExpectedOutcome}`
- Test fixtures: `sample_tickets.csv`, `invalid_tickets.json`, etc.

**Assertion Style:**
- Use AssertJ for fluent assertions: `assertThat(result).isNotNull()`
- Use MockMvc for API testing: `.andExpect(status().isOk())`
- Use `@SpringBootTest` for integration tests
- Use `@WebMvcTest` for controller-only tests

---

## 🗣️ Tone

**Test Readability:**
- Self-documenting - test name explains what it tests
- Clear setup with meaningful variable names
- Minimal comments unless complex logic
- Fail messages that explain what went wrong

**Error Messages:**
- Descriptive: `"Expected status code 201 but got 400"`
- Include context: `"Ticket with ID %s not found in database"`
- Actionable: `"Email validation should reject invalid format"`

---

## 👥 Audience

**Primary:** QA Engineers and Developers maintaining the codebase
**Secondary:** CI/CD systems running automated tests
**Skill Level:** Mid-level developers familiar with JUnit and Spring Boot testing

**Assumptions:**
- Understand `@SpringBootTest`, `@AutoConfigureMockMvc`, `@Transactional`
- Know how to use MockMvc for API testing
- Familiar with test fixtures and test data setup

---

## 📤 Response

### Expected Deliverables:

#### 1. Controller Tests (11 tests)
**File:** `src/test/java/com/workshop/ticketsystem/controller/TicketControllerTest.java`

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateTicket_WithValidData_Returns201() throws Exception {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setCustomerId("C001");
        request.setCustomerEmail("test@example.com");
        // ... set other required fields

        mockMvc.perform(post("/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.customer_id").value("C001"));
    }

    @Test
    void testCreateTicket_WithInvalidEmail_Returns400() { /* ... */ }

    @Test
    void testGetAllTickets_ReturnsTicketList() { /* ... */ }

    @Test
    void testGetTicketById_WhenExists_Returns200() { /* ... */ }

    @Test
    void testGetTicketById_WhenNotExists_Returns404() { /* ... */ }

    @Test
    void testUpdateTicket_WhenExists_Returns200() { /* ... */ }

    @Test
    void testDeleteTicket_WhenExists_Returns204() { /* ... */ }

    @Test
    void testImportCsv_WithValidFile_ReturnsSuccess() { /* ... */ }

    @Test
    void testImportJson_WithValidFile_ReturnsSuccess() { /* ... */ }

    @Test
    void testImportXml_WithValidFile_ReturnsSuccess() { /* ... */ }

    @Test
    void testAutoClassify_ReturnsClassificationResult() { /* ... */ }
}
```

#### 2. Entity Validation Tests (9 tests)
**File:** `src/test/java/com/workshop/ticketsystem/entity/TicketModelTest.java`

```java
@SpringBootTest
@ActiveProfiles("test")
class TicketModelTest {

    @Autowired
    private Validator validator;

    @Test
    void testTicket_WithValidData_PassesValidation() {
        Ticket ticket = new Ticket();
        // ... set all valid fields

        Set<ConstraintViolation<Ticket>> violations = validator.validate(ticket);
        assertThat(violations).isEmpty();
    }

    @Test
    void testTicket_WithInvalidEmail_FailsValidation() {
        Ticket ticket = new Ticket();
        ticket.setCustomerEmail("invalid-email");
        // ... set other fields

        Set<ConstraintViolation<Ticket>> violations = validator.validate(ticket);
        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting("message")
            .contains("Invalid email format");
    }

    @Test
    void testTicket_WithShortSubject_FailsValidation() { /* ... */ }

    @Test
    void testTicket_WithLongSubject_FailsValidation() { /* ... */ }

    @Test
    void testTicket_WithShortDescription_FailsValidation() { /* ... */ }

    @Test
    void testTicket_WithLongDescription_FailsValidation() { /* ... */ }

    @Test
    void testTicket_WithNullRequiredField_FailsValidation() { /* ... */ }

    @Test
    void testTicket_DefaultStatus_IsNew() { /* ... */ }

    @Test
    void testTicket_WhenResolved_SetsResolvedTimestamp() { /* ... */ }
}
```

#### 3. CSV Parser Tests (6 tests)
**File:** `src/test/java/com/workshop/ticketsystem/parser/CsvFileParserTest.java`

```java
@SpringBootTest
@ActiveProfiles("test")
class CsvFileParserTest {

    @Autowired
    private CsvFileParser csvParser;

    @Test
    void testParse_WithValidCsv_ReturnsTicketList() throws Exception {
        String csvContent = "customer_id,customer_email,customer_name,subject,description\n" +
                           "C001,test@example.com,Test User,Subject,Description text here\n";

        MockMultipartFile file = new MockMultipartFile(
            "file", "test.csv", "text/csv", csvContent.getBytes());

        List<CreateTicketRequest> tickets = csvParser.parse(file);

        assertThat(tickets).hasSize(1);
        assertThat(tickets.get(0).getCustomerId()).isEqualTo("C001");
    }

    @Test
    void testParse_WithMissingRequiredField_ThrowsException() { /* ... */ }

    @Test
    void testParse_WithInvalidEnumValue_ThrowsException() { /* ... */ }

    @Test
    void testParse_WithEmptyCsv_ReturnsEmptyList() { /* ... */ }

    @Test
    void testParse_WithMalformedCsv_ThrowsException() { /* ... */ }

    @Test
    void testGetSupportedFormat_ReturnsCsv() {
        assertThat(csvParser.getSupportedFormat()).isEqualTo("csv");
    }
}
```

#### 4. JSON Parser Tests (5 tests)
**File:** `src/test/java/com/workshop/ticketsystem/parser/JsonFileParserTest.java`

```java
@SpringBootTest
@ActiveProfiles("test")
class JsonFileParserTest {

    @Autowired
    private JsonFileParser jsonParser;

    @Test
    void testParse_WithJsonArray_ReturnsTicketList() { /* ... */ }

    @Test
    void testParse_WithSingleObject_ReturnsSingleTicket() { /* ... */ }

    @Test
    void testParse_WithNestedTicketsArray_ReturnsTicketList() { /* ... */ }

    @Test
    void testParse_WithMalformedJson_ThrowsException() { /* ... */ }

    @Test
    void testGetSupportedFormat_ReturnsJson() { /* ... */ }
}
```

#### 5. XML Parser Tests (5 tests)
**File:** `src/test/java/com/workshop/ticketsystem/parser/XmlFileParserTest.java`

```java
@SpringBootTest
@ActiveProfiles("test")
class XmlFileParserTest {

    @Autowired
    private XmlFileParser xmlParser;

    @Test
    void testParse_WithMultipleTickets_ReturnsTicketList() { /* ... */ }

    @Test
    void testParse_WithSingleTicket_ReturnsSingleTicket() { /* ... */ }

    @Test
    void testParse_WithMalformedXml_ThrowsException() { /* ... */ }

    @Test
    void testParse_WithEmptyXml_ReturnsEmptyList() { /* ... */ }

    @Test
    void testGetSupportedFormat_ReturnsXml() { /* ... */ }
}
```

#### 6. Classification Tests (10 tests)
**File:** `src/test/java/com/workshop/ticketsystem/service/ClassificationServiceTest.java`

```java
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ClassificationServiceTest {

    @Autowired
    private ClassificationService classificationService;

    @Autowired
    private TicketRepository ticketRepository;

    @Test
    void testClassify_WithLoginKeywords_ReturnsAccountAccess() {
        Ticket ticket = createTicket("Cannot login", "Forgot my password");
        ClassificationResult result = classificationService.classify(ticket);

        assertThat(result.getCategory()).isEqualTo(TicketCategory.ACCOUNT_ACCESS);
        assertThat(result.getConfidenceScore()).isGreaterThan(0.5);
    }

    @Test
    void testClassify_WithErrorKeywords_ReturnsTechnicalIssue() { /* ... */ }

    @Test
    void testClassify_WithBillingKeywords_ReturnsBillingQuestion() { /* ... */ }

    @Test
    void testClassify_WithFeatureKeywords_ReturnsFeatureRequest() { /* ... */ }

    @Test
    void testClassify_WithBugKeywords_ReturnsBugReport() { /* ... */ }

    @Test
    void testClassify_WithNoKeywords_ReturnsOther() { /* ... */ }

    @Test
    void testClassify_WithUrgentKeywords_ReturnsUrgentPriority() { /* ... */ }

    @Test
    void testClassify_WithHighKeywords_ReturnsHighPriority() { /* ... */ }

    @Test
    void testClassify_WithLowKeywords_ReturnsLowPriority() { /* ... */ }

    @Test
    void testClassify_CreatesClassificationLog() { /* ... */ }

    private Ticket createTicket(String subject, String description) {
        Ticket ticket = new Ticket();
        ticket.setCustomerId("TEST001");
        ticket.setCustomerEmail("test@example.com");
        ticket.setCustomerName("Test User");
        ticket.setSubject(subject);
        ticket.setDescription(description);
        return ticketRepository.save(ticket);
    }
}
```

#### 7. Test Fixtures
**Directory:** `src/test/resources/fixtures/`

**Files to create:**
- `sample_tickets.csv` - 50 valid tickets
- `sample_tickets.json` - 20 valid tickets
- `sample_tickets.xml` - 30 valid tickets
- `invalid_tickets.csv` - Missing required fields
- `malformed.csv` - Broken CSV structure
- `empty.json` - Empty JSON array
- `invalid_email_tickets.csv` - Invalid email formats

#### 8. Test Configuration
**File:** `src/test/resources/application-test.yml`

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false
  servlet:
    multipart:
      max-file-size: 10MB

logging:
  level:
    com.workshop.ticketsystem: INFO
```

#### 9. JaCoCo Configuration
**File:** Update `pom.xml`

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        <execution>
            <id>jacoco-check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.85</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

---

## 🔍 Additional Requirements

1. **Test Independence:**
   - Each test should be independent
   - Use `@Transactional` to rollback database changes
   - Don't rely on test execution order
   - Clean up resources in `@AfterEach` if needed

2. **Test Data:**
   - Use meaningful test data (not "test123")
   - Create reusable test data builders
   - Use fixtures for complex scenarios

3. **Coverage Targets:**
   - Controllers: >90%
   - Services: >85%
   - Parsers: >80%
   - Entities: >70% (exclude Lombok generated code)

4. **Performance:**
   - Tests should run quickly (< 30 seconds total)
   - Use H2 in-memory database for speed
   - Minimize database hits

---

## ✅ Definition of Done

- [ ] All 46 tests created across 6 test files
- [ ] All tests pass consistently
- [ ] Code coverage >85% verified via JaCoCo
- [ ] Test fixtures created for all formats
- [ ] All API endpoints tested (success + error)
- [ ] All parsers tested (valid + malformed data)
- [ ] Classification logic fully tested
- [ ] Entity validation tested
- [ ] Tests are maintainable and well-organized
- [ ] Coverage report generated: `target/site/jacoco/index.html`

---

## 🚀 Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=TicketControllerTest

# Generate coverage report
mvn clean test jacoco:report

# Verify coverage threshold
mvn verify

# View coverage report
open target/site/jacoco/index.html
```

---

## 📊 Expected Coverage Report

```
Package                        Coverage
─────────────────────────────────────────
controller                     92%
service                        87%
parser                         85%
entity                         75%
dto                           50% (mostly getters/setters)
─────────────────────────────────────────
Overall                       85%+
```
