import { api } from './client';
import type { VolunteerSkillDto } from '../types';

export async function fetchVolunteerSkills(): Promise<VolunteerSkillDto[]> {
  const { data } = await api.get<VolunteerSkillDto[]>('/api/volunteer-skills');
  return data;
}

export async function createVolunteerSkill(body: {
  volunteerId: number;
  skillId: number;
  proficiencyLevel: number;
}): Promise<VolunteerSkillDto> {
  const { data } = await api.post<VolunteerSkillDto>('/api/volunteer-skills', body);
  return data;
}

export async function updateVolunteerSkill(
  id: number,
  body: { volunteerId: number; skillId: number; proficiencyLevel: number },
): Promise<VolunteerSkillDto> {
  const { data } = await api.put<VolunteerSkillDto>(`/api/volunteer-skills/${id}`, body);
  return data;
}

export async function deleteVolunteerSkill(id: number): Promise<void> {
  await api.delete(`/api/volunteer-skills/${id}`);
}
