#!/bin/bash

# List pending vendors (admin only)
# Usage: ./list-pending-vendors.sh [admin_token]

BASE_URL=${BASE_URL:-"http://localhost:8080"}
ACCESS_TOKEN=${1:-""}

if [ -z "$ACCESS_TOKEN" ]; then
    echo "❌ Error: Admin access token required"
    echo "💡 Usage: ./list-pending-vendors.sh <admin_access_token>"
    echo "💡 Admin tokens have 'ADMIN' role in JWT claims"
    exit 1
fi

echo "👑 Listing pending vendors (Admin)"
echo ""

curl -s -X GET "$BASE_URL/api/admin/vendors/pending" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq .