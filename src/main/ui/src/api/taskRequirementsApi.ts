import { api } from './client';
import type { TaskRequirementDto } from '../types';

export async function fetchTaskRequirements(): Promise<TaskRequirementDto[]> {
  const { data } = await api.get<TaskRequirementDto[]>('/api/task-requirements');
  return data;
}

export async function fetchTaskRequirementsByTask(taskId: number): Promise<TaskRequirementDto[]> {
  const { data } = await api.get<TaskRequirementDto[]>(`/api/task-requirements/by-task/${taskId}`);
  return data;
}

export async function createTaskRequirement(body: {
  taskId: number;
  skillId: number;
  importanceWeight: number;
}): Promise<TaskRequirementDto> {
  const { data } = await api.post<TaskRequirementDto>('/api/task-requirements', body);
  return data;
}

export async function deleteTaskRequirement(id: number): Promise<void> {
  await api.delete(`/api/task-requirements/${id}`);
}
