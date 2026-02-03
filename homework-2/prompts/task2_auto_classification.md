# Task 2: Auto-Classification System - COSTAR Prompt

## 📋 Context

**Project:** Customer Support Ticket Management System
**Current State:** Task 1 completed - API and file import working
**Existing Components:**
- ✅ Ticket entity with category and priority fields
- ✅ CRUD API endpoints
- ✅ File import functionality
- ✅ Validation layer

**Tech Stack:** Java 17 + Spring Boot 3.2.2 + Maven
**Database:** PostgreSQL (production) + H2 (testing)

**Codebase Structure:**
```
src/main/java/com/workshop/ticketsystem/
├── entity/Ticket.java          # Has category & priority fields
├── service/TicketService.java   # Handles CRUD operations
├── controller/TicketController.java
└── [NEW] service/ClassificationService.java  # To be created
```

**Available Data:**
- Ticket subject (1-200 chars)
- Ticket description (10-2000 chars)
- Customer metadata (optional)

---

## 🎯 Objective

**Primary Goal:**
Implement an intelligent ticket classification system that automatically:
1. Categorizes tickets into 6 categories
2. Assigns priority levels
3. Provides confidence scores and reasoning
4. Logs all classification decisions

**Success Criteria:**
- ✅ New endpoint `/tickets/:id/auto-classify` working
- ✅ Auto-classification on ticket creation (optional flag)
- ✅ Classification based on keyword matching algorithm
- ✅ Returns category, priority, confidence score (0-1), reasoning, and keywords found
- ✅ All decisions logged to database
- ✅ Manual override allowed (ticket can be manually updated)
- ✅ >80% accuracy on test data

**Categories to Classify:**

| Category | Keywords/Patterns | Examples |
|----------|------------------|----------|
| `account_access` | login, password, 2FA, authentication, access denied, locked out | "Cannot login", "Forgot password", "2FA not working" |
| `technical_issue` | error, bug, crash, broken, not working, failure, exception | "App crashes", "Error 500", "Page not loading" |
| `billing_question` | billing, invoice, payment, charge, refund, subscription, pricing | "Need invoice", "Charged twice", "Cancel subscription" |
| `feature_request` | feature, request, enhancement, improvement, add, new, would like | "Add dark mode", "Export to PDF feature", "Need calendar" |
| `bug_report` | bug, issue, defect, incorrect, wrong, broken functionality | "Wrong calculation", "Data not saving", "Button doesn't work" |
| `other` | None of the above | Generic questions, unclear issues |

**Priority Rules:**

| Priority | Keywords | Examples |
|----------|----------|----------|
| `urgent` | can't access, critical, production down, security, emergency, immediately, asap | "Production down", "Can't login", "Security breach" |
| `high` | important, blocking, soon, high priority, significant, major | "Blocking workflow", "Important client" |
| `medium` | (default - no specific keywords) | Regular issues without urgency indicators |
| `low` | minor, cosmetic, suggestion, eventually, nice to have, whenever | "Minor typo", "Nice to have feature" |

---

## 💻 Style

