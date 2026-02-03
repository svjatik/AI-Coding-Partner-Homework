# Task 5: Integration & Performance Testing - COSTAR Prompt

## 📋 Context

**Project:** Customer Support Ticket Management System
**Current State:** Tasks 1-4 completed
- ✅ REST API with 7 endpoints
- ✅ Multi-format file import (CSV, JSON, XML)
- ✅ Auto-classification system
- ✅ Unit tests (46 tests, 81% coverage)
- ✅ Comprehensive documentation

**Tech Stack:** Java 17 + Spring Boot 3.2.2 + Maven
**Testing Framework:** JUnit 5 + Spring Boot Test + Testcontainers
**Performance Tools:** JMeter (optional) or custom load tests

**Existing Test Coverage:**
```
src/test/java/com/workshop/ticketsystem/
├── controller/TicketControllerTest.java (11 tests - unit/integration)
├── service/ClassificationServiceTest.java (10 tests - unit)
├── parser/CsvFileParserTest.java (6 tests - unit)
├── parser/JsonFileParserTest.java (5 tests - unit)
├── parser/XmlFileParserTest.java (5 tests - unit)
└── entity/TicketModelTest.java (9 tests - validation)
```

**Gap:** Missing end-to-end integration tests and performance benchmarks to validate system behavior under realistic conditions and load.

---

## 🎯 Objective

**Primary Goal:**
Create comprehensive integration and performance test suites to validate complete workflows, database interactions, and system performance under load.

**Success Criteria:**
- ✅ 10+ integration tests covering complete user workflows
- ✅ 5+ performance tests with measurable benchmarks
- ✅ Tests use Testcontainers for realistic database testing
- ✅ All integration tests pass consistently
- ✅ Performance tests meet defined benchmarks
- ✅ Tests are independent and can run in parallel
- ✅ Clear performance metrics and reporting

**Required Test Files:**

| Test File | Tests | Focus | Tool |
|-----------|-------|-------|------|
| `TicketIntegrationTest` | 10 | E2E workflows | @SpringBootTest + Testcontainers |
| `TicketPerformanceTest` | 5 | Load & throughput | Custom benchmarks |

---

## 💻 Style

**Testing Philosophy:**
- **Integration Tests**: Test complete vertical slices (API → Service → Repository → Database)
- **Performance Tests**: Measure real-world scenarios with realistic data volumes
- **Testcontainers**: Use PostgreSQL container for production-like environment
- **Data-Driven**: Use fixtures with varied data for comprehensive coverage
- **Metrics-Focused**: Capture timing, throughput, and resource usage

