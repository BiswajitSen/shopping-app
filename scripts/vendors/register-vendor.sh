#!/bin/bash

# Register as vendor
# Usage: ./register-vendor.sh [access_token]

BASE_URL=${BASE_URL:-"http://localhost:8080"}
ACCESS_TOKEN=${1:-""}

if [ -z "$ACCESS_TOKEN" ]; then
    echo "❌ Error: Access token required"
    echo "💡 Usage: ./register-vendor.sh <access_token>"
    echo "💡 Get token from: ./scripts/auth/login.sh"
    exit 1
fi

BUSINESS_NAME="Test Store $(date +%s)"
DESCRIPTION="A test vendor store"
CONTACT_EMAIL="vendor$(date +%s)@store.com"
CONTACT_PHONE="+1234567890"

echo "🏪 Registering as vendor"
echo "📊 Business: $BUSINESS_NAME"
echo ""

RESPONSE=$(curl -s -X POST "$BASE_URL/api/vendors/register" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"businessName\": \"$BUSINESS_NAME\",
    \"description\": \"$DESCRIPTION\",
    \"contactEmail\": \"$CONTACT_EMAIL\",
    \"contactPhone\": \"$CONTACT_PHONE\"
  }")

echo "📊 Response:"
echo "$RESPONSE" | jq .

STATUS=$(echo "$RESPONSE" | jq -r '.data.status // empty')
if [ "$STATUS" = "PENDING" ]; then
    echo ""
    echo "⏳ Vendor registration submitted!"
    echo "📋 Status: PENDING (requires admin approval)"
fi