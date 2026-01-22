import { describe, it, expect } from 'vitest';
import { vendorOrdersApi } from './vendorOrders';
import { server } from '../test/mocks/server';
import { http, HttpResponse } from 'msw';
import { OrderStatus } from '../types';

const API_URL = 'http://localhost:8080/api';

describe('vendorOrdersApi', () => {
  describe('getOrders', () => {
    it('should return vendor orders', async () => {
      const response = await vendorOrdersApi.getOrders(0, 20);

      expect(response).toBeDefined();
      expect(response.content).toBeInstanceOf(Array);
    });

    it('should filter by status', async () => {
      const response = await vendorOrdersApi.getOrders(0, 20, OrderStatus.PLACED);

      expect(response).toBeDefined();
    });
  });

  describe('getOrder', () => {
    it('should return order by id', async () => {
      server.use(
        http.get(`${API_URL}/vendor/orders/:id`, () => {
          return HttpResponse.json({
            success: true,
            data: {
              id: 'order-123',
              status: 'PLACED',
              items: [],
              totalAmount: 100,
            },
          });
        })
      );

      const response = await vendorOrdersApi.getOrder('order-123');

      expect(response).toBeDefined();
      expect(response.id).toBe('order-123');
    });
  });

  describe('updateStatus', () => {
    it('should update order status', async () => {
      const response = await vendorOrdersApi.updateStatus('order-123', {
        status: OrderStatus.PREPARING,
      });

      expect(response).toBeDefined();
      expect(response.status).toBe('PREPARING');
    });

    it('should update status with estimated delivery date', async () => {
      const response = await vendorOrdersApi.updateStatus('order-123', {
        status: OrderStatus.DELIVERY_SCHEDULED,
        estimatedDeliveryDate: '2024-02-01',
      });

      expect(response).toBeDefined();
    });

    it('should handle invalid status transition', async () => {
      server.use(
        http.patch(`${API_URL}/vendor/orders/:id/status`, () => {
          return HttpResponse.json(
            { success: false, message: 'Invalid status transition' },
            { status: 400 }
          );
        })
      );

      await expect(
        vendorOrdersApi.updateStatus('order-123', {
          status: OrderStatus.DELIVERED,
        })
      ).rejects.toThrow();
    });
  });

  describe('getAvailableStatuses', () => {
    it('should return available statuses', async () => {
      server.use(
        http.get(`${API_URL}/vendor/orders/statuses`, () => {
          return HttpResponse.json({
            success: true,
            data: [
              'PREPARING',
              'SHIPPED',
              'OUT_FOR_DELIVERY',
              'DELIVERED',
              'DELIVERY_SCHEDULED',
              'CANCELLED',
            ],
          });
        })
      );

      const response = await vendorOrdersApi.getAvailableStatuses();

      expect(response).toBeDefined();
      expect(response).toBeInstanceOf(Array);
      expect(response.length).toBeGreaterThan(0);
    });
  });
});
