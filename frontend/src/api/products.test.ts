import { describe, it, expect } from 'vitest';
import { productsApi } from './products';
import { server } from '../test/mocks/server';
import { http, HttpResponse } from 'msw';

const API_URL = 'http://localhost:8080/api';

describe('productsApi', () => {
  describe('getAll', () => {
    it('should return paginated products', async () => {
      const response = await productsApi.getAll(0, 20);

      expect(response).toBeDefined();
      expect(response.content).toBeInstanceOf(Array);
      expect(response.content.length).toBeGreaterThan(0);
    });

    it('should handle pagination parameters', async () => {
      const response = await productsApi.getAll(0, 10);

      expect(response.page).toBe(0);
      expect(response.size).toBe(10);
    });
  });

  describe('getById', () => {
    it('should return product by id', async () => {
      const response = await productsApi.getById('product-1');

      expect(response).toBeDefined();
      expect(response.id).toBe('product-1');
      expect(response.name).toBe('Test Product 1');
    });

    it('should throw error for non-existent product', async () => {
      await expect(productsApi.getById('non-existent')).rejects.toThrow();
    });
  });

  describe('getVendorProducts', () => {
    it('should return vendor products', async () => {
      const response = await productsApi.getVendorProducts(0, 20);

      expect(response).toBeDefined();
      expect(response.content).toBeInstanceOf(Array);
    });
  });

  describe('create', () => {
    it('should create a new product', async () => {
      const newProduct = {
        name: 'New Product',
        category: 'Electronics',
        description: 'A new product',
        price: 149.99,
        stock: 20,
        images: ['https://example.com/img.jpg'],
      };

      const response = await productsApi.create(newProduct);

      expect(response).toBeDefined();
      expect(response.name).toBe('New Product');
      expect(response.status).toBe('PENDING');
    });

    it('should handle validation errors', async () => {
      server.use(
        http.post(`${API_URL}/vendors/me/products`, () => {
          return HttpResponse.json(
            { success: false, message: 'Validation failed', errors: { name: 'Name is required' } },
            { status: 400 }
          );
        })
      );

      await expect(
        productsApi.create({
          name: '',
          description: 'Test description',
          category: 'Electronics',
          price: 99.99,
          stock: 10,
        })
      ).rejects.toThrow();
    });
  });

  describe('update', () => {
    it('should update product', async () => {
      server.use(
        http.put(`${API_URL}/vendors/me/products/:id`, async ({ request }) => {
          const body = await request.json() as { name?: string };
          return HttpResponse.json({
            success: true,
            data: {
              id: 'product-1',
              name: body.name || 'Updated Product',
              category: 'Electronics',
              price: 99.99,
              stock: 10,
              status: 'PENDING',
            },
          });
        })
      );

      const response = await productsApi.update('product-1', { name: 'Updated Name' });

      expect(response).toBeDefined();
    });
  });

  describe('delete', () => {
    it('should delete product', async () => {
      await expect(productsApi.delete('product-1')).resolves.not.toThrow();
    });

    it('should handle non-existent product deletion', async () => {
      server.use(
        http.delete(`${API_URL}/vendors/me/products/:id`, () => {
          return HttpResponse.json(
            { success: false, message: 'Product not found' },
            { status: 404 }
          );
        })
      );

      await expect(productsApi.delete('non-existent')).rejects.toThrow();
    });
  });
});
