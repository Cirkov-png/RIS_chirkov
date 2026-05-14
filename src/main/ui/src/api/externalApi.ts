import { api } from './client';
import type { MapGeocodeDto, TaskWatchDto, WeatherDto } from '../types';

// ── Watchlist ────────────────────────────────────────────────────────────────

export async function fetchWatchlist(volunteerId: number): Promise<TaskWatchDto[]> {
  const { data } = await api.get<TaskWatchDto[]>(`/api/volunteers/${volunteerId}/watchlist`);
  return data;
}

export async function fetchExpiringWatches(volunteerId: number, withinDays = 3): Promise<TaskWatchDto[]> {
  const { data } = await api.get<TaskWatchDto[]>(
    `/api/volunteers/${volunteerId}/watchlist/expiring?withinDays=${withinDays}`,
  );
  return data;
}

export async function watchTask(volunteerId: number, taskUuid: string): Promise<TaskWatchDto> {
  const { data } = await api.post<TaskWatchDto>(`/api/volunteers/${volunteerId}/watchlist/${taskUuid}`);
  return data;
}

export async function unwatchTask(volunteerId: number, taskUuid: string): Promise<void> {
  await api.delete(`/api/volunteers/${volunteerId}/watchlist/${taskUuid}`);
}

// ── External API: Weather ────────────────────────────────────────────────────

export async function fetchWeather(city: string): Promise<WeatherDto | null> {
  try {
    const { data } = await api.get<WeatherDto>(`/api/external/weather?city=${encodeURIComponent(city)}`);
    return data;
  } catch {
    return null;
  }
}

/** Геокодирование адреса через Google (серверный прокси). */
export async function fetchMapGeocode(address: string): Promise<MapGeocodeDto | null> {
  try {
    const { data, status } = await api.get<MapGeocodeDto>(
      `/api/external/maps/geocode?address=${encodeURIComponent(address)}`,
      { validateStatus: (s) => s === 200 || s === 204 },
    );
    if (status === 204 || data == null) return null;
    return data;
  } catch {
    return null;
  }
}
