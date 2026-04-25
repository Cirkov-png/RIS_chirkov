import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import * as tasksApi from '../api/tasksApi';
import type { TaskApplicationDto, TaskDto } from '../types';
import { errorMessage } from '../utils/errorMessage';
import { applicationStatusRu } from '../utils/applicationStatus';

/**
 * Отдельный раздел меню: только заявки волонтёров на задачи организатора.
 */
export function OrganizerApplicationsPage() {
  const { user } = useAuth();
  const [tasks, setTasks] = useState<TaskDto[]>([]);
  const [applications, setApplications] = useState<TaskApplicationDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);
  const [msg, setMsg] = useState<string | null>(null);
  const [closingAppId, setClosingAppId] = useState<number | null>(null);
  const [closeSuccess, setCloseSuccess] = useState(true);
  const [closeRating, setCloseRating] = useState('5');

  const load = useCallback(async () => {
    setLoading(true);
    setErr(null);
    try {
      const [t, apps] = await Promise.all([tasksApi.fetchTasks(), tasksApi.fetchApplications()]);
      setTasks(t);
      setApplications(apps);
    } catch (e) {
      setErr(errorMessage(e));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  const myTasks = useMemo(() => {
    if (!user) return [];
    return tasks.filter((x) => x.organizerId === user.userId);
  }, [tasks, user]);
  const myTaskIds = useMemo(() => new Set(myTasks.map((t) => t.id)), [myTasks]);
  const myApplications = useMemo(
    () => applications.filter((a) => myTaskIds.has(a.taskId)),
    [applications, myTaskIds],
  );

  async function approve(applicationId: number) {
    setErr(null);
    try {
      await tasksApi.approveApplication(applicationId);
      await load();
      setMsg('Заявка одобрена');
    } catch (e) {
      setErr(errorMessage(e));
    }
  }

  async function reject(applicationId: number) {
    setErr(null);
    try {
      await tasksApi.rejectApplication(applicationId);
      await load();
      setMsg('Заявка отклонена');
    } catch (e) {
      setErr(errorMessage(e));
    }
  }

  async function closeApplication(applicationId: number) {
    setErr(null);
    setMsg(null);
    try {
      const r = Number(closeRating);
      if (Number.isNaN(r) || r < 0 || r > 5) {
        setErr('Оценка должна быть числом от 0 до 5');
        return;
      }
      await tasksApi.closeApplication(applicationId, { successful: closeSuccess, rating: r });
      setClosingAppId(null);
      await load();
      setMsg('Заявка закрыта, рейтинг волонтёра пересчитан');
    } catch (e) {
      setErr(errorMessage(e));
    }
  }

  if (loading) return <p className="text-ink-light">Загрузка…</p>;

  return (
    <div className="space-y-8">
      <div>
        <h1 className="font-display text-3xl md:text-4xl font-bold mb-2">Заявки волонтёров</h1>
        <p className="text-ink-light max-w-2xl">
          Отклики на ваши задачи. Одобрение, отклонение и завершение с оценкой — только здесь; дашборд задач — в пункте
          меню «Дашборд».
        </p>
      </div>

      {msg && (
        <div className="rounded-lg bg-accent/15 border border-accent/40 text-accent text-sm px-4 py-2">{msg}</div>
      )}
      {err && <div className="rounded-lg bg-coral/15 border border-coral/40 text-coral text-sm px-4 py-2">{err}</div>}

      <section className="space-y-3">
        {myApplications.length === 0 && (
          <p className="text-sm text-ink-light">На ваши задачи пока нет заявок.</p>
        )}
        {myApplications.map((a) => (
          <article key={a.id} className="rounded-xl border border-white/10 bg-surface-card p-4 flex flex-col gap-3">
            <div className="flex flex-wrap justify-between gap-3">
              <div>
                <p className="font-semibold">Задача #{a.taskId} · Волонтёр #{a.volunteerId}</p>
                <p className="text-xs text-ink-light">{a.message || 'Без комментария'}</p>
                <p className="text-xs text-ink-light">{new Date(a.appliedAt).toLocaleString()}</p>
                {(a.status === 'COMPLETED_SUCCESS' || a.status === 'COMPLETED_FAILURE') &&
                  a.organizerRating != null && (
                    <p className="text-xs text-ink-light mt-1">
                      Оценка: {Number(a.organizerRating).toFixed(1)} ·{' '}
                      {a.taskCompletedSuccessfully ? 'выполнено' : 'не выполнено'}
                    </p>
                  )}
              </div>
              <div className="flex items-center gap-2 flex-wrap">
                <Link
                  to={`/volunteers/${a.volunteerId}`}
                  className="text-xs px-3 py-1 rounded border border-accent/40 text-accent"
                >
                  Профиль
                </Link>
                {a.status === 'PENDING' ? (
                  <>
                    <button
                      type="button"
                      onClick={() => void approve(a.id)}
                      className="text-xs px-3 py-1 rounded bg-green-500/20 text-green-400"
                    >
                      Одобрить
                    </button>
                    <button
                      type="button"
                      onClick={() => void reject(a.id)}
                      className="text-xs px-3 py-1 rounded bg-coral/20 text-coral"
                    >
                      Отклонить
                    </button>
                  </>
                ) : a.status === 'APPROVED' ? (
                  <button
                    type="button"
                    onClick={() => {
                      setClosingAppId(a.id);
                      setCloseSuccess(true);
                      setCloseRating('5');
                    }}
                    className="text-xs px-3 py-1 rounded bg-accent/20 text-accent"
                  >
                    Завершить с оценкой
                  </button>
                ) : (
                  <span className="text-xs px-2 py-1 rounded bg-surface-hover">{applicationStatusRu(a.status)}</span>
                )}
              </div>
            </div>
            {closingAppId === a.id && a.status === 'APPROVED' && (
              <div className="border-t border-white/10 pt-3 flex flex-col sm:flex-row flex-wrap gap-3 items-start sm:items-end">
                <label className="flex items-center gap-2 text-sm cursor-pointer">
                  <input type="checkbox" checked={closeSuccess} onChange={(e) => setCloseSuccess(e.target.checked)} />
                  Задача выполнена волонтёром
                </label>
                <div>
                  <label className="text-xs text-ink-light block">Оценка (0–5)</label>
                  <input
                    type="number"
                    min={0}
                    max={5}
                    step={0.1}
                    className="mt-1 w-24 rounded-lg bg-ink border border-white/10 px-2 py-1.5 text-sm"
                    value={closeRating}
                    onChange={(e) => setCloseRating(e.target.value)}
                  />
                </div>
                <div className="flex gap-2">
                  <button
                    type="button"
                    onClick={() => void closeApplication(a.id)}
                    className="text-xs px-4 py-2 rounded-xl bg-accent text-ink font-semibold"
                  >
                    Сохранить
                  </button>
                  <button
                    type="button"
                    onClick={() => setClosingAppId(null)}
                    className="text-xs px-4 py-2 rounded-xl border border-white/15"
                  >
                    Отмена
                  </button>
                </div>
              </div>
            )}
          </article>
        ))}
      </section>
    </div>
  );
}
