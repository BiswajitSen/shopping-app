import { describe, it, expect } from 'vitest';
import { render } from '../../test/test-utils';
import { LoadingSpinner, PageLoader } from './LoadingSpinner';

describe('LoadingSpinner', () => {
  it('should render spinner with animation', () => {
    const { container } = render(<LoadingSpinner />);
    
    const spinner = container.querySelector('.animate-spin');
    expect(spinner).toBeTruthy();
  });

  it('should render with custom size', () => {
    const { container } = render(<LoadingSpinner size="lg" />);
    
    const spinner = container.querySelector('.animate-spin');
    expect(spinner).toBeTruthy();
  });
});

describe('PageLoader', () => {
  it('should render full page loader with spinner', () => {
    const { container } = render(<PageLoader />);
    
    const spinner = container.querySelector('.animate-spin');
    expect(spinner).toBeTruthy();
  });

  it('should have flex centering', () => {
    const { container } = render(<PageLoader />);
    
    const flexContainer = container.querySelector('.flex');
    expect(flexContainer).toBeTruthy();
  });
});
