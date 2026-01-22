#!/bin/bash

# Complete user journey workflow
# This script demonstrates the full user registration to purchase flow

BASE_URL=${BASE_URL:-"http://localhost:8080"}

echo "🚀 Starting Complete User Journey"
echo "=================================="
echo ""

# Step 1: Register user
echo "Step 1: User Registration"
echo "------------------------"
EMAIL="user$(date +%s)@example.com"
RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"$EMAIL\",
    \"password\": \"password123\",
    \"firstName\": \"Journey\",
    \"lastName\": \"User\"
  }")

ACCESS_TOKEN=$(echo "$RESPONSE" | jq -r '.data.accessToken // empty')

if [ -z "$ACCESS_TOKEN" ] || [ "$ACCESS_TOKEN" = "null" ]; then
    echo "❌ User registration failed!"
    exit 1
fi

echo "✅ User registered: $EMAIL"
echo "🔑 Token: ${ACCESS_TOKEN:0:50}..."
echo ""

# Step 2: Get user profile
echo "Step 2: Get User Profile"
echo "-----------------------"
curl -s -X GET "$BASE_URL/api/users/me" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq '.data'
echo ""

# Step 3: Register as vendor
echo "Step 3: Register as Vendor"
echo "-------------------------"
VENDOR_RESPONSE=$(curl -s -X POST "$BASE_URL/api/vendors/register" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"businessName\": \"Journey Store $(date +%s)\",
    \"description\": \"A complete journey test store\",
    \"contactEmail\": \"journey$(date +%s)@store.com\",
    \"contactPhone\": \"+1234567890\"
  }")

echo "⏳ Vendor registration submitted (status: PENDING)"
echo ""

# Step 4: Get vendor profile
echo "Step 4: Get Vendor Profile"
echo "-------------------------"
curl -s -X GET "$BASE_URL/api/vendors/me" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq '.data'
echo ""

# Step 5: Create product
echo "Step 5: Create Product"
echo "---------------------"
PRODUCT_RESPONSE=$(curl -s -X POST "$BASE_URL/api/vendors/me/products" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Journey Product $(date +%s)\",
    \"category\": \"Electronics\",
    \"price\": 49.99,
    \"stock\": 100,
    \"description\": \"A product created during user journey\",
    \"images\": [\"journey1.jpg\"]
  }")

PRODUCT_ID=$(echo "$PRODUCT_RESPONSE" | jq -r '.data.id // empty')
echo "📦 Product created (ID: $PRODUCT_ID, status: PENDING)"
echo ""

echo "🎯 User Journey Summary"
echo "======================"
echo "✅ User registered and authenticated"
echo "✅ Vendor registration submitted"
echo "✅ Product created and pending approval"
echo ""
echo "📋 Next Steps:"
echo "1. Admin must approve the vendor: ./scripts/admin/approve-vendor.sh <admin_token> <vendor_id>"
echo "2. Admin must approve the product: ./scripts/admin/approve-product.sh <admin_token> $PRODUCT_ID"
echo "3. User can then create orders and complete purchases"
echo ""
echo "🔑 User Access Token: $ACCESS_TOKEN"
echo "📦 Product ID: $PRODUCT_ID"