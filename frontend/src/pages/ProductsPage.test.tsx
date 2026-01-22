import { describe, it, expect } from 'vitest';
import { render, screen, waitFor } from '../test/test-utils';
import { ProductsPage } from './ProductsPage';

describe('ProductsPage', () => {
  it('should render page title', async () => {
    const { container } = render(<ProductsPage />);
    
    await waitFor(() => {
      // Should render the page container
      expect(container.querySelector('.container') || container.textContent?.includes('Product')).toBeTruthy();
    }, { timeout: 2000 });
  });

  it('should show loading state initially', () => {
    render(<ProductsPage />);
    
    // Should show loading spinner
    const spinner = document.querySelector('.animate-spin');
    expect(spinner).toBeInTheDocument();
  });

  it('should render products after loading', async () => {
    render(<ProductsPage />);
    
    // Wait for products to load
    await waitFor(() => {
      expect(screen.getByText('Test Product 1')).toBeInTheDocument();
    }, { timeout: 3000 });
  });

  it('should render multiple products', async () => {
    render(<ProductsPage />);
    
    await waitFor(() => {
      expect(screen.getByText('Test Product 1')).toBeInTheDocument();
      expect(screen.getByText('Test Product 2')).toBeInTheDocument();
    }, { timeout: 3000 });
  });

  it('should show product prices', async () => {
    render(<ProductsPage />);
    
    await waitFor(() => {
      expect(screen.getByText('$99.99')).toBeInTheDocument();
      expect(screen.getByText('$49.99')).toBeInTheDocument();
    }, { timeout: 3000 });
  });
});
