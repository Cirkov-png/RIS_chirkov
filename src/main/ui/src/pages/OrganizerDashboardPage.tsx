import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import * as tasksApi from '../api/tasksApi';
import * as taskRequirementsApi from '../api/taskRequirementsApi';
import * as skillsApi from '../api/skillsApi';
import * as categoriesApi from '../api/categoriesApi';
import type { CategoryDto, SkillDto, TaskDto, TaskRequirementDto } from '../types';
import { errorMessage } from '../utils/errorMessage';
import { BY_REGIONS } from '../utils/regions';
import { taskStatusRu } from '../utils/taskStatus';

type CreateReqRow = { skillId: number | ''; level: number };

export function OrganizerDashboardPage() {
  type Tab = 'create' | 'tasks';
  const { user } = useAuth();
  const [tab, setTab] = useState<Tab>('create');
  const [tasks, setTasks] = useState<TaskDto[]>([]);
  const [requirements, setRequirements] = useState<TaskRequirementDto[]>([]);
  const [skills, setSkills] = useState<SkillDto[]>([]);
  const [categories, setCategories] = useState<CategoryDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);
  const [msg, setMsg] = useState<string | null>(null);

  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [location, setLocation] = useState('');
  const [categoryId, setCategoryId] = useState<number | ''>('');
  const [createRequirements, setCreateRequirements] = useState<CreateReqRow[]>([
    { skillId: '', level: 3 },
  ]);

  const [expandedId, setExpandedId] = useState<number | null>(null);
  const [reqSkillId, setReqSkillId] = useState<number | ''>('');
  const [reqLevel, setReqLevel] = useState(3);
  const load = useCallback(async () => {
    setLoading(true);
    setErr(null);
    try {
      const [t, r, sk, cat] = await Promise.all([
        tasksApi.fetchTasks(),
        taskRequirementsApi.fetchTaskRequirements(),
        skillsApi.fetchSkills(),
        categoriesApi.fetchCategories(),
      ]);
      setTasks(t);
      setRequirements(r);
      setSkills(sk);
      setCategories(cat);
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
    return tasks.filter((t) => t.organizerId === user.userId);
  }, [tasks, user]);
  const reqsFor = (taskId: number) => requirements.filter((x) => x.taskId === taskId);

  const skillName = (id: number) => skills.find((s) => s.id === id)?.name ?? `#${id}`;

  async function createTask(e: React.FormEvent) {
    e.preventDefault();
    if (!user) return;
    setErr(null);
    setMsg(null);
    try {
      const filled = createRequirements.filter((r) => r.skillId !== '');
      const skillIds = filled.map((r) => Number(r.skillId));
      if (new Set(skillIds).size !== skillIds.length) {
        setErr('Один и тот же навык нельзя указать дважды.');
        return;
      }
      const created = await tasksApi.createTask({
        title: title.trim(),
        description: description.trim() || null,
        categoryId: categoryId === '' ? null : Number(categoryId),
        status: 'OPEN',
        location: location.trim() || null,
        startTime: null,
        endTime: null,
      });
      for (const row of filled) {
        const lv = Math.min(5, Math.max(1, Math.round(row.level)));
        await taskRequirementsApi.createTaskRequirement({
          taskId: created.id,
          skillId: Number(row.skillId),
          importanceWeight: lv,
        });
      }
      setTitle('');
      setDescription('');
      setLocation('');
      setCategoryId('');
      setCreateRequirements([{ skillId: '', level: 3 }]);
      await load();
      setMsg('Задача создана');
    } catch (e) {
      setErr(errorMessage(e));
    }
  }

  async function addRequirement(taskId: number) {
    if (reqSkillId === '') return;
    setErr(null);
    setMsg(null);
    try {
      await taskRequirementsApi.createTaskRequirement({
        taskId,
        skillId: Number(reqSkillId),
        importanceWeight: Math.min(5, Math.max(1, Math.round(reqLevel))),
      });
      setReqSkillId('');
      setReqLevel(3);
      await load();
      setMsg('Требование добавлено');
    } catch (e) {
      setErr(errorMessage(e));
    }
  }

  async function removeRequirement(id: number) {
    setErr(null);
    try {
      await taskRequirementsApi.deleteTaskRequirement(id);
      await load();
    } catch (e) {
      setErr(errorMessage(e));
    }
  }

  async function deleteMyTask(taskId: number) {
    if (!confirm('Удалить задачу вместе с откликами и требованиями? Это действие необратимо.')) return;
    setErr(null);
    setMsg(null);
    try {
      await tasksApi.deleteTask(taskId);
      await load();
      setMsg('Задача удалена');
    } catch (e) {
      setErr(errorMessage(e));
    }
  }

  if (loading) return <p className="text-ink-light">Загрузка…</p>;

  return (
    <div className="space-y-8">
      <div className="glass-panel hero-grid p-6 md:p-8">
        <p className="text-xs uppercase tracking-[0.24em] text-accent/90 mb-2">Кабинет организатора</p>
        <h1 className="font-display text-3xl md:text-4xl font-bold mb-2">Управляйте задачами и требованиями</h1>
        <p className="text-ink-light max-w-3xl">
          Новая задача сразу публикуется как открытая. Требования к навыкам задаются при создании; регион — из того же
          списка, что и в профилях волонтёров.
        </p>
        <div className="grid sm:grid-cols-3 gap-3 mt-5">
          <div className="rounded-xl bg-ink/60 border border-white/10 px-4 py-3">
            <p className="text-xs text-ink-light">Моих задач</p>
            <p className="text-2xl font-bold text-accent">{myTasks.length}</p>
          </div>
          <div className="rounded-xl bg-ink/60 border border-white/10 px-4 py-3">
            <p className="text-xs text-ink-light">Навыков в системе</p>
            <p className="text-2xl font-bold text-blue-300">{skills.length}</p>
          </div>
          <div className="rounded-xl bg-ink/60 border border-white/10 px-4 py-3">
            <p className="text-xs text-ink-light">Категорий</p>
            <p className="text-2xl font-bold text-coral">{categories.length}</p>
          </div>
        </div>
      </div>

      {msg && (
        <div className="rounded-lg bg-accent/15 border border-accent/40 text-accent text-sm px-4 py-2">{msg}</div>
      )}
      {err && <div className="rounded-lg bg-coral/15 border border-coral/40 text-coral text-sm px-4 py-2">{err}</div>}

      <div className="flex flex-wrap gap-2 border-b border-white/10 pb-2">
        <button
          type="button"
          onClick={() => setTab('create')}
          className={`px-3 py-1.5 rounded-lg text-sm ${tab === 'create' ? 'bg-accent/20 text-accent' : 'text-ink-light hover:text-white'}`}
        >
          Создать задачу
        </button>
        <button
          type="button"
          onClick={() => setTab('tasks')}
          className={`px-3 py-1.5 rounded-lg text-sm ${tab === 'tasks' ? 'bg-accent/20 text-accent' : 'text-ink-light hover:text-white'}`}
        >
          Мои задачи
        </button>
      </div>

      {tab === 'create' && (
        <section className="glass-panel p-6 md:p-8">
          <h2 className="font-display text-xl font-semibold mb-4 text-accent">Новая задача</h2>
          <form onSubmit={createTask} className="grid md:grid-cols-2 gap-4">
            <div className="md:col-span-2">
              <label className="text-xs text-ink-light uppercase">Название</label>
              <input className="mt-1 w-full rounded-lg bg-ink border border-white/10 px-3 py-2" value={title} onChange={(e) => setTitle(e.target.value)} required />
            </div>
            <div className="md:col-span-2">
              <label className="text-xs text-ink-light uppercase">Описание</label>
              <textarea className="mt-1 w-full rounded-lg bg-ink border border-white/10 px-3 py-2 min-h-[80px]" value={description} onChange={(e) => setDescription(e.target.value)} />
            </div>
            <div className="md:col-span-2">
              <label className="text-xs text-ink-light uppercase">Регион (как у волонтёров)</label>
              <select
                className="mt-1 w-full rounded-lg bg-ink border border-white/10 px-3 py-2"
                value={location}
                onChange={(e) => setLocation(e.target.value)}
              >
                <option value="">Не указано</option>
                {BY_REGIONS.map((r) => (
                  <option key={r} value={r}>
                    {r}
                  </option>
                ))}
              </select>
            </div>
            <div className="md:col-span-2">
              <label className="text-xs text-ink-light uppercase">Категория (необязательно)</label>
              <select className="mt-1 w-full rounded-lg bg-ink border border-white/10 px-3 py-2" value={categoryId} onChange={(e) => setCategoryId(e.target.value === '' ? '' : Number(e.target.value))}>
                <option value="">—</option>
                {categories.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name}
                  </option>
                ))}
              </select>
            </div>
            <div className="md:col-span-2 space-y-3 rounded-xl border border-white/10 bg-ink/50 p-4">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <h3 className="font-display text-sm font-semibold text-accent">Требования к навыкам</h3>
                <button
                  type="button"
                  onClick={() => setCreateRequirements((rows) => [...rows, { skillId: '', level: 3 }])}
                  className="text-xs px-3 py-1.5 rounded-lg border border-white/15 hover:bg-surface-hover"
                >
                  + Ещё навык
                </button>
              </div>
              <p className="text-xs text-ink-light">Для каждой строки выберите навык и желаемый уровень владения от 1 до 5.</p>
              {createRequirements.map((row, idx) => (
                <div key={idx} className="flex flex-col sm:flex-row gap-2 sm:items-end">
                  <div className="flex-1">
                    <label className="text-xs text-ink-light">Навык</label>
                    <select
                      className="mt-1 w-full rounded-lg bg-ink border border-white/10 px-3 py-2"
                      value={row.skillId === '' ? '' : row.skillId}
                      onChange={(e) => {
                        const v = e.target.value === '' ? '' : Number(e.target.value);
                        setCreateRequirements((rows) => rows.map((r, i) => (i === idx ? { ...r, skillId: v } : r)));
                      }}
                    >
                      <option value="">Не выбрано</option>
                      {skills.map((s) => (
                        <option key={s.id} value={s.id}>
                          {s.name}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div className="w-full sm:w-28">
                    <label className="text-xs text-ink-light">Уровень (1–5)</label>
                    <input
                      type="number"
                      min={1}
                      max={5}
                      className="mt-1 w-full rounded-lg bg-ink border border-white/10 px-3 py-2"
                      value={row.level}
                      onChange={(e) => {
                        const n = Number(e.target.value);
                        setCreateRequirements((rows) => rows.map((r, i) => (i === idx ? { ...r, level: n } : r)));
                      }}
                    />
                  </div>
                  {createRequirements.length > 1 && (
                    <button
                      type="button"
                      className="text-xs text-coral px-2 py-2 sm:mb-0"
                      onClick={() => setCreateRequirements((rows) => rows.filter((_, i) => i !== idx))}
                    >
                      Удалить строку
                    </button>
                  )}
                </div>
              ))}
            </div>
            <div className="md:col-span-2">
              <button type="submit" className="px-6 py-2.5 rounded-xl bg-accent text-ink font-semibold">Создать задачу</button>
            </div>
          </form>
        </section>
      )}

      {tab === 'tasks' && (
        <section>
          <h2 className="font-display text-xl font-semibold mb-4">Мои задачи</h2>
          <p className="text-sm text-ink-light mb-4">
            Здесь можно добавить или убрать требования позже. При создании задачи требования уже задаются на первом шаге.
          </p>
          <div className="space-y-4">
            {myTasks.length === 0 && <p className="text-ink-light text-sm">Задач пока нет.</p>}
            {myTasks.map((t) => (
              <article key={t.id} className="glass-panel overflow-hidden">
                <button
                  type="button"
                  className="w-full text-left px-5 py-4 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2 hover:bg-surface-hover"
                  onClick={() => setExpandedId(expandedId === t.id ? null : t.id)}
                >
                  <div>
                    <p className="font-semibold text-lg">{t.title}</p>
                    <p className="text-xs text-ink-light">
                      id {t.id} · {taskStatusRu(t.status)}
                      {t.location ? ` · ${t.location}` : ''}
                    </p>
                  </div>
                  <div className="flex items-center gap-2 shrink-0">
                    <button
                      type="button"
                      className="text-xs px-2 py-1 rounded-lg border border-coral/40 text-coral hover:bg-coral/10"
                      onClick={(e) => {
                        e.stopPropagation();
                        void deleteMyTask(t.id);
                      }}
                    >
                      Удалить задачу
                    </button>
                    <span className="text-xs text-accent">{expandedId === t.id ? 'Свернуть' : 'Требования'}</span>
                  </div>
                </button>
                {expandedId === t.id && (
                  <div className="border-t border-white/10 px-5 py-4 space-y-4 bg-ink/40">
                    <ul className="space-y-2">
                      {reqsFor(t.id).length === 0 && (
                        <li className="text-sm text-ink-light">Требований нет — добавьте навык и уровень (1–5).</li>
                      )}
                      {reqsFor(t.id).map((r) => (
                        <li
                          key={r.id}
                          className="flex flex-wrap items-center justify-between gap-2 text-sm border border-white/5 rounded-lg px-3 py-2"
                        >
                          <span>
                            {skillName(r.skillId)} — уровень <strong>{r.importanceWeight}</strong>
                          </span>
                          <button
                            type="button"
                            className="text-coral text-xs hover:underline"
                            onClick={() => void removeRequirement(r.id)}
                          >
                            Удалить
                          </button>
                        </li>
                      ))}
                    </ul>
                    <div className="flex flex-col md:flex-row gap-2 md:items-end">
                      <div className="flex-1">
                        <label className="text-xs text-ink-light">Навык</label>
                        <select
                          className="mt-1 w-full rounded-lg bg-ink border border-white/10 px-3 py-2"
                          value={reqSkillId}
                          onChange={(e) => setReqSkillId(e.target.value === '' ? '' : Number(e.target.value))}
                        >
                          <option value="">Выберите…</option>
                          {skills.map((s) => (
                            <option key={s.id} value={s.id}>
                              {s.name}
                            </option>
                          ))}
                        </select>
                      </div>
                      <div className="w-full md:w-32">
                        <label className="text-xs text-ink-light">Уровень (1–5)</label>
                        <input
                          type="number"
                          min={1}
                          max={5}
                          className="mt-1 w-full rounded-lg bg-ink border border-white/10 px-3 py-2"
                          value={reqLevel}
                          onChange={(e) => setReqLevel(Number(e.target.value))}
                        />
                      </div>
                      <button
                        type="button"
                        disabled={reqSkillId === ''}
                        onClick={() => void addRequirement(t.id)}
                        className="px-4 py-2 rounded-xl bg-surface-hover border border-white/15 text-sm disabled:opacity-40"
                      >
                        Добавить требование
                      </button>
                    </div>
                  </div>
                )}
              </article>
            ))}
          </div>
        </section>
      )}

    </div>
  );
}
