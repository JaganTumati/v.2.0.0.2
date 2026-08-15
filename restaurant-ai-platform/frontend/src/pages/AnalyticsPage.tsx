import { useQuery } from '@tanstack/react-query';
import { BarChart, Bar, XAxis, YAxis, ResponsiveContainer, Tooltip, CartesianGrid } from 'recharts';
import { api } from '../lib/api';
import { AnalyticsOverview, MenuPopularity } from '../lib/types';

function Metric({ label, value, hint }: { label: string; value: string; hint?: string }) {
  return (
    <div className="rounded-md border border-paper-dim bg-white px-5 py-4">
      <p className="text-xs font-medium uppercase tracking-wide text-slate">{label}</p>
      <p className="mt-1 font-display text-2xl font-semibold text-ink">{value}</p>
      {hint && <p className="mt-1 text-xs text-slate">{hint}</p>}
    </div>
  );
}

export function AnalyticsPage() {
  const overview = useQuery({
    queryKey: ['analytics-overview'],
    queryFn: () => api.get<AnalyticsOverview>('/api/analytics/overview'),
  });
  const popularity = useQuery({
    queryKey: ['analytics-menu-popularity'],
    queryFn: () => api.get<MenuPopularity[]>('/api/analytics/menu-popularity'),
  });

  const o = overview.data;

  return (
    <div>
      <h1 className="font-display text-xl font-semibold text-ink">Analytics</h1>
      <p className="mt-1 text-sm text-slate">
        Real numbers from your data. The AI-handled share is an estimate based on current
        conversation state, not a full historical log.
      </p>

      {o && (
        <div className="mt-6 grid grid-cols-2 gap-4 lg:grid-cols-4">
          <Metric label="Conversations today" value={String(o.conversationsToday)} />
          <Metric label="Orders today" value={String(o.ordersToday)} />
          <Metric label="Revenue today" value={`₹${o.revenueToday.toFixed(0)}`} />
          <Metric label="Active orders" value={String(o.activeOrders)} />
          <Metric label="Open tickets" value={String(o.openTickets)} />
          <Metric label="New leads" value={String(o.newLeads)} />
          <Metric
            label="With a human right now"
            value={String(o.conversationsCurrentlyWithHuman)}
            hint={`of ${o.totalConversations} total`}
          />
          <Metric
            label="AI-handled share"
            value={`${Math.round(o.aiHandledShare * 100)}%`}
            hint="estimate, not historical"
          />
        </div>
      )}

      <div className="mt-8">
        <h2 className="font-display text-sm font-semibold text-ink">Most-ordered items</h2>
        <div className="mt-3 rounded-md border border-paper-dim bg-white p-4">
          {popularity.data && popularity.data.length > 0 ? (
            <ResponsiveContainer width="100%" height={280}>
              <BarChart data={popularity.data} layout="vertical" margin={{ left: 24 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#F1ECE2" horizontal={false} />
                <XAxis type="number" tick={{ fontSize: 11, fill: '#6B6862' }} allowDecimals={false} />
                <YAxis
                  type="category"
                  dataKey="itemName"
                  width={140}
                  tick={{ fontSize: 12, fill: '#1C1B1A' }}
                />
                <Tooltip
                  contentStyle={{ fontSize: 12, borderRadius: 4, borderColor: '#F1ECE2' }}
                  formatter={(value: number) => [`${value} sold`, '']}
                />
                <Bar dataKey="totalQuantity" fill="#E8A33D" radius={[0, 3, 3, 0]} />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <p className="p-4 text-sm text-slate">No completed orders yet.</p>
          )}
        </div>
      </div>
    </div>
  );
}
