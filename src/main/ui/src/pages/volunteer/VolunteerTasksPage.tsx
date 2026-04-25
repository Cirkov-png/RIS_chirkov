import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import * as volunteersApi from '../../api/volunteersApi';
import type { TaskDto, VolunteerDto } from '../../types';
import { errorMessage } from '../../utils/errorMessage';
import { taskStatusRu } from '../../utils/taskStatus';

export function VolunteerTasksPage() {
  const { user } = useAuth();
  const [volunteer, setVolunteer] = useState<VolunteerDto | null>(null);
  const [tasks, setTasks] = useState<TaskDto[]>([]);
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
      const open = await volunteersApi.fetchAvailableTasks();
      setTasks(open);
    } catch (e) {
      setErr(errorMessage(e));
    } finally {
      setLoading(false);
    }
  }, [user]);

  useEffect(() => {
    void load();
  }, [load]);

  if (loading) return <p className="text-ink-light">Загрузка задач…</p>;
  if (!volunteer) {
    return (
      <div className="rounded-2xl border border-coral/40 bg-coral/10 p-6 text-coral">
        Профиль волонтёра не найден.
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {err && <div className="rounded-lg bg-coral/15 border border-coral/40 text-coral text-sm px-4 py-2">{err}</div>}
      <p className="text-sm text-ink-light">
        Откройте карточку задачи — там описание, регион, сроки и требуемые навыки. Отклик можно отправить со страницы
        задачи.
      </p>
      <div className="space-y-3">
        {tasks.length === 0 && <p className="text-sm text-ink-light">Открытых задач нет.</p>}
        {tasks.map((t) => (
          <article key={t.id} className="rounded-xl border border-white/10 bg-surface-card p-4 flex flex-wrap justify-between gap-3 items-start">
            <div>
              <p className="font-semibold text-lg">{t.title}</p>
              <p className="text-xs text-ink-light mt-1">
                {t.location || 'Место не указано'} · {taskStatusRu(t.status)}
              </p>
            </div>
            <Link
              to={`/volunteer/tasks/${t.id}`}
              className="text-sm px-4 py-2 rounded-xl bg-accent text-ink font-semibold shrink-0"
            >
              Подробнее
            </Link>
          </article>
        ))}
      </div>
    </div>
  );
}
