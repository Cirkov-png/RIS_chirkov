import { Outlet } from 'react-router-dom';
import { Navbar } from './Navbar';

export function AppShell() {
  return (
    <div className="min-h-screen flex flex-col page-blur">
      <Navbar />
      <main className="flex-1 w-full max-w-6xl mx-auto px-4 py-6 md:py-10">
        <Outlet />
      </main>
      <footer className="border-t border-white/10 py-6 text-center text-sm text-ink-light bg-ink/30">
        Платформа волонтёров
      </footer>
    </div>
  );
}
