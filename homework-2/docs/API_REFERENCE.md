# API Reference

## Introduction

The Customer Support Ticket Management System provides a comprehensive REST API for managing customer support tickets. This API enables ticket creation, bulk import from various file formats (CSV, JSON, XML), filtering, updates, and automated classification using keyword-based analysis.

### Key Features

- Full CRUD operations for ticket management
- Bulk import from CSV, JSON, and XML files
- Advanced filtering by category, priority, and status
- Automated ticket classification based on content analysis
- Comprehensive validation and error handling
- OpenAPI/Swagger documentation support

### Technology Stack

- Spring Boot 3.2.2
- Java 17
- PostgreSQL Database
- Spring Data JPA
- Spring Validation

---

## Base URL

The API is accessible at the following base URL:

```
http://localhost:8080
```

All endpoints are prefixed with `/tickets` unless otherwise specified.

**API Documentation UI:**
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

---

## Endpoints

### 1. Create Ticket

Creates a new customer support ticket with optional auto-classification.

**Endpoint:** `POST /tickets`

**Description:** Creates a new support ticket. If `autoClassify` is set to `true`, the system will automatically determine the category and priority based on the ticket's subject and description.

**Request Headers:**
```
Content-Type: application/json
```

**Request Body:**

| Field | Type | Required | Description | Constraints |
|-------|------|----------|-------------|-------------|
| customerId | String | Yes | Unique identifier for the customer | Not blank |
| customerEmail | String | Yes | Customer's email address | Valid email format |
| customerName | String | Yes | Customer's full name | Not blank |
| subject | String | Yes | Ticket subject line | 1-200 characters |
| description | String | Yes | Detailed description of the issue | 10-2000 characters |
| category | TicketCategory | No | Ticket category | Enum value |
| priority | TicketPriority | No | Ticket priority | Enum value |
| assignedTo | String | No | Agent assigned to the ticket | - |
| tags | Array[String] | No | Tags for categorization | - |
| source | TicketSource | No | Origin of the ticket | Enum value |
| browser | String | No | Browser information | - |
| deviceType | DeviceType | No | Type of device used | Enum value |
| autoClassify | Boolean | No | Enable auto-classification | Default: false |

**Request Example:**

```json
{
  "customerId": "CUST-12345",
  "customerEmail": "john.doe@example.com",
  "customerName": "John Doe",
  "subject": "Unable to access my account",
  "description": "I have been trying to log in to my account for the past hour but keep getting an error message saying 'Invalid credentials'. I'm sure my password is correct. Please help urgently.",
  "category": "ACCOUNT_ACCESS",
  "priority": "HIGH",
  "assignedTo": "agent@support.com",
  "tags": ["login", "access", "urgent"],
  "source": "WEB_FORM",
  "browser": "Chrome 120.0",
  "deviceType": "DESKTOP",
  "autoClassify": false
}
```

**Response:** `201 Created`

```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "customerId": "CUST-12345",
  "customerEmail": "john.doe@example.com",
  "customerName": "John Doe",
  "subject": "Unable to access my account",
  "description": "I have been trying to log in to my account for the past hour but keep getting an error message saying 'Invalid credentials'. I'm sure my password is correct. Please help urgently.",
  "category": "ACCOUNT_ACCESS",
  "priority": "HIGH",
  "status": "NEW",
  "createdAt": "2026-02-02T10:30:00",
  "updatedAt": "2026-02-02T10:30:00",
  "resolvedAt": null,
  "assignedTo": "agent@support.com",
  "tags": ["login", "access", "urgent"],
  "metadata": {
    "source": "WEB_FORM",
    "browser": "Chrome 120.0",
    "deviceType": "DESKTOP"
  }
}
```

**cURL Example:**

```bash
curl -X POST http://localhost:8080/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-12345",
    "customerEmail": "john.doe@example.com",
    "customerName": "John Doe",
    "subject": "Unable to access my account",
    "description": "I have been trying to log in to my account for the past hour but keep getting an error message saying 'Invalid credentials'. I am sure my password is correct. Please help urgently.",
    "category": "ACCOUNT_ACCESS",
    "priority": "HIGH",
    "tags": ["login", "access", "urgent"],
    "source": "WEB_FORM",
    "browser": "Chrome 120.0",
    "deviceType": "DESKTOP",
    "autoClassify": false
  }'
```

**Error Responses:**

**400 Bad Request** - Validation errors

```json
{
  "timestamp": "2026-02-02T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/tickets",
  "fieldErrors": {
    "customerEmail": "Invalid email format",
    "description": "Description must be between 10 and 2000 characters"
  }
}
```

**500 Internal Server Error** - Server error

```json
{
  "timestamp": "2026-02-02T10:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred while processing the request",
  "path": "/tickets"
}
```

---

### 2. Bulk Import Tickets

Imports multiple tickets from a file in CSV, JSON, or XML format.

**Endpoint:** `POST /tickets/import`

