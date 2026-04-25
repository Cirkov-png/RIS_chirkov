import { api } from './client';
import type { UserPublicDto } from '../types';

export async function fetchUser(userId: number): Promise<UserPublicDto> {
  const { data } = await api.get<UserPublicDto>(`/api/users/${userId}`);
  return data;
}

export async function patchMyOrganizerProfile(body: {
  profileFullName?: string | null;
  profilePhone?: string | null;
  profileBio?: string | null;
  profileAvatarUrl?: string | null;
}): Promise<UserPublicDto> {
  const { data } = await api.patch<UserPublicDto>('/api/users/me/profile', body);
  return data;
}
