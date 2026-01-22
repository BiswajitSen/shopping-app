import apiClient from './client';
import { User } from '../types';

// Backend wraps all responses in ApiResponse
interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export const usersApi = {
  getProfile: async (): Promise<User> => {
    const response = await apiClient.get<ApiResponse<User>>('/users/me');
    return response.data.data;
  },

  updateProfile: async (data: Partial<User>): Promise<User> => {
    const response = await apiClient.put<ApiResponse<User>>('/users/me', data);
    return response.data.data;
  },
};
