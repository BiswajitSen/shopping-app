import apiClient from './client';
import { Vendor, VendorRegistrationRequest } from '../types';

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export const vendorsApi = {
  register: async (data: VendorRegistrationRequest): Promise<Vendor> => {
    const response = await apiClient.post<ApiResponse<Vendor>>('/vendors/register', data);
    return response.data.data;
  },

  getProfile: async (): Promise<Vendor> => {
    const response = await apiClient.get<ApiResponse<Vendor>>('/vendors/me');
    return response.data.data;
  },

  updateProfile: async (data: Partial<VendorRegistrationRequest>): Promise<Vendor> => {
    const response = await apiClient.put<ApiResponse<Vendor>>('/vendors/me', data);
    return response.data.data;
  },

  getDashboardStats: async (): Promise<VendorDashboardStats> => {
    const response = await apiClient.get<ApiResponse<VendorDashboardStats>>('/vendors/dashboard/stats');
    return response.data.data;
  },
};

export interface VendorDashboardStats {
  totalProducts: number;
  pendingProducts: number;
  totalOrders: number;
  pendingOrders: number;
  totalRevenue: number;
}
