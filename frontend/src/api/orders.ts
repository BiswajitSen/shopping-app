import apiClient from './client';
import { Order, CreateOrderRequest, Page } from '../types';

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export const ordersApi = {
  create: async (data: CreateOrderRequest): Promise<Order> => {
    const response = await apiClient.post<ApiResponse<Order>>('/orders', data);
    return response.data.data;
  },

  getById: async (id: string): Promise<Order> => {
    const response = await apiClient.get<ApiResponse<Order>>(`/orders/${id}`);
    return response.data.data;
  },

  getUserOrders: async (page = 0, size = 10): Promise<Page<Order>> => {
    const response = await apiClient.get<ApiResponse<Page<Order>>>(`/orders?page=${page}&size=${size}`);
    return response.data.data;
  },

  cancel: async (id: string): Promise<Order> => {
    const response = await apiClient.post<ApiResponse<Order>>(`/orders/${id}/cancel`);
    return response.data.data;
  },

  // Vendor endpoints
  getVendorOrders: async (page = 0, size = 10): Promise<Page<Order>> => {
    const response = await apiClient.get<ApiResponse<Page<Order>>>(`/orders/vendor?page=${page}&size=${size}`);
    return response.data.data;
  },

  updateOrderStatus: async (id: string, status: string): Promise<Order> => {
    const response = await apiClient.put<ApiResponse<Order>>(`/orders/${id}/status`, { status });
    return response.data.data;
  },
};