**Description:** Bulk imports tickets from an uploaded file. Supports CSV, JSON, and XML formats. Optionally auto-classifies all imported tickets.

**Request Headers:**
```
Content-Type: multipart/form-data
```

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| file | File | Yes | The file to import (CSV, JSON, or XML) |
| format | String | Yes | File format: "csv", "json", or "xml" |
| autoClassify | Boolean | No | Auto-classify imported tickets (default: false) |

**Request Example:**

Using form-data with parameters:
- `file`: sample_tickets.csv
- `format`: csv
- `autoClassify`: true

**Response:** `200 OK`

```json
{
  "totalRecords": 100,
  "successfulImports": 95,
  "failedImports": 5,
  "errors": [
    "Row 12: Invalid email format for customer@",
    "Row 34: Missing required field 'description'",
    "Row 56: Description too short (minimum 10 characters)",
    "Row 78: Invalid category value 'UNKNOWN'",
    "Row 89: Subject exceeds maximum length of 200 characters"
  ]
}
```

**cURL Example:**

```bash
curl -X POST http://localhost:8080/tickets/import \
  -F "file=@/path/to/sample_tickets.csv" \
  -F "format=csv" \
  -F "autoClassify=true"
```

**CSV File Format Example:**

```csv
customerId,customerEmail,customerName,subject,description,category,priority,assignedTo,tags,source,browser,deviceType
CUST-001,user1@example.com,Alice Smith,Login issue,Cannot access my account after password reset,ACCOUNT_ACCESS,HIGH,agent1@support.com,"login,password",WEB_FORM,Chrome,DESKTOP
CUST-002,user2@example.com,Bob Jones,Billing question,I was charged twice for my subscription,BILLING_QUESTION,MEDIUM,agent2@support.com,"billing,payment",EMAIL,Safari,MOBILE
```

**JSON File Format Example:**

```json
[
  {
    "customerId": "CUST-001",
    "customerEmail": "user1@example.com",
    "customerName": "Alice Smith",
    "subject": "Login issue",
    "description": "Cannot access my account after password reset",
    "category": "ACCOUNT_ACCESS",
    "priority": "HIGH",
    "assignedTo": "agent1@support.com",
    "tags": ["login", "password"],
    "source": "WEB_FORM",
    "browser": "Chrome",
    "deviceType": "DESKTOP"
  }
]
```

**XML File Format Example:**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<tickets>
  <ticket>
    <customerId>CUST-001</customerId>
    <customerEmail>user1@example.com</customerEmail>
    <customerName>Alice Smith</customerName>
    <subject>Login issue</subject>
    <description>Cannot access my account after password reset</description>
    <category>ACCOUNT_ACCESS</category>
    <priority>HIGH</priority>
    <assignedTo>agent1@support.com</assignedTo>
    <tags>
      <tag>login</tag>
      <tag>password</tag>
    </tags>
    <source>WEB_FORM</source>
    <browser>Chrome</browser>
    <deviceType>DESKTOP</deviceType>
  </ticket>
</tickets>
```

**Error Responses:**

**400 Bad Request** - Invalid file or format

```json
{
  "timestamp": "2026-02-02T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Unsupported file format. Supported formats: csv, json, xml",
  "path": "/tickets/import"
}
```

**500 Internal Server Error** - Import processing error

```json
{
  "timestamp": "2026-02-02T10:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Failed to parse the uploaded file",
  "path": "/tickets/import"
}
```

---

### 3. List Tickets with Filters

Retrieves all tickets with optional filtering by category, priority, and status.

**Endpoint:** `GET /tickets`

**Description:** Returns a list of all tickets. Supports filtering by category, priority, and status. If no filters are provided, returns all tickets.

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| category | TicketCategory | No | Filter by ticket category |
| priority | TicketPriority | No | Filter by priority level |
| status | TicketStatus | No | Filter by ticket status |

**Request Example:**

```
GET /tickets?category=TECHNICAL_ISSUE&priority=HIGH&status=NEW
```

**Response:** `200 OK`

```json
[
  {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "customerId": "CUST-12345",
    "customerEmail": "john.doe@example.com",
    "customerName": "John Doe",
    "subject": "Application crashes on startup",
    "description": "The mobile app crashes immediately after I open it. I've tried restarting my phone but the issue persists.",
    "category": "TECHNICAL_ISSUE",
    "priority": "HIGH",
    "status": "NEW",
    "createdAt": "2026-02-02T10:30:00",
    "updatedAt": "2026-02-02T10:30:00",
    "resolvedAt": null,
    "assignedTo": "tech@support.com",
    "tags": ["crash", "mobile", "startup"],
    "metadata": {
      "source": "API",
      "browser": null,
      "deviceType": "MOBILE"
    }
  },
  {
    "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "customerId": "CUST-67890",
    "customerEmail": "jane.smith@example.com",
    "customerName": "Jane Smith",
    "subject": "Database connection timeout",
    "description": "Getting timeout errors when trying to connect to the database. This started happening after the latest update.",
    "category": "TECHNICAL_ISSUE",
    "priority": "HIGH",
    "status": "NEW",
    "createdAt": "2026-02-02T11:15:00",
    "updatedAt": "2026-02-02T11:15:00",
    "resolvedAt": null,
    "assignedTo": "tech@support.com",
    "tags": ["database", "timeout", "urgent"],
    "metadata": {
      "source": "EMAIL",
      "browser": null,
      "deviceType": "DESKTOP"
    }
  }
]
```

**cURL Examples:**

```bash
# Get all tickets
curl -X GET http://localhost:8080/tickets

