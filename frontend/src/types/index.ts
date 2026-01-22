// User Types
export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: UserRole[];
  createdAt: string;
  updatedAt: string;
}

export enum UserRole {
  USER = 'USER',
  VENDOR = 'VENDOR',
  ADMIN = 'ADMIN',
}

// Auth Types
export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: {
    id: string;
    email: string;
    firstName: string;
    lastName: string;
    roles: UserRole[];
  };
}

// Vendor Types
export interface Vendor {
  id: string;
  userId: string;
  businessName: string;
  description: string;
  status: VendorStatus;
  contactEmail: string;
  contactPhone?: string;
  address?: Address;
  createdAt: string;
  updatedAt: string;
}

export enum VendorStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  SUSPENDED = 'SUSPENDED',
}

export interface VendorRegistrationRequest {
  businessName: string;
  description: string;
  contactEmail: string;
  contactPhone?: string;
}

// Product Types
export interface Product {
  id: string;
  vendorId: string;
  name: string;
  description: string;
  category: string;
  price: number;
  stock: number;
  images: string[];
  status: ProductStatus;
  createdAt: string;
  updatedAt: string;
}

export enum ProductStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  OUT_OF_STOCK = 'OUT_OF_STOCK',
}

export interface CreateProductRequest {
  name: string;
  description: string;
  category: string;
  price: number;
  stock: number;
  images?: string[];
}

// Order Types
export interface Order {
  id: string;
  userId: string;
  items: OrderItem[];
  totalAmount: number;
  status: OrderStatus;
  shippingAddress: ShippingAddressRequest;
  cancellationReason?: string;
  statusNote?: string;
  estimatedDeliveryDate?: string;
  createdAt: string;
  confirmedAt?: string;
  cancelledAt?: string;
  shippedAt?: string;
  deliveredAt?: string;
}

export interface OrderItem {
  productId: string;
  productName: string;
  productImage?: string;
  vendorId: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

export enum OrderStatus {
  PLACED = 'PLACED',
  PREPARING = 'PREPARING',
  SHIPPED = 'SHIPPED',
  OUT_FOR_DELIVERY = 'OUT_FOR_DELIVERY',
  DELIVERED = 'DELIVERED',
  DELIVERY_SCHEDULED = 'DELIVERY_SCHEDULED',
  CANCELLED = 'CANCELLED',
  // Legacy statuses for backward compatibility
  CREATED = 'CREATED',
  CONFIRMED = 'CONFIRMED',
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
}

export interface UpdateOrderStatusRequest {
  status: OrderStatus;
  note?: string;
  estimatedDeliveryDate?: string;
}

export interface CreateOrderRequest {
  items: CreateOrderItemRequest[];
  shippingAddress: ShippingAddressRequest;
}

export interface CreateOrderItemRequest {
  productId: string;
  quantity: number;
}

// Address Types
export interface Address {
  fullName: string;
  addressLine1: string;
  addressLine2?: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  phoneNumber: string;
}

export interface ShippingAddressRequest {
  fullName: string;
  addressLine1: string;
  addressLine2?: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  phoneNumber: string;
}

// Payment Types
export interface Payment {
  id: string;
  orderId: string;
  amount: number;
  status: PaymentStatus;
  method: PaymentMethod;
  transactionId?: string;
  createdAt: string;
}

export enum PaymentStatus {
  PENDING = 'PENDING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  REFUNDED = 'REFUNDED',
}

export enum PaymentMethod {
  CREDIT_CARD = 'CREDIT_CARD',
  DEBIT_CARD = 'DEBIT_CARD',
  PAYPAL = 'PAYPAL',
  BANK_TRANSFER = 'BANK_TRANSFER',
  CASH_ON_DELIVERY = 'CASH_ON_DELIVERY',
}

export interface InitiatePaymentRequest {
  orderId: string;
  paymentMethod: string;
}

export interface ProcessPaymentRequest {
  paymentId: string;
  simulateSuccess: boolean;
}

export interface CardDetails {
  cardNumber: string;
  expiryMonth: string;
  expiryYear: string;
  cvv: string;
  cardHolderName: string;
}

// Pagination Types
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  page: number;
  first: boolean;
  last: boolean;
}

// Cart Types (Frontend only)
export interface CartItem {
  product: Product;
  quantity: number;
}

// API Response Types
export interface ApiError {
  message: string;
  status: number;
  timestamp: string;
  path?: string;
}
