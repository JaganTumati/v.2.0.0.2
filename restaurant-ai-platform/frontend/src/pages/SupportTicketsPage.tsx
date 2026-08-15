import { useQuery } from '@tanstack/react-query';
import { api } from '../lib/api';
import { SupportTicket } from '../lib/types';
import { StatusBadge, ticketPriorityTone } from '../components/StatusBadge';

export function SupportTicketsPage() {
  const { data, isLoading } = useQuery({
    queryKey: ['support-tickets'],
    queryFn: () => api.get<SupportTicket[]>('/api/support-tickets'),
  });

  return (
    <div>
      <h1 className="font-display text-xl font-semibold text-ink">Support tickets</h1>
      <p className="mt-1 text-sm text-slate">Issues raised by the AI or by customers directly.</p>

      <div className="mt-5 overflow-hidden rounded-md border border-paper-dim bg-white">
        {isLoading && <p className="p-5 text-sm text-slate">Loading…</p>}
        {!isLoading && data?.length === 0 && (
          <p className="p-5 text-sm text-slate">No tickets open. Good sign.</p>
        )}
        {data?.map((ticket) => (
          <div key={ticket.id} className="flex items-start gap-4 border-b border-paper-dim px-5 py-4 last:border-b-0">
            <div className="min-w-0 flex-1">
              <div className="flex items-center gap-2">
                <StatusBadge label={ticket.priority} tone={ticketPriorityTone(ticket.priority)} />
                <span className="font-ticket text-[11px] uppercase tracking-wide text-slate">
                  {ticket.status.replace('_', ' ')}
                </span>
              </div>
              <p className="mt-1.5 text-sm text-ink">{ticket.issue}</p>
              {ticket.orderId && (
                <p className="mt-1 font-ticket text-[11px] text-slate">Order {ticket.orderId}</p>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
