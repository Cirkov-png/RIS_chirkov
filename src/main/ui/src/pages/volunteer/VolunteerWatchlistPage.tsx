import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import * as volunteersApi from '../../api/volunteersApi';
import { fetchExpiringWatches, fetchWatchlist, unwatchTask } from '../../api/externalApi';
import type { TaskWatchDto, TaskStatus, VolunteerDto } from '../../types';
import { errorMessage } from '../../utils/errorMessage';
import { taskStatusRu } from '../../utils/taskStatus';

export function VolunteerWatchlistPage() {
  const { user } = useAuth();
  const [volunteer, setVolunteer] = useState<VolunteerDto | null>(null);
  const [watches, setWatches] = useState<TaskWatchDto[]>([]);
  const [expiring, setExpiring] = useState<TaskWatchDto[]>([]);
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
        const [all, soon] = await Promise.all([
          fetchWatchlist(mine.id),
          fetchExpiringWatches(mine.id, 7),
        ]);
        setWatches(all);
        setExpiring(soon);
      } else {
        setWatches([]);
        setExpiring([]);
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

  async function removeWatch(taskUuid: string) {
    if (!volunteer) return;
    setErr(null);
    try {
      await unwatchTask(volunteer.id, taskUuid);
      await load();
    } catch (e) {
      setErr(errorMessage(e));
    }
  }

  if (loading) return <p className="text-ink-light">Загрузка…</p>;
  if (!volunteer) {
    return (
      <div className="rounded-2xl border border-coral/40 bg-coral/10 p-6 text-coral">
        Профиль волонтёра не найден.
      </div>
    );
  }

  return (
    <div className="space-y-6 max-w-4xl">
      {err && <div className="rounded-lg bg-coral/15 border border-coral/40 text-coral text-sm px-4 py-2">{err}</div>}

      <p className="text-sm text-ink-light">
        Здесь отображаются задачи, за сроками которых вы следите. Сервер периодически проверяет дедлайны; записи с
        приближающимся окончанием также видны в блоке «Скоро дедлайн».
      </p>

      {expiring.length > 0 && (
        <section className="rounded-2xl border border-amber-500/40 bg-amber-500/10 p-5 space-y-3">
          <h2 className="font-display text-lg font-semibold text-amber-200">Скоро дедлайн (7 дней)</h2>
          <ul className="space-y-2">
            {expiring.map((w) => (
              <li key={w.uuid} className="flex flex-wrap items-center justify-between gap-2 text-sm border border-white/10 rounded-lg px-3 py-2 bg-ink/40">
                <div>
                  <Link className="text-accent font-medium hover:underline" to={`/volunteer/tasks/u/${w.taskUuid}`}>
                    {w.taskTitle}
                  </Link>
                  <span className="text-ink-light ml-2">
                    {w.taskEndTime ? new Date(w.taskEndTime).toLocaleString() : 'срок не задан'} · через{' '}
                    {w.daysUntilDeadline} дн.
                  </span>
                </div>
              </li>
            ))}
          </ul>
        </section>
      )}

      <section className="bg-surface-card border border-white/10 rounded-2xl p-6 space-y-4">
        <h2 className="font-display text-xl font-semibold text-accent">Все отслеживаемые задачи</h2>
        {watches.length === 0 ? (
          <p className="text-sm text-ink-light">
            Пока пусто. Откройте карточку задачи и нажмите «Следить за сроком».
          </p>
        ) : (
          <ul className="divide-y divide-white/10">
            {watches.map((w) => (
              <li key={w.uuid} className="py-4 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
                <div>
                  <Link className="font-medium text-accent hover:underline" to={`/volunteer/tasks/u/${w.taskUuid}`}>
                    {w.taskTitle}
                  </Link>
                  <p className="text-xs text-ink-light mt-1">
                    {taskStatusRu(w.taskStatus as TaskStatus)}
                    {w.taskEndTime ? ` · конец: ${new Date(w.taskEndTime).toLocaleString()}` : ''}
                    {w.daysUntilDeadline >= 0 ? ` · до дедлайна: ${w.daysUntilDeadline} дн.` : ''}
                  </p>
                </div>
                <button
                  type="button"
                  onClick={() => void removeWatch(w.taskUuid)}
                  className="text-sm px-3 py-1.5 rounded-lg border border-white/15 text-ink-light hover:text-coral hover:border-coral/50 shrink-0"
                >
                  Не отслеживать
                </button>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}
