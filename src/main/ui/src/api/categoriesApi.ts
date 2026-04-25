import { api } from './client';
import type { CategoryDto } from '../types';

export async function fetchCategories(): Promise<CategoryDto[]> {
  const { data } = await api.get<CategoryDto[]>('/api/categories');
  return data;
}
