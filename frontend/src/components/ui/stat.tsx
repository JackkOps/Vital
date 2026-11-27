import * as React from "react";
import { cn } from "@/lib/utils";

type StatTone = "brand" | "ink" | "alerta" | "emergencia" | "inverse";

const toneValor: Record<StatTone, string> = {
  brand: "text-brand",
  ink: "text-ink",
  alerta: "text-alerta",
  emergencia: "text-emergencia",
  inverse: "text-inverse-ink",
};

/**
 * Número de destaque no estilo da referência ("86.2%" / "+125K"):
 * valor grande e fino, legenda em serifada itálica logo abaixo.
 */
export function Stat({
  value,
  unit,
  label,
  detail,
  tone = "brand",
  className,
}: {
  value: React.ReactNode;
  unit?: string;
  label: string;
  detail?: React.ReactNode;
  tone?: StatTone;
  className?: string;
}) {
  const inverse = tone === "inverse";
  return (
    <div className={cn("min-w-0", className)}>
      <p className={cn("display-num text-[2.6rem] sm:text-[3rem]", toneValor[tone])}>
        {value}
        {unit && (
          <span
            className={cn(
              "ml-1.5 text-sm font-normal tracking-normal",
              inverse ? "text-inverse-muted" : "text-ink-muted",
            )}
          >
            {unit}
          </span>
        )}
      </p>
      <p
        className={cn(
          "serif-accent mt-2 text-[15px] leading-tight",
          inverse ? "text-inverse-muted" : tone === "ink" ? "text-ink-muted" : toneValor[tone],
        )}
      >
        {label}
      </p>
      {detail && (
        <p className={cn("mt-1 text-xs leading-snug", inverse ? "text-inverse-muted" : "text-ink-muted")}>
          {detail}
        </p>
      )}
    </div>
  );
}

/** Faixa horizontal de números separados por fios verticais. */
export function StatRow({ children, className }: { children: React.ReactNode; className?: string }) {
  return (
    <div
      className={cn(
        "grid grid-cols-2 gap-y-6 border-y border-border py-6 md:grid-cols-4 md:gap-y-0 md:divide-x md:divide-border [&>*]:px-0 md:[&>*]:px-6 md:[&>*:first-child]:pl-0",
        className,
      )}
    >
      {children}
    </div>
  );
}

/** Mensagem de erro de formulário. */
export function FormError({ children }: { children: React.ReactNode }) {
  return (
    <p
      role="alert"
      className="mb-5 flex items-start gap-2 rounded-panel border border-emergencia/30 bg-emergencia-soft px-3.5 py-2.5 text-[13px] text-emergencia"
    >
      <span className="mt-[5px] size-1.5 flex-none rounded-full bg-emergencia" aria-hidden="true" />
      {children}
    </p>
  );
}

/** Classes compartilhadas para tabelas editoriais (fios horizontais, cabeçalho discreto). */
export const tabela = {
  table: "w-full whitespace-nowrap text-left text-sm",
  headRow: "border-b border-border",
  th: "px-5 py-3 text-[11.5px] font-medium text-ink-faint",
  row: "border-b border-border/70 transition-colors last:border-0 hover:bg-background/60",
  td: "px-5 py-3",
};