**Test Structure:**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("integration-test")
class TicketIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Test
    void testCompleteTicketLifecycle() {
        // Given: Create ticket
        // When: Update through all statuses
        // Then: Verify state transitions and database state
    }
}
```

**Performance Test Structure:**
```java
@Test
void testBulkTicketCreation() {
    long startTime = System.currentTimeMillis();

    // Create 1000 tickets
    for (int i = 0; i < 1000; i++) {
        // ... create ticket
    }

    long duration = System.currentTimeMillis() - startTime;
    assertThat(duration).isLessThan(5000); // < 5 seconds
}
```

**Naming Conventions:**
- Integration tests: `test{Workflow}_{CompleteScenario}`
- Performance tests: `test{Operation}_{VolumeOrConcurrency}`
- Helper methods: `given{Context}`, `when{Action}`, `then{Assertion}`

---

## 🗣️ Tone

**Test Documentation:**
- Clear test names that describe the complete workflow
- Comments for complex setup/teardown logic
- Performance benchmarks with rationale (e.g., "< 5s based on UX requirements")
- Failure messages that indicate which part of the workflow failed

**Logging:**
- INFO: Test execution start/end with timing
- DEBUG: Intermediate state during workflow
- ERROR: Failures with context (request, response, database state)

**Performance Reporting:**
- Tabular format: Operation | Volume | Expected | Actual | Pass/Fail
- Percentile reporting (p50, p95, p99) for latency
- Throughput metrics (requests/second)

---

## 👥 Audience

**Primary:** QA Engineers and DevOps teams validating production readiness
**Secondary:** Performance engineers optimizing system behavior
**Tertiary:** Development teams debugging integration issues

**Skill Level:** Mid to senior-level engineers familiar with:
- Testcontainers for Docker-based testing
- Spring Boot integration testing
- Performance testing concepts (throughput, latency, percentiles)
- Database transaction management

**Assumptions:**
- Understand `@SpringBootTest` with `webEnvironment`
- Know how to use `TestRestTemplate` or `WebTestClient`
- Familiar with database state verification
- Can interpret performance metrics

---

## 📤 Response

### Expected Deliverables:

#### 1. Integration Tests (10 tests)
**File:** `src/test/java/com/workshop/ticketsystem/integration/TicketIntegrationTest.java`

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("integration-test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TicketIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ClassificationLogRepository classificationLogRepository;

    @BeforeEach
    void setup() {
        ticketRepository.deleteAll();
        classificationLogRepository.deleteAll();
    }

    @Test
    void testCompleteTicketLifecycle_NewToResolved() {
        // Given: Create a new ticket
        CreateTicketRequest request = createValidTicketRequest(
            "Login issue", "Cannot access account"
        );

        ResponseEntity<TicketDto> createResponse = restTemplate.postForEntity(
            "/tickets", request, TicketDto.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UUID ticketId = createResponse.getBody().getId();
        assertThat(createResponse.getBody().getStatus()).isEqualTo(TicketStatus.NEW);

        // When: Update status to IN_PROGRESS
        UpdateTicketRequest updateToInProgress = new UpdateTicketRequest();
        updateToInProgress.setStatus(TicketStatus.IN_PROGRESS);
        updateToInProgress.setAssignedTo("agent@example.com");

        restTemplate.put("/tickets/" + ticketId, updateToInProgress);

        // Then: Verify status changed
        ResponseEntity<TicketDto> getResponse = restTemplate.getForEntity(
            "/tickets/" + ticketId, TicketDto.class
        );
        assertThat(getResponse.getBody().getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
        assertThat(getResponse.getBody().getAssignedTo()).isEqualTo("agent@example.com");

        // When: Update status to RESOLVED
        UpdateTicketRequest updateToResolved = new UpdateTicketRequest();
        updateToResolved.setStatus(TicketStatus.RESOLVED);

        restTemplate.put("/tickets/" + ticketId, updateToResolved);

        // Then: Verify resolved timestamp is set
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.RESOLVED);
        assertThat(ticket.getResolvedAt()).isNotNull();
    }

    @Test
    void testBulkImportWithAutoClassification_CsvFormat() {
        // Given: CSV file with 50 tickets
        ClassPathResource csvFile = new ClassPathResource("fixtures/sample_tickets.csv");
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", csvFile);
        body.add("format", "csv");
        body.add("autoClassify", true);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // When: Import tickets
        ResponseEntity<ImportSummaryResponse> response = restTemplate.postForEntity(
            "/tickets/import",
            new HttpEntity<>(body, headers),
            ImportSummaryResponse.class
        );

        // Then: Verify successful import
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ImportSummaryResponse summary = response.getBody();
        assertThat(summary.getTotalRecords()).isEqualTo(50);
        assertThat(summary.getSuccessfulImports()).isGreaterThanOrEqualTo(48); // Allow 2 failures

        // Verify tickets are in database with classifications
        List<Ticket> tickets = ticketRepository.findAll();
        assertThat(tickets).hasSizeGreaterThanOrEqualTo(48);

        // Verify classification logs were created
        List<ClassificationLog> logs = classificationLogRepository.findAll();
        assertThat(logs).hasSizeGreaterThanOrEqualTo(48);

        // Verify at least one ticket was classified correctly
        Ticket loginTicket = tickets.stream()
            .filter(t -> t.getSubject().toLowerCase().contains("login"))
            .findFirst()
            .orElseThrow();
        assertThat(loginTicket.getCategory()).isEqualTo(TicketCategory.ACCOUNT_ACCESS);
    }

    @Test
    void testConcurrentTicketCreation_NoRaceConditions() throws InterruptedException {
        // Given: 20 threads creating tickets simultaneously
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<UUID> createdTicketIds = Collections.synchronizedList(new ArrayList<>());

        // When: Create tickets concurrently
        for (int i = 0; i < threadCount; i++) {
            final int ticketNumber = i;
            executor.submit(() -> {
                try {
                    CreateTicketRequest request = createValidTicketRequest(
                        "Concurrent ticket " + ticketNumber,
                        "Testing concurrent creation"
                    );

                    ResponseEntity<TicketDto> response = restTemplate.postForEntity(
                        "/tickets", request, TicketDto.class
                    );

                    if (response.getStatusCode() == HttpStatus.CREATED) {
                        createdTicketIds.add(response.getBody().getId());
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Then: Verify all tickets created with unique IDs
        assertThat(createdTicketIds).hasSize(threadCount);
        assertThat(new HashSet<>(createdTicketIds)).hasSize(threadCount); // All unique

        // Verify database consistency
        List<Ticket> tickets = ticketRepository.findAll();
        assertThat(tickets).hasSizeGreaterThanOrEqualTo(threadCount);
    }

    @Test
    void testFilteredQuery_WithMultipleCriteria() {
        // Given: Tickets with various categories, priorities, and statuses
        createAndSaveTicket(TicketCategory.ACCOUNT_ACCESS, TicketPriority.URGENT, TicketStatus.NEW);
        createAndSaveTicket(TicketCategory.ACCOUNT_ACCESS, TicketPriority.HIGH, TicketStatus.NEW);
        createAndSaveTicket(TicketCategory.TECHNICAL_ISSUE, TicketPriority.URGENT, TicketStatus.IN_PROGRESS);
        createAndSaveTicket(TicketCategory.BILLING_QUESTION, TicketPriority.LOW, TicketStatus.RESOLVED);

        // When: Query with filters
        ResponseEntity<TicketDto[]> response = restTemplate.getForEntity(
            "/tickets?category=ACCOUNT_ACCESS&priority=URGENT&status=NEW",
            TicketDto[].class
        );

        // Then: Verify filtered results
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getCategory()).isEqualTo(TicketCategory.ACCOUNT_ACCESS);
        assertThat(response.getBody()[0].getPriority()).isEqualTo(TicketPriority.URGENT);
    }

    @Test
    void testTransactionRollback_OnValidationFailure() {
        // Given: Initial ticket count
        long initialCount = ticketRepository.count();

        // When: Try to create ticket with invalid data (should fail validation)
        CreateTicketRequest invalidRequest = new CreateTicketRequest();
        invalidRequest.setCustomerId("C001");
        invalidRequest.setCustomerEmail("invalid-email"); // Invalid format
        invalidRequest.setCustomerName("Test User");
        invalidRequest.setSubject("Test");
        invalidRequest.setDescription("Short"); // Too short (min 10 chars)

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
            "/tickets", invalidRequest, ErrorResponse.class
        );

        // Then: Verify no ticket was created
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ticketRepository.count()).isEqualTo(initialCount);
    }

    @Test
    void testAutoClassificationWithManualOverride() {
        // Given: Create ticket with auto-classification
        CreateTicketRequest request = createValidTicketRequest(
            "Urgent: Cannot login", "Forgot password and reset link not working"
        );
        request.setAutoClassify(true);

        ResponseEntity<TicketDto> createResponse = restTemplate.postForEntity(
            "/tickets", request, TicketDto.class
        );

        UUID ticketId = createResponse.getBody().getId();
        TicketCategory autoCategory = createResponse.getBody().getCategory();
        TicketPriority autoPriority = createResponse.getBody().getPriority();

        // Verify auto-classification occurred
        assertThat(autoCategory).isEqualTo(TicketCategory.ACCOUNT_ACCESS);
        assertThat(autoPriority).isEqualTo(TicketPriority.URGENT);

        // When: Manually override classification
        UpdateTicketRequest override = new UpdateTicketRequest();
        override.setCategory(TicketCategory.TECHNICAL_ISSUE);
        override.setPriority(TicketPriority.HIGH);

        restTemplate.put("/tickets/" + ticketId, override);

        // Then: Verify manual override succeeded
        ResponseEntity<TicketDto> getResponse = restTemplate.getForEntity(
            "/tickets/" + ticketId, TicketDto.class
        );
        assertThat(getResponse.getBody().getCategory()).isEqualTo(TicketCategory.TECHNICAL_ISSUE);
        assertThat(getResponse.getBody().getPriority()).isEqualTo(TicketPriority.HIGH);

        // Verify classification log still exists
        List<ClassificationLog> logs = classificationLogRepository.findByTicketId(ticketId);
        assertThat(logs).isNotEmpty();
    }

    @Test
    void testPaginationAndSorting() {
        // Given: 25 tickets
        for (int i = 0; i < 25; i++) {
            createAndSaveTicket(
                TicketCategory.TECHNICAL_ISSUE,
                TicketPriority.MEDIUM,
                TicketStatus.NEW
            );
        }

        // When: Query first page (10 items)
        ResponseEntity<TicketDto[]> page1 = restTemplate.getForEntity(
            "/tickets?page=0&size=10&sort=createdAt,desc",
            TicketDto[].class
        );

        // Then: Verify pagination
        assertThat(page1.getBody()).hasSize(10);

        // When: Query second page
        ResponseEntity<TicketDto[]> page2 = restTemplate.getForEntity(
            "/tickets?page=1&size=10&sort=createdAt,desc",
            TicketDto[].class
        );

        // Then: Verify different results
        assertThat(page2.getBody()).hasSize(10);
        assertThat(page1.getBody()[0].getId()).isNotEqualTo(page2.getBody()[0].getId());
    }

    @Test
    void testMultiFormatImport_JsonAndXml() {
        // Test JSON import
        ClassPathResource jsonFile = new ClassPathResource("fixtures/sample_tickets.json");
        ImportSummaryResponse jsonSummary = importFile(jsonFile, "json");
        assertThat(jsonSummary.getSuccessfulImports()).isEqualTo(20);

        // Test XML import
        ClassPathResource xmlFile = new ClassPathResource("fixtures/sample_tickets.xml");
        ImportSummaryResponse xmlSummary = importFile(xmlFile, "xml");
        assertThat(xmlSummary.getSuccessfulImports()).isEqualTo(30);

        // Verify total tickets in database
        assertThat(ticketRepository.count()).isEqualTo(50);
    }

    @Test
    void testDeleteTicket_CascadesCorrectly() {
        // Given: Ticket with classification log
        CreateTicketRequest request = createValidTicketRequest("Test", "Test description");
        request.setAutoClassify(true);

        ResponseEntity<TicketDto> createResponse = restTemplate.postForEntity(
            "/tickets", request, TicketDto.class
        );
        UUID ticketId = createResponse.getBody().getId();

        // Verify classification log exists
        assertThat(classificationLogRepository.findByTicketId(ticketId)).isNotEmpty();

        // When: Delete ticket
        restTemplate.delete("/tickets/" + ticketId);

        // Then: Verify ticket deleted
        assertThat(ticketRepository.findById(ticketId)).isEmpty();

        // Classification logs should remain for audit (or cascade - depends on design)
        // Adjust based on your implementation
    }

    @Test
    void testReClassification_UpdatesLog() {
        // Given: Existing ticket
        Ticket ticket = createAndSaveTicket(
            TicketCategory.OTHER,
            TicketPriority.MEDIUM,
            TicketStatus.NEW
        );

        // When: Auto-classify existing ticket
        ResponseEntity<ClassificationResult> response = restTemplate.postForEntity(
            "/tickets/" + ticket.getId() + "/auto-classify",
            null,
            ClassificationResult.class
        );

        // Then: Verify classification result
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getConfidenceScore()).isGreaterThan(0.0);

        // Verify new classification log entry
        List<ClassificationLog> logs = classificationLogRepository
            .findByTicketIdOrderByClassifiedAtDesc(ticket.getId());
        assertThat(logs).hasSizeGreaterThanOrEqualTo(1);
    }

    // Helper methods
    private CreateTicketRequest createValidTicketRequest(String subject, String description) {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setCustomerId("C" + System.currentTimeMillis());
        request.setCustomerEmail("test@example.com");
        request.setCustomerName("Test User");
        request.setSubject(subject);
        request.setDescription(description);
        request.setCategory(TicketCategory.OTHER);
        request.setPriority(TicketPriority.MEDIUM);
        return request;
    }

    private Ticket createAndSaveTicket(TicketCategory category, TicketPriority priority, TicketStatus status) {
        Ticket ticket = new Ticket();
        ticket.setCustomerId("C" + System.currentTimeMillis());
        ticket.setCustomerEmail("test@example.com");
        ticket.setCustomerName("Test User");
        ticket.setSubject("Test ticket");
        ticket.setDescription("Test description for integration test");
        ticket.setCategory(category);
        ticket.setPriority(priority);
        ticket.setStatus(status);
        return ticketRepository.save(ticket);
    }

    private ImportSummaryResponse importFile(Resource file, String format) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file);
        body.add("format", format);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        ResponseEntity<ImportSummaryResponse> response = restTemplate.postForEntity(
            "/tickets/import",
            new HttpEntity<>(body, headers),
            ImportSummaryResponse.class
        );

        return response.getBody();
    }
}
```

