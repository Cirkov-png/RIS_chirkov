import { NavLink, Outlet } from 'react-router-dom';

const subLink =
  'px-3 py-2 rounded-lg text-sm font-medium transition border border-transparent ' +
  'text-ink-light hover:text-white hover:bg-surface-hover';

const subActive = 'bg-accent/20 text-accent border-accent/30';

export function VolunteerLayout() {
  return (
    <div className="space-y-6">
      <div className="glass-panel hero-grid p-6 md:p-8 overflow-hidden">
        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-5">
          <div>
            <p className="text-xs uppercase tracking-[0.25em] text-accent/90 mb-2">Кабинет волонтера</p>
            <h1 className="font-display text-2xl md:text-3xl font-bold mb-1">Участвуйте в полезных инициативах</h1>
            <p className="text-sm text-ink-light max-w-2xl">
              Быстро находите задачи по навыкам и региону, отслеживайте отклики и развивайте профиль через выполненные
              проекты.
            </p>
          </div>
          <div className="accent-ring rounded-2xl border border-white/10 bg-ink/70 p-4 text-sm min-w-[220px]">
            <p className="text-ink-light">Фокус дня</p>
            <p className="font-semibold mt-1">Проверьте доступные задачи</p>
            <p className="text-xs text-ink-light mt-2">Чем точнее заполнен профиль и навыки, тем выше шанс одобрения.</p>
          </div>
        </div>
      </div>
      <nav className="flex flex-wrap gap-2 border-b border-white/10 pb-3">
        <NavLink to="/volunteer" end className={({ isActive }) => `${subLink} ${isActive ? subActive : ''}`}>
          Главная
        </NavLink>
        <NavLink to="/volunteer/tasks" className={({ isActive }) => `${subLink} ${isActive ? subActive : ''}`}>
          Задачи
        </NavLink>
        <NavLink to="/volunteer/profile" className={({ isActive }) => `${subLink} ${isActive ? subActive : ''}`}>
          Профиль и навыки
        </NavLink>
        <NavLink
          to="/volunteer/applications"
          className={({ isActive }) => `${subLink} ${isActive ? subActive : ''}`}
        >
          Заявки
        </NavLink>
      </nav>
      <Outlet />
    </div>
  );
}
