import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import * as skillsApi from '../../api/skillsApi';
import { findVolunteersBySkill, findVolunteersBySkillUuid } from '../../api/volunteersApi';
import type { SkillDto, VolunteerDto } from '../../types';
import { errorMessage } from '../../utils/errorMessage';

export function OrganizerVolunteerSearchPage() {
  const [skills, setSkills] = useState<SkillDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);

  const [skillSearchUuid, setSkillSearchUuid] = useState('');
  const [uuidResults, setUuidResults] = useState<VolunteerDto[]>([]);
  const [uuidBusy, setUuidBusy] = useState(false);

  const [skillId, setSkillId] = useState<number | ''>('');
  const [onlyActive, setOnlyActive] = useState(true);
  const [idResults, setIdResults] = useState<VolunteerDto[]>([]);
  const [idBusy, setIdBusy] = useState(false);

  const loadSkills = useCallback(async () => {
    setLoading(true);
    setErr(null);
    try {
      setSkills(await skillsApi.fetchSkills());
    } catch (e) {
      setErr(errorMessage(e));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadSkills();
  }, [loadSkills]);

  async function searchByUuid() {
    const u = skillSearchUuid.trim();
    if (!u) return;
    setUuidBusy(true);
    setErr(null);
    try {
      setUuidResults(await findVolunteersBySkillUuid(u));
    } catch (e) {
      setErr(errorMessage(e));
      setUuidResults([]);
    } finally {
      setUuidBusy(false);
    }
  }

  async function searchBySkillId() {
    if (skillId === '') return;
    setIdBusy(true);
    setErr(null);
    try {
      setIdResults(await findVolunteersBySkill(Number(skillId), onlyActive));
    } catch (e) {
      setErr(errorMessage(e));
      setIdResults([]);
    } finally {
      setIdBusy(false);
    }
  }

  function renderResults(list: VolunteerDto[]) {
    if (list.length === 0) {
      return <p className="text-sm text-ink-light py-2">Никого не найдено.</p>;
    }
    return (
      <ul className="rounded-lg border border-white/10 divide-y divide-white/10 mt-3">
        {list.map((v) => (
          <li key={v.id} className="px-3 py-3 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2">
            <div>
              <Link to={`/volunteers/${v.id}`} className="font-medium text-accent hover:underline">
                {v.fullName ?? `Волонтёр #${v.id}`}
              </Link>
              <p className="text-xs text-ink-light mt-1 font-mono">uuid: {v.uuid}</p>
              <p className="text-xs text-ink-light mt-0.5">
                {v.region ?? 'регион не указан'} · рейтинг {v.rating} · задач: {v.completedTasksCount}
                {v.active ? '' : ' · неактивен'}
              </p>
            </div>
            <Link
              to={`/volunteers/${v.id}`}
              className="text-sm px-3 py-1.5 rounded-lg border border-white/15 text-ink-light hover:text-accent shrink-0"
            >
              Карточка
            </Link>
          </li>
        ))}
      </ul>
    );
  }

  if (loading) return <p className="text-ink-light">Загрузка справочника навыков…</p>;

  return (
    <div className="space-y-8 max-w-4xl">
      {err && <div className="rounded-lg bg-coral/15 border border-coral/40 text-coral text-sm px-4 py-2">{err}</div>}

      <section className="bg-surface-card border border-white/10 rounded-2xl p-6 space-y-4">
        <h2 className="font-display text-xl font-semibold text-accent">По UUID навыка</h2>
        <p className="text-sm text-ink-light">
          Тот же поиск, что в профиле волонтёра: запрос GET к /api/volunteers/search/skill/ с UUID навыка в конце пути.
        </p>
        <div className="flex flex-col sm:flex-row gap-2">
          <input
            className="flex-1 rounded-lg bg-ink border border-white/10 px-3 py-2 text-sm font-mono"
            placeholder="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
            value={skillSearchUuid}
            onChange={(e) => setSkillSearchUuid(e.target.value)}
          />
          <button
            type="button"
            disabled={uuidBusy || !skillSearchUuid.trim()}
            onClick={() => void searchByUuid()}
            className="px-4 py-2 rounded-xl bg-accent text-ink font-semibold text-sm disabled:opacity-40"
          >
            {uuidBusy ? 'Поиск…' : 'Найти'}
          </button>
        </div>
        {renderResults(uuidResults)}
      </section>

      <section className="bg-surface-card border border-white/10 rounded-2xl p-6 space-y-4">
        <h2 className="font-display text-xl font-semibold text-coral">По навыку из списка</h2>
        <p className="text-sm text-ink-light">
          Запрос GET /api/volunteers/search/skill с параметрами skillId и onlyActive — удобно выбрать навык без
          копирования UUID.
        </p>
        <div className="flex flex-col sm:flex-row gap-3 sm:items-end flex-wrap">
          <div className="flex-1 min-w-[200px]">
            <label className="text-xs text-ink-light uppercase tracking-wide">Навык</label>
            <select
              className="mt-1 w-full rounded-lg bg-ink border border-white/10 px-3 py-2"
              value={skillId}
              onChange={(e) => setSkillId(e.target.value === '' ? '' : Number(e.target.value))}
            >
              <option value="">Выберите…</option>
              {skills.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name}
                </option>
              ))}
            </select>
          </div>
          <label className="flex items-center gap-2 text-sm text-stone-300 cursor-pointer pb-1">
            <input type="checkbox" checked={onlyActive} onChange={(e) => setOnlyActive(e.target.checked)} />
            Только активные
          </label>
          <button
            type="button"
            disabled={idBusy || skillId === ''}
            onClick={() => void searchBySkillId()}
            className="px-4 py-2 rounded-xl bg-surface-hover border border-white/15 font-semibold text-sm disabled:opacity-40"
          >
            {idBusy ? 'Поиск…' : 'Найти'}
          </button>
        </div>
        {renderResults(idResults)}
      </section>

      <section className="bg-surface-card border border-white/10 rounded-2xl p-6 space-y-3">
        <h2 className="font-display text-lg font-semibold text-ink-light">Справочник навыков (UUID)</h2>
        <ul className="max-h-64 overflow-y-auto text-xs space-y-1.5 text-stone-300">
          {skills.map((s) => (
            <li key={s.id} className="font-mono break-all">
              <span className="text-stone-100 font-sans font-medium">{s.name}</span>
              {' — '}
              {s.uuid}
            </li>
          ))}
        </ul>
      </section>
    </div>
  );
}
