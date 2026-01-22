#!/bin/bash

# Approve a pending product (admin only)
# Usage: ./approve-product.sh [admin_token] [product_id]

BASE_URL=${BASE_URL:-"http://localhost:8080"}
ACCESS_TOKEN=${1:-""}
PRODUCT_ID=${2:-""}

if [ -z "$ACCESS_TOKEN" ]; then
    echo "❌ Error: Admin access token required"
    echo "💡 Usage: ./approve-product.sh <admin_token> <product_id>"
    exit 1
fi

if [ -z "$PRODUCT_ID" ]; then
    echo "❌ Error: Product ID required"
    echo "💡 Get product IDs from: ./scripts/admin/list-pending-products.sh"
    exit 1
fi

echo "👑 Approving product (Admin)"
echo "📦 Product ID: $PRODUCT_ID"
echo ""

RESPONSE=$(curl -s -X POST "$BASE_URL/api/admin/products/$PRODUCT_ID/approve" \
  -H "Authorization: Bearer $ACCESS_TOKEN")

echo "📊 Response:"
echo "$RESPONSE" | jq .

STATUS=$(echo "$RESPONSE" | jq -r '.data.status // empty')
if [ "$STATUS" = "APPROVED" ]; then
    echo ""
    echo "✅ Product approved successfully!"
    echo "📦 Product is now publicly visible"
fi