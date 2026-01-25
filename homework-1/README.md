# 🏦 Homework 1: Banking Transactions API

> **Student Name**: Sviatoslav Glushchenko
> **Date Submitted**: 25.01.2026
> **AI Tools Used**: GitHub Copilot

---

## 📋 Project Overview

This project implements a comprehensive REST API for banking transactions using Node.js and Express.js. The API supports creating, retrieving, and filtering transactions with robust validation and multiple features for account management and reporting.

### Key Features Implemented

#### ✅ **Core Features (Task 1-3)**
- ✨ **Transaction Management**: Create, retrieve, and list transactions
- 🔍 **Advanced Filtering**: Filter by account, type, and date range
- ✅ **Robust Validation**: Comprehensive validation for amounts, accounts, currencies, and transaction types
- 💰 **Account Management**: Track balances and transaction history per account

#### 🌟 **Additional Features (Task 4)**

1. **Transaction Summary Endpoint** (Option A)
   - `GET /accounts/:accountId/summary`
   - Returns total deposits, withdrawals, transaction count, and recent activity

2. **Transaction Export** (Option C)
   - `GET /transactions/export?format=csv`
   - Export all transactions in CSV format for reporting

---

## 🏗️ Architecture & Technology Stack

- **Runtime**: Node.js
- **Framework**: Express.js (REST API)
- **Storage**: In-memory (JavaScript arrays and objects)
- **Validation**: Custom validation module with comprehensive rules
- **UUID**: Used for unique transaction IDs

### Project Structure

```
src/
├── index.js           # Main application entry point
├── routes.js          # API endpoint handlers
├── transaction.js     # Transaction model and business logic
└── validators.js      # Validation utilities
demo/
├── run.sh            # Script to start the application
├── sample-requests.sh # Bash script with sample API calls
├── sample-requests.http # REST Client format for testing
└── sample-data.json  # Sample transaction data
```

---

## 📋 Implemented Endpoints

### Transaction Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/transactions` | Create a new transaction |
| `GET` | `/transactions` | List all transactions (with filtering) |
| `GET` | `/transactions/:id` | Get a specific transaction by ID |
| `GET` | `/transactions/export?format=csv` | Export transactions as CSV |

### Account Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/accounts/:accountId/balance` | Get account balance |
| `GET` | `/accounts/:accountId/summary` | Get account summary with statistics |

### System Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/health` | API health check |

---

## ✅ Validation Rules Implemented

### Transaction Validation
- ✓ Amount must be positive
- ✓ Amount must have maximum 2 decimal places
- ✓ Account format: `ACC-XXXXX` (where X is alphanumeric)
- ✓ Currency must be valid ISO 4217 code (USD, EUR, GBP, JPY, etc.)
- ✓ Transaction type must be: `deposit`, `withdrawal`, or `transfer`
- ✓ From and To accounts must differ for transfers

### Error Response Format
```json
{
  "error": "Validation failed",
  "details": [
    {"field": "amount", "message": "Amount must be a positive number"},
    {"field": "currency", "message": "Invalid currency code"}
  ]
}
```

---

## 🔍 Query Filters

The `GET /transactions` endpoint supports multiple filters that can be combined:

- `?accountId=ACC-12345` - Filter by account
- `?type=transfer` - Filter by transaction type
- `?from=2024-01-01` - Start date (ISO 8601)
- `?to=2024-12-31` - End date (ISO 8601)

Example: `GET /transactions?accountId=ACC-12345&type=transfer&from=2024-01-01`

---

## 📊 Data Models

### Transaction Object
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "fromAccount": "ACC-12345",
  "toAccount": "ACC-67890",
  "amount": 150.50,
  "currency": "USD",
  "type": "transfer",
  "timestamp": "2026-01-25T10:30:00.000Z",
  "status": "completed"
}
```

### Account Summary Object
```json
{
  "accountId": "ACC-12345",
  "totalDeposits": 5000.00,
  "totalWithdrawals": 1500.00,
  "numberOfTransactions": 8,
  "mostRecentTransactionDate": "2026-01-25T10:30:00.000Z",
  "currentBalance": 3500.00
}
```

---

## 🤖 AI-Assisted Development Process

This project was developed with assistance from GitHub Copilot, which helped with:

1. **Code Structure**: Generating the initial project skeleton and module organization
2. **Validation Logic**: Creating comprehensive validation rules and error handling
3. **API Routes**: Building RESTful endpoints with proper HTTP status codes
4. **Error Handling**: Implementing consistent error response patterns
5. **Documentation**: Generating inline code comments and API documentation

**Key Prompts Used:**
- "Create a banking API with transaction management"
- "Add validation for transaction data"
- "Implement filtering for transactions"
- "Add account summary endpoint"
- "Export transactions as CSV"

---

<div align="center">

*This project was completed as part of the AI-Assisted Development course.*

</div>
