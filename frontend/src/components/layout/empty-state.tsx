import type { LucideIcon } from "lucide-react";

export function EmptyState({
  icon: Icon,
  title,
  description,
}: {
  icon: LucideIcon;
  title: string;
  description: string;
}) {
  return (
    <div className="flex flex-col items-center gap-2.5 px-6 py-12 text-center">
      <span className="grid size-11 place-items-center rounded-full border border-dashed border-border-strong text-ink-faint">
        <Icon className="size-[18px]" strokeWidth={1.5} />
      </span>
      <p className="serif-accent text-xl leading-tight text-ink">{title}</p>
      <p className="max-w-sm text-[13px] leading-relaxed text-ink-muted">{description}</p>
    </div>
  );
}
