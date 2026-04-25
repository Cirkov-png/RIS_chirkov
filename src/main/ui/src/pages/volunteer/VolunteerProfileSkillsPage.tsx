import { useCallback, useEffect, useMemo, useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import * as volunteersApi from '../../api/volunteersApi';
import * as skillsApi from '../../api/skillsApi';
import * as volunteerSkillsApi from '../../api/volunteerSkillsApi';
import type { VolunteerDto, VolunteerSkillDto } from '../../types';
import { errorMessage } from '../../utils/errorMessage';
import { BY_REGIONS } from '../../utils/regions';
import { AvatarImg } from '../../components/AvatarImg';

export function VolunteerProfileSkillsPage() {
  const { user } = useAuth();
  const [volunteer, setVolunteer] = useState<VolunteerDto | null>(null);
  const [skills, setSkills] = useState<{ id: number; name: string }[]>([]);
  const [vSkills, setVSkills] = useState<VolunteerSkillDto[]>([]);
  const [fullName, setFullName] = useState('');
  const [phone, setPhone] = useState('');
  const [region, setRegion] = useState('');
  const [bio, setBio] = useState('');
  const [birthDate, setBirthDate] = useState('');
  const [avatarUrl, setAvatarUrl] = useState('');
  const [addSkillId, setAddSkillId] = useState<number | ''>('');
  const [addLevel, setAddLevel] = useState(3);
  const [loading, setLoading] = useState(true);
  const [msg, setMsg] = useState<string | null>(null);
  const [err, setErr] = useState<string | null>(null);

  const load = useCallback(async () => {
    if (!user) return;
    setLoading(true);
    setErr(null);
    try {
      const [vols, sk, vs] = await Promise.all([
        volunteersApi.fetchVolunteers(),
        skillsApi.fetchSkills(),
        volunteerSkillsApi.fetchVolunteerSkills(),
      ]);
      const mine = vols.find((v) => v.userId === user.userId) ?? null;
      setVolunteer(mine);
      setSkills(sk.map((x) => ({ id: x.id, name: x.name })));
      setVSkills(mine ? vs.filter((x) => x.volunteerId === mine.id) : []);
      if (mine) {
        setFullName(mine.fullName ?? '');
        setPhone(mine.phone ?? '');
        setRegion(mine.region ?? '');
        setBio(mine.bio ?? '');
        setBirthDate(mine.birthDate ?? '');
        setAvatarUrl(mine.avatarUrl ?? '');
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

  const skillName = useCallback((id: number) => skills.find((s) => s.id === id)?.name ?? `#${id}`, [skills]);

  const availableToAdd = useMemo(() => {
    if (!volunteer) return [];
    const taken = new Set(vSkills.map((x) => x.skillId));
    return skills.filter((s) => !taken.has(s.id));
  }, [skills, vSkills, volunteer]);

  async function saveProfile() {
    if (!volunteer || !user) return;
    setMsg(null);
    setErr(null);
    try {
      const updated = await volunteersApi.updateVolunteer(volunteer.id, {
        userId: user.userId,
        fullName: fullName || null,
        phone: phone || null,
        region: region || null,
        bio: bio || null,
        birthDate: birthDate || null,
        avatarUrl: avatarUrl.trim() || null,
        active: true,
      });
      setVolunteer(updated);
      setMsg('Профиль сохранён');
    } catch (e) {
      setErr(errorMessage(e));
    }
  }

  async function addSkill() {
    if (!volunteer || addSkillId === '') return;
    setErr(null);
    try {
      await volunteerSkillsApi.createVolunteerSkill({
        volunteerId: volunteer.id,
        skillId: Number(addSkillId),
        proficiencyLevel: addLevel,
      });
      setAddSkillId('');
      setAddLevel(3);
      await load();
      setMsg('Навык добавлен');
    } catch (e) {
      setErr(errorMessage(e));
    }
  }

  async function removeSkill(id: number) {
    setErr(null);
    try {
      await volunteerSkillsApi.deleteVolunteerSkill(id);
      await load();
      setMsg('Навык удалён');
    } catch (e) {
      setErr(errorMessage(e));
    }
  }

  async function updateLevel(row: VolunteerSkillDto, level: number) {
    if (!volunteer) return;
    const lv = Math.min(5, Math.max(1, Math.round(level)));
    setErr(null);
    try {
      await volunteerSkillsApi.updateVolunteerSkill(row.id, {
        volunteerId: volunteer.id,
        skillId: row.skillId,
        proficiencyLevel: lv,
      });
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
    <div className="space-y-8">
      {msg && <div className="rounded-lg bg-accent/15 border border-accent/40 text-accent text-sm px-4 py-2">{msg}</div>}
      {err && <div className="rounded-lg bg-coral/15 border border-coral/40 text-coral text-sm px-4 py-2">{err}</div>}

      <section className="bg-surface-card border border-white/10 rounded-2xl p-6 space-y-4">
        <h2 className="font-display text-xl font-semibold text-accent">Профиль</h2>
        <div className="flex flex-col sm:flex-row gap-4 items-start">
          <div className="w-24 h-24 rounded-full border border-white/15 overflow-hidden shrink-0">
            <AvatarImg src={avatarUrl} className="w-full h-full object-cover" alt="Аватар" />
          </div>
          <div className="flex-1 w-full space-y-2">
            <label className="text-xs text-ink-light uppercase tracking-wide">Ссылка на фото (URL, https)</label>
            <input
              className="w-full rounded-lg bg-ink border border-white/10 px-3 py-2 text-sm"
              placeholder="https://..."
              value={avatarUrl}
              onChange={(e) => setAvatarUrl(e.target.value)}
            />
            <p className="text-xs text-ink-light">
              Если фото не видно, проверьте https и доступность ссылки. Некоторые сайты блокируют встраивание — тогда
              используйте прямую ссылку на файл изображения.
            </p>
          </div>
        </div>
        <div className="grid md:grid-cols-2 gap-4">
          <div>
            <label className="text-xs text-ink-light uppercase tracking-wide">ФИО</label>
            <input className="mt-1 w-full rounded-lg bg-ink border border-white/10 px-3 py-2" value={fullName} onChange={(e) => setFullName(e.target.value)} />
          </div>
          <div>
            <label className="text-xs text-ink-light uppercase tracking-wide">Телефон</label>
            <input className="mt-1 w-full rounded-lg bg-ink border border-white/10 px-3 py-2" value={phone} onChange={(e) => setPhone(e.target.value)} />
          </div>
          <div>
            <label className="text-xs text-ink-light uppercase tracking-wide">Регион</label>
            <select className="mt-1 w-full rounded-lg bg-ink border border-white/10 px-3 py-2" value={region} onChange={(e) => setRegion(e.target.value)}>
              <option value="">Выберите регион</option>
              {BY_REGIONS.map((r) => (
                <option key={r} value={r}>
                  {r}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="text-xs text-ink-light uppercase tracking-wide">Дата рождения</label>
            <input type="date" className="mt-1 w-full rounded-lg bg-ink border border-white/10 px-3 py-2" value={birthDate} onChange={(e) => setBirthDate(e.target.value)} />
          </div>
          <div className="md:col-span-2">
            <label className="text-xs text-ink-light uppercase tracking-wide">О себе</label>
            <textarea className="mt-1 w-full rounded-lg bg-ink border border-white/10 px-3 py-2 min-h-[100px]" value={bio} onChange={(e) => setBio(e.target.value)} />
          </div>
        </div>
        <button type="button" onClick={() => void saveProfile()} className="px-5 py-2.5 rounded-xl bg-accent text-ink font-semibold hover:bg-accent/90">
          Сохранить профиль
        </button>
      </section>

      <section className="bg-surface-card border border-white/10 rounded-2xl p-6 space-y-4">
        <h2 className="font-display text-xl font-semibold text-coral">Навыки</h2>
        <div className="flex flex-col sm:flex-row gap-2 items-stretch sm:items-end">
          <div className="flex-1">
            <label className="text-xs text-ink-light uppercase tracking-wide">Навык</label>
            <select className="mt-1 w-full rounded-lg bg-ink border border-white/10 px-3 py-2" value={addSkillId} onChange={(e) => setAddSkillId(e.target.value === '' ? '' : Number(e.target.value))}>
              <option value="">Выберите…</option>
              {availableToAdd.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name}
                </option>
              ))}
            </select>
          </div>
          <div className="w-full sm:w-24">
            <label className="text-xs text-ink-light uppercase tracking-wide">Уровень</label>
            <input type="number" min={1} max={5} className="mt-1 w-full rounded-lg bg-ink border border-white/10 px-3 py-2" value={addLevel} onChange={(e) => setAddLevel(Number(e.target.value))} />
          </div>
          <button type="button" disabled={addSkillId === ''} onClick={() => void addSkill()} className="px-4 py-2 rounded-xl bg-surface-hover border border-white/15 text-sm font-medium disabled:opacity-40">
            Добавить
          </button>
        </div>
        <ul className="divide-y divide-white/10">
          {vSkills.length === 0 && <li className="py-4 text-ink-light text-sm">Навыки ещё не указаны.</li>}
          {vSkills.map((row) => (
            <li key={row.id} className="py-4 flex flex-col sm:flex-row sm:items-center gap-3 justify-between">
              <div>
                <p className="font-medium">{row.skillName || skillName(row.skillId)}</p>
              </div>
              <div className="flex items-center gap-2">
                <label className="text-xs text-ink-light">Уровень</label>
                <input
                  type="number"
                  min={1}
                  max={5}
                  defaultValue={row.proficiencyLevel}
                  key={row.id + '-' + row.proficiencyLevel}
                  className="w-20 rounded-lg bg-ink border border-white/10 px-2 py-1"
                  onBlur={(e) => {
                    const v = Number(e.target.value);
                    if (v !== row.proficiencyLevel) void updateLevel(row, v);
                  }}
                />
                <button type="button" className="text-sm text-coral hover:underline" onClick={() => void removeSkill(row.id)}>
                  Удалить
                </button>
              </div>
            </li>
          ))}
        </ul>
      </section>
    </div>
  );
}
