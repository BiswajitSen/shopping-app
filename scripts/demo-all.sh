#!/bin/bash

# Comprehensive API testing demonstration
# This script shows how to use all the testing scripts

BASE_URL=${BASE_URL:-"http://localhost:8080"}

echo "🚀 Shopping App API Testing Demo"
echo "================================"
echo ""

# Check if app is running
echo "1. Checking if app is running..."
if curl -s --max-time 5 "$BASE_URL/api/products" >/dev/null 2>&1; then
    echo "✅ App is running at $BASE_URL"
else
    echo "❌ App is not running!"
    echo "💡 Start it with: ./gradlew bootRun"
    exit 1
fi

echo ""

# Show available scripts
echo "2. Available Testing Scripts:"
echo "----------------------------"
echo "📁 scripts/"
echo "├── 🔐 auth/"
echo "│   ├── register.sh          # User registration"
echo "│   └── login.sh             # User login"
echo "├── 👤 users/"
echo "│   └── get-profile.sh       # Get user profile"
echo "├── 🏪 vendors/"
echo "│   ├── register-vendor.sh   # Register as vendor"
echo "│   └── get-vendor-profile.sh # Get vendor profile"
echo "├── 📦 products/"
echo "│   ├── create-product.sh    # Create product"
echo "│   └── list-products.sh     # List products"
echo "├── 🛒 orders/"
echo "│   └── create-order.sh      # Create order"
echo "├── 👑 admin/"
echo "│   ├── list-pending-vendors.sh  # List pending vendors"
echo "│   ├── list-pending-products.sh # List pending products"
echo "│   ├── approve-vendor.sh        # Approve vendor"
echo "│   └── approve-product.sh       # Approve product"
echo "└── 🚀 workflows/"
echo "    ├── user-journey.sh      # Complete user workflow"
echo "    ├── admin-workflow.sh    # Admin responsibilities"
echo "    └── quick-test.sh        # Basic functionality test"
echo ""

# Quick functionality test
echo "3. Running Quick Functionality Test..."
echo "--------------------------------------"
./scripts/workflows/quick-test.sh

echo ""

# Show usage examples
echo "4. Usage Examples:"
echo "=================="
echo ""
echo "🔐 Authentication:"
echo "   ./scripts/auth/register.sh"
echo "   ./scripts/auth/login.sh demo@example.com password123"
echo ""
echo "👤 User Operations:"
echo "   ./scripts/users/get-profile.sh <access_token>"
echo ""
echo "🏪 Vendor Operations:"
echo "   ./scripts/vendors/register-vendor.sh <access_token>"
echo "   ./scripts/vendors/get-vendor-profile.sh <access_token>"
echo ""
echo "📦 Product Operations:"
echo "   ./scripts/products/create-product.sh <vendor_token>"
echo "   ./scripts/products/list-products.sh"
echo ""
echo "🛒 Order Operations:"
echo "   ./scripts/orders/create-order.sh <user_token> <product_id>"
echo ""
echo "👑 Admin Operations:"
echo "   ./scripts/admin/list-pending-vendors.sh <admin_token>"
echo "   ./scripts/admin/approve-vendor.sh <admin_token> <vendor_id>"
echo ""
echo "🚀 Complete Workflows:"
echo "   ./scripts/workflows/user-journey.sh"
echo "   ./scripts/workflows/admin-workflow.sh"
echo "   ./scripts/run-tests.sh all"
echo ""

echo "5. Quick Start Commands:"
echo "========================"
echo ""
echo "# Test everything at once:"
echo "./scripts/run-tests.sh all"
echo ""
echo "# Test specific category:"
echo "./scripts/run-tests.sh auth"
echo ""
echo "# Complete user journey:"
echo "./scripts/workflows/user-journey.sh"
echo ""

echo "🎯 Next Steps:"
echo "=============="
echo ""
echo "1. 🔐 Start with authentication: ./scripts/auth/login.sh"
echo "2. 🏪 Try vendor registration: ./scripts/vendors/register-vendor.sh"
echo "3. 📦 Create products: ./scripts/products/create-product.sh"
echo "4. 👑 Admin approval needed: ./scripts/admin/approve-vendor.sh"
echo "5. 🛒 Test ordering: ./scripts/orders/create-order.sh"
echo ""
echo "📚 Full documentation: scripts/README.md"
echo "🌐 Swagger UI: http://localhost:8080/swagger-ui/index.html"
echo ""

echo "✅ Demo Complete! Your API testing toolkit is ready to use."echo ""