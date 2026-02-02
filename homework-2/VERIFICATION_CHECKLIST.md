# Verification Checklist

Use this checklist to verify the implementation is complete and working correctly.

## ✅ Prerequisites Installation

- [ ] Java 17 installed: `java -version`
- [ ] Maven 3.6+ installed: `mvn -version`
- [ ] PostgreSQL installed (optional for testing, H2 is used by default in tests)

## ✅ Build Verification

- [ ] Project compiles successfully:
  ```bash
  mvn clean compile
  ```

- [ ] All dependencies download correctly:
  ```bash
  mvn dependency:tree
  ```

## ✅ Test Verification

- [ ] All tests pass:
  ```bash
  mvn test
  ```

- [ ] Test count verification (should be 46 tests):
  ```bash
  mvn test | grep "Tests run"
  ```

- [ ] Coverage report generates successfully:
  ```bash
  mvn clean test jacoco:report
  ```

- [ ] Coverage is >85%:
  ```bash
  open target/site/jacoco/index.html
  # Or check target/site/jacoco/index.html in browser
  ```

Expected test counts per file:
- TicketControllerTest: 11 tests
- ClassificationServiceTest: 10 tests
- CsvFileParserTest: 6 tests
- JsonFileParserTest: 5 tests
- XmlFileParserTest: 5 tests
- TicketModelTest: 9 tests
- **Total: 46 tests**

## ✅ Application Startup

- [ ] Application starts without errors:
  ```bash
  mvn spring-boot:run
  ```

- [ ] Server starts on port 8080
- [ ] No startup exceptions in console
- [ ] Database connections established (H2 in-memory for dev)

## ✅ API Endpoint Verification

### 1. Create Ticket
```bash
curl -X POST http://localhost:8080/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "C001",
    "customerEmail": "test@example.com",
    "customerName": "Test User",
    "subject": "Test Ticket",
    "description": "This is a test ticket for verification purposes.",
    "autoClassify": true
  }'
```
- [ ] Returns 201 Created
- [ ] Response includes UUID id
- [ ] Response includes all ticket fields
- [ ] Status is "NEW"

### 2. Get All Tickets
```bash
curl http://localhost:8080/tickets
```
- [ ] Returns 200 OK
- [ ] Returns array of tickets
- [ ] Includes previously created ticket

### 3. Get Ticket by ID
```bash
# Replace {id} with actual UUID from create response
curl http://localhost:8080/tickets/{id}
```
- [ ] Returns 200 OK
- [ ] Returns correct ticket data

### 4. Update Ticket
```bash
# Replace {id} with actual UUID
curl -X PUT http://localhost:8080/tickets/{id} \
  -H "Content-Type: application/json" \
  -d '{
    "status": "IN_PROGRESS",
    "subject": "Updated Subject"
  }'
```
- [ ] Returns 200 OK
- [ ] Fields are updated correctly

### 5. Filter Tickets
```bash
curl "http://localhost:8080/tickets?category=BUG_REPORT&priority=HIGH"
```
- [ ] Returns 200 OK
- [ ] Returns filtered results

### 6. Auto-Classify Ticket
```bash
# Replace {id} with actual UUID
curl -X POST http://localhost:8080/tickets/{id}/auto-classify
```
- [ ] Returns 200 OK
- [ ] Response includes category, priority, confidenceScore, reasoning, keywordsFound

### 7. Import CSV
```bash
curl -X POST http://localhost:8080/tickets/import \
  -F "file=@data/sample_tickets.csv" \
  -F "format=csv" \
  -F "autoClassify=true"
```
- [ ] Returns 200 OK
- [ ] Response includes totalRecords, successfulImports, failedImports
- [ ] Most/all tickets imported successfully

### 8. Import JSON
```bash
curl -X POST http://localhost:8080/tickets/import \
  -F "file=@data/sample_tickets.json" \
  -F "format=json" \
  -F "autoClassify=false"
```
- [ ] Returns 200 OK
- [ ] JSON tickets imported successfully

### 9. Import XML
```bash
curl -X POST http://localhost:8080/tickets/import \
  -F "file=@data/sample_tickets.xml" \
  -F "format=xml" \
  -F "autoClassify=false"
```
- [ ] Returns 200 OK
- [ ] XML tickets imported successfully

### 10. Delete Ticket
```bash
# Replace {id} with actual UUID
curl -X DELETE http://localhost:8080/tickets/{id}
```
- [ ] Returns 204 No Content
- [ ] Ticket no longer exists (GET returns 404)

## ✅ Error Handling Verification

### Invalid Email
```bash
curl -X POST http://localhost:8080/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "C001",
    "customerEmail": "invalid-email",
    "customerName": "Test User",
    "subject": "Test",
    "description": "Short description for testing"
  }'
```
- [ ] Returns 400 Bad Request
- [ ] Error response includes validation error for email field

### Short Description
```bash
curl -X POST http://localhost:8080/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "C001",
    "customerEmail": "test@example.com",
    "customerName": "Test User",
    "subject": "Test",
    "description": "Short"
  }'
```
- [ ] Returns 400 Bad Request
- [ ] Error response includes validation error for description

