import { http, HttpResponse } from 'msw';

const API_URL = 'http://localhost:8080/api';

// Mock data
export const mockUser = {
  id: 'user-123',
  email: 'test@example.com',
  firstName: 'Test',
  lastName: 'User',
  roles: ['USER'],
};

export const mockVendor = {
  id: 'vendor-123',
  userId: 'user-123',
  businessName: 'Test Store',
  description: 'A test store',
  status: 'APPROVED',
  contactEmail: 'vendor@example.com',
};

export const mockProducts = [
  {
    id: 'product-1',
    name: 'Test Product 1',
    description: 'A test product',
    category: 'Electronics',
    price: 99.99,
    stock: 10,
    images: ['https://example.com/img1.jpg'],
    vendorId: 'vendor-123',
    vendorName: 'Test Store',
    status: 'APPROVED',
  },
  {
    id: 'product-2',
    name: 'Test Product 2',
    description: 'Another test product',
    category: 'Clothing',
    price: 49.99,
    stock: 25,
    images: [],
    vendorId: 'vendor-123',
    vendorName: 'Test Store',
    status: 'APPROVED',
  },
];

export const mockOrders = [
  {
    id: 'order-123',
    userId: 'user-123',
    items: [
      {
        productId: 'product-1',
        productName: 'Test Product 1',
        productImage: 'https://example.com/img1.jpg',
        vendorId: 'vendor-123',
        quantity: 2,
        unitPrice: 99.99,
        subtotal: 199.98,
      },
    ],
    totalAmount: 199.98,
    status: 'PLACED',
    shippingAddress: {
      fullName: 'Test User',
      addressLine1: '123 Test St',
      city: 'Test City',
      state: 'TS',
      postalCode: '12345',
      country: 'USA',
      phoneNumber: '1234567890',
    },
    createdAt: '2024-01-20T10:00:00Z',
  },
];

export const mockAuthResponse = {
  accessToken: 'mock-access-token',
  refreshToken: 'mock-refresh-token',
  tokenType: 'Bearer',
  expiresIn: 900000,
  user: mockUser,
};

// API Response wrapper
const wrapResponse = <T>(data: T, message = 'Success') => ({
  success: true,
  message,
  data,
  timestamp: new Date().toISOString(),
});

const wrapPagedResponse = <T>(content: T[], page = 0, size = 20, totalElements = 0) => ({
  success: true,
  message: 'Success',
  data: {
    content,
    page,
    size,
    totalElements,
    totalPages: Math.ceil(totalElements / size),
  },
  timestamp: new Date().toISOString(),
});

export const handlers = [
  // Auth endpoints
  http.post(`${API_URL}/auth/login`, async ({ request }) => {
    const body = await request.json() as { email: string; password: string };
    
    if (body.email === 'test@example.com' && body.password === 'password123') {
      return HttpResponse.json(wrapResponse(mockAuthResponse, 'Login successful'));
    }
    
    return HttpResponse.json(
      { success: false, message: 'Invalid email or password' },
      { status: 401 }
    );
  }),

  http.post(`${API_URL}/auth/register`, async ({ request }) => {
    const body = await request.json() as { email: string };
    
    if (body.email === 'existing@example.com') {
      return HttpResponse.json(
        { success: false, message: 'Email already exists' },
        { status: 409 }
      );
    }
    
    return HttpResponse.json(wrapResponse(mockAuthResponse, 'Registration successful'));
  }),

  http.post(`${API_URL}/auth/refresh`, () => {
    return HttpResponse.json(wrapResponse({
      accessToken: 'new-mock-access-token',
      tokenType: 'Bearer',
      expiresIn: 900000,
    }));
  }),

  // User endpoints
  http.get(`${API_URL}/users/me`, () => {
    return HttpResponse.json(wrapResponse(mockUser));
  }),

  http.put(`${API_URL}/users/me`, async ({ request }) => {
    const body = await request.json() as Partial<typeof mockUser>;
    return HttpResponse.json(wrapResponse({ ...mockUser, ...body }));
  }),

  // Product endpoints
  http.get(`${API_URL}/products`, ({ request }) => {
    const url = new URL(request.url);
    const page = parseInt(url.searchParams.get('page') || '0');
    const size = parseInt(url.searchParams.get('size') || '20');
    
    return HttpResponse.json(wrapPagedResponse(mockProducts, page, size, mockProducts.length));
  }),

  http.get(`${API_URL}/products/:id`, ({ params }) => {
    const product = mockProducts.find(p => p.id === params.id);
    
    if (!product) {
      return HttpResponse.json(
        { success: false, message: 'Product not found' },
        { status: 404 }
      );
    }
    
    return HttpResponse.json(wrapResponse(product));
  }),

  // Vendor product endpoints
  http.get(`${API_URL}/vendors/me/products`, () => {
    return HttpResponse.json(wrapPagedResponse(mockProducts, 0, 20, mockProducts.length));
  }),

  http.post(`${API_URL}/vendors/me/products`, async ({ request }) => {
    const body = await request.json() as { name: string; category: string; price: number; stock: number };
    const newProduct = {
      id: 'product-new',
      ...body,
      images: [],
      vendorId: 'vendor-123',
      vendorName: 'Test Store',
      status: 'PENDING',
    };
    return HttpResponse.json(wrapResponse(newProduct, 'Product created successfully'));
  }),

  http.delete(`${API_URL}/vendors/me/products/:id`, () => {
    return HttpResponse.json(wrapResponse(null, 'Product deleted successfully'));
  }),

  // Vendor endpoints
  http.get(`${API_URL}/vendors/me`, () => {
    return HttpResponse.json(wrapResponse(mockVendor));
  }),

  http.post(`${API_URL}/vendors/register`, async ({ request }) => {
    const body = await request.json() as { businessName: string };
    return HttpResponse.json(wrapResponse({
      ...mockVendor,
      ...body,
      status: 'PENDING',
    }, 'Vendor registration submitted'));
  }),

  // Order endpoints
  http.get(`${API_URL}/orders`, () => {
    return HttpResponse.json(wrapPagedResponse(mockOrders, 0, 20, mockOrders.length));
  }),

  http.get(`${API_URL}/orders/:id`, ({ params }) => {
    const order = mockOrders.find(o => o.id === params.id);
    
    if (!order) {
      return HttpResponse.json(
        { success: false, message: 'Order not found' },
        { status: 404 }
      );
    }
    
    return HttpResponse.json(wrapResponse(order));
  }),

  http.post(`${API_URL}/orders`, async ({ request }) => {
    const body = await request.json() as { items: Array<{ productId: string; quantity: number }>; shippingAddress: object };
    const newOrder = {
      id: 'order-new',
      userId: 'user-123',
      items: body.items.map(item => ({
        ...item,
        productName: 'Test Product',
        productImage: '',
        vendorId: 'vendor-123',
        unitPrice: 99.99,
        subtotal: 99.99 * item.quantity,
      })),
      totalAmount: body.items.reduce((sum, item) => sum + 99.99 * item.quantity, 0),
      status: 'PLACED',
      shippingAddress: body.shippingAddress,
      createdAt: new Date().toISOString(),
    };
    return HttpResponse.json(wrapResponse(newOrder, 'Order created successfully'));
  }),

  http.post(`${API_URL}/orders/:id/cancel`, ({ params }) => {
    const order = mockOrders.find(o => o.id === params.id);
    if (order) {
      return HttpResponse.json(wrapResponse({ ...order, status: 'CANCELLED' }));
    }
    return HttpResponse.json({ success: false, message: 'Order not found' }, { status: 404 });
  }),

  // Vendor order endpoints
  http.get(`${API_URL}/vendor/orders`, () => {
    return HttpResponse.json(wrapPagedResponse(mockOrders, 0, 20, mockOrders.length));
  }),

  http.patch(`${API_URL}/vendor/orders/:id/status`, async ({ params, request }) => {
    const body = await request.json() as { status: string };
    const order = mockOrders.find(o => o.id === params.id);
    
    if (order) {
      return HttpResponse.json(wrapResponse({ ...order, status: body.status }));
    }
    return HttpResponse.json({ success: false, message: 'Order not found' }, { status: 404 });
  }),

  // Payment endpoints
  http.post(`${API_URL}/payments/initiate`, async ({ request }) => {
    const body = await request.json() as { orderId: string; paymentMethod: string };
    return HttpResponse.json(wrapResponse({
      id: 'payment-123',
      orderId: body.orderId,
      amount: 199.98,
      status: 'PENDING',
      paymentMethod: body.paymentMethod,
    }));
  }),

  http.post(`${API_URL}/payments/process`, async ({ request }) => {
    const body = await request.json() as { paymentId: string };
    return HttpResponse.json(wrapResponse({
      id: body.paymentId,
      status: 'SUCCESS',
      transactionId: 'txn-123',
    }));
  }),

  // Admin endpoints
  http.get(`${API_URL}/admin/vendors/pending`, () => {
    return HttpResponse.json(wrapPagedResponse([{ ...mockVendor, status: 'PENDING' }], 0, 20, 1));
  }),

  http.get(`${API_URL}/admin/products/pending`, () => {
    return HttpResponse.json(wrapPagedResponse([{ ...mockProducts[0], status: 'PENDING' }], 0, 20, 1));
  }),

  http.post(`${API_URL}/admin/vendors/:id/approve`, ({ params }) => {
    return HttpResponse.json(wrapResponse({ ...mockVendor, id: params.id, status: 'APPROVED' }));
  }),

  http.post(`${API_URL}/admin/products/:id/approve`, ({ params }) => {
    return HttpResponse.json(wrapResponse({ ...mockProducts[0], id: params.id, status: 'APPROVED' }));
  }),
];
