import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import type { UserRole } from '../types';
import { errorMessage } from '../utils/errorMessage';

const roles: { value: UserRole; label: string }[] = [
  { value: 'VOLUNTEER', label: 'Волонтёр' },
  { value: 'ORGANIZER', label: 'Организатор' },
  { value: 'COORDINATOR', label: 'Координатор' },
];

export function RegisterPage() {
  const { register, loading, user } = useAuth();
  const nav = useNavigate();
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState<UserRole>('VOLUNTEER');
  const [err, setErr] = useState<string | null>(null);

  useEffect(() => {
    if (user) nav('/', { replace: true });
  }, [user, nav]);

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErr(null);
    try {
      await register({
        username: username.trim(),
        email: email.trim(),
        password,
        role,
      });
      nav('/');
    } catch (e) {
      setErr(errorMessage(e));
    }
  };

  if (user) return null;

  return (
    <div className="max-w-5xl mx-auto pt-6 md:pt-12">
      <div className="grid lg:grid-cols-2 gap-6 items-stretch">
        <section className="glass-panel hero-grid p-6 md:p-8 flex flex-col justify-between">
          <div>
            <p className="text-xs uppercase tracking-[0.24em] text-accent/90 mb-3">Создание аккаунта</p>
            <h1 className="font-display text-3xl md:text-4xl font-bold mb-3">Присоединяйтесь к платформе</h1>
            <p className="text-ink-light text-sm md:text-base max-w-md">
              Выберите роль и начните работу: волонтёры находят задачи, организаторы публикуют инициативы, координаторы
              управляют процессом.
            </p>
          </div>
          <div className="mt-6 rounded-xl border border-white/10 bg-ink/60 p-4 text-sm">
            Уже есть аккаунт?{' '}
            <Link to="/login" className="text-accent hover:underline">
              Войти
            </Link>
          </div>
        </section>

        <form onSubmit={onSubmit} className="glass-panel p-6 md:p-8 space-y-4">
          {err && <div className="rounded-lg bg-coral/15 border border-coral/40 text-coral text-sm px-3 py-2">{err}</div>}
          <div>
            <label className="block text-xs uppercase tracking-wide text-ink-light mb-1">Логин</label>
            <input
              className="w-full rounded-lg bg-ink border border-white/10 px-3 py-2.5 focus:outline-none focus:ring-2 focus:ring-accent/50"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
          </div>
          <div>
            <label className="block text-xs uppercase tracking-wide text-ink-light mb-1">Email</label>
            <input
              type="email"
              className="w-full rounded-lg bg-ink border border-white/10 px-3 py-2.5 focus:outline-none focus:ring-2 focus:ring-accent/50"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>
          <div>
            <label className="block text-xs uppercase tracking-wide text-ink-light mb-1">Пароль</label>
            <input
              type="password"
              className="w-full rounded-lg bg-ink border border-white/10 px-3 py-2.5 focus:outline-none focus:ring-2 focus:ring-accent/50"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              minLength={4}
            />
          </div>
          <div>
            <label className="block text-xs uppercase tracking-wide text-ink-light mb-1">Роль</label>
            <select
              className="w-full rounded-lg bg-ink border border-white/10 px-3 py-2.5 focus:outline-none focus:ring-2 focus:ring-accent/50"
              value={role}
              onChange={(e) => setRole(e.target.value as UserRole)}
            >
              {roles.map((r) => (
                <option key={r.value} value={r.value}>
                  {r.label}
                </option>
              ))}
            </select>
          </div>
          <button
            type="submit"
            disabled={loading}
            className="w-full py-3 rounded-xl bg-accent text-ink font-semibold disabled:opacity-50 hover:bg-accent/90"
          >
            {loading ? 'Создание…' : 'Зарегистрироваться'}
          </button>
        </form>
      </div>
    </div>
  );
}
