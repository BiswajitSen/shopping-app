import { describe, it, expect, beforeEach } from 'vitest';
import { useCartStore } from './cartStore';
import { Product, ProductStatus } from '../types';

// Mock product for testing
const createMockProduct = (id: string, price: number, stock: number = 10): Product => ({
  id,
  vendorId: 'vendor-123',
  name: `Product ${id}`,
  description: 'Test product description',
  category: 'Electronics',
  price,
  stock,
  images: ['https://example.com/img.jpg'],
  status: ProductStatus.APPROVED,
  createdAt: new Date().toISOString(),
  updatedAt: new Date().toISOString(),
});

// Reset store before each test
beforeEach(() => {
  useCartStore.setState({
    items: [],
    isOpen: false,
  });
  localStorage.clear();
});

describe('cartStore', () => {
  describe('initial state', () => {
    it('should have empty items initially', () => {
      const { items } = useCartStore.getState();
      expect(items).toEqual([]);
    });

    it('should have cart closed initially', () => {
      const { isOpen } = useCartStore.getState();
      expect(isOpen).toBe(false);
    });
  });

  describe('addItem', () => {
    it('should add a new item to the cart', () => {
      const product = createMockProduct('1', 99.99);
      
      useCartStore.getState().addItem(product);
      
      const { items } = useCartStore.getState();
      expect(items).toHaveLength(1);
      expect(items[0].product.id).toBe('1');
      expect(items[0].quantity).toBe(1);
    });

    it('should add item with specified quantity', () => {
      const product = createMockProduct('1', 99.99);
      
      useCartStore.getState().addItem(product, 3);
      
      const { items } = useCartStore.getState();
      expect(items[0].quantity).toBe(3);
    });

    it('should increase quantity when adding existing item', () => {
      const product = createMockProduct('1', 99.99);
      
      useCartStore.getState().addItem(product, 2);
      useCartStore.getState().addItem(product, 3);
      
      const { items } = useCartStore.getState();
      expect(items).toHaveLength(1);
      expect(items[0].quantity).toBe(5);
    });

    it('should not exceed stock when adding items', () => {
      const product = createMockProduct('1', 99.99, 5);
      
      useCartStore.getState().addItem(product, 10);
      
      const { items } = useCartStore.getState();
      expect(items[0].quantity).toBe(5); // Limited by stock
    });

    it('should not exceed stock when adding to existing item', () => {
      const product = createMockProduct('1', 99.99, 5);
      
      useCartStore.getState().addItem(product, 3);
      useCartStore.getState().addItem(product, 10);
      
      const { items } = useCartStore.getState();
      expect(items[0].quantity).toBe(5); // Limited by stock
    });

    it('should add multiple different products', () => {
      const product1 = createMockProduct('1', 99.99);
      const product2 = createMockProduct('2', 49.99);
      
      useCartStore.getState().addItem(product1);
      useCartStore.getState().addItem(product2);
      
      const { items } = useCartStore.getState();
      expect(items).toHaveLength(2);
    });
  });

  describe('removeItem', () => {
    it('should remove item from cart', () => {
      const product = createMockProduct('1', 99.99);
      
      useCartStore.getState().addItem(product);
      useCartStore.getState().removeItem('1');
      
      const { items } = useCartStore.getState();
      expect(items).toHaveLength(0);
    });

    it('should only remove specified item', () => {
      const product1 = createMockProduct('1', 99.99);
      const product2 = createMockProduct('2', 49.99);
      
      useCartStore.getState().addItem(product1);
      useCartStore.getState().addItem(product2);
      useCartStore.getState().removeItem('1');
      
      const { items } = useCartStore.getState();
      expect(items).toHaveLength(1);
      expect(items[0].product.id).toBe('2');
    });

    it('should do nothing when removing non-existent item', () => {
      const product = createMockProduct('1', 99.99);
      
      useCartStore.getState().addItem(product);
      useCartStore.getState().removeItem('non-existent');
      
      const { items } = useCartStore.getState();
      expect(items).toHaveLength(1);
    });
  });

  describe('updateQuantity', () => {
    it('should update item quantity', () => {
      const product = createMockProduct('1', 99.99);
      
      useCartStore.getState().addItem(product, 1);
      useCartStore.getState().updateQuantity('1', 5);
      
      const { items } = useCartStore.getState();
      expect(items[0].quantity).toBe(5);
    });

    it('should remove item when quantity is 0 or less', () => {
      const product = createMockProduct('1', 99.99);
      
      useCartStore.getState().addItem(product, 2);
      useCartStore.getState().updateQuantity('1', 0);
      
      const { items } = useCartStore.getState();
      expect(items).toHaveLength(0);
    });

    it('should remove item when quantity is negative', () => {
      const product = createMockProduct('1', 99.99);
      
      useCartStore.getState().addItem(product, 2);
      useCartStore.getState().updateQuantity('1', -1);
      
      const { items } = useCartStore.getState();
      expect(items).toHaveLength(0);
    });

    it('should not exceed stock when updating quantity', () => {
      const product = createMockProduct('1', 99.99, 5);
      
      useCartStore.getState().addItem(product, 1);
      useCartStore.getState().updateQuantity('1', 100);
      
      const { items } = useCartStore.getState();
      expect(items[0].quantity).toBe(5); // Limited by stock
    });
  });

  describe('clearCart', () => {
    it('should remove all items from cart', () => {
      const product1 = createMockProduct('1', 99.99);
      const product2 = createMockProduct('2', 49.99);
      
      useCartStore.getState().addItem(product1);
      useCartStore.getState().addItem(product2);
      useCartStore.getState().clearCart();
      
      const { items } = useCartStore.getState();
      expect(items).toHaveLength(0);
    });
  });

  describe('cart visibility', () => {
    it('toggleCart should toggle isOpen state', () => {
      expect(useCartStore.getState().isOpen).toBe(false);
      
      useCartStore.getState().toggleCart();
      expect(useCartStore.getState().isOpen).toBe(true);
      
      useCartStore.getState().toggleCart();
      expect(useCartStore.getState().isOpen).toBe(false);
    });

    it('openCart should set isOpen to true', () => {
      useCartStore.getState().openCart();
      expect(useCartStore.getState().isOpen).toBe(true);
    });

    it('closeCart should set isOpen to false', () => {
      useCartStore.getState().openCart();
      useCartStore.getState().closeCart();
      expect(useCartStore.getState().isOpen).toBe(false);
    });
  });

  describe('getTotalItems', () => {
    it('should return 0 for empty cart', () => {
      const total = useCartStore.getState().getTotalItems();
      expect(total).toBe(0);
    });

    it('should return total quantity of all items', () => {
      const product1 = createMockProduct('1', 99.99);
      const product2 = createMockProduct('2', 49.99);
      
      useCartStore.getState().addItem(product1, 2);
      useCartStore.getState().addItem(product2, 3);
      
      const total = useCartStore.getState().getTotalItems();
      expect(total).toBe(5);
    });
  });

  describe('getTotalPrice', () => {
    it('should return 0 for empty cart', () => {
      const total = useCartStore.getState().getTotalPrice();
      expect(total).toBe(0);
    });

    it('should calculate total price correctly', () => {
      const product1 = createMockProduct('1', 100);
      const product2 = createMockProduct('2', 50);
      
      useCartStore.getState().addItem(product1, 2); // 200
      useCartStore.getState().addItem(product2, 3); // 150
      
      const total = useCartStore.getState().getTotalPrice();
      expect(total).toBe(350);
    });

    it('should handle decimal prices correctly', () => {
      const product = createMockProduct('1', 99.99);
      
      useCartStore.getState().addItem(product, 2);
      
      const total = useCartStore.getState().getTotalPrice();
      expect(total).toBeCloseTo(199.98, 2);
    });
  });

  describe('getItemQuantity', () => {
    it('should return 0 for non-existent item', () => {
      const quantity = useCartStore.getState().getItemQuantity('non-existent');
      expect(quantity).toBe(0);
    });

    it('should return correct quantity for existing item', () => {
      const product = createMockProduct('1', 99.99);
      
      useCartStore.getState().addItem(product, 5);
      
      const quantity = useCartStore.getState().getItemQuantity('1');
      expect(quantity).toBe(5);
    });
  });

  describe('persistence', () => {
    it('should persist items to localStorage', () => {
      const product = createMockProduct('1', 99.99);
      
      useCartStore.getState().addItem(product, 2);
      
      const storedState = localStorage.getItem('cart-storage');
      expect(storedState).not.toBeNull();
      
      if (storedState) {
        const parsed = JSON.parse(storedState);
        expect(parsed.state.items).toHaveLength(1);
      }
    });
  });
});
