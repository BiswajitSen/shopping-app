import { describe, it, expect, beforeEach } from 'vitest';
import { render, screen, waitFor } from '../test/test-utils';
import { OrdersPage } from './OrdersPage';
import { useAuthStore } from '../store/authStore';
import { UserRole } from '../types';

// Reset stores before each test
beforeEach(() => {
  useAuthStore.setState({
    user: null,
    token: null,
    isAuthenticated: false,
  });
});

describe('OrdersPage', () => {
  it('should show sign in required when not authenticated', () => {
    render(<OrdersPage />);
    
    expect(screen.getByText(/sign in required/i)).toBeInTheDocument();
  });

  it('should show sign in link when not authenticated', () => {
    const { container } = render(<OrdersPage />);
    
    // Should contain a link to sign in
    expect(container.textContent).toMatch(/sign in/i);
  });

  it('should render page title when authenticated', async () => {
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

    render(<OrdersPage />);
    
    await waitFor(() => {
      expect(screen.getByText(/my orders/i)).toBeInTheDocument();
    });
  });

  it('should show live updates indicator when authenticated', async () => {
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

    render(<OrdersPage />);
    
    await waitFor(() => {
      expect(screen.getByText(/live updates/i)).toBeInTheDocument();
    });
  });

  it('should render orders component', async () => {
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

    const { container } = render(<OrdersPage />);
    
    // Should render the orders container
    await waitFor(() => {
      expect(container.querySelector('.max-w-4xl')).toBeTruthy();
    });
  });
});
