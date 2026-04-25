import type { TaskStatus } from '../types';

export const TASK_STATUS_RU: Record<TaskStatus, string> = {
  DRAFT: 'Черновик',
  OPEN: 'Открыта',
  IN_PROGRESS: 'В работе',
  COMPLETED: 'Завершена',
  CANCELLED: 'Отменена',
};

export function taskStatusRu(status: TaskStatus): string {
  return TASK_STATUS_RU[status] ?? status;
}