# Filter by category
curl -X GET "http://localhost:8080/tickets?category=TECHNICAL_ISSUE"

# Filter by multiple parameters
curl -X GET "http://localhost:8080/tickets?category=BILLING_QUESTION&priority=HIGH&status=NEW"

# Filter by status only
curl -X GET "http://localhost:8080/tickets?status=RESOLVED"
```

**Error Responses:**

**400 Bad Request** - Invalid filter value

```json
{
  "timestamp": "2026-02-02T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid category value: INVALID_CATEGORY",
  "path": "/tickets"
}
```

**500 Internal Server Error** - Database error

```json
{
  "timestamp": "2026-02-02T10:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Failed to retrieve tickets from database",
  "path": "/tickets"
}
```

---

### 4. Get Ticket by ID

Retrieves a specific ticket by its unique identifier.

**Endpoint:** `GET /tickets/{id}`

**Description:** Returns detailed information about a single ticket identified by its UUID.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | UUID | Yes | Unique identifier of the ticket |

**Request Example:**

```
GET /tickets/a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

**Response:** `200 OK`

```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "customerId": "CUST-12345",
  "customerEmail": "john.doe@example.com",
  "customerName": "John Doe",
  "subject": "Unable to access my account",
  "description": "I have been trying to log in to my account for the past hour but keep getting an error message saying 'Invalid credentials'. I'm sure my password is correct. Please help urgently.",
  "category": "ACCOUNT_ACCESS",
  "priority": "HIGH",
  "status": "IN_PROGRESS",
  "createdAt": "2026-02-02T10:30:00",
  "updatedAt": "2026-02-02T11:45:00",
  "resolvedAt": null,
  "assignedTo": "agent@support.com",
  "tags": ["login", "access", "urgent"],
  "metadata": {
    "source": "WEB_FORM",
    "browser": "Chrome 120.0",
    "deviceType": "DESKTOP"
  }
}
```

**cURL Example:**

```bash
curl -X GET http://localhost:8080/tickets/a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

**Error Responses:**

**404 Not Found** - Ticket not found

```json
{
  "timestamp": "2026-02-02T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Ticket not found with id: a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "path": "/tickets/a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

**400 Bad Request** - Invalid UUID format

```json
{
  "timestamp": "2026-02-02T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid UUID format",
  "path": "/tickets/invalid-id"
}
```

**500 Internal Server Error** - Server error

```json
{
  "timestamp": "2026-02-02T10:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Failed to retrieve ticket",
  "path": "/tickets/a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

---

### 5. Update Ticket

Updates an existing ticket's information.

**Endpoint:** `PUT /tickets/{id}`

**Description:** Updates one or more fields of an existing ticket. All fields in the request body are optional; only provided fields will be updated.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | UUID | Yes | Unique identifier of the ticket to update |

**Request Headers:**
```
Content-Type: application/json
```

**Request Body:**

| Field | Type | Required | Description | Constraints |
|-------|------|----------|-------------|-------------|
| subject | String | No | Updated ticket subject | 1-200 characters |
| description | String | No | Updated description | 10-2000 characters |
| category | TicketCategory | No | Updated category | Enum value |
| priority | TicketPriority | No | Updated priority | Enum value |
| status | TicketStatus | No | Updated status | Enum value |
| assignedTo | String | No | Updated assigned agent | - |
| tags | Array[String] | No | Updated tags | - |

**Request Example:**

```json
{
  "status": "IN_PROGRESS",
  "assignedTo": "senior.agent@support.com",
  "priority": "URGENT",
  "tags": ["login", "access", "urgent", "escalated"]
}
```

**Response:** `200 OK`

```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "customerId": "CUST-12345",
  "customerEmail": "john.doe@example.com",
  "customerName": "John Doe",
  "subject": "Unable to access my account",
  "description": "I have been trying to log in to my account for the past hour but keep getting an error message saying 'Invalid credentials'. I'm sure my password is correct. Please help urgently.",
  "category": "ACCOUNT_ACCESS",
  "priority": "URGENT",
  "status": "IN_PROGRESS",
  "createdAt": "2026-02-02T10:30:00",
  "updatedAt": "2026-02-02T12:00:00",
  "resolvedAt": null,
  "assignedTo": "senior.agent@support.com",
  "tags": ["login", "access", "urgent", "escalated"],
  "metadata": {
    "source": "WEB_FORM",
    "browser": "Chrome 120.0",
    "deviceType": "DESKTOP"
  }
}
```

**cURL Example:**

```bash
curl -X PUT http://localhost:8080/tickets/a1b2c3d4-e5f6-7890-abcd-ef1234567890 \
  -H "Content-Type: application/json" \
  -d '{
    "status": "IN_PROGRESS",
    "assignedTo": "senior.agent@support.com",
    "priority": "URGENT",
    "tags": ["login", "access", "urgent", "escalated"]
  }'
