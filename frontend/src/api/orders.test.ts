import { describe, it, expect } from 'vitest';
import { ordersApi } from './orders';

describe('ordersApi', () => {
  describe('getUserOrders', () => {
    it('should return user orders', async () => {
      const response = await ordersApi.getUserOrders(0, 20);

      expect(response).toBeDefined();
      expect(response.content).toBeInstanceOf(Array);
    });

    it('should have paged response structure', async () => {
      const response = await ordersApi.getUserOrders(0, 10);

      expect(response).toHaveProperty('content');
      expect(response).toHaveProperty('page');
      expect(response).toHaveProperty('size');
    });
  });

  describe('getById', () => {
    it('should return order by id', async () => {
      const response = await ordersApi.getById('order-123');

      expect(response).toBeDefined();
      expect(response.id).toBe('order-123');
    });
  });

  describe('create', () => {
    it('should create a new order', async () => {
      const orderData = {
        items: [
          { productId: 'product-1', quantity: 2 },
        ],
        shippingAddress: {
          fullName: 'Test User',
          addressLine1: '123 Test St',
          city: 'Test City',
          state: 'TS',
          postalCode: '12345',
          country: 'USA',
          phoneNumber: '1234567890',
        },
      };

      const response = await ordersApi.create(orderData);

      expect(response).toBeDefined();
      expect(response.id).toBeDefined();
    });
  });

  describe('cancel', () => {
    it('should cancel an order', async () => {
      const response = await ordersApi.cancel('order-123');

      expect(response).toBeDefined();
      expect(response.status).toBe('CANCELLED');
    });
  });
});
