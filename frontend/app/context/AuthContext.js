'use client';

import { createContext, useContext, useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import axios from 'axios';

const AuthContext = createContext({});

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {
    // Check if user is logged in on mount
    checkAuth();
    // Install a global 401 handler
    const interceptorId = axios.interceptors.response.use(
      (resp) => resp,
      async (error) => {
        const originalRequest = error.config;
        
        // If 401 and we haven't tried to refresh yet
        if (error?.response?.status === 401 && !originalRequest._retry) {
          originalRequest._retry = true;
          
          try {
            const newToken = await refreshAccessToken();
            originalRequest.headers['Authorization'] = `Bearer ${newToken}`;
            return axios(originalRequest);
          } catch (refreshError) {
            // Refresh failed, logout
            logout();
            return Promise.reject(refreshError);
          }
        }
        
        // For other errors or if refresh also failed
        if (error?.response?.status === 401) {
          logout();
        }
        
        return Promise.reject(error);
      }
    );
    return () => {
      axios.interceptors.response.eject(interceptorId);
    };
  }, []);

  const checkAuth = async () => {
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        setLoading(false);
        return;
      }

      // Verify token with backend
      const response = await axios.get('/api/auth/me', {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });

      // response should be the user info object
      setUser(response.data);
      // set axios default header for subsequent requests
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    } catch (error) {
      // Token is invalid, clear it
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  // Accepts either { token, user } or { accessToken, user }
  const login = (loginResponse) => {
    const token = loginResponse?.token || loginResponse?.accessToken;
    const refreshToken = loginResponse?.refreshToken;
    const userInfo = loginResponse?.user || loginResponse;
    if (token) {
      localStorage.setItem('token', token);
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    }
    if (refreshToken) {
      localStorage.setItem('refreshToken', refreshToken);
    }
    if (userInfo) {
      localStorage.setItem('user', JSON.stringify(userInfo));
      setUser(userInfo);
    }
  };

  const logout = () => {
    const refreshToken = localStorage.getItem('refreshToken');
    
    // Call logout endpoint to revoke refresh token
    if (refreshToken) {
      axios.post('/api/auth/logout', { refreshToken }).catch(() => {});
    }
    
    setUser(null);
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    delete axios.defaults.headers.common['Authorization'];
    router.push('/login');
  };

  const refreshAccessToken = async () => {
    try {
      const refreshToken = localStorage.getItem('refreshToken');
      if (!refreshToken) {
        throw new Error('No refresh token available');
      }

      const response = await axios.post('/api/auth/refresh', { refreshToken });
      const { token, refreshToken: newRefreshToken, user } = response.data;

      localStorage.setItem('token', token);
      if (newRefreshToken) {
        localStorage.setItem('refreshToken', newRefreshToken);
      }
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      setUser(user);

      return token;
    } catch (error) {
      logout();
      throw error;
    }
  };

  const value = {
    user,
    loading,
    login,
    logout,
    refreshAccessToken,
    isAuthenticated: !!user
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}
