import { useCallback, useEffect, useState } from 'react';
import * as tasksApi from '../api/tasksApi';
import * as volunteersApi from '../api/volunteersApi';
import * as skillsApi from '../api/skillsApi';
import type { SkillDto, TaskApplicationDto, TaskDto, TaskStatus, VolunteerDto } from '../types';
import { errorMessage } from '../utils/errorMessage';

type Tab = 'tasks' | 'volunteers' | 'applications';

const STATUS_LABEL: Record<string, string> = {
  PENDING: 'На рассмотрении',
  APPROVED: 'Одобрена',
  REJECTED: 'Отклонена',
  WITHDRAWN: 'Отозвана',
  COMPLETED_SUCCESS: 'Завершена (выполнено)',
  COMPLETED_FAILURE: 'Завершена (не выполнено)',
};
const STATUS_COLOR: Record<string, string> = {
  PENDING: 'text-yellow-400',
  APPROVED: 'text-green-400',
  REJECTED: 'text-red-400',
  WITHDRAWN: 'text-gray-400',
  COMPLETED_SUCCESS: 'text-emerald-400',
  COMPLETED_FAILURE: 'text-orange-400',
};

export function CoordinatorDashboardPage() {
  const [tab, setTab] = useState<Tab>('tasks');
  const [tasks, setTasks] = useState<TaskDto[]>([]);
  const [volunteers, setVolunteers] = useState<VolunteerDto[]>([]);
  const [skills, setSkills] = useState<SkillDto[]>([]);
  const [allApplications, setAllApplications] = useState<TaskApplicationDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [msg, setMsg] = useState<string | null>(null);
  const [err, setErr] = useState<string | null>(null);

  // task edit
  const [editingTask, setEditingTask] = useState<TaskDto | null>(null);
  const [editTitle, setEditTitle] = useState('');
  const [editStatus, setEditStatus] = useState<TaskStatus>('OPEN');
  const [editLocation, setEditLocation] = useState('');

  // volunteer rate
  const [ratingVolId, setRatingVolId] = useState<number | null>(null);
  const [ratingValue, setRatingValue] = useState('5.0');

  // applications filter
  const [appFilter, setAppFilter] = useState<string>('ALL');

  const load = useCallback(async () => {
    setLoading(true); setErr(null);
    try {
      const [t, v, sk, apps] = await Promise.all([
        tasksApi.fetchTasks(),
        volunteersApi.fetchVolunteers(),
        skillsApi.fetchSkills(),
        tasksApi.fetchApplications(),
      ]);
      setTasks(t); setVolunteers(v); setSkills(sk); setAllApplications(apps);
    } catch (e) { setErr(errorMessage(e)); }
    finally { setLoading(false); }
  }, []);

  useEffect(() => { void load(); }, [load]);

  function notify(m: string) { setMsg(m); setErr(null); }
  function fail(e: unknown) { setErr(errorMessage(e)); setMsg(null); }

  async function saveTaskEdit() {
    if (!editingTask) return;
    try {
      await tasksApi.updateTask(editingTask.id, {
        title: editTitle, status: editStatus, location: editLocation || null,
        description: editingTask.description, categoryId: editingTask.categoryId,
        startTime: editingTask.startTime, endTime: editingTask.endTime,
      });
      setEditingTask(null); await load(); notify('Задача обновлена');
    } catch (e) { fail(e); }
  }

  async function deleteTask(id: number) {
    if (!confirm('Удалить задачу?')) return;
    try { await tasksApi.deleteTask(id); await load(); notify('Задача удалена'); }
    catch (e) { fail(e); }
  }

  async function deactivateVol(id: number) {
    try { await volunteersApi.deactivateVolunteer(id); await load(); notify('Волонтёр деактивирован'); }
    catch (e) { fail(e); }
  }

  async function activateVol(id: number) {
    try { await volunteersApi.activateVolunteer(id); await load(); notify('Волонтёр активирован'); }
    catch (e) { fail(e); }
  }

  async function rateVol(id: number) {
    try {
      await volunteersApi.rateVolunteer(id, Number(ratingValue));
      setRatingVolId(null); await load(); notify('Рейтинг обновлён');
    } catch (e) { fail(e); }
  }

  async function approveApp(id: number) {
    try { await tasksApi.approveApplication(id); await load(); notify('Заявка одобрена'); }
    catch (e) { fail(e); }
  }

  async function rejectApp(id: number) {
    try { await tasksApi.rejectApplication(id); await load(); notify('Заявка отклонена'); }
    catch (e) { fail(e); }
  }

  const filteredApps = appFilter === 'ALL' ? allApplications : allApplications.filter((a) => a.status === appFilter);

  if (loading) return <p className="text-ink-light">Загрузка…</p>;

  const tabs: { key: Tab; label: string }[] = [
    { key: 'tasks', label: `Все задачи (${tasks.length})` },
    { key: 'volunteers', label: `Волонтёры (${volunteers.length})` },
    { key: 'applications', label: `Все заявки (${allApplications.length})` },
  ];

  return (
    <div className="space-y-6">
      <div className="glass-panel hero-grid p-6 md:p-8">
        <p className="text-xs uppercase tracking-[0.22em] text-accent/90 mb-2">Панель координатора</p>
        <h1 className="font-display text-3xl font-bold mb-1">Надзор и управление всей платформой</h1>
        <p className="text-ink-light text-sm max-w-2xl">
          Централизованно контролируйте задачи, волонтеров и поток заявок в одном интерфейсе.
        </p>
      </div>

      {msg && <div className="rounded-lg bg-accent/15 border border-accent/40 text-accent text-sm px-4 py-2">{msg}</div>}
      {err && <div className="rounded-lg bg-coral/15 border border-coral/40 text-coral text-sm px-4 py-2">{err}</div>}

      {/* Stats bar */}
      <div className="grid grid-cols-3 gap-4">
        {[
          { label: 'Задач', value: tasks.length, color: 'text-accent' },
          { label: 'Волонтёров', value: volunteers.length, color: 'text-blue-400' },
          { label: 'Заявок', value: allApplications.length, color: 'text-yellow-400' },
        ].map((s) => (
          <div key={s.label} className="glass-panel p-4 text-center">
            <p className={`text-3xl font-bold ${s.color}`}>{s.value}</p>
            <p className="text-xs text-ink-light mt-1">{s.label}</p>
          </div>
        ))}
      </div>

      <div className="flex gap-2 border-b border-white/10 pb-2 flex-wrap">
        {tabs.map((t) => (
          <button key={t.key} onClick={() => setTab(t.key)}
            className={`px-4 py-1.5 rounded-lg text-sm font-medium ${tab === t.key ? 'bg-accent text-ink' : 'text-ink-light hover:text-white'}`}>
            {t.label}
          </button>
        ))}
      </div>

      {/* TASKS */}
      {tab === 'tasks' && (
        <div className="space-y-3">
          {tasks.length === 0 && <p className="text-ink-light text-sm">Задач нет.</p>}
          {tasks.map((t) => (
            <article key={t.id} className="glass-panel overflow-hidden">
              {editingTask?.id === t.id ? (
                <div className="p-5 space-y-3">
                  <input className="w-full rounded-lg bg-ink border border-white/10 px-3 py-2"
                    value={editTitle} onChange={(e) => setEditTitle(e.target.value)} />
                  <div className="flex gap-3">
                    <select className="rounded-lg bg-ink border border-white/10 px-3 py-2"
                      value={editStatus} onChange={(e) => setEditStatus(e.target.value as TaskStatus)}>
                      {(['DRAFT', 'OPEN', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'] as TaskStatus[]).map((s) => (
                        <option key={s} value={s}>{s}</option>
                      ))}
                    </select>
                    <input className="flex-1 rounded-lg bg-ink border border-white/10 px-3 py-2"
                      placeholder="Локация" value={editLocation} onChange={(e) => setEditLocation(e.target.value)} />
                  </div>
                  <div className="flex gap-2">
                    <button onClick={() => void saveTaskEdit()} className="px-4 py-2 rounded-xl bg-accent text-ink text-sm font-semibold">Сохранить</button>
                    <button onClick={() => setEditingTask(null)} className="px-4 py-2 rounded-xl border border-white/15 text-sm">Отмена</button>
                  </div>
                </div>
              ) : (
                <div className="px-5 py-4 flex justify-between items-start gap-4">
                  <div>
                    <p className="font-semibold">{t.title}</p>
                    <p className="text-xs text-ink-light">#{t.id} · {t.status} · Организатор #{t.organizerId}{t.location ? ` · ${t.location}` : ''}</p>
                  </div>
                  <div className="flex gap-2 shrink-0">
                    <button onClick={() => { setEditingTask(t); setEditTitle(t.title); setEditStatus(t.status); setEditLocation(t.location ?? ''); }}
                      className="text-xs px-3 py-1 rounded-lg border border-white/15 hover:bg-surface-hover">Изменить</button>
                    <button onClick={() => void deleteTask(t.id)}
                      className="text-xs px-3 py-1 rounded-lg border border-coral/30 text-coral hover:bg-coral/10">Удалить</button>
                  </div>
                </div>
              )}
            </article>
          ))}
        </div>
      )}

      {/* VOLUNTEERS */}
      {tab === 'volunteers' && (
        <div className="space-y-3">
          {volunteers.map((v) => (
            <div key={v.id} className="glass-panel p-4 flex justify-between items-center gap-4">
              <div className="flex items-center gap-3">
                {v.avatarUrl
                  ? <img src={v.avatarUrl} className="w-10 h-10 rounded-full object-cover border border-white/20" alt="" />
                  : <div className="w-10 h-10 rounded-full bg-white/10 flex items-center justify-center text-lg">👤</div>}
                <div>
                  <p className="font-medium">{v.fullName ?? `Волонтёр #${v.id}`}</p>
                  <p className="text-xs text-ink-light">
                    {v.region ?? '—'} · Рейтинг: <span className="text-accent">{Number(v.rating).toFixed(2)}</span> · Задач: {v.completedTasksCount}
                  </p>
                  <p className={`text-xs ${v.active ? 'text-green-400' : 'text-red-400'}`}>{v.active ? '● Активен' : '● Неактивен'}</p>
                </div>
              </div>
              <div className="flex flex-col gap-2 items-end">
                {ratingVolId === v.id ? (
                  <div className="flex gap-2 items-center">
                    <input type="number" min="0" max="5" step="0.1" className="w-20 rounded-lg bg-ink border border-white/10 px-2 py-1 text-sm"
                      value={ratingValue} onChange={(e) => setRatingValue(e.target.value)} />
                    <button onClick={() => void rateVol(v.id)} className="text-xs px-3 py-1 rounded-lg bg-accent/20 text-accent hover:bg-accent/30">Сохранить</button>
                    <button onClick={() => setRatingVolId(null)} className="text-xs text-ink-light hover:text-white">✕</button>
                  </div>
                ) : (
                  <button onClick={() => { setRatingVolId(v.id); setRatingValue('5.0'); }}
                    className="text-xs px-3 py-1 rounded-lg border border-accent/30 text-accent hover:bg-accent/10">Оценить</button>
                )}
                <button
                  onClick={() => v.active ? void deactivateVol(v.id) : void activateVol(v.id)}
                  className={`text-xs px-3 py-1 rounded-lg border ${v.active ? 'border-red-400/30 text-red-400 hover:bg-red-400/10' : 'border-green-400/30 text-green-400 hover:bg-green-400/10'}`}>
                  {v.active ? 'Деактивировать' : 'Активировать'}
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* APPLICATIONS */}
      {tab === 'applications' && (
        <div className="space-y-4">
          <div className="flex gap-2 flex-wrap">
            {[
              'ALL',
              'PENDING',
              'APPROVED',
              'REJECTED',
              'WITHDRAWN',
              'COMPLETED_SUCCESS',
              'COMPLETED_FAILURE',
            ].map((f) => (
              <button key={f} onClick={() => setAppFilter(f)}
                className={`px-3 py-1 rounded-lg text-xs font-medium border ${appFilter === f ? 'bg-accent text-ink border-accent' : 'border-white/15 text-ink-light hover:text-white'}`}>
                {f === 'ALL' ? 'Все' : STATUS_LABEL[f]}
              </button>
            ))}
          </div>
          {filteredApps.length === 0 && <p className="text-ink-light text-sm">Заявок нет.</p>}
          {filteredApps.map((app) => (
            <div key={app.id} className="glass-panel rounded-xl p-4 flex justify-between items-center gap-4">
              <div>
                <p className="text-sm font-medium">Задача #{app.taskId} · Волонтёр #{app.volunteerId}</p>
                {app.message && <p className="text-xs text-ink-light mt-1">"{app.message}"</p>}
                <p className="text-xs text-ink-light">{new Date(app.appliedAt).toLocaleDateString()}</p>
              </div>
              <div className="flex flex-col items-end gap-2">
                <span className={`text-xs font-semibold ${STATUS_COLOR[app.status] ?? ''}`}>
                  {STATUS_LABEL[app.status] ?? app.status}
                </span>
                {app.status === 'PENDING' && (
                  <div className="flex gap-2">
                    <button onClick={() => void approveApp(app.id)}
                      className="text-xs px-3 py-1 rounded-lg bg-green-500/20 text-green-400 hover:bg-green-500/30">Одобрить</button>
                    <button onClick={() => void rejectApp(app.id)}
                      className="text-xs px-3 py-1 rounded-lg bg-red-500/20 text-red-400 hover:bg-red-500/30">Отклонить</button>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
