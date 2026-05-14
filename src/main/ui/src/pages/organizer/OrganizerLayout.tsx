import { NavLink, Outlet } from 'react-router-dom';

const subLink =
  'px-3 py-2 rounded-lg text-sm font-medium transition border border-transparent ' +
  'text-ink-light hover:text-white hover:bg-surface-hover';

const subActive = 'bg-accent/20 text-accent border-accent/30';

export function OrganizerLayout() {
  return (
    <div className="space-y-6">
      <div className="glass-panel hero-grid p-6 md:p-8 overflow-hidden">
        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-5">
          <div>
            <p className="text-xs uppercase tracking-[0.25em] text-accent/90 mb-2">Кабинет организатора</p>
            <h1 className="font-display text-2xl md:text-3xl font-bold mb-1">Задачи и волонтёры</h1>
            <p className="text-sm text-ink-light max-w-2xl">
              Создавайте задачи, обрабатывайте заявки и находите волонтёров по навыкам — в том числе через поиск по
              справочнику.
            </p>
          </div>
        </div>
      </div>
      <nav className="flex flex-wrap gap-2 border-b border-white/10 pb-3">
        <NavLink to="/organizer" end className={({ isActive }) => `${subLink} ${isActive ? subActive : ''}`}>
          Дашборд
        </NavLink>
        <NavLink
          to="/organizer/applications"
          className={({ isActive }) => `${subLink} ${isActive ? subActive : ''}`}
        >
          Заявки волонтёров
        </NavLink>
        <NavLink to="/organizer/profile" className={({ isActive }) => `${subLink} ${isActive ? subActive : ''}`}>
          Профиль
        </NavLink>
        <NavLink
          to="/organizer/search-volunteers"
          className={({ isActive }) => `${subLink} ${isActive ? subActive : ''}`}
        >
          Поиск волонтёров
        </NavLink>
        <NavLink
          to="/organizer/external-services"
          className={({ isActive }) => `${subLink} ${isActive ? subActive : ''}`}
        >
          Внешние сервисы
        </NavLink>
      </nav>
      <Outlet />
    </div>
  );
}