```

**Error Responses:**

**400 Bad Request** - Validation errors

```json
{
  "timestamp": "2026-02-02T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/tickets/a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "fieldErrors": {
    "description": "Description must be between 10 and 2000 characters",
    "subject": "Subject must be between 1 and 200 characters"
  }
}
```

**404 Not Found** - Ticket not found

```json
{
  "timestamp": "2026-02-02T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Ticket not found with id: a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "path": "/tickets/a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

**500 Internal Server Error** - Server error

```json
{
  "timestamp": "2026-02-02T10:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Failed to update ticket",
  "path": "/tickets/a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

---

### 6. Delete Ticket

Deletes a ticket from the system.

**Endpoint:** `DELETE /tickets/{id}`

**Description:** Permanently deletes a ticket identified by its UUID.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | UUID | Yes | Unique identifier of the ticket to delete |

**Request Example:**

```
DELETE /tickets/a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

**Response:** `204 No Content`

No response body is returned for successful deletion.

**cURL Example:**

```bash
curl -X DELETE http://localhost:8080/tickets/a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

**Error Responses:**

**404 Not Found** - Ticket not found

```json
{
  "timestamp": "2026-02-02T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Ticket not found with id: a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "path": "/tickets/a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

**400 Bad Request** - Invalid UUID format

```json
{
  "timestamp": "2026-02-02T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid UUID format",
  "path": "/tickets/invalid-id"
}
```

**500 Internal Server Error** - Server error

```json
{
  "timestamp": "2026-02-02T10:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Failed to delete ticket",
  "path": "/tickets/a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

---

### 7. Auto-Classify Ticket

Automatically classifies a ticket's category and priority using keyword-based analysis.

**Endpoint:** `POST /tickets/{id}/auto-classify`

**Description:** Analyzes the ticket's subject and description to automatically determine the most appropriate category and priority level. The classification is based on keyword matching and pattern recognition. The ticket will be updated with the classification results.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | UUID | Yes | Unique identifier of the ticket to classify |

**Request Example:**

```
POST /tickets/a1b2c3d4-e5f6-7890-abcd-ef1234567890/auto-classify
```

**Response:** `200 OK`

```json
{
  "category": "ACCOUNT_ACCESS",
  "priority": "HIGH",
  "confidenceScore": 0.89,
  "reasoning": "Detected keywords related to account access issues and urgent indicators",
  "keywordsFound": ["login", "access", "account", "error", "urgent", "password"]
}
```

**cURL Example:**

```bash
curl -X POST http://localhost:8080/tickets/a1b2c3d4-e5f6-7890-abcd-ef1234567890/auto-classify
```

**Classification Logic:**

The auto-classification algorithm analyzes the ticket's subject and description for specific keywords:

**Category Keywords:**
- ACCOUNT_ACCESS: login, password, access, authentication, credentials, locked, sign in, username
- TECHNICAL_ISSUE: error, bug, crash, broken, not working, malfunction, technical, system, failure
- BILLING_QUESTION: billing, invoice, payment, charge, refund, subscription, cost, price, fee
- FEATURE_REQUEST: feature, request, enhancement, suggestion, improvement, add, new
- BUG_REPORT: bug, defect, issue, problem, incorrect, wrong, unexpected
- OTHER: (default when no specific keywords match)

**Priority Keywords:**
- URGENT: urgent, critical, down, emergency, asap, immediately, blocker
- HIGH: important, high priority, serious, significant, major
- MEDIUM: moderate, medium, normal, regular
- LOW: minor, small, low priority, trivial, cosmetic

**Confidence Score:** A value between 0.0 and 1.0 indicating the confidence level of the classification based on the number and relevance of matched keywords.

**Error Responses:**

**404 Not Found** - Ticket not found

```json
{
  "timestamp": "2026-02-02T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Ticket not found with id: a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "path": "/tickets/a1b2c3d4-e5f6-7890-abcd-ef1234567890/auto-classify"
}
```

**400 Bad Request** - Invalid UUID format

```json
{
  "timestamp": "2026-02-02T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid UUID format",
  "path": "/tickets/invalid-id/auto-classify"
}
```

**500 Internal Server Error** - Classification error

```json
{
  "timestamp": "2026-02-02T10:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Failed to classify ticket",
  "path": "/tickets/a1b2c3d4-e5f6-7890-abcd-ef1234567890/auto-classify"
}
```

---

## Data Models

### CreateTicketRequest

Request model for creating a new ticket.

| Field | Type | Required | Description | Validation |
|-------|------|----------|-------------|------------|
| customerId | String | Yes | Unique identifier for the customer | Not blank |
| customerEmail | String | Yes | Customer's email address | Valid email format |
| customerName | String | Yes | Customer's full name | Not blank |
| subject | String | Yes | Brief summary of the issue | 1-200 characters |
| description | String | Yes | Detailed description of the issue | 10-2000 characters |
| category | TicketCategory | No | Ticket category | Must be valid enum value |
| priority | TicketPriority | No | Priority level | Must be valid enum value |
| assignedTo | String | No | Email of the assigned agent | - |
| tags | Array[String] | No | List of tags for categorization | - |
| source | TicketSource | No | Channel through which ticket was created | Must be valid enum value |
| browser | String | No | Browser information (if applicable) | - |
| deviceType | DeviceType | No | Type of device used | Must be valid enum value |
| autoClassify | Boolean | No | Whether to auto-classify the ticket | Default: false |

**Example:**

```json
{
  "customerId": "CUST-12345",
  "customerEmail": "customer@example.com",
  "customerName": "John Doe",
  "subject": "Payment processing error",
  "description": "I'm getting an error when trying to process my monthly subscription payment. The error code is ERR_PAYMENT_503.",
  "category": "BILLING_QUESTION",
  "priority": "MEDIUM",
  "assignedTo": "billing@support.com",
  "tags": ["payment", "subscription", "error"],
  "source": "WEB_FORM",
  "browser": "Firefox 122.0",
  "deviceType": "DESKTOP",
  "autoClassify": false
}
```

---

### UpdateTicketRequest

Request model for updating an existing ticket. All fields are optional.

| Field | Type | Required | Description | Validation |
|-------|------|----------|-------------|------------|
| subject | String | No | Updated ticket subject | 1-200 characters |
| description | String | No | Updated description | 10-2000 characters |
| category | TicketCategory | No | Updated category | Must be valid enum value |
| priority | TicketPriority | No | Updated priority level | Must be valid enum value |
| status | TicketStatus | No | Updated ticket status | Must be valid enum value |
| assignedTo | String | No | Updated assigned agent | - |
| tags | Array[String] | No | Updated tags list | - |

**Example:**

```json
{
  "status": "RESOLVED",
  "assignedTo": "agent2@support.com",
  "tags": ["payment", "subscription", "error", "resolved"]
}
```

---

### TicketDto

Response model representing a ticket. This is the primary data structure returned by most API endpoints.

| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Unique identifier of the ticket |
| customerId | String | Customer's unique identifier |
| customerEmail | String | Customer's email address |
| customerName | String | Customer's full name |
| subject | String | Ticket subject line |
| description | String | Detailed description of the issue |
| category | TicketCategory | Ticket category |
| priority | TicketPriority | Priority level |
| status | TicketStatus | Current status of the ticket |
| createdAt | LocalDateTime | Timestamp when ticket was created |
| updatedAt | LocalDateTime | Timestamp of last update |
| resolvedAt | LocalDateTime | Timestamp when ticket was resolved (null if not resolved) |
| assignedTo | String | Email of the assigned agent |
| tags | Array[String] | List of tags |
| metadata | TicketMetadata | Additional metadata about the ticket |

**TicketMetadata Object:**

| Field | Type | Description |
|-------|------|-------------|
| source | TicketSource | Channel through which ticket was created |
| browser | String | Browser information (if applicable) |
| deviceType | DeviceType | Type of device used |

**Example:**

```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "customerId": "CUST-12345",
  "customerEmail": "customer@example.com",
  "customerName": "John Doe",
  "subject": "Payment processing error",
  "description": "I'm getting an error when trying to process my monthly subscription payment. The error code is ERR_PAYMENT_503.",
  "category": "BILLING_QUESTION",
  "priority": "MEDIUM",
  "status": "RESOLVED",
  "createdAt": "2026-02-01T14:30:00",
  "updatedAt": "2026-02-02T10:15:00",
  "resolvedAt": "2026-02-02T10:15:00",
  "assignedTo": "billing@support.com",
  "tags": ["payment", "subscription", "error", "resolved"],
  "metadata": {
    "source": "WEB_FORM",
    "browser": "Firefox 122.0",
    "deviceType": "DESKTOP"
  }
}
```

---

### ClassificationResult

Response model for auto-classification results.

| Field | Type | Description |
|-------|------|-------------|
| category | TicketCategory | Automatically determined category |
| priority | TicketPriority | Automatically determined priority |
| confidenceScore | Double | Confidence level of classification (0.0-1.0) |
| reasoning | String | Explanation of the classification decision |
| keywordsFound | Array[String] | List of keywords that influenced the classification |

**Example:**

```json
{
  "category": "TECHNICAL_ISSUE",
  "priority": "URGENT",
  "confidenceScore": 0.92,
  "reasoning": "Detected critical technical issue keywords with urgent indicators",
  "keywordsFound": ["crash", "error", "critical", "system", "down", "urgent"]
}
```

---

### ImportSummaryResponse

Response model for bulk import operations.

| Field | Type | Description |
|-------|------|-------------|
| totalRecords | Integer | Total number of records in the import file |
| successfulImports | Integer | Number of tickets successfully imported |
| failedImports | Integer | Number of records that failed to import |
| errors | Array[String] | List of error messages for failed imports |

**Example:**

```json
{
  "totalRecords": 50,
  "successfulImports": 47,
  "failedImports": 3,
  "errors": [
    "Row 5: Invalid email format for customer@invalid",
    "Row 12: Description too short (minimum 10 characters)",
    "Row 34: Missing required field 'customerName'"
  ]
}
```

---

### ErrorResponse

Standard error response model returned for all error conditions.

| Field | Type | Description |
|-------|------|-------------|
| timestamp | LocalDateTime | Timestamp when the error occurred |
| status | Integer | HTTP status code |
| error | String | HTTP error description |
| message | String | Detailed error message |
| path | String | API endpoint path that generated the error |
| fieldErrors | Map[String, String] | Field-specific validation errors (optional) |

**Example:**

```json
{
  "timestamp": "2026-02-02T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for request",
  "path": "/tickets",
  "fieldErrors": {
    "customerEmail": "Invalid email format",
    "description": "Description must be between 10 and 2000 characters",
    "subject": "Subject is required"
  }
}
```

---

## Enumerations

### TicketCategory

Defines the main categories for classifying tickets.

| Value | Description |
|-------|-------------|
| ACCOUNT_ACCESS | Issues related to account login, password reset, or access permissions |
| TECHNICAL_ISSUE | Technical problems with the system, application errors, or malfunctions |
| BILLING_QUESTION | Questions or issues related to billing, invoices, payments, or subscriptions |
| FEATURE_REQUEST | Requests for new features or enhancements to existing functionality |
| BUG_REPORT | Reports of bugs, defects, or unexpected behavior in the system |
| OTHER | General inquiries or issues that don't fit other categories |

**Usage Example:**
```json
{
  "category": "TECHNICAL_ISSUE"
}
```

---

### TicketPriority

Defines the priority levels for tickets.

| Value | Description |
|-------|-------------|
| URGENT | Critical issues requiring immediate attention (system down, security issues) |
| HIGH | Important issues that significantly impact user experience |
| MEDIUM | Standard issues with moderate impact |
| LOW | Minor issues or inquiries with minimal impact |

**Priority Guidelines:**
- **URGENT**: Service outages, security vulnerabilities, data loss, critical business impact
- **HIGH**: Major functionality broken, significant user impact, escalated issues
- **MEDIUM**: Regular issues, moderate functionality impact, standard support requests
- **LOW**: Minor bugs, cosmetic issues, general questions, feature suggestions

**Usage Example:**
```json
{
  "priority": "HIGH"
}
```

---

### TicketStatus

Defines the lifecycle status of a ticket.

| Value | Description |
|-------|-------------|
| NEW | Newly created ticket that hasn't been assigned or worked on |
| IN_PROGRESS | Ticket is actively being worked on by a support agent |
| WAITING_CUSTOMER | Waiting for additional information or action from the customer |
| RESOLVED | Issue has been resolved but ticket remains open for verification |
| CLOSED | Ticket is completely resolved and closed |

**Status Workflow:**
```
NEW → IN_PROGRESS → [WAITING_CUSTOMER] → RESOLVED → CLOSED
                      ↓                      ↑
                      └──────────────────────┘
```

**Usage Example:**
```json
{
  "status": "IN_PROGRESS"
}
```

---

### TicketSource

Defines the channel through which a ticket was created.

| Value | Description |
|-------|-------------|
| WEB_FORM | Ticket submitted through web-based support form |
| EMAIL | Ticket created from email communication |
| API | Ticket created programmatically through the API |
| CHAT | Ticket originated from live chat support |
| PHONE | Ticket created from phone call |

**Usage Example:**
```json
{
  "metadata": {
    "source": "WEB_FORM"
  }
}
```

---

### DeviceType

Defines the type of device used by the customer.

| Value | Description |
|-------|-------------|
| DESKTOP | Desktop computer or laptop |
| MOBILE | Mobile phone or smartphone |
| TABLET | Tablet device |

**Usage Example:**
```json
{
  "metadata": {
    "deviceType": "MOBILE"
  }
}
```

---

## Example Workflows

### Workflow 1: Creating and Auto-Classifying a Ticket

This workflow demonstrates creating a ticket with auto-classification enabled.

**Step 1: Create ticket with auto-classification**

```bash
curl -X POST http://localhost:8080/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-99001",
    "customerEmail": "urgent.user@example.com",
    "customerName": "Urgent User",
    "subject": "Critical system crash - cannot access any features",
    "description": "The entire system crashes whenever I try to open the dashboard. This is blocking all my work and I need this fixed urgently as soon as possible. Error message says application has stopped working.",
    "tags": ["urgent", "crash"],
    "source": "CHAT",
    "deviceType": "DESKTOP",
    "autoClassify": true
  }'
