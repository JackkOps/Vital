import type { LucideIcon } from "lucide-react";
import { cn } from "@/lib/utils";

/** Cartão de indicador: número grande e fino, legenda serifada, detalhe abaixo de um fio. */
export function KpiCard({
  icon: Icon,
  index,
  title,
  value,
  unit,
  detail,
  tone = "neutral",
  inverse = false,
}: {
  icon: LucideIcon;
  index: string;
  title: string;
  value: number;
  unit: string;
  detail: string;
  tone?: "neutral" | "alerta" | "emergencia";
  inverse?: boolean;
}) {
  return (
    <div
      className={cn(
        "flex flex-col rounded-panel border p-5",
        inverse ? "border-inverse bg-inverse text-inverse-ink" : "border-border bg-surface",
      )}
    >
      <div className="flex items-center justify-between">
        <span className={cn("font-mono text-[11px]", inverse ? "text-inverse-muted" : "text-brand")}>({index})</span>
        <Icon className={cn("size-4", inverse ? "text-inverse-muted" : "text-ink-faint")} strokeWidth={1.5} />
      </div>
      <p
        className={cn(
          "display-num mt-8 text-[3.4rem]",
          inverse && "text-inverse-ink",
          !inverse && tone === "alerta" && "text-alerta",
          !inverse && tone === "emergencia" && "text-emergencia",
          !inverse && tone === "neutral" && "text-ink",
        )}
      >
        {value}
        <span className={cn("ml-1.5 text-sm tracking-normal", inverse ? "text-inverse-muted" : "text-ink-muted")}>{unit}</span>
      </p>
      <p className={cn("serif-accent mt-1.5 text-[16px]", inverse ? "text-inverse-muted" : "text-brand")}>{title}</p>
      <p className={cn("mt-4 border-t pt-3 text-xs leading-snug", inverse ? "border-inverse-line text-inverse-muted" : "border-border text-ink-muted")}>
        {detail}
      </p>
    </div>
  );
}
