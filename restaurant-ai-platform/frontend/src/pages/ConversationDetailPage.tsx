import { FormEvent, useState } from 'react';
import { useParams } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '../lib/api';
import { Conversation, Message } from '../lib/types';
import { StatusBadge, conversationModeTone } from '../components/StatusBadge';

function formatTime(iso: string): string {
  return new Date(iso).toLocaleString(undefined, {
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  });
}

export function ConversationDetailPage() {
  const { id } = useParams<{ id: string }>();
  const queryClient = useQueryClient();
  const [draft, setDraft] = useState('');

  const conversation = useQuery({
    queryKey: ['conversation', id],
    queryFn: () => api.get<Conversation>(`/api/conversations/${id}`),
  });

  const messages = useQuery({
    queryKey: ['conversation-messages', id],
    queryFn: () => api.get<Message[]>(`/api/conversations/${id}/messages`),
    refetchInterval: 5000,
  });

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ['conversation', id] });
    queryClient.invalidateQueries({ queryKey: ['conversation-messages', id] });
    queryClient.invalidateQueries({ queryKey: ['conversations'] });
  };

  const takeover = useMutation({
    mutationFn: () => api.post(`/api/conversations/${id}/takeover`),
    onSuccess: invalidate,
  });
  const returnToAi = useMutation({
    mutationFn: () => api.post(`/api/conversations/${id}/return-to-ai`),
    onSuccess: invalidate,
  });
  const close = useMutation({
    mutationFn: () => api.post(`/api/conversations/${id}/close`),
    onSuccess: invalidate,
  });
  const sendMessage = useMutation({
    mutationFn: (content: string) => api.post(`/api/conversations/${id}/messages`, { content }),
    onSuccess: () => {
      setDraft('');
      invalidate();
    },
  });

  function handleSend(e: FormEvent) {
    e.preventDefault();
    if (draft.trim()) sendMessage.mutate(draft.trim());
  }

  if (conversation.isLoading || !conversation.data) {
    return <p className="text-sm text-slate">Loading…</p>;
  }

  const c = conversation.data;
  const canReply = c.mode === 'HUMAN_ACTIVE';

  return (
    <div className="mx-auto max-w-2xl">
      <div className="mb-5 flex items-start justify-between">
        <div>
          <h1 className="font-display text-xl font-semibold text-ink">
            {c.customerDisplayName || 'Unnamed customer'}
          </h1>
          <p className="font-ticket text-xs text-slate">{c.customerWhatsappNumber}</p>
        </div>
        <StatusBadge label={c.mode.replace('_', ' ')} tone={conversationModeTone(c.mode)} />
      </div>

      <div className="mb-5 flex flex-wrap gap-2">
        {c.mode !== 'HUMAN_ACTIVE' && c.mode !== 'CLOSED' && (
          <button
            onClick={() => takeover.mutate()}
            className="rounded-sm bg-brick px-3 py-1.5 text-xs font-semibold text-white hover:opacity-90"
          >
            Take over from AI
          </button>
        )}
        {c.mode === 'HUMAN_ACTIVE' && (
          <button
            onClick={() => returnToAi.mutate()}
            className="rounded-sm bg-teal px-3 py-1.5 text-xs font-semibold text-white hover:opacity-90"
          >
            Return to AI
          </button>
        )}
        {c.mode !== 'CLOSED' && (
          <button
            onClick={() => close.mutate()}
            className="rounded-sm border border-paper-dim bg-white px-3 py-1.5 text-xs font-semibold text-ink-soft hover:bg-paper-dim"
          >
            Close conversation
          </button>
        )}
      </div>

      <div className="rounded-md border border-paper-dim bg-white">
        <div className="max-h-[60vh] space-y-3 overflow-y-auto p-5">
          {messages.data?.map((m) => (
            <div
              key={m.id}
              className={`flex ${m.direction === 'INBOUND' ? 'justify-start' : 'justify-end'}`}
            >
              <div
                className={`max-w-[75%] rounded-md px-3.5 py-2.5 text-sm ${
                  m.direction === 'INBOUND'
                    ? 'bg-paper-dim text-ink'
                    : m.senderType === 'AGENT'
                      ? 'bg-brick text-white'
                      : 'bg-teal text-white'
                }`}
              >
                <p>{m.content}</p>
                <p className="mt-1 font-ticket text-[10px] opacity-70">
                  {m.senderType} · {formatTime(m.createdAt)}
                </p>
              </div>
            </div>
          ))}
          {messages.data?.length === 0 && (
            <p className="text-sm text-slate">No messages yet.</p>
          )}
        </div>

        <form onSubmit={handleSend} className="flex gap-2 border-t border-paper-dim p-3">
          <input
            value={draft}
            onChange={(e) => setDraft(e.target.value)}
            disabled={!canReply}
            placeholder={canReply ? 'Reply as yourself…' : 'Take over the conversation to reply'}
            className="flex-1 rounded-sm border border-paper-dim bg-paper px-3 py-2 text-sm outline-none focus:border-teal disabled:opacity-50"
          />
          <button
            type="submit"
            disabled={!canReply || !draft.trim()}
            className="rounded-sm bg-marigold px-4 py-2 text-sm font-semibold text-charcoal hover:opacity-90 disabled:opacity-40"
          >
            Send
          </button>
        </form>
      </div>
    </div>
  );
}
