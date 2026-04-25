import { api } from './client';
import type {
  RecommendedVolunteerDto,
  TaskApplicationDto,
  TaskDto,
  TaskStatus,
} from '../types';

export async function fetchTasks(): Promise<TaskDto[]> {
  const { data } = await api.get<TaskDto[]>('/api/tasks');
  return data;
}

export async function fetchTask(id: number): Promise<TaskDto> {
  const { data } = await api.get<TaskDto>(`/api/tasks/${id}`);
  return data;
}

export async function createTask(body: {
  title: string;
  description?: string | null;
  categoryId?: number | null;
  status: TaskStatus;
  location?: string | null;
  startTime?: string | null;
  endTime?: string | null;
  /** Только для COORDINATOR: назначить организатора */
  organizerUserId?: number | null;
}): Promise<TaskDto> {
  const { data } = await api.post<TaskDto>('/api/tasks', body);
  return data;
}

export async function fetchRecommendedVolunteers(
  taskId: number,
): Promise<RecommendedVolunteerDto[]> {
  const { data } = await api.get<RecommendedVolunteerDto[]>(
    `/api/tasks/${taskId}/recommended-volunteers`,
  );
  return data;
}

export async function updateTask(
  id: number,
  body: {
    title: string;
    description?: string | null;
    categoryId?: number | null;
    status: TaskStatus;
    location?: string | null;
    startTime?: string | null;
    endTime?: string | null;
    organizerUserId?: number | null;
  },
): Promise<TaskDto> {
  const { data } = await api.put<TaskDto>(`/api/tasks/${id}`, body);
  return data;
}

export async function deleteTask(id: number): Promise<void> {
  await api.delete(`/api/tasks/${id}`);
}

export async function fetchApplications(): Promise<TaskApplicationDto[]> {
  const { data } = await api.get<TaskApplicationDto[]>('/api/applications');
  return data;
}

export async function fetchApplicationsByTask(taskId: number): Promise<TaskApplicationDto[]> {
  const { data } = await api.get<TaskApplicationDto[]>(`/api/applications/by-task/${taskId}`);
  return data;
}

export async function approveApplication(id: number): Promise<TaskApplicationDto> {
  const { data } = await api.patch<TaskApplicationDto>(`/api/applications/${id}/approve`);
  return data;
}

export async function rejectApplication(id: number): Promise<TaskApplicationDto> {
  const { data } = await api.patch<TaskApplicationDto>(`/api/applications/${id}/reject`);
  return data;
}

export async function closeApplication(
  applicationId: number,
  body: { successful: boolean; rating: number },
): Promise<TaskApplicationDto> {
  const { data } = await api.patch<TaskApplicationDto>(`/api/applications/${applicationId}/close`, body);
  return data;
}