```

**Response:**
```json
{
  "id": "f9e8d7c6-b5a4-3210-fedc-ba9876543210",
  "customerId": "CUST-99001",
  "customerEmail": "urgent.user@example.com",
  "customerName": "Urgent User",
  "subject": "Critical system crash - cannot access any features",
  "description": "The entire system crashes whenever I try to open the dashboard. This is blocking all my work and I need this fixed urgently as soon as possible. Error message says application has stopped working.",
  "category": "TECHNICAL_ISSUE",
  "priority": "URGENT",
  "status": "NEW",
  "createdAt": "2026-02-02T15:30:00",
  "updatedAt": "2026-02-02T15:30:00",
  "resolvedAt": null,
  "assignedTo": null,
  "tags": ["urgent", "crash"],
  "metadata": {
    "source": "CHAT",
    "browser": null,
    "deviceType": "DESKTOP"
  }
}
```

**Step 2: Retrieve the ticket to verify classification**

```bash
curl -X GET http://localhost:8080/tickets/f9e8d7c6-b5a4-3210-fedc-ba9876543210
```

**Step 3: Manually trigger classification if needed**

```bash
curl -X POST http://localhost:8080/tickets/f9e8d7c6-b5a4-3210-fedc-ba9876543210/auto-classify
```

**Response:**
```json
{
  "category": "TECHNICAL_ISSUE",
  "priority": "URGENT",
  "confidenceScore": 0.95,
  "reasoning": "Detected critical technical issue keywords with multiple urgent indicators",
  "keywordsFound": ["crash", "system", "error", "urgent", "critical", "blocking"]
}
```

---

### Workflow 2: Importing Tickets in Bulk

This workflow demonstrates importing multiple tickets from a CSV file with auto-classification.

**Step 1: Prepare CSV file** (`bulk_tickets.csv`)

```csv
customerId,customerEmail,customerName,subject,description,tags,source,deviceType
CUST-101,user101@example.com,Alice Johnson,Password reset not working,I requested a password reset 3 hours ago but still haven't received the email,password;reset;email,EMAIL,DESKTOP
CUST-102,user102@example.com,Bob Smith,Feature suggestion: dark mode,Would be great to have a dark mode option for the interface,feature;enhancement,WEB_FORM,MOBILE
CUST-103,user103@example.com,Carol White,Billing discrepancy,I was charged twice for last month subscription,billing;payment;duplicate,PHONE,DESKTOP
CUST-104,user104@example.com,David Brown,App crashes on iOS,The mobile app crashes immediately after opening on my iPhone 14,crash;mobile;ios;bug,CHAT,MOBILE
CUST-105,user105@example.com,Eve Davis,Cannot upload documents,Getting error when trying to upload PDF files larger than 5MB,upload;error;technical,API,TABLET
```

**Step 2: Import the CSV file with auto-classification**

```bash
curl -X POST http://localhost:8080/tickets/import \
  -F "file=@bulk_tickets.csv" \
  -F "format=csv" \
  -F "autoClassify=true"
