import type { ApplicationStatus } from '../types';

export const APPLICATION_STATUS_RU: Record<ApplicationStatus, string> = {
  PENDING: 'На рассмотрении',
  APPROVED: 'Одобрена',
  REJECTED: 'Отклонена',
  WITHDRAWN: 'Отозвана',
  COMPLETED_SUCCESS: 'Завершена (выполнено)',
  COMPLETED_FAILURE: 'Завершена (не выполнено)',
};

export function applicationStatusRu(status: ApplicationStatus | string): string {
  if (Object.prototype.hasOwnProperty.call(APPLICATION_STATUS_RU, status)) {
    return APPLICATION_STATUS_RU[status as ApplicationStatus];
  }
  return String(status);
}
