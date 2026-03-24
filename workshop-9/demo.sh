#!/usr/bin/env bash
# demo.sh — End-to-end banking pipeline demo
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

API_URL="http://localhost:5001"
SERVER_PID=""

cleanup() {
  if [[ -n "$SERVER_PID" ]]; then
    echo ""
    echo "Stopping API server (PID $SERVER_PID)..."
    kill "$SERVER_PID" 2>/dev/null || true
    wait "$SERVER_PID" 2>/dev/null || true
    echo "Server stopped."
  fi
}
trap cleanup EXIT

# ---------------------------------------------------------------------------
# 1. Clear previous results
# ---------------------------------------------------------------------------
echo "=== Banking Pipeline Demo ==="
echo ""
echo "Clearing previous results..."
rm -rf shared/results shared/input shared/output shared/processing
mkdir -p shared/results shared/input shared/output shared/processing

# ---------------------------------------------------------------------------
# 2. Start API server
# ---------------------------------------------------------------------------
echo "Starting API server on port 5001..."
python api_gateway.py > shared/api_server.log 2>&1 &
SERVER_PID=$!

# Wait for server to be ready
echo -n "Waiting for server"
for i in $(seq 1 15); do
  if curl -s "$API_URL/api/health" > /dev/null 2>&1; then
    echo " ready."
    break
  fi
  echo -n "."
  sleep 1
  if [[ $i -eq 15 ]]; then
    echo " TIMEOUT — check shared/api_server.log"
    exit 1
  fi
done

# ---------------------------------------------------------------------------
# 3. Submit test transactions via HTTP POST
# ---------------------------------------------------------------------------
echo ""
echo "Submitting 3 test transactions..."
echo ""

# TXN001 — normal transfer
RESP1=$(curl -s -w "\n%{http_code}" -X POST "$API_URL/api/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "transaction_id": "TXN001",
    "timestamp": "2026-03-24T09:00:00Z",
    "source_account": "ACC-1001",
    "destination_account": "ACC-2001",
    "amount": "1500.00",
    "currency": "USD",
    "transaction_type": "transfer",
    "description": "Monthly rent payment",
    "metadata": {"channel": "online", "country": "US"}
  }')
HTTP_CODE1=$(echo "$RESP1" | tail -1)
BODY1=$(echo "$RESP1" | sed '$d')
TRACKING1=$(echo "$BODY1" | python3 -c "import sys,json; print(json.load(sys.stdin)['tracking_id'])" 2>/dev/null || echo "TXN001")
echo "  POST TXN001 (\$1,500 USD)   -> HTTP $HTTP_CODE1  tracking_id=$TRACKING1"

# TXN002 — high-value (should trigger fraud flag)
RESP2=$(curl -s -w "\n%{http_code}" -X POST "$API_URL/api/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "transaction_id": "TXN002",
    "timestamp": "2026-03-24T09:15:00Z",
    "source_account": "ACC-1002",
    "destination_account": "ACC-3001",
    "amount": "75000.00",
    "currency": "USD",
    "transaction_type": "wire_transfer",
    "description": "Property settlement",
    "metadata": {"channel": "branch", "country": "US"}
  }')
HTTP_CODE2=$(echo "$RESP2" | tail -1)
BODY2=$(echo "$RESP2" | sed '$d')
TRACKING2=$(echo "$BODY2" | python3 -c "import sys,json; print(json.load(sys.stdin)['tracking_id'])" 2>/dev/null || echo "TXN002")
echo "  POST TXN002 (\$75,000 USD)  -> HTTP $HTTP_CODE2  tracking_id=$TRACKING2"

# TXN003 — blocked currency (should be rejected)
RESP3=$(curl -s -w "\n%{http_code}" -X POST "$API_URL/api/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "transaction_id": "TXN003",
    "timestamp": "2026-03-24T10:05:00Z",
    "source_account": "ACC-1006",
    "destination_account": "ACC-7700",
    "amount": "200.00",
    "currency": "XYZ",
    "transaction_type": "transfer",
    "description": "Test payment",
    "metadata": {"channel": "online", "country": "US"}
  }')
