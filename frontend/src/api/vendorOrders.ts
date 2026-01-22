import { Order, OrderStatus, Page, UpdateOrderStatusRequest } from '../types';
import apiClient from './client';

interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
  timestamp: string;
}

export const vendorOrdersApi = {
  // Get all orders for the vendor
  getOrders: async (page = 0, size = 20, status?: OrderStatus): Promise<Page<Order>> => {
    const params: Record<string, string | number> = { page, size };
    if (status) {
      params.status = status;
    }
    const response = await apiClient.get<ApiResponse<Page<Order>>>('/vendor/orders', { params });
    return response.data.data;
  },

  // Get a specific order
  getOrder: async (orderId: string): Promise<Order> => {
    const response = await apiClient.get<ApiResponse<Order>>(`/vendor/orders/${orderId}`);
    return response.data.data;
  },

  // Update order status
  updateStatus: async (orderId: string, request: UpdateOrderStatusRequest): Promise<Order> => {
    const response = await apiClient.patch<ApiResponse<Order>>(
      `/vendor/orders/${orderId}/status`,
      request
    );
    return response.data.data;
  },

  // Get available statuses
  getAvailableStatuses: async (): Promise<OrderStatus[]> => {
    const response = await apiClient.get<ApiResponse<OrderStatus[]>>('/vendor/orders/statuses');
    return response.data.data;
  },
};

export default vendorOrdersApi;
