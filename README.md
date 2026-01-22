# ShopApp - Multi-Vendor eCommerce Platform

A full-stack, scalable multi-vendor eCommerce platform with a **Modular Monolith** backend and **React** frontend.

## Project Structure

```
shopping-app/
├── backend/                 # Spring Boot API (Modular Monolith)
│   ├── src/
│   ├── build.gradle
│   ├── gradlew
│   └── README.md
├── frontend/                # React + Vite + TypeScript
│   ├── src/
│   ├── package.json
│   └── vite.config.ts
└── README.md               # This file
```

## Tech Stack

| Layer | Technology |
|-------|------------|
| **Frontend** | React 18, TypeScript, Vite, Tailwind CSS, React Query, Zustand |
| **Backend** | Spring Boot 3.2.5, Java 21, MongoDB, JWT Authentication |
| **Database** | MongoDB |
| **API Style** | REST |

## Quick Start

### Prerequisites

- **Node.js 18+** for frontend
- **Java 21 (JDK)** for backend
- **MongoDB** (local or Docker)

### 1. Start MongoDB

```bash
# Using Docker (recommended)
docker run -d -p 27017:27017 --name mongodb mongo:latest

# Or use local MongoDB installation
```

### 2. Start Backend

```bash
cd backend
cp .env.example .env  # Configure environment variables
./gradlew bootRun
```

Backend runs at: `http://localhost:8080`

### 3. Start Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs at: `http://localhost:3000`

## Features

### For Customers
- Browse and search products
- Add products to cart
- Secure checkout with payment processing
- Order history and tracking
- User profile management

### For Vendors
- Register as a vendor (requires admin approval)
- Add and manage products
- Track sales and orders
- Vendor dashboard with analytics

### For Admins
- Approve/reject vendor applications
- Approve/reject products
- User management
- Platform oversight

## Architecture

### Backend - Modular Monolith

```
┌─────────────────────────────────────────────────────────────────┐
│                        API Layer                                │
│                    (REST Controllers)                           │
└─────────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────────┐
│                         Modules                                  │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐   │
│  │  Auth   │ │  User   │ │ Vendor  │ │ Product │ │  Order  │   │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘   │
│  ┌─────────┐ ┌─────────┐                                        │
│  │ Payment │ │  Admin  │                                        │
│  └─────────┘ └─────────┘                                        │
└─────────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────────┐
│                       MongoDB                                    │
└─────────────────────────────────────────────────────────────────┘
```

### Frontend - Component Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         Pages                                    │
│  Home │ Products │ Checkout │ Profile │ Vendor │ Admin          │
└─────────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────────┐
│                       Components                                 │
│  Layout │ Common │ Products │ Cart │ Auth │ Vendor │ Admin      │
└─────────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────────┐
│                     State Management                             │
│               Zustand (Auth, Cart) + React Query                 │
└─────────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────────┐
│                        API Client                                │
│                     Axios + Interceptors                         │
└─────────────────────────────────────────────────────────────────┘
```

## API Documentation

When the backend is running, access:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/api-docs

## Frontend Pages

| Route | Page | Description |
|-------|------|-------------|
| `/` | Home | Landing page with featured products |
| `/login` | Login | User authentication |
| `/register` | Register | New user registration |
| `/products` | Products | Product catalog with search/filter |
| `/products/:id` | Product Detail | Single product view |
| `/checkout` | Checkout | Cart review and payment |
| `/profile` | Profile | User profile and orders |
| `/vendor/register` | Vendor Registration | Apply to become a vendor |
| `/vendor/dashboard` | Vendor Dashboard | Manage products (vendors only) |
| `/vendor/products/new` | Add Product | Create new product |
| `/admin/dashboard` | Admin Dashboard | Platform management (admins only) |

## Running Tests

### Backend Tests

```bash
cd backend

# Run all tests
./gradlew test

# Run unit tests only
./gradlew test --tests "com.shopapp.*.service.*Test"

# Run integration tests
./gradlew test --tests "com.shopapp.integration.*"
```

### Frontend Tests

```bash
cd frontend

# Run tests (when configured)
npm test
```

## Environment Configuration

### Backend (`backend/.env`)

```env
MONGODB_URI=mongodb://localhost:27017/shopping_app
JWT_SECRET=YourSuperSecretKeyForJWTTokenGenerationMustBeAtLeast256BitsLong123456789
JWT_ACCESS_TOKEN_EXPIRATION=900000
JWT_REFRESH_TOKEN_EXPIRATION=604800000
SERVER_PORT=8080
```

### Frontend (`frontend/.env`)

```env
VITE_API_URL=http://localhost:8080/api
```

## Development Workflow

1. **Start MongoDB** - Required for backend
2. **Start Backend** - API server on port 8080
3. **Start Frontend** - Dev server on port 3000 with hot reload
4. **Make changes** - Frontend auto-reloads, backend requires restart

## Production Build

### Backend

```bash
cd backend
./gradlew build
java -jar build/libs/shopping-app-1.0.0.jar
```

### Frontend

```bash
cd frontend
npm run build
# Serve the dist/ folder with any static file server
```

## Default Accounts

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@shopapp.com | admin123456 |

Register new users through the `/register` page.

## Screenshots

The frontend includes:
- Modern, responsive design with Tailwind CSS
- Clean product cards with quick-add to cart
- Slide-out shopping cart
- Multi-step checkout process
- Role-based dashboards for vendors and admins

## License

MIT License

## Contributing

1. Fork the repository
2. Create a feature branch
3. Write tests for your changes
4. Submit a pull request
