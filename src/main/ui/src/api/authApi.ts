import { api, clearStoredAuth, setStoredAuth } from './client';
import type { AuthUser, UserRole } from '../types';

export interface LoginBody {
  username: string;
  password: string;
}

export interface RegisterBody {
  username: string;
  email: string;
  password: string;
  role?: UserRole;
}

interface AuthResponseDto {
  token: string;
  userId: number;
  username: string;
  role: UserRole;
}

export async function login(body: LoginBody): Promise<AuthUser> {
  const { data } = await api.post<AuthResponseDto>('/api/auth/login', body);
  const user: AuthUser = {
    token: data.token,
    userId: data.userId,
    username: data.username,
    role: data.role,
  };
  setStoredAuth(data.token, JSON.stringify(user));
  return user;
}

export async function register(body: RegisterBody): Promise<AuthUser> {
  const { data } = await api.post<AuthResponseDto>('/api/auth/register', body);
  const user: AuthUser = {
    token: data.token,
    userId: data.userId,
    username: data.username,
    role: data.role,
  };
  setStoredAuth(data.token, JSON.stringify(user));
  return user;
}

export function logout(): void {
  clearStoredAuth();
}
