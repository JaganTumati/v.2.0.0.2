interface StatusBadgeProps {
  label: string;
  tone: 'teal' | 'marigold' | 'brick' | 'slate';
}

const TONE_CLASSES: Record<StatusBadgeProps['tone'], string> = {
  teal: 'bg-teal-soft text-teal border-teal/30',
  marigold: 'bg-marigold/15 text-marigold-deep border-marigold/40',
  brick: 'bg-brick-soft text-brick border-brick/30',
  slate: 'bg-paper-dim text-slate border-slate/20',
};

export function StatusBadge({ label, tone }: StatusBadgeProps) {
  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-sm border px-2 py-0.5 font-ticket text-[11px] font-medium uppercase tracking-wide ${TONE_CLASSES[tone]}`}
    >
      <span className="h-1.5 w-1.5 rounded-full bg-current" />
      {label}
    </span>
  );
}

export function conversationModeTone(mode: string): StatusBadgeProps['tone'] {
  switch (mode) {
    case 'AI_ACTIVE':
      return 'teal';
    case 'HUMAN_PENDING':
      return 'marigold';
    case 'HUMAN_ACTIVE':
      return 'brick';
    default:
      return 'slate';
  }
}

export function ticketPriorityTone(priority: string): StatusBadgeProps['tone'] {
  switch (priority) {
    case 'URGENT':
    case 'HIGH':
      return 'brick';
    case 'NORMAL':
      return 'marigold';
    default:
      return 'slate';
  }
}
