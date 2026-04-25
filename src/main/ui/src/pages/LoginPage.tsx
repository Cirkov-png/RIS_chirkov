import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { errorMessage } from '../utils/errorMessage';

export function LoginPage() {
  const { login, loading, user } = useAuth();
  const nav = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [err, setErr] = useState<string | null>(null);

  useEffect(() => {
    if (user) nav('/', { replace: true });
  }, [user, nav]);

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErr(null);
    try {
      await login(username.trim(), password);
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
            <p className="text-xs uppercase tracking-[0.24em] text-accent/90 mb-3">Авторизация</p>
            <h1 className="font-display text-3xl md:text-4xl font-bold mb-3">С возвращением!</h1>
            <p className="text-ink-light text-sm md:text-base max-w-md">
              Войдите в аккаунт, чтобы управлять задачами, заявками и своим профилем в едином пространстве платформы.
            </p>
          </div>
          <div className="mt-6 rounded-xl border border-white/10 bg-ink/60 p-4 text-sm">
            Нет аккаунта?{' '}
            <Link to="/register" className="text-accent hover:underline">
              Регистрация
            </Link>
          </div>
        </section>

        <form onSubmit={onSubmit} className="glass-panel p-6 md:p-8 space-y-4">
          {err && <div className="rounded-lg bg-coral/15 border border-coral/40 text-coral text-sm px-3 py-2">{err}</div>}
          <div>
            <label className="block text-xs uppercase tracking-wide text-ink-light mb-1">Логин</label>
            <input
              className="w-full rounded-lg bg-ink border border-white/10 px-3 py-2.5 text-stone-100 focus:outline-none focus:ring-2 focus:ring-accent/50"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              autoComplete="username"
              required
            />
          </div>
          <div>
            <label className="block text-xs uppercase tracking-wide text-ink-light mb-1">Пароль</label>
            <input
              type="password"
              className="w-full rounded-lg bg-ink border border-white/10 px-3 py-2.5 text-stone-100 focus:outline-none focus:ring-2 focus:ring-accent/50"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="current-password"
              required
            />
          </div>
          <button
            type="submit"
            disabled={loading}
            className="w-full py-3 rounded-xl bg-accent text-ink font-semibold disabled:opacity-50 hover:bg-accent/90"
          >
            {loading ? 'Вход…' : 'Войти'}
          </button>
          <p className="text-xs text-ink-light text-center">Безопасный вход для волонтёров, организаторов и координаторов.</p>
        </form>
      </div>
    </div>
  );
}
