# ▶️ How to Run the Application

## 📋 Prerequisites

Before running the application, make sure you have installed:

- **Node.js** (version 14 or higher) - [Download](https://nodejs.org/)
- **npm** (comes with Node.js)

### Verify Installation

```bash
node --version
npm --version
```

---

## 🚀 Quick Start

### Step 1: Navigate to Project Directory

```bash
cd homework-1
```

### Step 2: Install Dependencies

```bash
npm install
```

This will install Express.js and UUID packages required for the API.

### Step 3: Start the Application

```bash
npm start
```

Or manually run:

```bash
node src/index.js
```

### Expected Output

```
🏦 Banking Transactions API running on http://localhost:3000
📝 API Documentation:
   - POST /transactions - Create a transaction
   - GET /transactions - List all transactions
   - GET /transactions/:id - Get a transaction
   - GET /accounts/:accountId/balance - Get account balance
   - GET /accounts/:accountId/summary - Get account summary
   - GET /transactions/export?format=csv - Export as CSV
   - GET /health - Health check
```

---

## 🧪 Testing the API

### Option 1: Using cURL (Command Line)

Test the health endpoint:

```bash
curl http://localhost:3000/health
```

Create a transaction:

```bash
curl -X POST http://localhost:3000/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 100.50,
    "currency": "USD",
    "type": "transfer"
  }'
```

Get all transactions:

```bash
curl http://localhost:3000/transactions
```

Get account balance:

```bash
curl http://localhost:3000/accounts/ACC-12345/balance
```

### Option 2: Using VS Code REST Client Extension

Install the REST Client extension, then open [demo/sample-requests.http](demo/sample-requests.http) and click "Send Request" above each request.

### Option 3: Using Postman

1. Install [Postman](https://www.postman.com/)
2. Create a new collection
3. Import requests from [demo/sample-requests.http](demo/sample-requests.http)
4. Test the endpoints

### Option 4: Run Sample Script

Make the script executable and run it:

```bash
chmod +x demo/sample-requests.sh
./demo/sample-requests.sh
```

This will run a comprehensive test suite showing all API features.

---

## 📝 API Examples

### Create a Transfer

```bash
curl -X POST http://localhost:3000/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-11111",
    "toAccount": "ACC-22222",
    "amount": 250.75,
    "currency": "USD",
    "type": "transfer"
  }'
```

**Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "fromAccount": "ACC-11111",
  "toAccount": "ACC-22222",
  "amount": 250.75,
  "currency": "USD",
  "type": "transfer",
  "timestamp": "2026-01-25T10:30:00.000Z",
  "status": "completed"
}
```

### Get Transactions with Filters

Filter by account:
```bash
curl "http://localhost:3000/transactions?accountId=ACC-12345"
```

Filter by type:
```bash
curl "http://localhost:3000/transactions?type=transfer"
```

Filter by date range:
```bash
curl "http://localhost:3000/transactions?from=2024-01-01&to=2026-12-31"
```

Combine filters:
```bash
curl "http://localhost:3000/transactions?accountId=ACC-12345&type=transfer&from=2024-01-01"
```

### Get Account Summary

```bash
curl http://localhost:3000/accounts/ACC-12345/summary
```

**Response:**
```json
{
  "accountId": "ACC-12345",
  "totalDeposits": 1000.00,
  "totalWithdrawals": 250.75,
  "numberOfTransactions": 3,
  "mostRecentTransactionDate": "2026-01-25T10:30:00.000Z",
  "currentBalance": 749.25
}
```

### Export Transactions as CSV

```bash
curl "http://localhost:3000/transactions/export?format=csv" > transactions.csv
```

---

## ❌ Validation Examples

### Invalid Amount (Negative)

```bash
curl -X POST http://localhost:3000/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": -100,
    "currency": "USD",
    "type": "transfer"
  }'
```

**Response (400 Bad Request):**
```json
{
  "error": "Validation failed",
  "details": [
    {
      "field": "amount",
      "message": "Amount must be a positive number"
    }
  ]
}
```

### Invalid Account Format

```bash
curl -X POST http://localhost:3000/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "INVALID",
    "toAccount": "ACC-67890",
    "amount": 100,
    "currency": "USD",
    "type": "transfer"
  }'
```

**Response (400 Bad Request):**
```json
{
  "error": "Validation failed",
  "details": [
    {
      "field": "fromAccount",
      "message": "Account number must follow format ACC-XXXXX (where X is alphanumeric)"
    }
  ]
}
```

### Invalid Currency

```bash
curl -X POST http://localhost:3000/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 100,
    "currency": "XYZ",
    "type": "transfer"
  }'
```

**Response (400 Bad Request):**
```json
{
  "error": "Validation failed",
  "details": [
    {
      "field": "currency",
      "message": "Invalid currency code. Use valid ISO 4217 codes (e.g., USD, EUR, GBP)"
    }
  ]
}
```

---

## 🛑 Stopping the Application

Press `Ctrl + C` in your terminal to stop the running API server.

---

## 📦 Project Structure

```
homework-1/
├── package.json           # Node.js dependencies
├── .gitignore            # Git ignore file
├── README.md             # Project documentation
├── HOWTORUN.md          # This file
├── src/
│   ├── index.js          # Main application entry
│   ├── routes.js         # API route handlers
│   ├── transaction.js    # Transaction model & logic
│   └── validators.js     # Validation utilities
└── demo/
    ├── run.sh            # Startup script
    ├── sample-requests.sh # Test script with cURL
    ├── sample-requests.http # REST Client format
    └── sample-data.json  # Sample data
```

---

## 🐛 Troubleshooting

### Port Already in Use

If port 3000 is already in use, you can specify a different port:

```bash
PORT=3001 npm start
```

### Node Modules Not Installed

Make sure to run:

```bash
npm install
```

### Permission Denied on run.sh

Make the script executable:

```bash
chmod +x demo/run.sh
chmod +x demo/sample-requests.sh
```

### EACCES Error

If you get permission errors on macOS/Linux, use sudo:

```bash
sudo npm install
sudo npm start
```

---

## 📞 Support

For issues or questions, refer to:
- [README.md](README.md) - Project overview and features
- [TASKS.md](TASKS.md) - Assignment requirements
- Source code comments in `src/` directory
