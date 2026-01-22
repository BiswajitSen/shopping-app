import apiClient from './client';
import { Vendor, Product, Page } from '../types';

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export const adminApi = {
  // Vendor management
  getPendingVendors: async (page = 0, size = 10): Promise<Page<Vendor>> => {
    const response = await apiClient.get<ApiResponse<Page<Vendor>>>(
      `/admin/vendors/pending?page=${page}&size=${size}`
    );
    return response.data.data;
  },

  getAllVendors: async (page = 0, size = 10): Promise<Page<Vendor>> => {
    const response = await apiClient.get<ApiResponse<Page<Vendor>>>(`/admin/vendors?page=${page}&size=${size}`);
    return response.data.data;
  },

  approveVendor: async (vendorId: string): Promise<Vendor> => {
    const response = await apiClient.post<ApiResponse<Vendor>>(`/admin/vendors/${vendorId}/approve`);
    return response.data.data;
  },

  rejectVendor: async (vendorId: string, reason: string): Promise<Vendor> => {
    const response = await apiClient.post<ApiResponse<Vendor>>(`/admin/vendors/${vendorId}/reject`, { reason });
    return response.data.data;
  },

  suspendVendor: async (vendorId: string, reason: string): Promise<Vendor> => {
    const response = await apiClient.post<ApiResponse<Vendor>>(`/admin/vendors/${vendorId}/suspend`, { reason });
    return response.data.data;
  },

  // Product management
  getPendingProducts: async (page = 0, size = 10): Promise<Page<Product>> => {
    const response = await apiClient.get<ApiResponse<Page<Product>>>(
      `/admin/products/pending?page=${page}&size=${size}`
    );
    return response.data.data;
  },

  getAllProducts: async (page = 0, size = 10): Promise<Page<Product>> => {
    const response = await apiClient.get<ApiResponse<Page<Product>>>(
      `/admin/products?page=${page}&size=${size}`
    );
    return response.data.data;
  },

  approveProduct: async (productId: string): Promise<Product> => {
    const response = await apiClient.post<ApiResponse<Product>>(`/admin/products/${productId}/approve`);
    return response.data.data;
  },

  rejectProduct: async (productId: string, reason: string): Promise<Product> => {
    const response = await apiClient.post<ApiResponse<Product>>(`/admin/products/${productId}/reject`, {
      reason,
    });
    return response.data.data;
  },

  // Dashboard stats
  getDashboardStats: async (): Promise<AdminDashboardStats> => {
    const response = await apiClient.get<ApiResponse<AdminDashboardStats>>('/admin/dashboard/stats');
    return response.data.data;
  },
};

export interface AdminDashboardStats {
  totalUsers: number;
  totalVendors: number;
  pendingVendors: number;
  totalProducts: number;
  pendingProducts: number;
  totalOrders: number;
  totalRevenue: number;
}
