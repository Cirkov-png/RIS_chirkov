import { useCallback, useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import * as usersApi from '../api/usersApi';
import type { UserPublicDto } from '../types';
import { errorMessage } from '../utils/errorMessage';
import { AvatarImg } from '../components/AvatarImg';

export function OrganizerSelfProfilePage() {
  const { user } = useAuth();
  const [profile, setProfile] = useState<UserPublicDto | null>(null);
  const [fullName, setFullName] = useState('');
  const [phone, setPhone] = useState('');
  const [bio, setBio] = useState('');
  const [avatarUrl, setAvatarUrl] = useState('');
  const [loading, setLoading] = useState(true);
  const [msg, setMsg] = useState<string | null>(null);
  const [err, setErr] = useState<string | null>(null);

  const load = useCallback(async () => {
    if (!user) return;
    setLoading(true);
    setErr(null);
    try {
      const u = await usersApi.fetchUser(user.userId);
      setProfile(u);
      setFullName(u.profileFullName ?? '');
      setPhone(u.profilePhone ?? '');
      setBio(u.profileBio ?? '');
      setAvatarUrl(u.profileAvatarUrl ?? '');
    } catch (e) {
      setErr(errorMessage(e));
    } finally {
      setLoading(false);
    }
  }, [user]);

  useEffect(() => {
    void load();
  }, [load]);

  async function save() {
    setMsg(null);
    setErr(null);
    try {
      const u = await usersApi.patchMyOrganizerProfile({
        profileFullName: fullName.trim() || null,
        profilePhone: phone.trim() || null,
        profileBio: bio.trim() || null,
        profileAvatarUrl: avatarUrl.trim() || null,
      });
      setProfile(u);
      setMsg('Профиль сохранён');
    } catch (e) {
      setErr(errorMessage(e));
    }
  }

  if (loading) return <p className="text-ink-light">Загрузка…</p>;

  return (
    <div className="space-y-6 max-w-2xl">
      <div>
        <h1 className="font-display text-3xl font-bold mb-2">Профиль организатора</h1>
        <p className="text-sm text-ink-light">
          Эти данные видят волонтёры в карточке задачи. Логин: <span className="text-stone-200">{profile?.username}</span>
        </p>
      </div>
      {msg && <div className="rounded-lg bg-accent/15 border border-accent/40 text-accent text-sm px-4 py-2">{msg}</div>}
      {err && <div className="rounded-lg bg-coral/15 border border-coral/40 text-coral text-sm px-4 py-2">{err}</div>}

      <section className="bg-surface-card border border-white/10 rounded-2xl p-6 space-y-4">
        <div className="flex flex-col sm:flex-row gap-4 items-start">
          <div className="w-24 h-24 rounded-full border border-white/15 overflow-hidden shrink-0">
            <AvatarImg src={avatarUrl} className="w-full h-full object-cover" alt="Аватар" />
          </div>
          <div className="flex-1 w-full space-y-2">
            <label className="text-xs text-ink-light uppercase tracking-wide">URL аватара (https)</label>
            <input
              className="w-full rounded-lg bg-ink border border-white/10 px-3 py-2 text-sm"
              placeholder="https://..."
              value={avatarUrl}
              onChange={(e) => setAvatarUrl(e.target.value)}
            />
            <p className="text-xs text-ink-light">
              Прямая ссылка на изображение. Если не отображается — попробуйте другой хостинг или формат (png, jpg).
            </p>
          </div>
        </div>
        <div>
          <label className="text-xs text-ink-light uppercase tracking-wide">Как вас представлять</label>
          <input
            className="mt-1 w-full rounded-lg bg-ink border border-white/10 px-3 py-2"
            value={fullName}
            onChange={(e) => setFullName(e.target.value)}
            placeholder="ФИО или название"
          />
        </div>
        <div>
          <label className="text-xs text-ink-light uppercase tracking-wide">Телефон для связи</label>
          <input className="mt-1 w-full rounded-lg bg-ink border border-white/10 px-3 py-2" value={phone} onChange={(e) => setPhone(e.target.value)} />
        </div>
        <div>
          <label className="text-xs text-ink-light uppercase tracking-wide">О себе / об организации</label>
          <textarea className="mt-1 w-full rounded-lg bg-ink border border-white/10 px-3 py-2 min-h-[120px]" value={bio} onChange={(e) => setBio(e.target.value)} />
        </div>
        <button type="button" onClick={() => void save()} className="px-5 py-2.5 rounded-xl bg-accent text-ink font-semibold">
          Сохранить
        </button>
      </section>
    </div>
  );
}
