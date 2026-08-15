import { createContext, useContext, useState, ReactNode } from 'react';
import { api } from '../lib/api';
import { setTokens, clearTokens, getAccessToken } from '../lib/api';
import { AuthResponse } from '../lib/types';

interface AuthState {
  email: string | null;
  role: string | null;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthState | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [email, setEmail] = useState<string | null>(sessionStorage.getItem('orderrail_email'));
  const [role, setRole] = useState<string | null>(sessionStorage.getItem('orderrail_role'));

  async function login(loginEmail: string, password: string) {
    const res = await api.post<AuthResponse>('/api/auth/login', { email: loginEmail, password });
    setTokens(res.accessToken, res.refreshToken);
    sessionStorage.setItem('orderrail_email', res.email);
    sessionStorage.setItem('orderrail_role', res.role);
    setEmail(res.email);
    setRole(res.role);
  }

  function logout() {
    clearTokens();
    sessionStorage.removeItem('orderrail_email');
    sessionStorage.removeItem('orderrail_role');
    setEmail(null);
    setRole(null);
  }

  return (
    <AuthContext.Provider
      value={{ email, role, isAuthenticated: !!getAccessToken(), login, logout }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