### Ticket Not Found
```bash
curl http://localhost:8080/tickets/00000000-0000-0000-0000-000000000000
```
- [ ] Returns 404 Not Found
- [ ] Error message indicates ticket not found

### Invalid File Format
```bash
curl -X POST http://localhost:8080/tickets/import \
  -F "file=@data/sample_tickets.csv" \
  -F "format=invalid" \
  -F "autoClassify=false"
```
- [ ] Returns 400 Bad Request
- [ ] Error message indicates unsupported format

## ✅ Swagger UI Verification

- [ ] Access Swagger UI: http://localhost:8080/swagger-ui.html
- [ ] All 7 endpoints are documented
- [ ] Can execute requests from Swagger UI
- [ ] Request/response models are documented
- [ ] Error responses are documented

## ✅ Documentation Verification

- [ ] README.md exists and is complete
- [ ] docs/API_REFERENCE.md exists with all endpoint documentation
- [ ] docs/ARCHITECTURE.md exists with diagrams
- [ ] docs/TESTING_GUIDE.md exists with test instructions
- [ ] All Mermaid diagrams render correctly (view on GitHub or with Mermaid viewer)

## ✅ File Structure Verification

- [ ] All 5 enum files exist in enums package
- [ ] All 3 entity files exist in entity package
- [ ] All 2 repository files exist in repository package
- [ ] All 7 DTO files exist in dto package
- [ ] All 4 exception files exist in exception package
- [ ] All 5 parser files exist in parser package
- [ ] All 6 service files exist in service package
- [ ] All 2 controller files exist in controller package
- [ ] Main application class exists
- [ ] All 6 test files exist
- [ ] All 3 sample data files exist in data/ directory

## ✅ Classification Verification

### Test Account Access Classification
```bash
curl -X POST http://localhost:8080/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "C001",
    "customerEmail": "test@example.com",
    "customerName": "Test User",
    "subject": "Cannot login",
    "description": "I forgot my password and cannot reset it. The reset link is not working.",
    "autoClassify": true
  }'
```
- [ ] Category should be ACCOUNT_ACCESS
- [ ] Priority should be reasonable (URGENT or HIGH)

### Test Technical Issue Classification
```bash
curl -X POST http://localhost:8080/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "C002",
    "customerEmail": "test2@example.com",
    "customerName": "Test User 2",
    "subject": "Application crashes",
    "description": "The application crashes frequently with error messages and timeouts.",
    "autoClassify": true
  }'
```
- [ ] Category should be TECHNICAL_ISSUE
- [ ] Priority should be HIGH or URGENT

### Test Billing Question Classification
```bash
curl -X POST http://localhost:8080/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "C003",
    "customerEmail": "test3@example.com",
    "customerName": "Test User 3",
    "subject": "Invoice question",
    "description": "I have a question about my billing and invoice charges.",
    "autoClassify": true
  }'
```
- [ ] Category should be BILLING_QUESTION
- [ ] Priority should be MEDIUM or LOW

## ✅ Code Quality Checks

- [ ] No compilation errors
- [ ] No compilation warnings (optional)
- [ ] All imports are used
- [ ] No TODO comments in production code
- [ ] Proper exception handling in all layers
- [ ] All public methods have appropriate annotations

## ✅ Performance Verification (Optional)

### Bulk Create Test
```bash
# Import 50 tickets from CSV
time curl -X POST http://localhost:8080/tickets/import \
  -F "file=@data/sample_tickets.csv" \
  -F "format=csv" \
  -F "autoClassify=true"
```
- [ ] Completes in reasonable time (< 5 seconds)

### Multiple Queries Test
```bash
# Query multiple times
for i in {1..10}; do
  curl -s http://localhost:8080/tickets > /dev/null
  echo "Request $i completed"
done
```
- [ ] All requests complete successfully
- [ ] Response times are consistent

## 📊 Final Checklist

- [ ] All 46 tests pass
- [ ] Code coverage is >85%
- [ ] Application starts successfully
- [ ] All 7 API endpoints work correctly
- [ ] Error handling works properly
- [ ] Validation works correctly
- [ ] Auto-classification works
- [ ] CSV/JSON/XML import works
- [ ] Swagger UI is accessible
- [ ] All documentation files are present and complete
- [ ] Sample data files are present
- [ ] No console errors during normal operation

## 🎉 Success Criteria Met

If all items above are checked, the implementation is complete and meets all requirements from the plan:

✅ All 6 CRUD endpoints + auto-classify endpoint working
✅ CSV/JSON/XML import functional with error reporting
✅ Auto-classification with confidence scores
✅ 46 tests passing across 6 test files
✅ >85% code coverage (enforced by JaCoCo)
✅ 4 documentation files with 3+ Mermaid diagrams
✅ Sample data files (50 CSV, 20 JSON, 30 XML tickets)
✅ Proper HTTP status codes (201, 400, 404, etc.)
✅ Validation for all required fields

## 📝 Notes

- Some checks require the application to be running
- Replace `{id}` placeholders with actual UUIDs from responses
- Coverage percentage may vary slightly but should be >85%
- All curl commands assume localhost:8080
- Use `-v` flag with curl for verbose output if debugging
