#!/bin/bash

# Get current user profile
# Usage: ./get-profile.sh [access_token]

BASE_URL=${BASE_URL:-"http://localhost:8080"}
ACCESS_TOKEN=${1:-""}

if [ -z "$ACCESS_TOKEN" ]; then
    echo "❌ Error: Access token required"
    echo "💡 Usage: ./get-profile.sh <access_token>"
    echo "💡 Get token from: ./scripts/auth/login.sh"
    exit 1
fi

echo "👤 Getting user profile"
echo ""

curl -s -X GET "$BASE_URL/api/users/me" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq .