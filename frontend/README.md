# Shopping App Frontend

A modern React-based e-commerce frontend for the multi-vendor shopping platform.

## Tech Stack

- **React 18** - UI library
- **TypeScript** - Type safety
- **Vite** - Build tool and dev server
- **Tailwind CSS** - Utility-first CSS framework
- **Zustand** - State management
- **React Query** - Server state management
- **React Router** - Client-side routing
- **Axios** - HTTP client
- **Lucide React** - Icons

## Prerequisites

- Node.js 20.x (use `nvm use 20` if you have nvm installed)
- npm 10.x or higher

## Getting Started

### 1. Install Dependencies

```bash
cd frontend
npm install
```

### 2. Environment Setup

Create a `.env` file in the frontend directory:

```env
VITE_API_URL=http://localhost:8080/api
```

### 3. Start Development Server

```bash
npm run dev
```

The app will be available at `http://localhost:3000`

## Available Scripts

| Script | Description |
|--------|-------------|
| `npm run dev` | Start development server on port 3000 |
| `npm run build` | Build for production |
| `npm run preview` | Preview production build |
| `npm run test` | Run tests in watch mode |
| `npm run test:run` | Run tests once |
| `npm run test:coverage` | Run tests with coverage report |

## Project Structure

```
frontend/
├── src/
│   ├── api/                 # API client modules
│   │   ├── auth.ts          # Authentication API
│   │   ├── client.ts        # Axios client configuration
│   │   ├── orders.ts        # Orders API
│   │   ├── payments.ts      # Payments API
│   │   ├── products.ts      # Products API
│   │   ├── users.ts         # Users API
│   │   ├── vendors.ts       # Vendors API
│   │   └── vendorOrders.ts  # Vendor order management API
│   │
│   ├── components/          # Reusable components
│   │   ├── common/          # Generic components (LoadingSpinner, etc.)
│   │   ├── layout/          # Layout components (Header, Footer, etc.)
│   │   ├── products/        # Product-related components
│   │   └── vendor/          # Vendor-specific components
│   │
│   ├── pages/               # Page components
│   │   ├── admin/           # Admin dashboard pages
│   │   ├── vendor/          # Vendor dashboard pages
│   │   ├── HomePage.tsx
│   │   ├── LoginPage.tsx
│   │   ├── RegisterPage.tsx
│   │   ├── ProductsPage.tsx
│   │   ├── ProductDetailPage.tsx
│   │   ├── CheckoutPage.tsx
│   │   ├── OrdersPage.tsx
│   │   └── ProfilePage.tsx
│   │
│   ├── store/               # Zustand state stores
│   │   ├── authStore.ts     # Authentication state
│   │   └── cartStore.ts     # Shopping cart state
│   │
│   ├── test/                # Test configuration and utilities
│   │   ├── mocks/           # MSW mock handlers
│   │   ├── setup.ts         # Test setup
│   │   └── test-utils.tsx   # Custom render utilities
│   │
│   ├── types/               # TypeScript type definitions
│   │   └── index.ts
│   │
│   ├── App.tsx              # Main app component with routes
│   ├── main.tsx             # App entry point
│   └── index.css            # Global styles
│
├── public/                  # Static assets
├── .env                     # Environment variables
├── .nvmrc                   # Node version specification
├── vite.config.ts           # Vite configuration
├── tailwind.config.js       # Tailwind CSS configuration
├── tsconfig.json            # TypeScript configuration
└── package.json
```

## Features

### For Customers
- Browse and search products
- View product details
- Add products to cart
- Checkout with shipping address
- Multiple payment methods (Card, Cash on Delivery)
- View and track orders
- Real-time order status updates

### For Vendors
- Register as a vendor
- Manage products (CRUD)
- Bulk upload products via CSV
- View and manage orders
- Update order statuses
- Schedule deliveries

### For Admins
- Approve/reject vendor registrations
- Approve/reject product listings
- Platform oversight

## Testing

The project uses **Vitest** with **React Testing Library** for testing and **MSW** (Mock Service Worker) for API mocking.

### Run Tests

```bash
# Watch mode (re-runs on file changes)
npm run test

# Single run
npm run test:run

# With coverage report
npm run test:coverage
```

### Test Coverage

Tests cover:
- **Stores**: `authStore`, `cartStore`
- **API Clients**: `auth`, `products`, `orders`, `vendorOrders`
- **Components**: `Header`, `ProductCard`, `LoadingSpinner`
- **Pages**: `LoginPage`, `ProductsPage`, `OrdersPage`

## Building for Production

### Standard Build

```bash
npm run build
```

Output will be in the `dist/` directory.

### Build for Single-Origin Deployment (with Backend)

Use the build script to deploy frontend with the Spring Boot backend:

```bash
cd ..  # Go to project root
./build-for-ngrok.sh
```

This will:
1. Build the frontend with API URL set to `/api`
2. Copy built files to `backend/src/main/resources/static/`
3. Allow serving both frontend and backend from a single origin

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `VITE_API_URL` | Backend API base URL | `http://localhost:8080/api` |

## Demo Accounts

When running with the backend, these accounts are available:

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@shopapp.com | admin123 |
| Vendor | vendor@shopapp.com | vendor123 |
| User | user@shopapp.com | user123 |

## API Integration

The frontend expects the backend API to return responses in this format:

```json
{
  "success": true,
  "message": "Success",
  "data": { ... },
  "timestamp": "2024-01-20T10:00:00Z"
}
```

For paginated responses:

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "content": [...],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5
  },
  "timestamp": "2024-01-20T10:00:00Z"
}
```

## Troubleshooting

### CORS Issues

If you encounter CORS errors, ensure the backend has the correct CORS configuration for your frontend URL.

### Node Version Issues

Use Node.js 20:

```bash
nvm use 20
```

Or install Node 20 if not available:

```bash
nvm install 20
nvm use 20
```

### API Connection Issues

1. Verify backend is running on port 8080
2. Check `.env` file has correct `VITE_API_URL`
3. Restart the dev server after changing environment variables

## Contributing

1. Follow existing code style
2. Write tests for new features
3. Ensure all tests pass before submitting
4. Use meaningful commit messages
