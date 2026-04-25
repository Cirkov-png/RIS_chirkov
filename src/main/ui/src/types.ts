export type UserRole = 'VOLUNTEER' | 'ORGANIZER' | 'COORDINATOR' | 'ADMIN';

export type TaskStatus = 'DRAFT' | 'OPEN' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

export interface AuthUser {
  token: string;
  userId: number;
  username: string;
  role: UserRole;
}

export interface VolunteerDto {
  id: number;
  userId: number;
  fullName: string | null;
  phone: string | null;
  region: string | null;
  bio: string | null;
  active: boolean;
  birthDate: string | null;
  rating: string;
  completedTasksCount: number;
  avatarUrl: string | null;
}

export interface SkillDto {
  id: number;
  name: string;
  categoryId: number | null;
}

export interface VolunteerSkillDto {
  id: number;
  volunteerId: number;
  skillId: number;
  skillName: string | null;
  categoryName: string | null;
  proficiencyLevel: number;
}

export interface TaskDto {
  id: number;
  title: string;
  description: string | null;
  organizerId: number;
  categoryId: number | null;
  status: TaskStatus;
  location: string | null;
  startTime: string | null;
  endTime: string | null;
  createdAt: string;
}

export interface TaskRequirementDto {
  id: number;
  taskId: number;
  skillId: number;
  importanceWeight: string;
  skillName?: string | null;
}

/** Публичные поля пользователя (в т.ч. карточка организатора). */
export interface UserPublicDto {
  id: number;
  username: string;
  email: string;
  role: UserRole;
  enabled: boolean;
  createdAt: string;
  profileFullName?: string | null;
  profilePhone?: string | null;
  profileBio?: string | null;
  profileAvatarUrl?: string | null;
}

export interface RecommendedVolunteerDto {
  volunteerId: number;
  userId: number;
  fullName: string | null;
  matchScore: string;
}

export interface CategoryDto {
  id: number;
  name: string;
  description: string | null;
}

export type ApplicationStatus =
  | 'PENDING'
  | 'APPROVED'
  | 'REJECTED'
  | 'WITHDRAWN'
  | 'COMPLETED_SUCCESS'
  | 'COMPLETED_FAILURE';

export interface TaskApplicationDto {
  id: number;
  taskId: number;
  volunteerId: number;
  status: ApplicationStatus;
  message: string | null;
  appliedAt: string;
  organizerRating?: string | null;
  taskCompletedSuccessfully?: boolean | null;
  organizerReviewedAt?: string | null;
}

export interface VolunteerStatsDto {
  volunteerId: number;
  fullName: string | null;
  rating: string;
  completedTasksCount: number;
  pendingApplicationsCount: number;
  approvedApplicationsCount: number;
}
