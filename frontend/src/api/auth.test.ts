import { describe, it, expect, beforeEach } from 'vitest';
import { authApi } from './auth';
import { server } from '../test/mocks/server';
import { http, HttpResponse } from 'msw';

const API_URL = 'http://localhost:8080/api';

describe('authApi', () => {
  describe('login', () => {
    it('should return auth response on successful login', async () => {
      const response = await authApi.login({
        email: 'test@example.com',
        password: 'password123',
      });

      expect(response).toBeDefined();
      expect(response.accessToken).toBe('mock-access-token');
      expect(response.user.email).toBe('test@example.com');
    });

    it('should throw error on invalid credentials', async () => {
      await expect(
        authApi.login({
          email: 'wrong@example.com',
          password: 'wrongpassword',
        })
      ).rejects.toThrow();
    });
  });

  describe('register', () => {
    it('should return auth response on successful registration', async () => {
      const response = await authApi.register({
        email: 'new@example.com',
        password: 'password123',
        firstName: 'New',
        lastName: 'User',
      });

      expect(response).toBeDefined();
      expect(response.accessToken).toBeDefined();
    });

    it('should throw error when email already exists', async () => {
      await expect(
        authApi.register({
          email: 'existing@example.com',
          password: 'password123',
          firstName: 'Existing',
          lastName: 'User',
        })
      ).rejects.toThrow();
    });
  });

  describe('refreshToken', () => {
    it('should return new access token', async () => {
      const response = await authApi.refreshToken('old-refresh-token');

      expect(response).toBeDefined();
      expect(response.accessToken).toBe('new-mock-access-token');
    });

    it('should handle invalid refresh token', async () => {
      server.use(
        http.post(`${API_URL}/auth/refresh`, () => {
          return HttpResponse.json(
            { success: false, message: 'Invalid refresh token' },
            { status: 401 }
          );
        })
      );

      await expect(authApi.refreshToken('invalid-token')).rejects.toThrow();
    });
  });
});
