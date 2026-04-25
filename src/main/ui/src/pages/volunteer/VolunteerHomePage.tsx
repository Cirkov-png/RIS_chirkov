import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import * as volunteersApi from '../../api/volunteersApi';
import type { TaskApplicationDto, VolunteerDto } from '../../types';
import { errorMessage } from '../../utils/errorMessage';

export function VolunteerHomePage() {
  const { user } = useAuth();
  const [volunteer, setVolunteer] = useState<VolunteerDto | null>(null);
  const [applications, setApplications] = useState<TaskApplicationDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);

  const load = useCallback(async () => {
    if (!user) return;
    setLoading(true);
    setErr(null);
    try {
      const vols = await volunteersApi.fetchVolunteers();
      const mine = vols.find((v) => v.userId === user.userId) ?? null;
      setVolunteer(mine);
      if (mine) {
        const apps = await volunteersApi.fetchMyApplications(mine.id);
        setApplications(apps);
      } else {
        setApplications([]);
      }
    } catch (e) {
      setErr(errorMessage(e));
    } finally {
      setLoading(false);
    }
  }, [user]);

  useEffect(() => {
    void load();
  }, [load]);

  const stats = useMemo(() => {
    const pending = applications.filter((a) => a.status === 'PENDING').length;
    const approved = applications.filter(
      (a) =>
        a.status === 'APPROVED' ||
        a.status === 'COMPLETED_SUCCESS' ||
        a.status === 'COMPLETED_FAILURE',
    ).length;
    return {
      rating: volunteer?.rating ?? '0',
      completedTasksCount: volunteer?.completedTasksCount ?? 0,
      pending,
      approved,
      total: applications.length,
    };
  }, [applications, volunteer]);

  if (loading) return <p className="text-ink-light">Загрузка…</p>;
  if (!volunteer) {
    return (
      <div className="rounded-2xl border border-coral/40 bg-coral/10 p-6 text-coral">
        Профиль волонтёра не найден. Зарегистрируйтесь с ролью VOLUNTEER.
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {err && <div className="rounded-lg bg-coral/15 border border-coral/40 text-coral text-sm px-4 py-2">{err}</div>}

      <section className="glass-panel p-6 md:p-7">
        <p className="text-xs uppercase tracking-[0.2em] text-accent/90">Моя активность</p>
        <h2 className="font-display text-2xl md:text-3xl font-bold mt-2">Добро пожаловать, {volunteer.fullName || user?.username}</h2>
        <p className="text-sm text-ink-light mt-2 max-w-2xl">
          Здесь показана ваша текущая динамика по откликам. Используйте быстрые действия ниже, чтобы перейти к новым
          задачам или управлению заявками.
        </p>
      </section>

      <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {[
          { label: 'Рейтинг', value: Number(stats.rating).toFixed(2), color: 'text-accent', icon: '★' },
          { label: 'Задач выполнено', value: stats.completedTasksCount, color: 'text-green-400', icon: '✓' },
          { label: 'Заявок на рассмотрении', value: stats.pending, color: 'text-yellow-400', icon: '⏳' },
          { label: 'Всего заявок', value: stats.total, color: 'text-coral', icon: '✉' },
        ].map((s) => (
          <div key={s.label} className="glass-panel p-4 text-center">
            <p className="text-lg mb-1 opacity-80">{s.icon}</p>
            <p className="text-xs text-ink-light">{s.label}</p>
            <p className={`text-2xl font-bold mt-1 ${s.color}`}>{s.value}</p>
          </div>
        ))}
      </div>

      <div className="flex flex-wrap gap-3">
        <Link
          to="/volunteer/tasks"
          className="px-5 py-2.5 rounded-xl bg-accent text-ink font-semibold hover:bg-accent/90"
        >
          Смотреть открытые задачи
        </Link>
        <Link
          to="/volunteer/applications"
          className="px-5 py-2.5 rounded-xl border border-white/15 hover:bg-surface-hover"
        >
          Мои заявки
        </Link>
      </div>
    </div>
  );
}
