import * as React from "react";
import { cn } from "@/lib/utils";

/**
 * Painel no formato de "lâmina": papel claro, fio de 1px e cantos quase retos.
 * `inverse` gera a lâmina azul cobalto com texto creme.
 */
function Card({
  className,
  inverse,
  ...props
}: React.HTMLAttributes<HTMLDivElement> & { inverse?: boolean }) {
  return (
    <div
      className={cn(
        "rounded-panel border",
        inverse
          ? "border-inverse bg-inverse text-inverse-ink"
          : "border-border bg-surface",
        className,
      )}
      {...props}
    />
  );
}

/**
 * Cabeçalho de painel: índice "(01)" em mono, título em grotesca e,
 * opcionalmente, um complemento em serifada itálica azul.
 */
function CardHeader({
  className,
  index,
  title,
  accent,
  meta,
  children,
  ...props
}: Omit<React.HTMLAttributes<HTMLDivElement>, "title"> & {
  index?: string;
  title?: React.ReactNode;
  accent?: React.ReactNode;
  meta?: React.ReactNode;
}) {
  return (
    <div
      className={cn(
        "flex flex-wrap items-center gap-x-4 gap-y-2 border-b border-border px-5 py-4",
        className,
      )}
      {...props}
    >
      {title !== undefined && (
        <div className="flex min-w-0 items-baseline gap-2.5">
          {index && (
            <span className="font-mono text-[11px] font-medium text-brand">({index})</span>
          )}
          <h2 className="text-[1.05rem] font-medium tracking-[-0.01em] text-ink">
            {title}
            {accent && <span className="serif-accent ml-1.5 text-[1.2rem] text-brand">{accent}</span>}
          </h2>
          {meta !== undefined && (
            <span className="meta rounded-full border border-border-strong px-2 py-px text-ink-muted">
              {meta}
            </span>
          )}
        </div>
      )}
      {children}
    </div>
  );
}

function CardTitle({ className, ...props }: React.HTMLAttributes<HTMLHeadingElement>) {
  return (
    <h2
      className={cn("text-[1.05rem] font-medium tracking-[-0.01em] text-ink", className)}
      {...props}
    />
  );
}

function CardDescription({ className, ...props }: React.HTMLAttributes<HTMLParagraphElement>) {
  return <p className={cn("text-sm text-ink-muted", className)} {...props} />;
}

function CardContent({ className, ...props }: React.HTMLAttributes<HTMLDivElement>) {
  return <div className={cn("p-5", className)} {...props} />;
}

/** Nota entre colchetes — "[começa com uma decisão]" — usada como legenda curta. */
function Bracket({ className, children }: { className?: string; children: React.ReactNode }) {
  return (
    <p className={cn("text-xs leading-snug text-ink-muted", className)}>
      <span className="text-ink-faint">[</span>
      {children}
      <span className="text-ink-faint">]</span>
    </p>
  );
}

export { Card, CardHeader, CardTitle, CardDescription, CardContent, Bracket };
