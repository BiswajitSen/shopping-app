#!/bin/bash

# Login user and get tokens
# Usage: ./login.sh [email] [password]

BASE_URL=${BASE_URL:-"http://localhost:8080"}

EMAIL=${1:-"demo@example.com"}
PASSWORD=${2:-"password123"}

echo "🔑 Logging in user: $EMAIL"
echo ""

RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"$EMAIL\",
    \"password\": \"$PASSWORD\"
  }")

echo "📊 Response:"
echo "$RESPONSE" | jq .

# Extract access token for convenience
ACCESS_TOKEN=$(echo "$RESPONSE" | jq -r '.data.accessToken // empty')

if [ -n "$ACCESS_TOKEN" ] && [ "$ACCESS_TOKEN" != "null" ]; then
    echo ""
    echo "✅ Login successful!"
    echo "🔑 Access Token: $ACCESS_TOKEN"
    echo ""
    echo "💡 Use this token in other requests:"
    echo "   Authorization: Bearer $ACCESS_TOKEN"
else
    echo ""
    echo "❌ Login failed!"
fi