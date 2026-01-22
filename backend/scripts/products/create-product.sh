#!/bin/bash

# Create a new product (vendor only)
# Usage: ./create-product.sh [access_token]

BASE_URL=${BASE_URL:-"http://localhost:8080"}
ACCESS_TOKEN=${1:-""}

if [ -z "$ACCESS_TOKEN" ]; then
    echo "❌ Error: Access token required (must be approved vendor)"
    echo "💡 Usage: ./create-product.sh <access_token>"
    echo "💡 Get token from: ./scripts/auth/login.sh"
    exit 1
fi

PRODUCT_NAME="Test Product $(date +%s)"
CATEGORY="Electronics"
PRICE="99.99"
STOCK="50"
DESCRIPTION="A test product for demonstration"

echo "📦 Creating product"
echo "📊 Name: $PRODUCT_NAME"
echo "📊 Category: $CATEGORY"
echo "📊 Price: $$PRICE"
echo ""

RESPONSE=$(curl -s -X POST "$BASE_URL/api/vendors/me/products" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"$PRODUCT_NAME\",
    \"category\": \"$CATEGORY\",
    \"price\": $PRICE,
    \"stock\": $STOCK,
    \"description\": \"$DESCRIPTION\",
    \"images\": [\"image1.jpg\", \"image2.jpg\"]
  }")

echo "📊 Response:"
echo "$RESPONSE" | jq .

STATUS=$(echo "$RESPONSE" | jq -r '.data.status // empty')
if [ "$STATUS" = "PENDING" ]; then
    echo ""
    echo "⏳ Product created!"
    echo "📋 Status: PENDING (requires admin approval to be visible)"
fi