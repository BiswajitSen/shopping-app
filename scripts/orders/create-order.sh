#!/bin/bash

# Create a new order
# Usage: ./create-order.sh [access_token] [product_id] [quantity]

BASE_URL=${BASE_URL:-"http://localhost:8080"}
ACCESS_TOKEN=${1:-""}
PRODUCT_ID=${2:-""}
QUANTITY=${3:-"1"}

if [ -z "$ACCESS_TOKEN" ]; then
    echo "❌ Error: Access token required"
    echo "💡 Usage: ./create-order.sh <access_token> [product_id] [quantity]"
    exit 1
fi

if [ -z "$PRODUCT_ID" ]; then
    echo "⚠️  Warning: No product ID provided, using a sample ID"
    echo "💡 To get real product IDs, run: ./scripts/products/list-products.sh"
    PRODUCT_ID="507f1f77bcf86cd799439011"  # Sample MongoDB ObjectId
fi

echo "🛒 Creating order"
echo "📦 Product ID: $PRODUCT_ID"
echo "📊 Quantity: $QUANTITY"
echo ""

RESPONSE=$(curl -s -X POST "$BASE_URL/api/orders" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"items\": [
      {
        \"productId\": \"$PRODUCT_ID\",
        \"quantity\": $QUANTITY
      }
    ],
    \"shippingAddress\": {
      \"fullName\": \"Test User\",
      \"addressLine1\": \"123 Test Street\",
      \"city\": \"Test City\",
      \"state\": \"TS\",
      \"postalCode\": \"12345\",
      \"country\": \"USA\",
      \"phoneNumber\": \"+1234567890\"
    }
  }")

echo "📊 Response:"
echo "$RESPONSE" | jq .

STATUS=$(echo "$RESPONSE" | jq -r '.data.status // empty')
if [ "$STATUS" = "CREATED" ]; then
    echo ""
    echo "✅ Order created successfully!"
fi