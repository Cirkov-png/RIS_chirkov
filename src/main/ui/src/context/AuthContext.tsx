import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import type { AuthUser } from '../types';
import { getStoredToken, getStoredUserJson } from '../api/client';
import * as authApi from '../api/authApi';

interface AuthContextValue {
  user: AuthUser | null;
  loading: boolean;
  login: (u: string, p: string) => Promise<void>;
  register: (b: authApi.RegisterBody) => Promise<void>;
  logout: () => void;
  refreshFromStorage: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

function readUserFromStorage(): AuthUser | null {
  const token = getStoredToken();
  const raw = getStoredUserJson();
  if (!token || !raw) return null;
  try {
    const u = JSON.parse(raw) as AuthUser;
    if (!u.token) u.token = token;
    return u;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(() => readUserFromStorage());
  const [loading, setLoading] = useState(false);

  const refreshFromStorage = useCallback(() => {
    setUser(readUserFromStorage());
  }, []);

  const login = useCallback(async (username: string, password: string) => {
    setLoading(true);
    try {
      const u = await authApi.login({ username, password });
      setUser(u);
    } finally {
      setLoading(false);
    }
  }, []);

  const register = useCallback(async (body: authApi.RegisterBody) => {
    setLoading(true);
    try {
      const u = await authApi.register(body);
      setUser(u);
    } finally {
      setLoading(false);
    }
  }, []);

  const logout = useCallback(() => {
    authApi.logout();
    setUser(null);
  }, []);

  const value = useMemo(
    () => ({ user, loading, login, register, logout, refreshFromStorage }),
    [user, loading, login, register, logout, refreshFromStorage],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth вне AuthProvider');
  return ctx;
}
