import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import * as usersApi from '../api/usersApi';
import type { UserPublicDto } from '../types';
import { errorMessage } from '../utils/errorMessage';
import { AvatarImg } from '../components/AvatarImg';

export function OrganizerPublicProfilePage() {
  const { userId: idParam } = useParams<{ userId: string }>();
  const userId = idParam ? Number(idParam) : NaN;
  const nav = useNavigate();
  const [u, setU] = useState<UserPublicDto | null>(null);
  const [err, setErr] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!Number.isFinite(userId)) {
      setLoading(false);
      return;
    }
    setLoading(true);
    setErr(null);
    usersApi
      .fetchUser(userId)
      .then(setU)
      .catch((e) => setErr(errorMessage(e)))
      .finally(() => setLoading(false));
  }, [userId]);

  if (loading) return <p className="text-ink-light">Загрузка…</p>;
  if (err || !u) {
    return (
      <div className="rounded-xl border border-coral/40 bg-coral/10 p-6 text-coral max-w-lg">
        {err ?? 'Пользователь не найден'}
        <button type="button" className="block mt-4 text-accent underline" onClick={() => nav(-1)}>
          Назад
        </button>
      </div>
    );
  }

  const title = u.profileFullName ?? u.username;
  const isOrganizer = u.role === 'ORGANIZER';

  return (
    <div className="space-y-6 max-w-2xl">
      <button type="button" onClick={() => nav(-1)} className="text-sm text-ink-light hover:text-white">
        ← Назад
      </button>

      <div className="bg-surface-card border border-white/10 rounded-2xl p-6 flex gap-5 items-start">
        <div className="w-24 h-24 rounded-full border border-white/15 overflow-hidden shrink-0">
          <AvatarImg src={u.profileAvatarUrl} className="w-full h-full object-cover" alt="" />
        </div>
        <div className="space-y-2">
          <h1 className="font-display text-2xl font-bold">{title}</h1>
          <p className="text-xs text-ink-light uppercase tracking-wide">{u.role}</p>
          {!isOrganizer && <p className="text-sm text-ink-light">Публичная карточка предназначена для организаторов.</p>}
          {u.profilePhone && (
            <p className="text-sm text-ink-light">
              📞 <span className="text-stone-200">{u.profilePhone}</span>
            </p>
          )}
        </div>
      </div>

      {u.profileBio && (
        <div className="bg-surface-card border border-white/10 rounded-2xl p-5">
          <h2 className="font-semibold mb-2 text-accent">О себе</h2>
          <p className="text-stone-200 whitespace-pre-wrap">{u.profileBio}</p>
        </div>
      )}
    </div>
  );
}
