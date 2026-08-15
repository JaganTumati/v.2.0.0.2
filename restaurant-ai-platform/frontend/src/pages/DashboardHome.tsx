import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { api } from '../lib/api';
import { AnalyticsOverview } from '../lib/types';

function StatCard({ label, value, to }: { label: string; value: number | string; to: string }) {
  return (
    <Link
      to={to}
      className="block rounded-md border border-paper-dim bg-white px-5 py-4 transition-colors hover:border-teal/40"
    >
      <p className="text-xs font-medium uppercase tracking-wide text-slate">{label}</p>
      <p className="mt-1 font-display text-3xl font-semibold text-ink">{value}</p>
    </Link>
  );
}

export function DashboardHome() {
  const { data } = useQuery({
    queryKey: ['analytics-overview'],
    queryFn: () => api.get<AnalyticsOverview>('/api/analytics/overview'),
  });

  const pendingHandoff = data?.conversationsCurrentlyWithHuman ?? 0;

  return (
    <div>
      <h1 className="font-display text-xl font-semibold text-ink">Overview</h1>
      <p className="mt-1 text-sm text-slate">What needs attention right now.</p>

      <div className="mt-6 grid grid-cols-2 gap-4 lg:grid-cols-4">
        <StatCard label="Conversations" value={data?.totalConversations ?? '—'} to="/conversations" />
        <StatCard label="Waiting for a human" value={pendingHandoff} to="/conversations?mode=HUMAN_PENDING" />
        <StatCard label="Open tickets" value={data?.openTickets ?? '—'} to="/support-tickets" />
        <StatCard label="New leads" value={data?.newLeads ?? '—'} to="/leads" />
      </div>

      {pendingHandoff > 0 && (
        <div className="mt-6 rounded-md border border-marigold/40 bg-marigold/10 px-5 py-4">
          <p className="font-display text-sm font-semibold text-marigold-deep">
            {pendingHandoff} conversation{pendingHandoff === 1 ? '' : 's'} waiting for a team member
          </p>
          <p className="mt-1 text-sm text-ink-soft">
            The AI has stepped back on these - a customer is waiting on a person.
          </p>
          <Link
            to="/conversations?mode=HUMAN_PENDING"
            className="mt-3 inline-block rounded-sm bg-marigold px-3 py-1.5 text-xs font-semibold text-charcoal hover:opacity-90"
          >
            View waiting conversations
          </Link>
        </div>
      )}

      <div className="mt-6">
        <Link to="/analytics" className="text-sm font-medium text-teal hover:underline">
          View full analytics →
        </Link>
      </div>
    </div>
  );
}
