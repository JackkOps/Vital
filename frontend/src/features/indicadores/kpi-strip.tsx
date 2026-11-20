import type { LucideIcon } from "lucide-react";
import { cn } from "@/lib/utils";

export function KpiCard({
  icon: Icon,
  title,
  value,
  unit,
  detail,
  tone = "neutral",
}: {
  icon: LucideIcon;
  title: string;
  value: number;
  unit: string;
  detail: string;
  tone?: "neutral" | "alerta" | "emergencia";
}) {
  return (
    <div className="rounded-panel border border-border bg-surface p-4">
      <p className="flex items-center gap-1.5 text-xs font-medium text-ink-muted">
        <Icon className="size-3.5" />
        {title}
      </p>
      <p
        className={cn(
          "mt-1.5 flex items-baseline gap-1.5 text-2xl font-bold tabular",
          tone === "alerta" && "text-alerta",
          tone === "emergencia" && "text-emergencia",
          tone === "neutral" && "text-ink",
        )}
      >
        {value}
        <span className="text-xs font-medium text-ink-muted">{unit}</span>
      </p>
      <p className="mt-1 text-xs text-ink-muted">{detail}</p>
    </div>
  );
}
