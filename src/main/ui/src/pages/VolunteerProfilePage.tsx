import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api } from '../api/client';
import type { VolunteerDto, VolunteerSkillDto, VolunteerStatsDto } from '../types';
import { errorMessage } from '../utils/errorMessage';
import { AvatarImg } from '../components/AvatarImg';

export function VolunteerProfilePage() {
  const { id } = useParams<{ id: string }>();
  const nav = useNavigate();
  const [volunteer, setVolunteer] = useState<VolunteerDto | null>(null);
  const [skills, setSkills] = useState<VolunteerSkillDto[]>([]);
  const [stats, setStats] = useState<VolunteerStatsDto | null>(null);
  const [err, setErr] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    Promise.all([
      api.get<VolunteerDto>(`/api/volunteers/${id}`),
      api.get<VolunteerSkillDto[]>(`/api/volunteers/${id}/skills`),
      api.get<VolunteerStatsDto>(`/api/volunteers/${id}/stats`),
    ])
      .then(([v, sk, st]) => {
        setVolunteer(v.data);
        setSkills(sk.data);
        setStats(st.data);
      })
      .catch((e) => setErr(errorMessage(e)))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) return <p className="text-ink-light">Загрузка профиля…</p>;
  if (err) return <div className="rounded-lg bg-coral/15 border border-coral/40 text-coral px-4 py-3">{err}</div>;
  if (!volunteer) return null;

  const LEVEL_LABEL = ['', 'Начальный', 'Базовый', 'Средний', 'Продвинутый', 'Эксперт'];

  return (
    <div className="space-y-6 max-w-2xl">
      <button onClick={() => nav(-1)} className="text-sm text-ink-light hover:text-white flex items-center gap-1">
        ← Назад
      </button>

      {/* Header */}
      <div className="bg-surface-card border border-white/10 rounded-2xl p-6 flex gap-5 items-start">
        <div className="w-20 h-20 rounded-full border border-white/20 overflow-hidden shrink-0">
          <AvatarImg src={volunteer.avatarUrl} className="w-full h-full object-cover" alt="" />
        </div>
        <div className="space-y-1">
          <h1 className="font-display text-2xl font-bold">{volunteer.fullName ?? `Волонтёр #${volunteer.id}`}</h1>
          {volunteer.region && <p className="text-sm text-ink-light">📍 {volunteer.region}</p>}
          {volunteer.phone && <p className="text-sm text-ink-light">📞 {volunteer.phone}</p>}
          {volunteer.birthDate && <p className="text-sm text-ink-light">🎂 {volunteer.birthDate}</p>}
          <p className={`text-xs font-medium ${volunteer.active ? 'text-green-400' : 'text-red-400'}`}>
            {volunteer.active ? '● Активен' : '● Неактивен'}
          </p>
        </div>
      </div>

      {/* Stats */}
      {stats && (
        <div className="grid grid-cols-3 gap-3">
          {[
            { label: 'Рейтинг', value: Number(stats.rating).toFixed(2), color: 'text-accent' },
            { label: 'Задач выполнено', value: stats.completedTasksCount, color: 'text-green-400' },
            { label: 'Одобренных заявок', value: stats.approvedApplicationsCount, color: 'text-blue-400' },
          ].map((s) => (
            <div key={s.label} className="bg-surface-card border border-white/10 rounded-xl p-4 text-center">
              <p className={`text-2xl font-bold ${s.color}`}>{s.value}</p>
              <p className="text-xs text-ink-light mt-1">{s.label}</p>
            </div>
          ))}
        </div>
      )}

      {/* Bio */}
      {volunteer.bio && (
        <div className="bg-surface-card border border-white/10 rounded-2xl p-5">
          <h2 className="font-semibold mb-2 text-accent">О себе</h2>
          <p className="text-sm text-ink-light leading-relaxed">{volunteer.bio}</p>
        </div>
      )}

      {/* Skills */}
      <div className="bg-surface-card border border-white/10 rounded-2xl p-5">
        <h2 className="font-semibold mb-3 text-coral">Навыки ({skills.length})</h2>
        {skills.length === 0
          ? <p className="text-sm text-ink-light">Навыки не указаны.</p>
          : (
            <ul className="space-y-2">
              {skills.map((s) => (
                <li key={s.id} className="flex justify-between items-center border border-white/5 rounded-lg px-3 py-2">
                  <div>
                    <p className="text-sm font-medium">{s.skillName}</p>
                    {s.categoryName && <p className="text-xs text-ink-light">{s.categoryName}</p>}
                  </div>
                  <div className="text-right">
                    <p className="text-xs font-semibold text-accent">{LEVEL_LABEL[s.proficiencyLevel] ?? s.proficiencyLevel}</p>
                    <div className="flex gap-0.5 mt-1">
                      {[1, 2, 3, 4, 5].map((n) => (
                        <div key={n} className={`w-3 h-1.5 rounded-sm ${n <= s.proficiencyLevel ? 'bg-accent' : 'bg-white/10'}`} />
                      ))}
                    </div>
                  </div>
                </li>
              ))}
            </ul>
          )}
      </div>
    </div>
  );
}