#### 2. Performance Tests (5 tests)
**File:** `src/test/java/com/workshop/ticketsystem/performance/TicketPerformanceTest.java`

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("performance-test")
@Tag("performance")
class TicketPerformanceTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TicketRepository ticketRepository;

    @BeforeEach
    void setup() {
        ticketRepository.deleteAll();
    }

    @Test
    void testBulkTicketCreation_1000Tickets_LessThan5Seconds() {
        // Given: 1000 ticket requests
        int ticketCount = 1000;
        List<CreateTicketRequest> requests = new ArrayList<>();
        for (int i = 0; i < ticketCount; i++) {
            requests.add(createTicketRequest("Ticket " + i));
        }

        // When: Create all tickets
        long startTime = System.currentTimeMillis();

        for (CreateTicketRequest request : requests) {
            restTemplate.postForEntity("/tickets", request, TicketDto.class);
        }

        long duration = System.currentTimeMillis() - startTime;

        // Then: Verify performance
        assertThat(duration).isLessThan(5000); // < 5 seconds
        assertThat(ticketRepository.count()).isEqualTo(ticketCount);

        System.out.printf("Created %d tickets in %d ms (%.2f tickets/sec)%n",
            ticketCount, duration, (ticketCount * 1000.0) / duration);
    }

    @Test
    void testBulkImport_100Tickets_LessThan2Seconds() throws Exception {
        // Given: CSV file with 100 tickets
        File csvFile = createCsvFile(100);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(csvFile));
        body.add("format", "csv");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // When: Import tickets
        long startTime = System.currentTimeMillis();

        ResponseEntity<ImportSummaryResponse> response = restTemplate.postForEntity(
            "/tickets/import",
            new HttpEntity<>(body, headers),
            ImportSummaryResponse.class
        );

        long duration = System.currentTimeMillis() - startTime;

        // Then: Verify performance
        assertThat(duration).isLessThan(2000); // < 2 seconds
        assertThat(response.getBody().getSuccessfulImports()).isEqualTo(100);

        System.out.printf("Imported %d tickets in %d ms%n", 100, duration);
    }

    @Test
    void testQueryLargeDataset_10000Tickets_LessThan1Second() {
        // Given: 10000 tickets in database
        List<Ticket> tickets = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            tickets.add(createTicket("Ticket " + i));
        }
        ticketRepository.saveAll(tickets);

        // When: Query all tickets
        long startTime = System.currentTimeMillis();

        ResponseEntity<TicketDto[]> response = restTemplate.getForEntity(
            "/tickets?page=0&size=100",
            TicketDto[].class
        );

        long duration = System.currentTimeMillis() - startTime;

        // Then: Verify performance
        assertThat(duration).isLessThan(1000); // < 1 second
        assertThat(response.getBody()).hasSize(100);

        System.out.printf("Queried 100 tickets from 10,000 in %d ms%n", duration);
    }

    @Test
    void testConcurrentReads_50Threads_LessThan3Seconds() throws InterruptedException {
        // Given: 100 tickets in database
        List<Ticket> tickets = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            tickets.add(createTicket("Concurrent test " + i));
        }
        ticketRepository.saveAll(tickets);
        UUID[] ticketIds = tickets.stream().map(Ticket::getId).toArray(UUID[]::new);

        // When: 50 threads reading concurrently
        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            final int index = i % ticketIds.length;
            executor.submit(() -> {
                try {
                    ResponseEntity<TicketDto> response = restTemplate.getForEntity(
                        "/tickets/" + ticketIds[index],
                        TicketDto.class
                    );
                    if (response.getStatusCode() == HttpStatus.OK) {
                        successCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        long duration = System.currentTimeMillis() - startTime;

        // Then: Verify performance
        assertThat(duration).isLessThan(3000); // < 3 seconds
        assertThat(successCount.get()).isEqualTo(threadCount);

        System.out.printf("Completed %d concurrent reads in %d ms (%.2f req/sec)%n",
            threadCount, duration, (threadCount * 1000.0) / duration);
    }

    @Test
    void testBulkClassification_100Tickets_LessThan3Seconds() {
        // Given: 100 tickets without classification
        List<Ticket> tickets = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Ticket ticket = createTicket("Test ticket " + i);
            ticket.setSubject("Login issue " + i);
            ticket.setDescription("Cannot access my account, password reset not working");
            tickets.add(ticket);
        }
        ticketRepository.saveAll(tickets);

        // When: Auto-classify all tickets
        long startTime = System.currentTimeMillis();

        for (Ticket ticket : tickets) {
            restTemplate.postForEntity(
                "/tickets/" + ticket.getId() + "/auto-classify",
                null,
                ClassificationResult.class
            );
        }

        long duration = System.currentTimeMillis() - startTime;

        // Then: Verify performance
        assertThat(duration).isLessThan(3000); // < 3 seconds

        System.out.printf("Classified %d tickets in %d ms (%.2f tickets/sec)%n",
            100, duration, (100 * 1000.0) / duration);
    }

    // Helper methods
    private CreateTicketRequest createTicketRequest(String subject) {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setCustomerId("PERF" + System.nanoTime());
        request.setCustomerEmail("perf@example.com");
        request.setCustomerName("Performance Test");
        request.setSubject(subject);
        request.setDescription("Performance test description that meets minimum length requirement");
        request.setCategory(TicketCategory.TECHNICAL_ISSUE);
        request.setPriority(TicketPriority.MEDIUM);
        return request;
    }

    private Ticket createTicket(String subject) {
        Ticket ticket = new Ticket();
        ticket.setCustomerId("PERF" + System.nanoTime());
        ticket.setCustomerEmail("perf@example.com");
        ticket.setCustomerName("Performance Test");
        ticket.setSubject(subject);
        ticket.setDescription("Performance test description that meets minimum length requirement");
        ticket.setCategory(TicketCategory.TECHNICAL_ISSUE);
        ticket.setPriority(TicketPriority.MEDIUM);
        ticket.setStatus(TicketStatus.NEW);
        return ticket;
    }

    private File createCsvFile(int recordCount) throws IOException {
        File tempFile = File.createTempFile("perf_test_", ".csv");
        tempFile.deleteOnExit();

        try (PrintWriter writer = new PrintWriter(tempFile)) {
            writer.println("customer_id,customer_email,customer_name,subject,description,category,priority");
            for (int i = 0; i < recordCount; i++) {
                writer.printf("PERF%d,perf%d@example.com,Perf User %d,Performance ticket %d," +
                    "Description for performance testing that is long enough,%s,%s%n",
                    i, i, i, i, "technical_issue", "medium");
            }
        }

        return tempFile;
    }
}
```

#### 3. Test Configuration
**File:** `src/test/resources/application-integration-test.yml`

```yaml
spring:
  datasource:
    url: ${TESTCONTAINERS_POSTGRES_URL:jdbc:postgresql://localhost:5432/testdb}
    username: ${TESTCONTAINERS_POSTGRES_USERNAME:test}
    password: ${TESTCONTAINERS_POSTGRES_PASSWORD:test}
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
    org.testcontainers: INFO
```

**File:** `src/test/resources/application-performance-test.yml`

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:perfdb;DB_CLOSE_DELAY=-1
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
    com.workshop.ticketsystem: WARN
```

#### 4. Maven Configuration Updates
**File:** Update `pom.xml` with Testcontainers

```xml
<dependencies>
    <!-- Existing dependencies... -->

    <!-- Testcontainers -->
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>testcontainers</artifactId>
        <version>1.19.3</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>postgresql</artifactId>
        <version>1.19.3</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>1.19.3</version>
        <scope>test</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <!-- Existing plugins... -->

        <!-- Surefire plugin for test execution -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.0.0-M9</version>
            <configuration>
                <excludedGroups>performance</excludedGroups>
                <!-- Run performance tests separately -->
            </configuration>
        </plugin>

        <!-- Failsafe plugin for integration tests -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-failsafe-plugin</artifactId>
            <version>3.0.0-M9</version>
            <executions>
                <execution>
                    <goals>
                        <goal>integration-test</goal>
                        <goal>verify</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

---

## 🔍 Additional Requirements

1. **Testcontainers Setup:**
   - Use PostgreSQL 15 container
   - Configure dynamic properties with @DynamicPropertySource
   - Ensure containers are shared across tests when possible
   - Clean up resources properly

2. **Test Isolation:**
   - Each test should be independent
   - Use @BeforeEach to clear database state
   - Don't rely on test execution order
   - Use @Transactional where appropriate

3. **Performance Benchmarks:**
   - All benchmarks based on realistic user expectations
   - Report metrics to console for visibility
   - Use percentiles (p50, p95, p99) for latency
   - Measure throughput (operations/second)

4. **Resource Management:**
   - Close ExecutorService properly
   - Clean up temporary files
   - Release database connections
   - Avoid memory leaks in long-running tests

5. **Failure Debugging:**
   - Log performance metrics on failure
   - Include database state in failure messages
   - Capture timing details for slow tests
   - Provide actionable failure messages

---

## ✅ Definition of Done

- [ ] TicketIntegrationTest.java created with 10 tests
- [ ] TicketPerformanceTest.java created with 5 tests
- [ ] All integration tests pass consistently
- [ ] All performance tests meet benchmarks
- [ ] Testcontainers properly configured
- [ ] Test configurations created (integration-test.yml, performance-test.yml)
- [ ] pom.xml updated with Testcontainers dependencies
- [ ] Performance metrics logged and visible
- [ ] Tests can run in CI/CD environment
- [ ] Documentation updated with test execution commands

---

## 🚀 Running Tests

```bash
# Run all unit tests (excludes performance tests)
mvn test

# Run integration tests
mvn verify

# Run specific integration test
mvn test -Dtest=TicketIntegrationTest

# Run performance tests (explicitly include)
mvn test -Dgroups=performance

# Run specific performance test
mvn test -Dtest=TicketPerformanceTest

# Run all tests including performance
mvn verify -Dgroups=performance

# Generate coverage report (all tests)
mvn clean verify jacoco:report
```

---

## 📊 Expected Performance Results

```
Operation                          Target      Pass Criteria
──────────────────────────────────────────────────────────────
Create 1000 tickets                < 5s        ✓ 4.2s
Import 100 tickets (CSV)           < 2s        ✓ 1.8s
Query 10000 tickets (paginated)    < 1s        ✓ 0.7s
50 concurrent reads                < 3s        ✓ 2.5s
Classify 100 tickets               < 3s        ✓ 2.1s
──────────────────────────────────────────────────────────────
Overall Performance                            ✓ All passed
```

---

## 🧪 Integration Test Coverage

| Workflow | Test Method | Focus |
|----------|-------------|-------|
| Complete lifecycle | testCompleteTicketLifecycle | NEW → IN_PROGRESS → RESOLVED |
| Bulk import | testBulkImportWithAutoClassification | CSV with 50 tickets |
| Concurrent creation | testConcurrentTicketCreation | 20 threads, no race conditions |
| Filtered queries | testFilteredQuery_WithMultipleCriteria | Complex filter combinations |
| Transaction rollback | testTransactionRollback_OnValidationFailure | Atomicity verification |
| Auto-classification | testAutoClassificationWithManualOverride | Classification + override |
| Pagination | testPaginationAndSorting | Page traversal |
| Multi-format import | testMultiFormatImport_JsonAndXml | JSON + XML |
| Cascade delete | testDeleteTicket_CascadesCorrectly | Orphan handling |
| Re-classification | testReClassification_UpdatesLog | Log updates |

---

## 🎯 Success Metrics

**Integration Tests:**
- ✅ 10 tests covering complete workflows
- ✅ 100% pass rate
- ✅ Average execution time < 2 minutes
- ✅ No flaky tests

**Performance Tests:**
- ✅ 5 benchmarks all passing
- ✅ Throughput meets requirements
- ✅ Latency within acceptable bounds
- ✅ Scales to expected load

**Coverage:**
- Integration tests cover end-to-end scenarios
- Performance tests validate non-functional requirements
- Combined with unit tests, achieve >85% code coverage
