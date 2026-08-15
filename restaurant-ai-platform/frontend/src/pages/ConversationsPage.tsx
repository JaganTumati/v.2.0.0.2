import { useQuery } from '@tanstack/react-query';
import { Link, useSearchParams } from 'react-router-dom';
import { api } from '../lib/api';
import { Conversation } from '../lib/types';
import { StatusBadge, conversationModeTone } from '../components/StatusBadge';

const MODE_FILTERS: { value: string; label: string }[] = [
  { value: '', label: 'All' },
  { value: 'AI_ACTIVE', label: 'AI active' },
  { value: 'HUMAN_PENDING', label: 'Waiting for human' },
  { value: 'HUMAN_ACTIVE', label: 'Human active' },
  { value: 'CLOSED', label: 'Closed' },
];

function timeAgo(iso: string): string {
  const diffMs = Date.now() - new Date(iso).getTime();
  const mins = Math.floor(diffMs / 60000);
  if (mins < 1) return 'just now';
  if (mins < 60) return `${mins}m ago`;
  const hours = Math.floor(mins / 60);
  if (hours < 24) return `${hours}h ago`;
  return `${Math.floor(hours / 24)}d ago`;
}

export function ConversationsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const modeFilter = searchParams.get('mode') ?? '';

  const { data, isLoading } = useQuery({
    queryKey: ['conversations', modeFilter],
    queryFn: () =>
      api.get<Conversation[]>(`/api/conversations${modeFilter ? `?mode=${modeFilter}` : ''}`),
  });

  return (
    <div>
      <h1 className="font-display text-xl font-semibold text-ink">Conversations</h1>
      <p className="mt-1 text-sm text-slate">Every customer thread, AI and human-handled.</p>

      <div className="mt-5 flex gap-1.5">
        {MODE_FILTERS.map((f) => (
          <button
            key={f.value}
            onClick={() => setSearchParams(f.value ? { mode: f.value } : {})}
            className={`rounded-sm px-3 py-1.5 font-ticket text-[11px] font-medium uppercase tracking-wide transition-colors ${
              modeFilter === f.value
                ? 'bg-charcoal text-paper'
                : 'bg-white text-slate hover:bg-paper-dim'
            }`}
          >
            {f.label}
          </button>
        ))}
      </div>

      <div className="mt-5 overflow-hidden rounded-md border border-paper-dim bg-white">
        {isLoading && <p className="p-5 text-sm text-slate">Loading…</p>}
        {!isLoading && data?.length === 0 && (
          <p className="p-5 text-sm text-slate">No conversations here yet.</p>
        )}
        {data?.map((c) => (
          <Link
            key={c.id}
            to={`/conversations/${c.id}`}
            className={`flex items-center gap-4 border-l-4 border-b border-paper-dim px-5 py-3.5 last:border-b-0 hover:bg-paper-dim/50 ${
              c.mode === 'HUMAN_PENDING'
                ? 'border-l-marigold'
                : c.mode === 'HUMAN_ACTIVE'
                  ? 'border-l-brick'
                  : c.mode === 'AI_ACTIVE'
                    ? 'border-l-teal'
                    : 'border-l-transparent'
            }`}
          >
            <div className="min-w-0 flex-1">
              <p className="truncate text-sm font-medium text-ink">
                {c.customerDisplayName || 'Unnamed customer'}
              </p>
              <p className="truncate font-ticket text-xs text-slate">
                {c.customerWhatsappNumber}
                {c.summary ? ` · ${c.summary}` : ''}
              </p>
            </div>
            <StatusBadge label={c.mode.replace('_', ' ')} tone={conversationModeTone(c.mode)} />
            <span className="w-16 shrink-0 text-right font-ticket text-[11px] text-slate">
              {timeAgo(c.updatedAt)}
            </span>
          </Link>
        ))}
      </div>
    </div>
  );
}
