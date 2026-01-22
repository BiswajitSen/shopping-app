#!/bin/bash

# Quick API test suite
# Tests basic functionality of all endpoints

BASE_URL=${BASE_URL:-"http://localhost:8080"}

echo "🧪 Quick API Test Suite"
echo "======================="
echo ""

FAILED=0
PASSED=0

# Function to check test result
check_test() {
    local test_name="$1"
    local response="$2"

    if echo "$response" | jq -e '.success == true or (.status == 401) or (.status == 200)' >/dev/null 2>&1; then
        echo "✅ $test_name: PASSED"
        ((PASSED++))
    else
        echo "❌ $test_name: FAILED"
        ((FAILED++))
    fi
}

echo "Testing Public Endpoints..."
echo "---------------------------"

# Test 1: Health check
RESPONSE=$(curl -s "$BASE_URL/")
if echo "$RESPONSE" | jq -e '.status == 401' >/dev/null 2>&1; then
    echo "✅ Health Check: PASSED (401 Unauthorized as expected)"
    ((PASSED++))
else
    echo "❌ Health Check: FAILED"
    ((FAILED++))
fi

# Test 2: List products
RESPONSE=$(curl -s "$BASE_URL/api/products")
if echo "$RESPONSE" | jq -e '.success == true' >/dev/null 2>&1; then
    echo "✅ List Products: PASSED"
    ((PASSED++))
else
    echo "❌ List Products: FAILED"
    ((FAILED++))
fi

echo ""
echo "Testing Authentication..."
echo "------------------------"

# Test 3: Register user
EMAIL="quicktest$(date +%s)@example.com"
RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"password123\",\"firstName\":\"Quick\",\"lastName\":\"Test\"}")

if echo "$RESPONSE" | jq -e '.success == true' >/dev/null 2>&1; then
    echo "✅ User Registration: PASSED"
    ((PASSED++))

    # Extract token for next tests
    ACCESS_TOKEN=$(echo "$RESPONSE" | jq -r '.data.accessToken // empty')
else
    echo "❌ User Registration: FAILED"
    ((FAILED++))
fi

# Test 4: Login user
if [ -n "$ACCESS_TOKEN" ]; then
    RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
      -H "Content-Type: application/json" \
      -d "{\"email\":\"$EMAIL\",\"password\":\"password123\"}")

    if echo "$RESPONSE" | jq -e '.success == true' >/dev/null 2>&1; then
        echo "✅ User Login: PASSED"
        ((PASSED++))
    else
        echo "❌ User Login: FAILED"
        ((FAILED++))
    fi
fi

# Test 5: Get user profile
if [ -n "$ACCESS_TOKEN" ]; then
    RESPONSE=$(curl -s -X GET "$BASE_URL/api/users/me" \
      -H "Authorization: Bearer $ACCESS_TOKEN")

    if echo "$RESPONSE" | jq -e '.success == true' >/dev/null 2>&1; then
        echo "✅ Get User Profile: PASSED"
        ((PASSED++))
    else
        echo "❌ Get User Profile: FAILED"
        ((FAILED++))
    fi
fi

echo ""
echo "Test Results:"
echo "============="
echo "✅ Passed: $PASSED"
echo "❌ Failed: $FAILED"
echo "📊 Total: $((PASSED + FAILED))"

if [ $FAILED -eq 0 ]; then
    echo ""
    echo "🎉 All tests passed! Your API is working correctly."
else
    echo ""
    echo "⚠️  Some tests failed. Check the API implementation or MongoDB connection."
fi