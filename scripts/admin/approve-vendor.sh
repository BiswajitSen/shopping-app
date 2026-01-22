#!/bin/bash

# Approve a pending vendor (admin only)
# Usage: ./approve-vendor.sh [admin_token] [vendor_id]

BASE_URL=${BASE_URL:-"http://localhost:8080"}
ACCESS_TOKEN=${1:-""}
VENDOR_ID=${2:-""}

if [ -z "$ACCESS_TOKEN" ]; then
    echo "❌ Error: Admin access token required"
    echo "💡 Usage: ./approve-vendor.sh <admin_token> <vendor_id>"
    exit 1
fi

if [ -z "$VENDOR_ID" ]; then
    echo "❌ Error: Vendor ID required"
    echo "💡 Get vendor IDs from: ./scripts/admin/list-pending-vendors.sh"
    exit 1
fi

echo "👑 Approving vendor (Admin)"
echo "🏪 Vendor ID: $VENDOR_ID"
echo ""

RESPONSE=$(curl -s -X POST "$BASE_URL/api/admin/vendors/$VENDOR_ID/approve" \
  -H "Authorization: Bearer $ACCESS_TOKEN")

echo "📊 Response:"
echo "$RESPONSE" | jq .

STATUS=$(echo "$RESPONSE" | jq -r '.data.status // empty')
if [ "$STATUS" = "APPROVED" ]; then
    echo ""
    echo "✅ Vendor approved successfully!"
    echo "🏪 Vendor can now create products"
fi