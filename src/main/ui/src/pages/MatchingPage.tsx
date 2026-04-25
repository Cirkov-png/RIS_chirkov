import { useCallback, useEffect, useMemo, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import * as tasksApi from '../api/tasksApi';
import type { RecommendedVolunteerDto, TaskDto } from '../types';
import { errorMessage } from '../utils/errorMessage';

function scorePercent(matchScore: string): number {
  const n = Number(matchScore);
  if (Number.isNaN(n)) return 0;
  return Math.min(100, Math.round(n * 10000) / 100);
}

export function MatchingPage() {
  const { user } = useAuth();
  const [tasks, setTasks] = useState<TaskDto[]>([]);
  const [taskId, setTaskId] = useState<number | ''>('');
  const [rows, setRows] = useState<RecommendedVolunteerDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [running, setRunning] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  const loadTasks = useCallback(async () => {
    setLoading(true);
    setErr(null);
    try {
      const t = await tasksApi.fetchTasks();
      setTasks(t);
    } catch (e) {
      setErr(errorMessage(e));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadTasks();
  }, [loadTasks]);

  const selectable = useMemo(() => {
    if (!user) return [];
    const base = user.role === 'COORDINATOR' ? tasks : tasks.filter((t) => t.organizerId === user.userId);
    return base.filter((t) => t.status !== 'COMPLETED' && t.status !== 'CANCELLED');
  }, [tasks, user]);

  useEffect(() => {
    if (taskId === '') return;
    const id = Number(taskId);
    if (!selectable.some((t) => t.id === id)) setTaskId('');
  }, [selectable, taskId]);

  async function runMatch() {
    if (taskId === '') return;
    const t = tasks.find((x) => x.id === Number(taskId));
    if (t && (t.status === 'COMPLETED' || t.status === 'CANCELLED')) {
      setErr('Матчинг недоступен для завершённых или отменённых задач.');
      return;
    }
    setRunning(true);
    setErr(null);
    setRows([]);
    try {
      const data = await tasksApi.fetchRecommendedVolunteers(Number(taskId));
      setRows(data);
    } catch (e) {
      setErr(errorMessage(e));
    } finally {
      setRunning(false);
    }
  }

  if (loading) return <p className="text-ink-light">Загрузка задач…</p>;

  return (
    <div className="space-y-8">
      <div>
        <h1 className="font-display text-3xl md:text-4xl font-bold mb-2">Матчинг волонтёров</h1>
        <p className="text-ink-light max-w-2xl">
          Подбор среди волонтёров, которые откликнулись на задачу. Процент считается по уровням требований к навыкам
          (1–5) и совпадению с навыками волонтёра.
        </p>
      </div>

      {err && <div className="rounded-lg bg-coral/15 border border-coral/40 text-coral text-sm px-4 py-2">{err}</div>}

      <div className="flex flex-col md:flex-row gap-4 md:items-end bg-surface-card border border-white/10 rounded-2xl p-6">
        <div className="flex-1">
          <label className="text-xs text-ink-light uppercase tracking-wide">Задача</label>
          <select
            className="mt-1 w-full rounded-lg bg-ink border border-white/10 px-3 py-2.5"
            value={taskId}
            onChange={(e) => setTaskId(e.target.value === '' ? '' : Number(e.target.value))}
          >
            <option value="">Выберите задачу…</option>
            {selectable.map((t) => (
              <option key={t.id} value={t.id}>
                {t.title} (id {t.id})
              </option>
            ))}
          </select>
        </div>
        <button
          type="button"
          disabled={taskId === '' || running}
          onClick={() => void runMatch()}
          className="px-6 py-2.5 rounded-xl bg-coral text-ink font-semibold disabled:opacity-40 hover:bg-coral/90"
        >
          {running ? 'Расчёт…' : 'Подобрать волонтёров'}
        </button>
      </div>

      {rows.length > 0 && (
        <section>
          <h2 className="font-display text-xl font-semibold mb-4">Рекомендованные кандидаты</h2>
          <ul className="grid gap-4 sm:grid-cols-2">
            {rows.map((r, i) => {
              const pct = scorePercent(r.matchScore);
              return (
                <li
                  key={`${r.volunteerId}-${i}`}
                  className="rounded-2xl border border-white/10 bg-surface-card p-5 flex flex-col gap-3"
                >
                  <div className="flex justify-between items-start gap-2">
                    <div>
                      <p className="font-semibold text-lg">{r.fullName || `Волонтёр #${r.volunteerId}`}</p>
                      <p className="text-xs text-ink-light">user id {r.userId}</p>
                    </div>
                    <span className="text-2xl font-display font-bold text-accent tabular-nums">{pct}%</span>
                  </div>
                  <div className="h-2 rounded-full bg-ink overflow-hidden">
                    <div
                      className="h-full rounded-full bg-gradient-to-r from-accent-dim to-accent transition-all duration-500"
                      style={{ width: `${pct}%` }}
                    />
                  </div>
                  <p className="text-xs text-ink-light">Числовая оценка: {r.matchScore}</p>
                </li>
              );
            })}
          </ul>
        </section>
      )}

      {!running && rows.length === 0 && taskId !== '' && (
        <p className="text-sm text-ink-light">Нажмите «Подобрать волонтёров», чтобы получить ранжированный список.</p>
      )}
    </div>
  );
}
