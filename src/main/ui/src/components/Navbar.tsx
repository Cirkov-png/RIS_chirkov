import { Fragment, useState } from 'react';
import { Link, NavLink, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import type { UserRole } from '../types';

const linkClass = ({ isActive }: { isActive: boolean }) =>
  `px-3 py-2 rounded-lg text-sm font-medium transition ${
    isActive ? 'bg-accent/20 text-accent' : 'text-stone-300 hover:bg-surface-hover'
  }`;

/** Точное совпадение пути (для /organizer, /volunteer), без вложенных URL */
function pathMatchesExact(pathname: string, to: string) {
  return pathname === to || pathname === `${to}/`;
}

/** Префикс пути (для /volunteer/tasks и карточки /volunteer/tasks/:id) */
function pathMatchesPrefix(pathname: string, to: string) {
  return pathname === to || pathname.startsWith(`${to}/`);
}

function navButtonClass(pathname: string, to: string, end?: boolean) {
  const active = end ? pathMatchesExact(pathname, to) : pathMatchesPrefix(pathname, to);
  return `text-left py-2 rounded-lg px-2 -mx-2 ${active ? 'bg-accent/20 text-accent' : 'text-stone-200'}`;
}

export function Navbar() {
  const { user, logout } = useAuth();
  const nav = useNavigate();
  const pathname = useLocation().pathname;
  const [open, setOpen] = useState(false);

  const go = (path: string) => {
    setOpen(false);
    nav(path);
  };

  const links: { to: string; label: string; roles?: UserRole[]; dividerBefore?: boolean; end?: boolean }[] = [
    { to: '/volunteer', label: 'Главная', roles: ['VOLUNTEER'], end: true },
    { to: '/volunteer/tasks', label: 'Задачи', roles: ['VOLUNTEER'] },
    { to: '/volunteer/profile', label: 'Профиль', roles: ['VOLUNTEER'] },
    { to: '/volunteer/applications', label: 'Заявки', roles: ['VOLUNTEER'] },
    { to: '/organizer', label: 'Дашборд', roles: ['ORGANIZER'], end: true },
    { to: '/organizer/applications', label: 'Заявки волонтёров', roles: ['ORGANIZER'], dividerBefore: true },
    { to: '/organizer/profile', label: 'Профиль', roles: ['ORGANIZER'] },
    { to: '/coordinator', label: 'Панель координатора', roles: ['COORDINATOR', 'ADMIN'] },
    { to: '/matching', label: 'Матчинг', roles: ['ORGANIZER', 'COORDINATOR', 'ADMIN'] },
  ];

  const visible = links.filter((l) => !l.roles || (user && l.roles.includes(user.role)));

  return (
    <header className="sticky top-0 z-40 border-b border-white/10 bg-ink/85 backdrop-blur-md">
      <div className="max-w-6xl mx-auto px-4 h-14 md:h-16 flex items-center justify-between gap-4">
        <Link to="/" className="font-display text-lg md:text-xl font-semibold tracking-tight flex items-center gap-2">
          <span className="inline-flex h-7 w-7 items-center justify-center rounded-lg bg-accent/20 text-accent text-sm">
            ПВ
          </span>
          <span className="text-stone-100">Платформа волонтёров</span>
        </Link>

        <nav className="hidden md:flex items-center gap-1 flex-wrap">
          {user &&
            visible.map((l) => (
              <Fragment key={l.to}>
                {l.dividerBefore && (
                  <span className="inline-flex h-6 w-px bg-white/25 mx-1.5 shrink-0 self-center" aria-hidden />
                )}
                <NavLink to={l.to} end={l.end === true} className={linkClass}>
                  {l.label}
                </NavLink>
              </Fragment>
            ))}
        </nav>

        <div className="hidden md:flex items-center gap-3">
          {user ? (
            <>
              <span className="text-xs text-ink-light truncate max-w-[140px]">{user.username}</span>
              <span className="text-[10px] uppercase tracking-wider px-2 py-0.5 rounded bg-surface-card text-accent border border-accent/30">
                {user.role}
              </span>
              <button
                type="button"
                onClick={() => {
                  logout();
                  nav('/login');
                }}
                className="text-sm text-coral hover:text-coral/80"
              >
                Выйти
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="text-sm text-stone-300 hover:text-white">
                Вход
              </Link>
              <Link
                to="/register"
                className="text-sm px-3 py-1.5 rounded-lg bg-accent text-ink font-semibold hover:bg-accent/90"
              >
                Регистрация
              </Link>
            </>
          )}
        </div>

        <button
          type="button"
          className="md:hidden p-2 rounded-lg bg-surface-card text-stone-200"
          aria-label="Меню"
          onClick={() => setOpen((o) => !o)}
        >
          <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            {open ? (
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            ) : (
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
            )}
          </svg>
        </button>
      </div>

      {open && (
        <div className="md:hidden border-t border-white/10 bg-surface px-4 py-4 flex flex-col gap-2">
          {user &&
            visible.map((l) => (
              <Fragment key={l.to}>
                {l.dividerBefore && <div className="border-t border-white/10 my-2" aria-hidden />}
                <button
                  type="button"
                  className={navButtonClass(pathname, l.to, l.end)}
                  onClick={() => go(l.to)}
                >
                  {l.label}
                </button>
              </Fragment>
            ))}
          {user ? (
            <button
              type="button"
              className="text-left py-2 text-coral"
              onClick={() => {
                logout();
                go('/login');
              }}
            >
              Выйти ({user.username})
            </button>
          ) : (
            <>
              <button type="button" className="text-left py-2" onClick={() => go('/login')}>
                Вход
              </button>
              <button type="button" className="text-left py-2 text-accent" onClick={() => go('/register')}>
                Регистрация
              </button>
            </>
          )}
        </div>
      )}
    </header>
  );
}
