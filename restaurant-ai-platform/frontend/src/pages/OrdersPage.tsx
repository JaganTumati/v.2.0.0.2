import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '../lib/api';
import { Order } from '../lib/types';
import { StatusBadge } from '../components/StatusBadge';

const NEXT_STATUS: Record<string, string | null> = {
  DRAFT: 'CONFIRMATION_REQUIRED',
  CONFIRMATION_REQUIRED: 'CONFIRMED',
  CONFIRMED: 'PREPARING',
  PREPARING: 'READY',
  READY: 'OUT_FOR_DELIVERY',
  OUT_FOR_DELIVERY: 'DELIVERED',
  DELIVERED: null,
  CANCELLED: null,
};

function statusTone(status: string): 'teal' | 'marigold' | 'brick' | 'slate' {
  if (status === 'DELIVERED') return 'teal';
  if (status === 'CANCELLED') return 'brick';
  if (status === 'DRAFT' || status === 'CONFIRMATION_REQUIRED') return 'slate';
  return 'marigold';
}

export function OrdersPage() {
  const queryClient = useQueryClient();

  const { data, isLoading } = useQuery({
    queryKey: ['orders'],
    queryFn: () => api.get<Order[]>('/api/orders'),
    refetchInterval: 10000,
  });

  const advance = useMutation({
    mutationFn: ({ id, status }: { id: string; status: string }) =>
      api.put(`/api/orders/${id}/status`, { status }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['orders'] }),
  });

  const cancel = useMutation({
    mutationFn: (id: string) => api.post(`/api/orders/${id}/cancel`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['orders'] }),
  });

  return (
    <div>
      <h1 className="font-display text-xl font-semibold text-ink">Orders</h1>
      <p className="mt-1 text-sm text-slate">
        Advancing to DELIVERED fires any matching automation rules (e.g. the seeded follow-up message).
      </p>

      <div className="mt-5 overflow-hidden rounded-md border border-paper-dim bg-white">
        {isLoading && <p className="p-5 text-sm text-slate">Loading…</p>}
        {!isLoading && data?.length === 0 && (
          <p className="p-5 text-sm text-slate">
            No orders yet - place one through the AI chat test endpoint to see it here.
          </p>
        )}
        {data?.map((order) => {
          const next = NEXT_STATUS[order.status];
          const cancellable = !['DELIVERED', 'CANCELLED'].includes(order.status);
          return (
            <div key={order.id} className="border-b border-paper-dim px-5 py-4 last:border-b-0">
              <div className="flex items-start justify-between gap-4">
                <div className="min-w-0">
                  <p className="font-ticket text-xs text-slate">{order.id}</p>
                  <p className="mt-1 text-sm text-ink">
                    {order.items.map((i) => `${i.quantity}× ${i.itemName}`).join(', ')}
                  </p>
                  <p className="mt-1 font-ticket text-xs text-slate">
                    {order.fulfillmentType}
                    {order.deliveryAddress ? ` · ${order.deliveryAddress}` : ''}
                  </p>
                </div>
                <div className="shrink-0 text-right">
                  <p className="font-ticket text-sm font-medium text-ink">₹{order.total.toFixed(0)}</p>
                  <div className="mt-1">
                    <StatusBadge label={order.status.replace(/_/g, ' ')} tone={statusTone(order.status)} />
                  </div>
                </div>
              </div>

              <div className="mt-3 flex gap-2">
                {next && (
                  <button
                    onClick={() => advance.mutate({ id: order.id, status: next })}
                    className="rounded-sm bg-teal px-3 py-1.5 text-xs font-semibold text-white hover:opacity-90"
                  >
                    Advance to {next.replace(/_/g, ' ')}
                  </button>
                )}
                {cancellable && (
                  <button
                    onClick={() => cancel.mutate(order.id)}
                    className="rounded-sm border border-paper-dim px-3 py-1.5 text-xs font-semibold text-ink-soft hover:bg-paper-dim"
                  >
                    Cancel
                  </button>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
