#!/bin/bash

# Get current vendor profile
# Usage: ./get-vendor-profile.sh [access_token]

BASE_URL=${BASE_URL:-"http://localhost:8080"}
ACCESS_TOKEN=${1:-""}

if [ -z "$ACCESS_TOKEN" ]; then
    echo "❌ Error: Access token required"
    echo "💡 Usage: ./get-vendor-profile.sh <access_token>"
    echo "💡 Get token from: ./scripts/auth/login.sh"
    exit 1
fi

echo "🏪 Getting vendor profile"
echo ""

curl -s -X GET "$BASE_URL/api/vendors/me" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq .