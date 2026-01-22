# API Testing Scripts

This directory contains comprehensive scripts for testing all Shopping App API endpoints.

## Directory Structure

```
scripts/
├── auth/              # Authentication scripts
├── users/             # User management scripts
├── vendors/           # Vendor management scripts
├── products/          # Product management scripts
├── orders/            # Order management scripts
├── admin/             # Admin-only scripts
├── workflows/         # Complete workflow demonstrations
└── README.md         # This file
```

## Prerequisites

1. **Start the application:**
   ```bash
   ./gradlew bootRun
   ```

2. **Ensure MongoDB is running:**
   ```bash
   docker ps | grep mongodb-test
   ```

3. **Set environment variables (optional):**
   ```bash
   export BASE_URL="http://localhost:8080"
   ```

## Quick Start

### 1. Run Quick Test Suite
```bash
./scripts/workflows/quick-test.sh
```

### 2. Complete User Journey
```bash
./scripts/workflows/user-journey.sh
```

### 3. Manual Testing Steps

#### Register & Login
```bash
# Register new user
./scripts/auth/register.sh

# Login existing user
./scripts/auth/login.sh demo@example.com password123

# Get user profile
./scripts/users/get-profile.sh <access_token>
```

#### Vendor Management
```bash
# Register as vendor
./scripts/vendors/register-vendor.sh <access_token>

# Get vendor profile
./scripts/vendors/get-vendor-profile.sh <access_token>
```

#### Product Management
```bash
# Create product (requires approved vendor)
./scripts/products/create-product.sh <vendor_token>

# List public products
./scripts/products/list-products.sh
```

#### Admin Operations
```bash
# List pending vendors (requires admin token)
./scripts/admin/list-pending-vendors.sh <admin_token>

# Approve vendor
./scripts/admin/approve-vendor.sh <admin_token> <vendor_id>

# Approve product
./scripts/admin/approve-product.sh <admin_token> <product_id>
```

#### Order Management
```bash
# Create order
./scripts/orders/create-order.sh <user_token> <product_id> <quantity>
```

## Script Details

### Authentication Scripts (`auth/`)

| Script | Purpose | Parameters |
|--------|---------|------------|
| `register.sh` | Register new user | `[email] [password] [firstName] [lastName]` |
| `login.sh` | Login user & get tokens | `[email] [password]` |

### User Scripts (`users/`)

| Script | Purpose | Parameters |
|--------|---------|------------|
| `get-profile.sh` | Get current user profile | `<access_token>` |

### Vendor Scripts (`vendors/`)

| Script | Purpose | Parameters |
|--------|---------|------------|
| `register-vendor.sh` | Register as vendor | `<access_token>` |
| `get-vendor-profile.sh` | Get vendor profile | `<access_token>` |

### Product Scripts (`products/`)

| Script | Purpose | Parameters |
|--------|---------|------------|
| `create-product.sh` | Create new product | `<vendor_token>` |
| `list-products.sh` | List approved products | `[page] [size] [search]` |

### Admin Scripts (`admin/`)

| Script | Purpose | Parameters |
|--------|---------|------------|
| `list-pending-vendors.sh` | List vendors awaiting approval | `<admin_token>` |
| `approve-vendor.sh` | Approve pending vendor | `<admin_token> <vendor_id>` |
| `approve-product.sh` | Approve pending product | `<admin_token> <product_id>` |

### Workflow Scripts (`workflows/`)

| Script | Purpose | Description |
|--------|---------|-------------|
| `user-journey.sh` | Complete user workflow | Register → Vendor → Product |
| `admin-workflow.sh` | Admin responsibilities guide | Platform management overview |
| `quick-test.sh` | Automated test suite | Tests basic API functionality |

## Usage Examples

### Example 1: Complete User Registration Flow
```bash
# 1. Register user and get token
TOKEN=$(./scripts/auth/register.sh | jq -r '.data.accessToken')

# 2. Register as vendor
./scripts/vendors/register-vendor.sh $TOKEN

# 3. Create product (requires admin approval first)
./scripts/products/create-product.sh $TOKEN

# 4. View public products
./scripts/products/list-products.sh
```

### Example 2: Admin Approval Workflow
```bash
# Get admin token (admin user must exist)
ADMIN_TOKEN=$(./scripts/auth/login.sh admin@example.com admin123 | jq -r '.data.accessToken')

# Check pending vendors
./scripts/admin/list-pending-vendors.sh $ADMIN_TOKEN

# Approve a vendor (replace with actual vendor ID)
./scripts/admin/approve-vendor.sh $ADMIN_TOKEN 507f1f77bcf86cd799439011
```

### Example 3: Order Creation
```bash
# Get user token
USER_TOKEN=$(./scripts/auth/login.sh demo@example.com password123 | jq -r '.data.accessToken')

# Create order (replace with actual product ID)
./scripts/orders/create-order.sh $USER_TOKEN 507f1f77bcf86cd799439011 2
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `BASE_URL` | `http://localhost:8080` | API base URL |

## Error Handling

- **401 Unauthorized**: Token missing/invalid - authenticate first
- **403 Forbidden**: Insufficient permissions - check user role
- **409 Conflict**: Resource already exists (duplicate email, etc.)
- **404 Not Found**: Resource doesn't exist
- **400 Bad Request**: Invalid request data

## Tips

1. **Token Management**: Save tokens to variables for reuse
   ```bash
   TOKEN=$(./scripts/auth/login.sh user@example.com password123 | jq -r '.data.accessToken')
   ```

2. **Admin Access**: Use admin credentials to approve vendors/products
   ```bash
   ADMIN_TOKEN=$(./scripts/auth/login.sh admin@example.com admin123 | jq -r '.data.accessToken')
   ```

3. **Workflow Order**: Follow this sequence for complete testing:
   - User registration → Vendor registration → Product creation
   - Admin approvals → Public product visibility → Order creation

4. **Debugging**: Add `-v` flag to curl commands for detailed output
   ```bash
   curl -v -X GET "$BASE_URL/api/users/me" -H "Authorization: Bearer $TOKEN"
   ```

## Contributing

When adding new scripts:
1. Use descriptive filenames with `.sh` extension
2. Include usage comments and parameter validation
3. Use consistent error handling and output formatting
4. Update this README with new script documentation