import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const NAV_ITEMS = [
  { to: '/', label: 'Overview', end: true },
  { to: '/conversations', label: 'Conversations' },
  { to: '/orders', label: 'Orders' },
  { to: '/menu', label: 'Menu' },
  { to: '/leads', label: 'Leads' },
  { to: '/support-tickets', label: 'Support tickets' },
  { to: '/analytics', label: 'Analytics' },
];

export function Layout() {
  const { email, role, logout } = useAuth();

  return (
    <div className="flex min-h-screen">
      <aside className="flex w-60 shrink-0 flex-col justify-between bg-charcoal px-5 py-6 text-paper">
        <div>
          <div className="mb-8 flex items-baseline gap-2">
            <span className="font-display text-lg font-semibold tracking-tight text-paper">
              Order Rail
            </span>
          </div>
          <nav className="flex flex-col gap-1">
            {NAV_ITEMS.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.end}
                className={({ isActive }) =>
                  `rounded-sm px-3 py-2 text-sm font-medium transition-colors ${
                    isActive
                      ? 'bg-marigold text-charcoal'
                      : 'text-paper/70 hover:bg-charcoal-raised hover:text-paper'
                  }`
                }
              >
                {item.label}
              </NavLink>
            ))}
          </nav>
        </div>

        <div className="border-t border-paper/10 pt-4">
          <p className="truncate text-xs text-paper/50">{email}</p>
          <p className="mb-3 font-ticket text-[11px] uppercase tracking-wide text-marigold">{role}</p>
          <button
            onClick={logout}
            className="w-full rounded-sm border border-paper/20 px-3 py-1.5 text-left text-xs font-medium text-paper/70 transition-colors hover:border-paper/40 hover:text-paper"
          >
            Sign out
          </button>
        </div>
      </aside>

      <main className="flex-1 bg-paper px-8 py-7">
        <Outlet />
      </main>
    </div>
  );
}
