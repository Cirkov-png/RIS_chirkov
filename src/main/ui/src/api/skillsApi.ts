import { api } from './client';
import type { SkillDto } from '../types';

export async function fetchSkills(): Promise<SkillDto[]> {
  const { data } = await api.get<SkillDto[]>('/api/skills');
  return data;
}
