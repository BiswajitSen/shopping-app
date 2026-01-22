import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { User, UserRole, AuthResponse } from '../types';
import { authApi } from '../api/auth';
import { usersApi } from '../api/users';

interface AuthState {
  user: User | null;
  token: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  
  // Actions
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string, firstName: string, lastName: string) => Promise<void>;
  logout: () => void;
  fetchUser: () => Promise<void>;
  setAuth: (response: AuthResponse) => void;
  clearError: () => void;
  
  // Role checks
  isAdmin: () => boolean;
  isVendor: () => boolean;
  isUser: () => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      token: null,
      refreshToken: null,
      isAuthenticated: false,
      isLoading: false,
      error: null,

      login: async (email: string, password: string) => {
        set({ isLoading: true, error: null });
        try {
          const response = await authApi.login({ email, password });
          // Set auth state with user info from response
          set({
            token: response.accessToken,
            refreshToken: response.refreshToken,
            isAuthenticated: true,
            user: {
              id: response.user.id,
              email: response.user.email,
              firstName: response.user.firstName,
              lastName: response.user.lastName,
              roles: response.user.roles || [UserRole.USER],
              createdAt: '',
              updatedAt: '',
            },
            isLoading: false,
          });
        } catch (error: unknown) {
          const message = error instanceof Error ? error.message : 'Login failed';
          set({ error: message, isLoading: false });
          throw error;
        }
      },

      register: async (email: string, password: string, firstName: string, lastName: string) => {
        set({ isLoading: true, error: null });
        try {
          const response = await authApi.register({ email, password, firstName, lastName });
          // Set auth state with user info from response
          set({
            token: response.accessToken,
            refreshToken: response.refreshToken,
            isAuthenticated: true,
            user: {
              id: response.user.id,
              email: response.user.email,
              firstName: response.user.firstName,
              lastName: response.user.lastName,
              roles: response.user.roles || [UserRole.USER],
              createdAt: '',
              updatedAt: '',
            },
            isLoading: false,
          });
        } catch (error: unknown) {
          const message = error instanceof Error ? error.message : 'Registration failed';
          set({ error: message, isLoading: false });
          throw error;
        }
      },

      logout: () => {
        set({
          user: null,
          token: null,
          refreshToken: null,
          isAuthenticated: false,
          error: null,
        });
      },

      fetchUser: async () => {
        try {
          const user = await usersApi.getProfile();
          set({ user, isLoading: false });
        } catch (error) {
          set({ isLoading: false });
          throw error;
        }
      },

      setAuth: (response: AuthResponse) => {
        set({
          token: response.accessToken,
          refreshToken: response.refreshToken,
          isAuthenticated: true,
          user: {
            id: response.user.id,
            email: response.user.email,
            firstName: response.user.firstName,
            lastName: response.user.lastName,
            roles: response.user.roles || [UserRole.USER],
            createdAt: '',
            updatedAt: '',
          },
        });
      },

      clearError: () => set({ error: null }),

      isAdmin: () => get().user?.roles?.includes(UserRole.ADMIN) || false,
      isVendor: () => get().user?.roles?.includes(UserRole.VENDOR) || false,
      isUser: () => get().user?.roles?.includes(UserRole.USER) || false,
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        token: state.token,
        refreshToken: state.refreshToken,
        user: state.user,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);
