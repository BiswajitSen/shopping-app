#!/bin/bash

# Main test runner script
# Usage: ./run-tests.sh [category] [options]

BASE_URL=${BASE_URL:-"http://localhost:8080"}

show_help() {
    echo "🧪 Shopping App API Test Runner"
    echo "==============================="
    echo ""
    echo "Usage: ./run-tests.sh [category] [options]"
    echo ""
    echo "Categories:"
    echo "  all        - Run all tests"
    echo "  auth       - Authentication tests"
    echo "  users      - User management tests"
    echo "  vendors    - Vendor management tests"
    echo "  products   - Product management tests"
    echo "  orders     - Order management tests"
    echo "  admin      - Admin-only tests"
    echo "  workflows  - Complete workflow tests"
    echo ""
    echo "Options:"
    echo "  --help     - Show this help"
    echo "  --verbose  - Show detailed output"
    echo ""
    echo "Examples:"
    echo "  ./run-tests.sh auth          # Run auth tests"
    echo "  ./run-tests.sh all           # Run all tests"
    echo "  ./run-tests.sh workflows     # Run workflow demos"
    echo ""
    echo "Environment:"
    echo "  BASE_URL=$BASE_URL"
}

check_app_running() {
    if ! curl -s --max-time 5 "$BASE_URL/api/products" >/dev/null 2>&1; then
        echo "❌ Error: Application not running at $BASE_URL"
        echo "💡 Start the app: ./gradlew bootRun"
        exit 1
    fi
}

run_auth_tests() {
    echo "🔐 Running Authentication Tests..."
    echo "=================================="

    # Test registration
    echo "1. Testing user registration..."
    ./scripts/auth/register.sh >/dev/null 2>&1 && echo "✅ Registration: PASSED" || echo "❌ Registration: FAILED"

    # Test login
    echo "2. Testing user login..."
    RESPONSE=$(./scripts/auth/login.sh demo@example.com password123 2>/dev/null)
    if echo "$RESPONSE" | grep -q "Login successful"; then
        echo "✅ Login: PASSED"
    else
        echo "❌ Login: FAILED"
    fi

    echo ""
}

run_user_tests() {
    echo "👤 Running User Tests..."
    echo "======================="

    # Get token first
    TOKEN=$(./scripts/auth/login.sh demo@example.com password123 2>/dev/null | grep "Access Token:" | cut -d: -f2 | tr -d ' ')

    if [ -n "$TOKEN" ]; then
        echo "1. Testing get user profile..."
        ./scripts/users/get-profile.sh "$TOKEN" >/dev/null 2>&1 && echo "✅ User Profile: PASSED" || echo "❌ User Profile: FAILED"
    else
        echo "❌ User tests: No valid token available"
    fi

    echo ""
}

run_vendor_tests() {
    echo "🏪 Running Vendor Tests..."
    echo "========================="

    # Get token first
    TOKEN=$(./scripts/auth/login.sh demo@example.com password123 2>/dev/null | grep "Access Token:" | cut -d: -f2 | tr -d ' ')

    if [ -n "$TOKEN" ]; then
        echo "1. Testing vendor registration..."
        ./scripts/vendors/register-vendor.sh "$TOKEN" >/dev/null 2>&1 && echo "✅ Vendor Registration: PASSED" || echo "❌ Vendor Registration: FAILED"

        echo "2. Testing vendor profile..."
        ./scripts/vendors/get-vendor-profile.sh "$TOKEN" >/dev/null 2>&1 && echo "✅ Vendor Profile: PASSED" || echo "❌ Vendor Profile: FAILED"
    else
        echo "❌ Vendor tests: No valid token available"
    fi

    echo ""
}

run_product_tests() {
    echo "📦 Running Product Tests..."
    echo "=========================="

    echo "1. Testing list products..."
    ./scripts/products/list-products.sh >/dev/null 2>&1 && echo "✅ List Products: PASSED" || echo "❌ List Products: FAILED"

    # Note: Product creation requires approved vendor, so we skip that test here
    echo "2. Product creation requires approved vendor (test manually)"
    echo ""
}

run_admin_tests() {
    echo "👑 Running Admin Tests..."
    echo "========================"

    # Note: Admin tests require admin token, which may not exist in demo setup
    echo "Admin tests require admin user with ADMIN role"
    echo "Run manually: ./scripts/admin/list-pending-vendors.sh <admin_token>"
    echo ""
}

run_workflow_tests() {
    echo "🚀 Running Workflow Tests..."
    echo "==========================="

    echo "Running quick test suite..."
    ./scripts/workflows/quick-test.sh
}

run_all_tests() {
    echo "🧪 Running Complete Test Suite"
    echo "=============================="
    echo ""

    check_app_running

    run_auth_tests
    run_user_tests
    run_vendor_tests
    run_product_tests
    run_admin_tests
    run_workflow_tests

    echo "🎯 Test Suite Complete!"
    echo "======================"
    echo "💡 For more detailed testing, run individual scripts manually"
}

# Main logic
case "${1:-all}" in
    "auth")
        check_app_running
        run_auth_tests
        ;;
    "users")
        check_app_running
        run_user_tests
        ;;
    "vendors")
        check_app_running
        run_vendor_tests
        ;;
    "products")
        check_app_running
        run_product_tests
        ;;
    "admin")
        check_app_running
        run_admin_tests
        ;;
    "workflows")
        check_app_running
        run_workflow_tests
        ;;
    "all")
        run_all_tests
        ;;
    "--help"|"-h")
        show_help
        ;;
    *)
        echo "❌ Unknown category: $1"
        echo ""
        show_help
        exit 1
        ;;
esac