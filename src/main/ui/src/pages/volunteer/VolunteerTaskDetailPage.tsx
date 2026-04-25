import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import * as volunteersApi from '../../api/volunteersApi';
import * as tasksApi from '../../api/tasksApi';
import * as taskRequirementsApi from '../../api/taskRequirementsApi';
import * as usersApi from '../../api/usersApi';
import type { ApplicationStatus, TaskApplicationDto, TaskDto, TaskRequirementDto, UserPublicDto, VolunteerDto } from '../../types';
import { errorMessage } from '../../utils/errorMessage';
import { applicationStatusRu } from '../../utils/applicationStatus';
import { taskStatusRu } from '../../utils/taskStatus';

export function VolunteerTaskDetailPage() {
  const { taskId: taskIdParam } = useParams<{ taskId: string }>();
  const taskId = taskIdParam ? Number(taskIdParam) : NaN;
  const nav = useNavigate();
  const { user } = useAuth();

  const [volunteer, setVolunteer] = useState<VolunteerDto | null>(null);
  const [task, setTask] = useState<TaskDto | null>(null);
  const [requirements, setRequirements] = useState<TaskRequirementDto[]>([]);
  const [organizer, setOrganizer] = useState<UserPublicDto | null>(null);
  const [application, setApplication] = useState<TaskApplicationDto | null>(null);
  const [applyMessage, setApplyMessage] = useState('');
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);
  const [msg, setMsg] = useState<string | null>(null);
  const [applying, setApplying] = useState(false);

  const load = useCallback(async () => {
    if (!user || !Number.isFinite(taskId)) return;
    setLoading(true);
    setErr(null);
    try {
      const vols = await volunteersApi.fetchVolunteers();
      const mine = vols.find((v) => v.userId === user.userId) ?? null;
      setVolunteer(mine);
      const t = await tasksApi.fetchTask(taskId);
      setTask(t);
      const reqs = await taskRequirementsApi.fetchTaskRequirementsByTask(taskId);
      setRequirements(reqs);
      const org = await usersApi.fetchUser(t.organizerId);
      setOrganizer(org);
      if (mine) {
        const app = await volunteersApi.fetchApplicationForTask(mine.id, taskId);
        setApplication(app);
      } else {
        setApplication(null);
      }
    } catch (e) {
      setErr(errorMessage(e));
    } finally {
      setLoading(false);
    }
  }, [user, taskId]);

  useEffect(() => {
    void load();
  }, [load]);

  const hasActiveApplication = Boolean(
    application && (application.status === 'PENDING' || application.status === 'APPROVED'),
  );
  const canApply = Boolean(volunteer && task && task.status === 'OPEN' && !hasActiveApplication);

  async function apply() {
    if (!volunteer || !Number.isFinite(taskId)) return;
    setApplying(true);
    setErr(null);
    setMsg(null);
    try {
      await volunteersApi.applyToTask(volunteer.id, {
        taskId,
        message: applyMessage.trim() || null,
      });
      setApplyMessage('');
      setMsg('Заявка отправлена');
      await load();
    } catch (e) {
      setErr(errorMessage(e));
    } finally {
      setApplying(false);
    }
  }

  async function withdraw(applicationId: number) {
    if (!volunteer) return;
    setErr(null);
    try {
      await volunteersApi.withdrawApplication(volunteer.id, applicationId);
      await load();
      setMsg('Заявка отозвана');
    } catch (e) {
      setErr(errorMessage(e));
    }
  }

  if (loading) return <p className="text-ink-light">Загрузка…</p>;
  if (!Number.isFinite(taskId) || !task) {
    return (
      <div className="rounded-xl border border-coral/40 bg-coral/10 p-6 text-coral">
        Задача не найдена.
        <button type="button" className="block mt-4 text-accent underline" onClick={() => nav('/volunteer/tasks')}>
          К списку задач
        </button>
      </div>
    );
  }

  if (!volunteer) {
    return <div className="text-coral">Профиль волонтёра не найден.</div>;
  }

  return (
    <div className="space-y-6 max-w-3xl">
      <button type="button" onClick={() => nav(-1)} className="text-sm text-ink-light hover:text-white">
        ← Назад
      </button>

      {msg && <div className="rounded-lg bg-accent/15 border border-accent/40 text-accent text-sm px-4 py-2">{msg}</div>}
      {err && <div className="rounded-lg bg-coral/15 border border-coral/40 text-coral text-sm px-4 py-2">{err}</div>}

      <article className="bg-surface-card border border-white/10 rounded-2xl p-6 md:p-8 space-y-4">
        <div>
          <h1 className="font-display text-2xl md:text-3xl font-bold">{task.title}</h1>
          <p className="text-sm text-ink-light mt-2">
            {taskStatusRu(task.status)}
            {task.location ? ` · ${task.location}` : ''}
            {task.startTime ? ` · начало: ${new Date(task.startTime).toLocaleString()}` : ''}
            {task.endTime ? ` · конец: ${new Date(task.endTime).toLocaleString()}` : ''}
          </p>
        </div>

        {task.description && (
          <div>
            <h2 className="text-sm font-semibold text-accent uppercase tracking-wide mb-2">Описание</h2>
            <p className="text-stone-200 whitespace-pre-wrap">{task.description}</p>
          </div>
        )}

        <div>
          <h2 className="text-sm font-semibold text-accent uppercase tracking-wide mb-2">Нужные навыки</h2>
          {requirements.length === 0 ? (
            <p className="text-sm text-ink-light">Организатор не указал требований к навыкам.</p>
          ) : (
            <ul className="space-y-2">
              {requirements.map((r) => (
                <li
                  key={r.id}
                  className="rounded-lg border border-white/10 px-3 py-2 flex justify-between gap-2 text-sm"
                >
                  <span>{r.skillName ?? `Навык #${r.skillId}`}</span>
                  <span className="text-ink-light">уровень ≥ {Number(r.importanceWeight).toFixed(0)}</span>
                </li>
              ))}
            </ul>
          )}
        </div>

        {organizer && (
          <div className="rounded-xl border border-white/10 bg-ink/40 p-4">
            <h2 className="text-sm font-semibold text-accent uppercase tracking-wide mb-2">Организатор</h2>
            <p className="text-stone-200">
              {organizer.profileFullName ?? organizer.username}{' '}
              <Link className="text-accent text-sm underline ml-1" to={`/organizers/${organizer.id}`}>
                Профиль
              </Link>
            </p>
          </div>
        )}

        <div className="border-t border-white/10 pt-4">
          {application?.status === 'PENDING' && (
            <div className="flex flex-wrap items-center gap-3">
              <span className="text-xs px-2 py-1 rounded bg-yellow-500/20 text-yellow-400">
                {applicationStatusRu('PENDING')}
              </span>
              <button
                type="button"
                onClick={() => void withdraw(application.id)}
                className="text-sm px-3 py-1.5 rounded-lg border border-coral/40 text-coral"
              >
                Отозвать заявку
              </button>
            </div>
          )}
          {application?.status === 'APPROVED' && (
            <span className="text-xs px-2 py-1 rounded bg-green-500/20 text-green-400">
              {applicationStatusRu('APPROVED')}
            </span>
          )}
          {application &&
            application.status !== 'PENDING' &&
            application.status !== 'APPROVED' && (
              <span className="text-xs px-2 py-1 rounded bg-surface-hover text-ink-light">
                {applicationStatusRu(application.status as ApplicationStatus)}
              </span>
            )}
          {canApply && (
            <div className="space-y-2">
              <label className="text-xs text-ink-light block">Комментарий к отклику (необязательно)</label>
              <textarea
                className="w-full rounded-lg bg-ink border border-white/10 px-3 py-2 text-sm min-h-[72px]"
                value={applyMessage}
                onChange={(e) => setApplyMessage(e.target.value)}
                placeholder="Коротко о себе и мотивации"
              />
              <button
                type="button"
                disabled={applying}
                onClick={() => void apply()}
                className="px-5 py-2.5 rounded-xl bg-accent text-ink font-semibold disabled:opacity-40"
              >
                {applying ? 'Отправка…' : 'Откликнуться'}
              </button>
            </div>
          )}
          {!canApply && !application && task.status !== 'OPEN' && (
            <p className="text-sm text-ink-light">На эту задачу отклик невозможен (задача не открыта).</p>
          )}
        </div>
      </article>
    </div>
  );
}