```

**Response:**
```json
{
  "totalRecords": 5,
  "successfulImports": 5,
  "failedImports": 0,
  "errors": []
}
```

**Step 3: Verify imported tickets**

```bash
curl -X GET "http://localhost:8080/tickets?status=NEW" | jq '.[0:5]'
```

---

### Workflow 3: Filtering and Searching Tickets

This workflow demonstrates various filtering options to find specific tickets.

**Step 1: Get all high-priority tickets**

```bash
curl -X GET "http://localhost:8080/tickets?priority=HIGH"
```

**Step 2: Get all billing-related tickets that are new**

```bash
curl -X GET "http://localhost:8080/tickets?category=BILLING_QUESTION&status=NEW"
```

**Step 3: Get all urgent technical issues**

```bash
curl -X GET "http://localhost:8080/tickets?category=TECHNICAL_ISSUE&priority=URGENT"
```

**Step 4: Get all resolved tickets**

```bash
curl -X GET "http://localhost:8080/tickets?status=RESOLVED"
```

**Response Example:**
```json
[
  {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "customerId": "CUST-101",
    "customerEmail": "user101@example.com",
    "customerName": "Alice Johnson",
    "subject": "Password reset not working",
    "description": "I requested a password reset 3 hours ago but still haven't received the email",
    "category": "ACCOUNT_ACCESS",
    "priority": "MEDIUM",
    "status": "RESOLVED",
    "createdAt": "2026-02-01T09:00:00",
    "updatedAt": "2026-02-01T11:30:00",
    "resolvedAt": "2026-02-01T11:30:00",
    "assignedTo": "agent1@support.com",
    "tags": ["password", "reset", "email"],
    "metadata": {
      "source": "EMAIL",
      "browser": null,
      "deviceType": "DESKTOP"
    }
  }
]
```

---

### Workflow 4: Complete Ticket Lifecycle

This workflow demonstrates the complete lifecycle of a ticket from creation to resolution.

**Step 1: Customer creates a ticket**

```bash
curl -X POST http://localhost:8080/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-200",
    "customerEmail": "customer200@example.com",
    "customerName": "John Customer",
    "subject": "Cannot export report to PDF",
    "description": "When I click the export to PDF button on my monthly report, nothing happens. I need this report for my meeting tomorrow.",
    "tags": ["export", "pdf", "report"],
    "source": "WEB_FORM",
    "browser": "Chrome 120.0",
    "deviceType": "DESKTOP",
    "autoClassify": true
  }'
