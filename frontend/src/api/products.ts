import apiClient from './client';
import { Product, CreateProductRequest, Page } from '../types';

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export const productsApi = {
  getAll: async (page = 0, size = 12, category?: string): Promise<Page<Product>> => {
    const params = new URLSearchParams();
    params.append('page', page.toString());
    params.append('size', size.toString());
    if (category) {
      params.append('category', category);
    }
    const response = await apiClient.get<ApiResponse<Page<Product>>>(`/products?${params.toString()}`);
    return response.data.data;
  },

  getById: async (id: string): Promise<Product> => {
    const response = await apiClient.get<ApiResponse<Product>>(`/products/${id}`);
    return response.data.data;
  },

  getByCategory: async (category: string, page = 0, size = 12): Promise<Page<Product>> => {
    const response = await apiClient.get<ApiResponse<Page<Product>>>(
      `/products/category/${category}?page=${page}&size=${size}`
    );
    return response.data.data;
  },

  search: async (query: string, page = 0, size = 12): Promise<Page<Product>> => {
    const response = await apiClient.get<ApiResponse<Page<Product>>>(
      `/products/search?q=${encodeURIComponent(query)}&page=${page}&size=${size}`
    );
    return response.data.data;
  },

  // Vendor endpoints
  getVendorProducts: async (page = 0, size = 12): Promise<Page<Product>> => {
    const response = await apiClient.get<ApiResponse<Page<Product>>>(
      `/vendors/me/products?page=${page}&size=${size}`
    );
    return response.data.data;
  },

  create: async (data: CreateProductRequest): Promise<Product> => {
    const response = await apiClient.post<ApiResponse<Product>>('/vendors/me/products', data);
    return response.data.data;
  },

  update: async (id: string, data: Partial<CreateProductRequest>): Promise<Product> => {
    const response = await apiClient.put<ApiResponse<Product>>(`/vendors/me/products/${id}`, data);
    return response.data.data;
  },

  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`/vendors/me/products/${id}`);
  },

  // Bulk upload
  bulkUpload: async (file: File): Promise<BulkUploadResponse> => {
    const formData = new FormData();
    formData.append('file', file);
    
    const response = await apiClient.post<ApiResponse<BulkUploadResponse>>(
      '/vendors/me/products/bulk-upload',
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    );
    return response.data.data;
  },

  downloadTemplate: (): string => {
    return '/api/vendors/me/products/bulk-upload/template';
  },
};

export interface BulkUploadResponse {
  totalRows: number;
  successCount: number;
  failureCount: number;
  successfulProducts: Product[];
  errors: BulkUploadError[];
}

export interface BulkUploadError {
  rowNumber: number;
  productName: string;
  errorMessage: string;
}