HTTP_CODE3=$(echo "$RESP3" | tail -1)
BODY3=$(echo "$RESP3" | sed '$d')
TRACKING3=$(echo "$BODY3" | python3 -c "import sys,json; print(json.load(sys.stdin)['tracking_id'])" 2>/dev/null || echo "TXN003")
echo "  POST TXN003 (\$200 XYZ)     -> HTTP $HTTP_CODE3  tracking_id=$TRACKING3"

# ---------------------------------------------------------------------------
# 4. Demo: error handling — missing fields
# ---------------------------------------------------------------------------
echo ""
echo "Testing error handling (missing fields)..."
RESP_ERR=$(curl -s -w "\n%{http_code}" -X POST "$API_URL/api/transactions" \
  -H "Content-Type: application/json" \
  -d '{"transaction_id": "TXN_BAD"}')
HTTP_ERR=$(echo "$RESP_ERR" | tail -1)
BODY_ERR=$(echo "$RESP_ERR" | sed '$d')
echo "  POST incomplete tx         -> HTTP $HTTP_ERR  $(echo "$BODY_ERR" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('error',''))" 2>/dev/null)"

# Demo: 404 not found
RESP_404=$(curl -s -w "\n%{http_code}" "$API_URL/api/transactions/TXN999/status")
HTTP_404=$(echo "$RESP_404" | tail -1)
echo "  GET TXN999 status          -> HTTP $HTTP_404"

# ---------------------------------------------------------------------------
# 5. Fetch individual statuses
# ---------------------------------------------------------------------------
echo ""
echo "Fetching individual transaction statuses..."
for TID in TXN001 TXN002 TXN003; do
  STATUS_RESP=$(curl -s "$API_URL/api/transactions/$TID/status")
  STATUS=$(echo "$STATUS_RESP" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('status','?'))" 2>/dev/null || echo "?")
  RISK=$(echo "$STATUS_RESP" | python3 -c "import sys,json; d=json.load(sys.stdin)['details']; print(d.get('fraud_risk_level',''))" 2>/dev/null || echo "")
  REJECT=$(echo "$STATUS_RESP" | python3 -c "import sys,json; d=json.load(sys.stdin)['details']; print(d.get('rejection_reason',''))" 2>/dev/null || echo "")
  FLAGGED=$(echo "$STATUS_RESP" | python3 -c "import sys,json; d=json.load(sys.stdin)['details']; print('flagged' if d.get('notification_sent') else '')" 2>/dev/null || echo "")

  DETAIL=""
  [[ -n "$RISK" ]] && DETAIL="risk: $RISK"
  [[ -n "$FLAGGED" ]] && DETAIL="$DETAIL, $FLAGGED"
  [[ -n "$REJECT" ]] && DETAIL="rejected: $REJECT"

  echo "  $TID: $STATUS${DETAIL:+ ($DETAIL)}"
done

# ---------------------------------------------------------------------------
# 6. GET /api/results — full list
# ---------------------------------------------------------------------------
echo ""
echo "Fetching all results from GET /api/results..."
ALL=$(curl -s "$API_URL/api/results")
echo "$ALL" | python3 -c "
import sys, json
results = json.load(sys.stdin)
approved = sum(1 for r in results if r['status'] == 'approved')
rejected = sum(1 for r in results if r['status'] == 'rejected')
flagged  = sum(1 for r in results if r.get('flagged'))
for r in results:
    line = '  {:<8} {}'.format(r['transaction_id'], r['status'].upper())
    if r.get('risk_level'):
        line += ' (risk: {})'.format(r['risk_level'])
    if r.get('flagged'):
        line += ' [FLAGGED]'
    if r.get('rejection_reason'):
        line += ' — ' + r['rejection_reason']
    print(line)
print()
print('Summary: {} approved, {} rejected, {} flagged'.format(approved, rejected, flagged))
" 2>/dev/null || echo "$ALL"

echo ""
echo "=== Demo Complete ==="
