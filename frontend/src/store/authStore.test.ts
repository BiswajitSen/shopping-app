import { describe, it, expect, beforeEach } from 'vitest';
import { useAuthStore } from './authStore';
import { UserRole } from '../types';

// Reset store before each test
beforeEach(() => {
  useAuthStore.setState({
    user: null,
    token: null,
    isAuthenticated: false,
  });
  localStorage.clear();
});

describe('authStore', () => {
  it('should have null user initially', () => {
    const { user } = useAuthStore.getState();
    expect(user).toBeNull();
  });

  it('should have null token initially', () => {
    const { token } = useAuthStore.getState();
    expect(token).toBeNull();
  });

  it('should not be authenticated initially', () => {
    const { isAuthenticated } = useAuthStore.getState();
    expect(isAuthenticated).toBe(false);
  });

  it('should set user and token correctly', () => {
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

    const state = useAuthStore.getState();
    expect(state.isAuthenticated).toBe(true);
    expect(state.user?.email).toBe('test@example.com');
    expect(state.token).toBe('test-token');
  });

  it('should clear user and token on logout', () => {
    // Setup authenticated state
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

    // Logout
    const { logout } = useAuthStore.getState();
    logout();

    const state = useAuthStore.getState();
    expect(state.user).toBeNull();
    expect(state.token).toBeNull();
    expect(state.isAuthenticated).toBe(false);
  });

  it('isAdmin should return true for admin users', () => {
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
      isAuthenticated: true,
    });

    const { isAdmin } = useAuthStore.getState();
    expect(isAdmin()).toBe(true);
  });

  it('isAdmin should return false for non-admin users', () => {
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
      isAuthenticated: true,
    });

    const { isAdmin } = useAuthStore.getState();
    expect(isAdmin()).toBe(false);
  });

  it('isVendor should return true for vendor users', () => {
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
      isAuthenticated: true,
    });

    const { isVendor } = useAuthStore.getState();
    expect(isVendor()).toBe(true);
  });

  it('isVendor should return false for non-vendor users', () => {
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
      isAuthenticated: true,
    });

    const { isVendor } = useAuthStore.getState();
    expect(isVendor()).toBe(false);
  });

  it('role checks should return false when user is null', () => {
    const { isAdmin, isVendor, isUser } = useAuthStore.getState();
    
    expect(isAdmin()).toBe(false);
    expect(isVendor()).toBe(false);
    expect(isUser()).toBe(false);
  });

  it('setState should update user information', () => {
    const newUser = {
      id: 'user-456',
      email: 'new@example.com',
      firstName: 'New',
      lastName: 'User',
      roles: [UserRole.USER] as UserRole[],
      createdAt: '',
      updatedAt: '',
    };

    useAuthStore.setState({ user: newUser });

    const state = useAuthStore.getState();
    expect(state.user?.id).toBe('user-456');
    expect(state.user?.email).toBe('new@example.com');
  });
});
