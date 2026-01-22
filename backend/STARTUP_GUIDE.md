# Shopping App - Startup and Testing Guide

## Overview

This guide provides step-by-step instructions to start the Shopping App and test all its features. The app is a multi-vendor eCommerce platform built with Spring Boot, featuring JWT authentication, MongoDB database, and comprehensive API testing.

## Prerequisites

### 1. Java 21
```bash
# Check Java version
java -version

# Expected output:
# openjdk version "21.0.9" 2024-01-16 LTS
# OpenJDK Runtime Environment Temurin-21.0.9+8 (build 21.0.9+8-LTS)
# OpenJDK 64-Bit Server VM Temurin-21.0.9+8 (build 21.0.9+8-LTS, mixed mode)
```

### 2. Environment Configuration
```bash
# Copy the environment template
cp .env.example .env

# Edit the .env file with your values
nano .env  # or use your preferred editor

# Required environment variables:
# MONGODB_URI - MongoDB connection string
# JWT_SECRET - Secure JWT signing key (min 256 bits)
# SERVER_PORT - Application port (default: 8080)
```

### 3. MongoDB
The app requires MongoDB running on localhost:27017.

#### Option A: Docker (Recommended)
```bash
# Start MongoDB in Docker
docker run -d \
  --name mongodb-test \
  -p 27017:27017 \
  -v mongodb_data:/data/db \
  mongo:latest

# Check if MongoDB is running
docker ps | grep mongodb-test
```

#### Option B: Local Installation
```bash
# macOS with Homebrew
brew install mongodb-community
brew services start mongodb-community

# Check MongoDB status
brew services list | grep mongodb
```

### 3. Gradle
```bash
# Check Gradle version
./gradlew --version

# Expected: Gradle 8.5 or higher
```

## Starting the Application

### 1. Build the Application
```bash
# Clean and build
./gradlew clean build

# Or just build (faster)
./gradlew build
```

### 2. Start the Application
```bash
# Start in development mode
./gradlew bootRun

# Application will start on http://localhost:8080
```

### 3. Verify Application Startup
```bash
# Check if app is running
curl -s http://localhost:8080/ | head -5

# Expected: 401 Unauthorized (authentication required)
# {"status":401,"error":"Unauthorized","message":"You need to login to access this resource","path":"/","timestamp":"..."}

# Check Swagger UI
curl -s http://localhost:8080/swagger-ui/index.html | head -3

# Expected: HTML content starting with "<!-- HTML for static distribution bundle build -->"
```

## Testing the Application

### 1. Run All Tests
```bash
# Run complete test suite
./gradlew test

# Expected: BUILD SUCCESSFUL with 180+ tests passing
```

### 2. Run Specific Test Categories
```bash
# Unit tests only
./gradlew test --tests "*Test" --exclude-tests "*EndToEnd*"

# Integration tests only
./gradlew test --tests "*EndToEndIntegrationTest*"

# Authentication tests
./gradlew test --tests "*EndToEndIntegrationTest*AuthenticationFlow*"

# Vendor management tests
./gradlew test --tests "*EndToEndIntegrationTest*VendorFlow*"

# Product management tests
./gradlew test --tests "*EndToEndIntegrationTest*ProductFlow*"

# Admin operations tests
./gradlew test --tests "*EndToEndIntegrationTest*AdminOperations*"
```

### 3. Manual API Testing

#### A. User Registration and Authentication
```bash
# 1. Register a new user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User"
  }'

# Expected: 201 Created with JWT tokens
# {
#   "success": true,
#   "data": {
#     "accessToken": "eyJ...",
#     "refreshToken": "dummy-token-...",
#     "tokenType": "Bearer",
#     "expiresIn": 900,
#     "user": {
#       "id": "...",
#       "email": "test@example.com",
#       "roles": ["USER"]
#     }
#   }
# }

# 2. Get user profile (use access token from registration)
ACCESS_TOKEN="your_access_token_here"

curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# Expected: 200 OK with user details
```

