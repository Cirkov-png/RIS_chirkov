import { api } from './client';
import type { TaskApplicationDto, TaskDto, VolunteerDto, VolunteerSkillDto, VolunteerStatsDto } from '../types';

export async function fetchVolunteers(): Promise<VolunteerDto[]> {
  const { data } = await api.get<VolunteerDto[]>('/api/volunteers');
  return data;
}

export async function updateVolunteer(
  id: number,
  body: {
    userId: number;
    fullName?: string | null;
    phone?: string | null;
    region?: string | null;
    bio?: string | null;
    birthDate?: string | null;
    avatarUrl?: string | null;
    active?: boolean | null;
  },
): Promise<VolunteerDto> {
  const { data } = await api.put<VolunteerDto>(`/api/volunteers/${id}`, body);
  return data;
}

export async function fetchVolunteerById(id: number): Promise<VolunteerDto> {
  const { data } = await api.get<VolunteerDto>(`/api/volunteers/${id}`);
  return data;
}

export async function deactivateVolunteer(id: number): Promise<VolunteerDto> {
  const { data } = await api.patch<VolunteerDto>(`/api/volunteers/${id}/deactivate`);
  return data;
}

export async function activateVolunteer(id: number): Promise<VolunteerDto> {
  const { data } = await api.patch<VolunteerDto>(`/api/volunteers/${id}/activate`);
  return data;
}

export async function rateVolunteer(id: number, rating: number): Promise<VolunteerDto> {
  const { data } = await api.patch<VolunteerDto>(`/api/volunteers/${id}/rate`, { rating });
  return data;
}

export async function fetchVolunteerSkills(id: number): Promise<VolunteerSkillDto[]> {
  const { data } = await api.get<VolunteerSkillDto[]>(`/api/volunteers/${id}/skills`);
  return data;
}

export async function fetchVolunteerStats(id: number): Promise<VolunteerStatsDto> {
  const { data } = await api.get<VolunteerStatsDto>(`/api/volunteers/${id}/stats`);
  return data;
}

export async function fetchAvailableTasks(): Promise<TaskDto[]> {
  const { data } = await api.get<TaskDto[]>('/api/volunteers/tasks/available');
  return data;
}

export async function applyToTask(
  volunteerId: number,
  body: { taskId: number; message?: string | null },
): Promise<TaskApplicationDto> {
  const { data } = await api.post<TaskApplicationDto>(`/api/volunteers/${volunteerId}/apply`, body);
  return data;
}

export async function fetchMyApplications(volunteerId: number): Promise<TaskApplicationDto[]> {
  const { data } = await api.get<TaskApplicationDto[]>(`/api/volunteers/${volunteerId}/applications`);
  return data;
}

export async function withdrawApplication(
  volunteerId: number,
  applicationId: number,
): Promise<TaskApplicationDto> {
  const { data } = await api.patch<TaskApplicationDto>(
    `/api/volunteers/${volunteerId}/applications/${applicationId}/withdraw`,
  );
  return data;
}

export async function fetchApprovedTasks(volunteerId: number): Promise<TaskDto[]> {
  const { data } = await api.get<TaskDto[]>(`/api/volunteers/${volunteerId}/tasks/approved`);
  return data;
}

export async function fetchApplicationForTask(
  volunteerId: number,
  taskId: number,
): Promise<TaskApplicationDto | null> {
  try {
    const { data } = await api.get<TaskApplicationDto>(`/api/volunteers/${volunteerId}/tasks/${taskId}/application`);
    return data;
  } catch (e) {
    if (typeof e === 'object' && e && 'response' in e) {
      const err = e as { response?: { status?: number } };
      if (err.response?.status === 204) return null;
    }
    throw e;
  }
}

export async function updateAvatar(volunteerId: number, avatarUrl: string): Promise<VolunteerDto> {
  const { data } = await api.patch<VolunteerDto>(`/api/volunteers/${volunteerId}/avatar`, { avatarUrl });
  return data;
}
