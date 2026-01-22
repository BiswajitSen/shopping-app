#!/bin/bash

# Register a new user
# Usage: ./register.sh [email] [password] [firstName] [lastName]

BASE_URL=${BASE_URL:-"http://localhost:8080"}

EMAIL=${1:-"test$(date +%s)@example.com"}
PASSWORD=${2:-"password123"}
FIRST_NAME=${3:-"Test"}
LAST_NAME=${4:-"User"}

echo "🔐 Registering user: $EMAIL"
echo "📊 Request:"
echo "   Email: $EMAIL"
echo "   Name: $FIRST_NAME $LAST_NAME"
echo ""

curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"$EMAIL\",
    \"password\": \"$PASSWORD\",
    \"firstName\": \"$FIRST_NAME\",
    \"lastName\": \"$LAST_NAME\"
  }" | jq .

echo ""
echo "💡 Tip: Use the accessToken from response for authenticated requests"