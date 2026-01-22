import apiClient from './client';
import { Payment, InitiatePaymentRequest, ProcessPaymentRequest } from '../types';

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export const paymentsApi = {
  initiatePayment: async (data: InitiatePaymentRequest): Promise<Payment> => {
    const response = await apiClient.post<ApiResponse<Payment>>('/payments/initiate', data);
    return response.data.data;
  },

  processPayment: async (data: ProcessPaymentRequest): Promise<Payment> => {
    const response = await apiClient.post<ApiResponse<Payment>>('/payments/process', data);
    return response.data.data;
  },

  getByOrderId: async (orderId: string): Promise<Payment> => {
    const response = await apiClient.get<ApiResponse<Payment>>(`/payments/order/${orderId}`);
    return response.data.data;
  },

  getPaymentHistory: async (page = 0, size = 10): Promise<Payment[]> => {
    const response = await apiClient.get<ApiResponse<Payment[]>>(`/payments?page=${page}&size=${size}`);
    return response.data.data;
  },
};
