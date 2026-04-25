import axios from 'axios';

export function errorMessage(e: unknown): string {
  if (axios.isAxiosError(e)) {
    const d = e.response?.data;
    if (typeof d === 'string') return d;
    if (d && typeof d === 'object') {
      if ('error' in d && typeof (d as { error: unknown }).error === 'string') {
        return (d as { error: string }).error;
      }
      if ('message' in d && typeof (d as { message: unknown }).message === 'string') {
        return (d as { message: string }).message;
      }
    }
    return e.response?.statusText || e.message || 'Ошибка запроса';
  }
  if (e instanceof Error) return e.message;
  return 'Неизвестная ошибка';
}