#### B. Vendor Registration Flow
```bash
# 1. Register as vendor (using user access token)
curl -X POST http://localhost:8080/api/vendors/register \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "businessName": "Test Electronics Store",
    "description": "Best electronics in town",
    "contactEmail": "vendor@teststore.com",
    "contactPhone": "+1234567890"
  }'

# Expected: 201 Created - PENDING status initially

# 2. Admin approves vendor (need admin token - see below)
ADMIN_TOKEN="admin_access_token"

curl -X POST http://localhost:8080/api/admin/vendors/{vendor_id}/approve \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# 3. Check vendor profile after approval
curl -X GET http://localhost:8080/api/vendors/me \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# Expected: APPROVED status
```

#### C. Admin User Creation
```bash
# Register admin user (manual process for testing)
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "admin123",
    "firstName": "Admin",
    "lastName": "User"
  }'

# Then manually update user roles in MongoDB or use admin endpoints
# (In production, this would be handled by a database script)
```

#### D. Product Management
```bash
# 1. Create product (vendor must be approved first)
curl -X POST http://localhost:8080/api/vendors/me/products \
  -H "Authorization: Bearer $VENDOR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "iPhone 15 Pro",
    "category": "Electronics",
    "price": 999.99,
    "stock": 50,
    "description": "Latest iPhone with A17 Pro chip",
    "images": ["image1.jpg", "image2.jpg"]
  }'

# Expected: 201 Created - PENDING status initially

# 2. Admin approves product
curl -X POST http://localhost:8080/api/admin/products/{product_id}/approve \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# 3. Browse approved products
curl -X GET http://localhost:8080/api/products

# 4. Search products
curl -X GET "http://localhost:8080/api/products/search?keyword=iPhone"
```

#### E. Order and Payment Flow
```bash
# 1. Create order
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {
        "productId": "product_id_here",
        "quantity": 2
      }
    ],
    "shippingAddress": {
      "fullName": "Test User",
      "addressLine1": "123 Test Street",
      "city": "Test City",
      "state": "TS",
      "postalCode": "12345",
      "country": "USA",
      "phoneNumber": "+1234567890"
    }
  }'

# 2. Initiate payment
curl -X POST http://localhost:8080/api/payments/initiate \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "order_id_here",
    "paymentMethod": "CARD"
  }'

# 3. Process payment (simulate success)
curl -X POST http://localhost:8080/api/payments/process \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "paymentId": "payment_id_here",
    "simulateSuccess": true
  }'

# 4. Check order status (should be CONFIRMED)
curl -X GET http://localhost:8080/api/orders/{order_id} \
  -H "Authorization: Bearer $USER_TOKEN"
```

## API Documentation

### Swagger UI
Access the interactive API documentation at:
```
http://localhost:8080/swagger-ui/index.html
```

### OpenAPI Specification
Download the API spec at:
```
http://localhost:8080/v3/api-docs
```

## Application Architecture

### Modules
- **Auth**: User authentication, JWT tokens, registration
- **User**: User profile management, roles
- **Vendor**: Vendor registration, approval workflow
- **Product**: Product CRUD, approval, search
- **Order**: Order creation, status tracking
- **Payment**: Payment processing, integration
- **Admin**: Administrative operations

### Key Features
- JWT-based authentication with access tokens
- Role-based access control (USER, VENDOR, ADMIN)
- Modular monolith architecture
- MongoDB for data persistence
- Comprehensive API testing
- Swagger/OpenAPI documentation

## Troubleshooting

### 1. Application Won't Start
```bash
# Check Java version
java -version

# Check if MongoDB is running
docker ps | grep mongo
# or
brew services list | grep mongodb

# Check application logs
./gradlew bootRun 2>&1 | head -50
```