```

**Response:**
```json
{
  "id": "lifecycle-example-uuid-1234",
  "status": "NEW",
  "category": "TECHNICAL_ISSUE",
  "priority": "HIGH",
  ...
}
```

**Step 2: Support agent picks up the ticket**

```bash
curl -X PUT http://localhost:8080/tickets/lifecycle-example-uuid-1234 \
  -H "Content-Type: application/json" \
  -d '{
    "status": "IN_PROGRESS",
    "assignedTo": "support.agent@company.com"
  }'
```

**Step 3: Agent needs more information**

```bash
curl -X PUT http://localhost:8080/tickets/lifecycle-example-uuid-1234 \
  -H "Content-Type: application/json" \
  -d '{
    "status": "WAITING_CUSTOMER",
    "tags": ["export", "pdf", "report", "awaiting-info"]
  }'
```

**Step 4: Customer provides additional details (agent updates ticket)**

```bash
curl -X PUT http://localhost:8080/tickets/lifecycle-example-uuid-1234 \
  -H "Content-Type: application/json" \
  -d '{
    "status": "IN_PROGRESS",
    "description": "When I click the export to PDF button on my monthly report, nothing happens. I need this report for my meeting tomorrow. Browser console shows: ERR_PDF_GENERATOR_TIMEOUT. Report has 150 pages with multiple charts."
  }'
