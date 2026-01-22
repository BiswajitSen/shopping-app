import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '../test/test-utils';
import { LoginPage } from './LoginPage';
import { useAuthStore } from '../store/authStore';

// Mock react-router-dom navigate
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

// Reset stores before each test
beforeEach(() => {
  useAuthStore.setState({
    user: null,
    token: null,
    isAuthenticated: false,
  });
  mockNavigate.mockClear();
});

describe('LoginPage', () => {
  it('should render login form', () => {
    render(<LoginPage />);
    
    expect(screen.getByText('Welcome back')).toBeInTheDocument();
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument();
  });

  it('should render link to register page', () => {
    render(<LoginPage />);
    
    expect(screen.getByText(/sign up/i)).toBeInTheDocument();
  });

  it('should show demo accounts information', () => {
    render(<LoginPage />);
    
    expect(screen.getByText(/demo accounts/i)).toBeInTheDocument();
  });

  it('should update email input value', () => {
    render(<LoginPage />);
    
    const emailInput = screen.getByLabelText(/email/i);
    fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
    
    expect(emailInput).toHaveValue('test@example.com');
  });

  it('should update password input value', () => {
    render(<LoginPage />);
    
    const passwordInput = screen.getByLabelText(/password/i);
    fireEvent.change(passwordInput, { target: { value: 'password123' } });
    
    expect(passwordInput).toHaveValue('password123');
  });

  it('should show error for invalid credentials', async () => {
    render(<LoginPage />);
    
    const emailInput = screen.getByLabelText(/email/i);
    const passwordInput = screen.getByLabelText(/password/i);
    const submitButton = screen.getByRole('button', { name: /sign in/i });
    
    fireEvent.change(emailInput, { target: { value: 'wrong@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'wrongpassword' } });
    fireEvent.click(submitButton);
    
    // Wait for error message
    await waitFor(() => {
      expect(screen.getByText(/invalid email or password/i)).toBeInTheDocument();
    }, { timeout: 3000 });
  });

  it('should have remember me checkbox', () => {
    render(<LoginPage />);
    
    const checkbox = screen.getByLabelText(/remember me/i);
    expect(checkbox).toBeInTheDocument();
    expect(checkbox).toHaveAttribute('type', 'checkbox');
  });

  it('should have password input type', () => {
    render(<LoginPage />);
    
    const passwordInput = screen.getByLabelText(/password/i);
    expect(passwordInput).toHaveAttribute('type', 'password');
  });
});
