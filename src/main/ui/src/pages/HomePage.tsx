import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export function HomePage() {
  const { user } = useAuth();

  return (
    <div className="max-w-3xl mx-auto text-center pt-8 md:pt-16 pb-12">
      <p className="text-accent text-sm font-semibold tracking-widest uppercase mb-4">
        Распределённые информационные системы
      </p>
      <h1 className="font-display text-4xl md:text-5xl font-bold text-balance leading-tight mb-6">
        Платформа волонтёров с интеллектуальным матчингом
      </h1>
      <p className="text-ink-light text-lg md:text-xl text-balance mb-10 max-w-2xl mx-auto">
        Управление навыками, задачами и подбором кандидатов по весам требований и уровню владения навыками.
      </p>
      {user ? (
        <div className="flex flex-col sm:flex-row gap-4 justify-center">
          {user.role === 'VOLUNTEER' && (
            <Link
              to="/volunteer"
              className="inline-flex justify-center px-6 py-3 rounded-xl bg-accent text-ink font-semibold shadow-glow hover:bg-accent/90"
            >
              Личный кабинет
            </Link>
          )}
          {(user.role === 'ORGANIZER' || user.role === 'COORDINATOR') && (
            <>
              <Link
                to="/organizer"
                className="inline-flex justify-center px-6 py-3 rounded-xl bg-surface-card border border-white/15 hover:border-accent/50"
              >
                Дашборд организатора
              </Link>
              <Link
                to="/matching"
                className="inline-flex justify-center px-6 py-3 rounded-xl bg-coral/20 text-coral border border-coral/40 font-semibold"
              >
                Матчинг
              </Link>
            </>
          )}
        </div>
      ) : (
        <div className="flex flex-col sm:flex-row gap-4 justify-center">
          <Link
            to="/login"
            className="inline-flex justify-center px-6 py-3 rounded-xl bg-surface-card border border-white/15 hover:border-accent/50"
          >
            Вход
          </Link>
          <Link
            to="/register"
            className="inline-flex justify-center px-6 py-3 rounded-xl bg-accent text-ink font-semibold shadow-glow"
          >
            Регистрация
          </Link>
        </div>
      )}
    </div>
  );
}