### 2. Tests Are Failing
```bash
# Clean and rerun tests
./gradlew clean test

# Run tests with more verbose output
./gradlew test --info

# Check test reports
open build/reports/tests/test/index.html
```

### 3. Database Connection Issues
```bash
# Test MongoDB connection
mongosh --eval "db.adminCommand('ping')"

# Check if MongoDB is listening on port 27017
netstat -an | grep 27017

# Restart MongoDB container
docker restart mongodb-test
```

### 4. Port Already in Use
```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>

# Or use different port
./gradlew bootRun --args='--server.port=8081'
```

### 5. Build Issues
```bash
# Clean Gradle cache
./gradlew clean --refresh-dependencies

# Invalidate IntelliJ cache if using IDE
# File > Invalidate Caches / Restart
```

## Environment Configuration

### Environment Variables
Create a `.env` file in the project root (copy from `.env.example`):

```bash
# MongoDB Configuration
MONGODB_URI=mongodb://localhost:27017/shopping_app

# JWT Configuration (Must be at least 256 bits / 32 characters)
JWT_SECRET=YourSuperSecretKeyForJWTTokenGenerationMustBeAtLeast256BitsLong123456789
JWT_ACCESS_TOKEN_EXPIRATION=900000
JWT_REFRESH_TOKEN_EXPIRATION=604800000

# Server Configuration
SERVER_PORT=8080

# Logging Level
LOG_LEVEL_APP=DEBUG
LOG_LEVEL_SECURITY=DEBUG
LOG_LEVEL_MONGODB=DEBUG
```

### Application Properties
The app uses the following default configuration (with environment variable fallbacks):

```properties
# Server
server.port=${SERVER_PORT:8080}

# Database
spring.data.mongodb.uri=${MONGODB_URI:mongodb://localhost:27017/shopping_app}

# JWT
jwt.secret=${JWT_SECRET:fallback-secret-here}
jwt.access-token-expiration=${JWT_ACCESS_TOKEN_EXPIRATION:900000}
jwt.refresh-token-expiration=${JWT_REFRESH_TOKEN_EXPIRATION:604800000}

# Logging
logging.level.com.shopapp=${LOG_LEVEL_APP:DEBUG}
```

### Custom Configuration
Create `application-local.yml` for local overrides:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/shopping_app_dev
```

## Performance Testing

### Load Testing with Apache Bench
```bash
# Simple load test
ab -n 100 -c 10 http://localhost:8080/api/products

# Authenticated endpoint test (replace TOKEN)
ab -n 50 -c 5 \
  -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8080/api/users/me
```

## Development Workflow

### 1. Code Changes
```bash
# Run tests after changes
./gradlew test

# Run specific module tests
./gradlew test --tests "*auth*"
./gradlew test --tests "*product*"
```

### 2. Database Migrations
The app uses MongoDB, so schema changes are handled automatically. For data migrations:

```java
// Add to DataInitializer.java
// Or create migration scripts
```

### 3. Adding New Features
1. Add to appropriate module
2. Write unit tests
3. Add integration tests to EndToEndIntegrationTest.java
4. Update API documentation
5. Test manually

## Production Deployment

### 1. Build Production JAR
```bash
./gradlew bootJar
```

### 2. Run Production JAR
```bash
java -jar build/libs/shopping-app-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --server.port=8080
```

### 3. Environment Variables
```bash
export MONGODB_URI="mongodb://prod-server:27017/shopping_app"
export JWT_SECRET="your-production-secret-here-at-least-256-bits"
export JWT_ACCESS_TOKEN_EXPIRATION=900000
export JWT_REFRESH_TOKEN_EXPIRATION=604800000
export SERVER_PORT=8080
export LOG_LEVEL_APP=INFO
```

## Support

If you encounter issues:

1. Check this guide first
2. Review application logs
3. Run tests to verify functionality
4. Check MongoDB connectivity
5. Verify Java version compatibility

The application includes comprehensive test coverage, so if tests pass, the core functionality is working correctly.