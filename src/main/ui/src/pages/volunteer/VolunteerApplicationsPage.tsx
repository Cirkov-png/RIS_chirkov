import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import * as volunteersApi from '../../api/volunteersApi';
import type { TaskApplicationDto, VolunteerDto } from '../../types';
import { errorMessage } from '../../utils/errorMessage';
import { applicationStatusRu } from '../../utils/applicationStatus';

export function VolunteerApplicationsPage() {
  const { user } = useAuth();
  const [volunteer, setVolunteer] = useState<VolunteerDto | null>(null);
  const [applications, setApplications] = useState<TaskApplicationDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [msg, setMsg] = useState<string | null>(null);
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

  async function withdraw(applicationId: number) {
    if (!volunteer) return;
    setErr(null);
    setMsg(null);
    try {
      await volunteersApi.withdrawApplication(volunteer.id, applicationId);
      await load();
      setMsg('Заявка отозвана');
    } catch (e) {
      setErr(errorMessage(e));
    }
  }

  if (loading) return <p className="text-ink-light">Загрузка…</p>;
  if (!volunteer) {
    return <div className="text-coral">Профиль волонтёра не найден.</div>;
  }

  return (
    <section className="bg-surface-card border border-white/10 rounded-2xl p-6 space-y-3">
      <h2 className="font-display text-xl font-semibold text-coral">Мои заявки</h2>
      {msg && <div className="rounded-lg bg-accent/15 border border-accent/40 text-accent text-sm px-4 py-2">{msg}</div>}
      {err && <div className="rounded-lg bg-coral/15 border border-coral/40 text-coral text-sm px-4 py-2">{err}</div>}
      {applications.length === 0 && <p className="text-sm text-ink-light">Заявок пока нет.</p>}
      {applications.map((a) => (
        <article key={a.id} className="rounded-xl border border-white/10 p-4 flex flex-wrap justify-between gap-3">
          <div>
            <p className="font-semibold">
              Задача #{a.taskId}{' '}
              <Link to={`/volunteer/tasks/${a.taskId}`} className="text-xs text-accent underline font-normal">
                открыть
              </Link>
            </p>
            <p className="text-xs text-ink-light">{a.message || 'Без комментария'}</p>
            <p className="text-xs text-ink-light">{new Date(a.appliedAt).toLocaleString()}</p>
          </div>
          <div className="flex items-center gap-2">
            <span className="text-xs px-2 py-1 rounded bg-surface-hover">{applicationStatusRu(a.status)}</span>
            {a.status === 'PENDING' && (
              <button type="button" onClick={() => void withdraw(a.id)} className="text-xs px-3 py-1 rounded border border-coral/40 text-coral">
                Отозвать
              </button>
            )}
          </div>
        </article>
      ))}
    </section>
  );
}