**Algorithm Approach:**
- **Simple Keyword Matching**: Use predefined keyword lists (no ML required)
- **Confidence Scoring**: Based on number of matching keywords
- **Multiple Matches**: Take the category with most keyword hits
- **Case Insensitive**: Convert all text to lowercase for matching
- **Whole Word Matching**: Avoid partial matches (e.g., "cat" shouldn't match "category")

**Code Style:**
- Service layer handles all classification logic
- Repository stores classification logs
- Controller exposes REST endpoint
- Keep classification algorithm simple and maintainable
- Use maps/dictionaries for keyword storage

**Design Patterns:**
- **Strategy Pattern**: Different classification strategies (keyword-based, ML-based in future)
- **Repository Pattern**: ClassificationLogRepository for persistence
- **Service Layer Pattern**: ClassificationService encapsulates logic

**Data Structures:**
```java
Map<TicketCategory, List<String>> CATEGORY_KEYWORDS = Map.of(
    TicketCategory.ACCOUNT_ACCESS, List.of("login", "password", "2fa", ...),
    TicketCategory.TECHNICAL_ISSUE, List.of("error", "bug", "crash", ...),
    // ... other categories
);

Map<TicketPriority, List<String>> PRIORITY_KEYWORDS = Map.of(
    TicketPriority.URGENT, List.of("critical", "production down", ...),
    // ... other priorities
);
```

---

## 🗣️ Tone

**Classification Reasoning:**
- Clear and transparent: "Classified as 'account_access' based on keywords: login, password"
- Confidence explanation: "High confidence (0.85) - found 3 relevant keywords"
- Actionable: "Consider reviewing if keywords don't match intent"

**Logging:**
- INFO: Every classification decision with category/priority
- DEBUG: Detailed keyword matches and scores
- WARN: Low confidence classifications (< 0.5)

**API Responses:**
- Professional and informative
- Include both machine-readable (scores) and human-readable (reasoning) data

---

## 👥 Audience

**Primary:** Backend developers implementing classification logic
**Secondary:** Data analysts evaluating classification accuracy
**Tertiary:** Support team managers reviewing classification decisions

**Skill Level:** Mid-level Java developers familiar with algorithms and data structures

**Assumptions:**
- Understand basic text processing (lowercase, split, trim)
- Familiar with Map/List data structures
- Know how to calculate simple metrics (confidence scores)

---

## 📤 Response

### Expected Deliverables:

#### 1. Classification Service Interface
**File:** `src/main/java/com/workshop/ticketsystem/service/ClassificationService.java`
```java
public interface ClassificationService {
    /**
     * Classifies a ticket by analyzing subject and description
     * @param ticket The ticket to classify
     * @return Classification result with category, priority, confidence, reasoning
     */
    ClassificationResult classify(Ticket ticket);

    /**
     * Classifies a ticket by ID
     * @param ticketId UUID of the ticket
     * @return Classification result
     */
    ClassificationResult classifyById(UUID ticketId);
}
```

#### 2. Classification Service Implementation
**File:** `src/main/java/com/workshop/ticketsystem/service/ClassificationServiceImpl.java`

**Key Methods:**
```java
@Service
public class ClassificationServiceImpl implements ClassificationService {

    // Keyword mappings
    private static final Map<TicketCategory, List<String>> CATEGORY_KEYWORDS = ...;
    private static final Map<TicketPriority, List<String>> PRIORITY_KEYWORDS = ...;

    @Override
    public ClassificationResult classify(Ticket ticket) {
        String content = (ticket.getSubject() + " " + ticket.getDescription()).toLowerCase();

        // Find matching category
        TicketCategory category = classifyCategory(content);
        double categoryConfidence = calculateCategoryConfidence(content, category);

        // Find matching priority
        TicketPriority priority = classifyPriority(content);
        double priorityConfidence = calculatePriorityConfidence(content, priority);

        // Generate reasoning
        String reasoning = generateReasoning(category, priority, content);

        // Find matched keywords
        List<String> keywords = findMatchedKeywords(content);

        // Log decision
        logClassification(ticket.getId(), category, priority, ...);

        return new ClassificationResult(category, priority,
            (categoryConfidence + priorityConfidence) / 2, reasoning, keywords);
    }

    private TicketCategory classifyCategory(String content) {
        // Count keyword matches for each category
        // Return category with most matches
        // Default to OTHER if no matches
    }

    private TicketPriority classifyPriority(String content) {
        // Check urgent keywords first (highest priority)
        // Then high, then low
        // Default to MEDIUM if no matches
    }

    private double calculateConfidence(String content, List<String> keywords) {
        // Simple formula: (matches / total_keywords)
        // Or: matches > 0 ? 0.6 + (matches * 0.1) : 0.3
    }

    private List<String> findMatchedKeywords(String content) {
        // Return list of all matched keywords
    }
}
```

#### 3. Classification Result DTO
**File:** `src/main/java/com/workshop/ticketsystem/dto/ClassificationResult.java`
```java
public class ClassificationResult {
    private TicketCategory category;
    private TicketPriority priority;
    private Double confidenceScore;  // 0.0 to 1.0
    private String reasoning;
    private List<String> keywordsFound;

    // Constructors, getters, setters
}
```

#### 4. Classification Log Entity
**File:** `src/main/java/com/workshop/ticketsystem/entity/ClassificationLog.java`
```java
@Entity
@Table(name = "classification_logs")
public class ClassificationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID ticketId;

    @Enumerated(EnumType.STRING)
    private TicketCategory suggestedCategory;

    @Enumerated(EnumType.STRING)
    private TicketPriority suggestedPriority;

    private Double confidenceScore;

    private String reasoning;

    @ElementCollection
    private List<String> keywordsFound;

    private LocalDateTime classifiedAt;

    // Getters, setters
}
```

#### 5. Classification Repository
**File:** `src/main/java/com/workshop/ticketsystem/repository/ClassificationLogRepository.java`
```java
public interface ClassificationLogRepository extends JpaRepository<ClassificationLog, UUID> {
    List<ClassificationLog> findByTicketId(UUID ticketId);
    List<ClassificationLog> findByTicketIdOrderByClassifiedAtDesc(UUID ticketId);
}
```

#### 6. Controller Endpoint
**File:** Update `src/main/java/com/workshop/ticketsystem/controller/TicketController.java`
```java
@RestController
@RequestMapping("/tickets")
public class TicketController {

    @Autowired
    private ClassificationService classificationService;

    @PostMapping("/{id}/auto-classify")
    public ResponseEntity<ClassificationResult> autoClassify(@PathVariable UUID id) {
        ClassificationResult result = classificationService.classifyById(id);
        return ResponseEntity.ok(result);
    }
}
```

#### 7. Update CreateTicketRequest DTO
**File:** Update `src/main/java/com/workshop/ticketsystem/dto/CreateTicketRequest.java`
```java
public class CreateTicketRequest {
    // ... existing fields

    @JsonProperty("auto_classify")
    private Boolean autoClassify = false;  // Optional flag

    // Getters, setters
}
```

#### 8. Update TicketService
**File:** Update `src/main/java/com/workshop/ticketsystem/service/TicketServiceImpl.java`
```java
@Service
public class TicketServiceImpl implements TicketService {

    @Autowired
    private ClassificationService classificationService;

    @Override
    public TicketResponse createTicket(CreateTicketRequest request) {
        Ticket ticket = mapToEntity(request);

        // Auto-classify if requested
        if (Boolean.TRUE.equals(request.getAutoClassify())) {
            ClassificationResult result = classificationService.classify(ticket);
            ticket.setCategory(result.getCategory());
            ticket.setPriority(result.getPriority());
        }

        Ticket saved = ticketRepository.save(ticket);
        return mapToResponse(saved);
    }
}
```

#### 9. Keyword Lists (Examples)

**Account Access Keywords:**
```java
"login", "password", "2fa", "two-factor", "authentication", "sign in",
"access denied", "locked out", "cannot access", "forgot password",
"reset password", "verify account", "activation", "credentials"
```

**Technical Issue Keywords:**
```java
"error", "bug", "crash", "broken", "not working", "failure", "exception",
"timeout", "slow", "performance", "loading", "freeze", "stuck"
```

**Urgent Priority Keywords:**
```java
"urgent", "critical", "emergency", "production", "down", "can't access",
"security", "breach", "hack", "immediately", "asap", "right now"
```

#### 10. Sample Request/Response

**Request:**
```bash
POST /tickets/123e4567-e89b-12d3-a456-426614174000/auto-classify
```

**Response:**
```json
{
  "category": "account_access",
  "priority": "high",
  "confidence_score": 0.75,
  "reasoning": "Classified as 'account_access' (confidence: 0.8) based on keywords: login, password. Priority 'high' (confidence: 0.7) based on keywords: important, cannot access.",
  "keywords_found": ["login", "password", "cannot access", "important"]
}
```

**Create Ticket with Auto-Classification:**
```bash
POST /tickets
{
  "customer_id": "C001",
  "customer_email": "user@example.com",
  "customer_name": "John Doe",
  "subject": "Urgent: Cannot login to my account",
  "description": "I forgot my password and the reset link is not working. This is blocking my work.",
  "auto_classify": true
}
```

**Response:**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "customer_id": "C001",
  "subject": "Urgent: Cannot login to my account",
  "description": "I forgot my password and the reset link is not working. This is blocking my work.",
  "category": "account_access",
  "priority": "urgent",
  "status": "new",
  // ... other fields
}
```

---

## 🔍 Additional Requirements

1. **Confidence Score Calculation:**
   - 0-2 matches: 0.3 (low confidence)
   - 3-4 matches: 0.6 (medium confidence)
   - 5+ matches: 0.8+ (high confidence)
   - Adjust based on total keywords available

2. **Multiple Category Matches:**
   - Take category with highest keyword count
   - If tie, default to first match or OTHER
   - Log when multiple categories match

3. **Priority Takes Precedence:**
   - Check URGENT first, then HIGH, then LOW
   - MEDIUM is default (no keyword matches)
   - Urgent keywords override all others

4. **Logging All Decisions:**
   - Every classification creates a ClassificationLog entry
   - Store ticket ID, suggested values, confidence, keywords, timestamp
   - Useful for analytics and improving algorithm

5. **Manual Override:**
   - Classification is just a suggestion
   - Users can manually update category/priority via PUT /tickets/:id
   - Keep classification log separate from ticket record

---

## ✅ Definition of Done

- [ ] ClassificationService interface and implementation created
- [ ] Keyword maps defined for all categories and priorities
- [ ] Classification algorithm working (keyword matching)
- [ ] Confidence score calculation implemented
- [ ] Reasoning text generation working
- [ ] ClassificationLog entity and repository created
- [ ] All decisions logged to database
- [ ] POST /tickets/:id/auto-classify endpoint working
- [ ] Auto-classification on ticket creation with flag working
- [ ] Manual testing shows >80% accuracy
- [ ] Code is maintainable and well-documented

---

## 🧪 Testing Considerations

**Test Cases to Verify:**
- Ticket with "login" and "password" → classified as `account_access`
- Ticket with "bug" and "error" → classified as `technical_issue`
- Ticket with "urgent" and "critical" → priority `urgent`
- Ticket with mixed keywords → highest match wins
- Ticket with no keywords → default to `other` and `medium`
- Confidence scores calculated correctly
- Classification logs created for all decisions

**Example Test Ticket:**
```
Subject: "Cannot login - urgent"
Description: "I forgot my password and cannot access my account"

Expected:
- Category: account_access (keywords: login, password, access)
- Priority: urgent (keyword: urgent)
- Confidence: >0.7
- Keywords: ["login", "password", "access", "urgent"]
```

---

## 🚀 Implementation Phases

**Phase 1:** Create ClassificationResult DTO and ClassificationLog entity
**Phase 2:** Build keyword maps for categories and priorities
**Phase 3:** Implement classification algorithm (keyword matching)
**Phase 4:** Add confidence score calculation and reasoning generation
**Phase 5:** Create ClassificationService and repository
**Phase 6:** Add /tickets/:id/auto-classify endpoint
**Phase 7:** Integrate auto-classification into ticket creation
**Phase 8:** Test with various ticket examples

Start with Phase 1 and progress sequentially.