```

**Step 5: Agent resolves the issue**

```bash
curl -X PUT http://localhost:8080/tickets/lifecycle-example-uuid-1234 \
  -H "Content-Type: application/json" \
  -d '{
    "status": "RESOLVED",
    "tags": ["export", "pdf", "report", "resolved", "timeout-issue"]
  }'
```

**Step 6: Verify resolution and close ticket**

```bash
curl -X PUT http://localhost:8080/tickets/lifecycle-example-uuid-1234 \
  -H "Content-Type: application/json" \
  -d '{
    "status": "CLOSED"
  }'
```

**Step 7: Retrieve final ticket state**

```bash
curl -X GET http://localhost:8080/tickets/lifecycle-example-uuid-1234
```

**Final Response:**
```json
{
  "id": "lifecycle-example-uuid-1234",
  "customerId": "CUST-200",
  "customerEmail": "customer200@example.com",
  "customerName": "John Customer",
  "subject": "Cannot export report to PDF",
  "description": "When I click the export to PDF button on my monthly report, nothing happens. I need this report for my meeting tomorrow. Browser console shows: ERR_PDF_GENERATOR_TIMEOUT. Report has 150 pages with multiple charts.",
  "category": "TECHNICAL_ISSUE",
  "priority": "HIGH",
  "status": "CLOSED",
  "createdAt": "2026-02-02T08:00:00",
  "updatedAt": "2026-02-02T14:30:00",
  "resolvedAt": "2026-02-02T14:00:00",
  "assignedTo": "support.agent@company.com",
  "tags": ["export", "pdf", "report", "resolved", "timeout-issue"],
  "metadata": {
    "source": "WEB_FORM",
    "browser": "Chrome 120.0",
    "deviceType": "DESKTOP"
  }
}
```

---

## Additional Information

### Rate Limiting

Currently, there are no rate limits enforced by the API. However, it's recommended to implement rate limiting in production environments.

### Authentication

The current version of the API does not require authentication. For production use, implement OAuth 2.0 or JWT-based authentication.

### Pagination

The current API does not support pagination. All tickets are returned in a single response. For large datasets, consider implementing pagination in future versions.

### Versioning

This is version 1.0.0 of the API. Future versions will maintain backward compatibility or provide versioned endpoints (e.g., `/v2/tickets`).

### CORS

Cross-Origin Resource Sharing (CORS) should be configured appropriately for web applications accessing this API from different domains.

### Support

For API support and questions:
- API Documentation: `http://localhost:8080/swagger-ui.html`
- OpenAPI Specification: `http://localhost:8080/api-docs`

---

**Last Updated:** 2026-02-02
**API Version:** 1.0.0
**Framework:** Spring Boot 3.2.2
