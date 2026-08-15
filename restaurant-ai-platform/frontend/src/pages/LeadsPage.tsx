import { useQuery } from '@tanstack/react-query';
import { api } from '../lib/api';
import { Lead } from '../lib/types';
import { StatusBadge } from '../components/StatusBadge';

function leadStatusTone(status: string): 'teal' | 'marigold' | 'brick' | 'slate' {
  if (status === 'CONVERTED') return 'teal';
  if (status === 'NEW' || status === 'QUALIFIED') return 'marigold';
  if (status === 'LOST') return 'brick';
  return 'slate';
}

export function LeadsPage() {
  const { data, isLoading } = useQuery({ queryKey: ['leads'], queryFn: () => api.get<Lead[]>('/api/leads') });

  return (
    <div>
      <h1 className="font-display text-xl font-semibold text-ink">Leads</h1>
      <p className="mt-1 text-sm text-slate">
        Catering, bulk, and event opportunities the AI has flagged from conversations.
      </p>

      <div className="mt-5 overflow-hidden rounded-md border border-paper-dim bg-white">
        {isLoading && <p className="p-5 text-sm text-slate">Loading…</p>}
        {!isLoading && data?.length === 0 && <p className="p-5 text-sm text-slate">No leads yet.</p>}
        {data?.map((lead) => (
          <div key={lead.id} className="flex items-start gap-4 border-b border-paper-dim px-5 py-4 last:border-b-0">
            <div className="min-w-0 flex-1">
              <div className="flex items-center gap-2">
                <span className="font-ticket text-[11px] uppercase tracking-wide text-slate">
                  {lead.source ?? 'UNSPECIFIED'}
                </span>
                <StatusBadge label={lead.status} tone={leadStatusTone(lead.status)} />
              </div>
              <p className="mt-1.5 text-sm text-ink">{lead.intentNotes}</p>
            </div>
            {lead.estimatedValue != null && (
              <p className="shrink-0 font-ticket text-sm font-medium text-ink">
                ₹{lead.estimatedValue.toFixed(0)}
              </p>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
