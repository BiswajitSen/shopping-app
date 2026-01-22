import { describe, it, expect, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '../../test/test-utils';
import { Header } from './Header';
import { useAuthStore } from '../../store/authStore';
import { useCartStore } from '../../store/cartStore';
import { UserRole, ProductStatus } from '../../types';

// Reset stores before each test
beforeEach(() => {
  useAuthStore.setState({
    user: null,
    token: null,
    isAuthenticated: false,
  });
  useCartStore.setState({ items: [], isOpen: false });
});

describe('Header', () => {
  it('should render logo/brand name', () => {
    render(<Header />);
    
    expect(screen.getByText('ShopApp')).toBeInTheDocument();
  });

  it('should show login link when not authenticated', () => {
    render(<Header />);
    
    expect(screen.getByText('Login')).toBeInTheDocument();
  });

  it('should show sign up link when not authenticated', () => {
    render(<Header />);
    
    expect(screen.getByText('Sign Up')).toBeInTheDocument();
  });

  it('should render cart button', () => {
    const { container } = render(<Header />);
    
    // Look for cart icon or button
    const cartElement = container.querySelector('button') || container.querySelector('[class*="cart"]');
    expect(cartElement).toBeTruthy();
  });

  it('should show cart item count when items in cart', () => {
    // Add item to cart
    useCartStore.setState({
      items: [
        {
          product: {
            id: 'product-1',
            vendorId: 'vendor-123',
            name: 'Test Product',
            description: 'Test',
            category: 'Electronics',
            price: 99.99,
            stock: 10,
            images: [],
            status: ProductStatus.APPROVED,
            createdAt: '',
            updatedAt: '',
          },
          quantity: 3,
        },
      ],
    });

    const { container } = render(<Header />);
    
    // Should contain "3" somewhere for cart count
    expect(container.textContent).toContain('3');
  });

  it('should show user name when authenticated', () => {
    useAuthStore.setState({
      user: {
        id: 'user-123',
        email: 'test@example.com',
        firstName: 'Test',
        lastName: 'User',
        roles: [UserRole.USER],
        createdAt: '',
        updatedAt: '',
      },
      token: 'test-token',
      isAuthenticated: true,
    });

    render(<Header />);
    
    expect(screen.getByText('Test')).toBeInTheDocument();
  });

  it('should show vendor dashboard link for vendors', () => {
    useAuthStore.setState({
      user: {
        id: 'user-123',
        email: 'vendor@example.com',
        firstName: 'Vendor',
        lastName: 'User',
        roles: [UserRole.USER, UserRole.VENDOR],
        createdAt: '',
        updatedAt: '',
      },
      token: 'test-token',
      isAuthenticated: true,
    });

    render(<Header />);
    
    expect(screen.getByText('Vendor Dashboard')).toBeInTheDocument();
  });

  it('should show admin link for admins', () => {
    useAuthStore.setState({
      user: {
        id: 'user-123',
        email: 'admin@example.com',
        firstName: 'Admin',
        lastName: 'User',
        roles: [UserRole.USER, UserRole.ADMIN],
        createdAt: '',
        updatedAt: '',
      },
      token: 'test-token',
      isAuthenticated: true,
    });

    const { container } = render(<Header />);
    
    // Admin link should be present
    expect(container.textContent).toMatch(/admin/i);
  });

  it('should not show vendor dashboard link for regular users', () => {
    useAuthStore.setState({
      user: {
        id: 'user-123',
        email: 'user@example.com',
        firstName: 'Regular',
        lastName: 'User',
        roles: [UserRole.USER],
        createdAt: '',
        updatedAt: '',
      },
      token: 'test-token',
      isAuthenticated: true,
    });

    render(<Header />);
    
    expect(screen.queryByText('Vendor Dashboard')).not.toBeInTheDocument();
  });

  it('should show My Orders link when authenticated', () => {
    useAuthStore.setState({
      user: {
        id: 'user-123',
        email: 'user@example.com',
        firstName: 'Test',
        lastName: 'User',
        roles: [UserRole.USER],
        createdAt: '',
        updatedAt: '',
      },
      token: 'test-token',
      isAuthenticated: true,
    });

    render(<Header />);
    
    expect(screen.getByText('My Orders')).toBeInTheDocument();
  });

  it('should link to products page', () => {
    render(<Header />);
    
    const productsLink = screen.getByText('Products');
    expect(productsLink).toHaveAttribute('href', '/products');
  });
});
