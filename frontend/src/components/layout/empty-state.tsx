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
    <div className="flex flex-col items-center gap-2 px-6 py-10 text-center">
      <Icon className="size-6 text-ink-faint" />
      <p className="text-sm font-semibold text-ink">{title}</p>
      <p className="max-w-sm text-sm text-ink-muted">{description}</p>
    </div>
  );
}
