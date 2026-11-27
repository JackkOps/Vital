import * as React from "react";
import { useLocation, useOutletContext } from "react-router-dom";
import { NAV_ITEMS, doisDigitos, indiceDaRota } from "./nav-items";

/**
 * Cabeçalho de página no formato de lâmina editorial:
 * linha de metadados (marca · ano · página) e um título em duas vozes —
 * a grotesca em tinta e o complemento em serifada itálica cobalto, recuado.
 */
export function PageHeader({
  title,
  accent,
  description,
  actions,
}: {
  title: string;
  accent: string;
  description: string;
  actions?: React.ReactNode;
}) {
  const { agora } = useOutletContext<{ agora: Date }>();
  const { pathname } = useLocation();
  const indice = indiceDaRota(pathname);
  const data = agora.toLocaleDateString("pt-BR", { weekday: "long", day: "numeric", month: "long" });

  return (
    <header className="mb-8 animate-rise">
      <div className="meta mb-6 flex items-center gap-3 border-b border-border pb-3 text-ink-muted">
        <span>Rota Vital</span>
        <span className="h-px flex-1 bg-border" aria-hidden="true" />
        <span className="hidden sm:inline">{data.charAt(0).toUpperCase() + data.slice(1)}</span>
        <span className="h-px w-8 bg-border sm:w-16" aria-hidden="true" />
        <span className="font-mono">
          {doisDigitos(indice + 1)}
          <span className="text-ink-faint">/{doisDigitos(NAV_ITEMS.length)}</span>
        </span>
      </div>

      <div className="flex flex-wrap items-end justify-between gap-x-10 gap-y-5">
        <h1 className="text-[2.4rem] font-normal leading-[0.98] tracking-[-0.035em] text-ink sm:text-[3.1rem]">
          {title}
          <span className="serif-accent block pl-[1.1em] text-[1.08em] tracking-[-0.02em] text-brand">
            {accent}
          </span>
        </h1>

        <div className="flex max-w-sm flex-col items-start gap-4 sm:items-end">
          <p className="text-[13px] leading-snug text-ink-muted sm:text-right">
            <span className="text-ink-faint">[</span>
            {description}
            <span className="text-ink-faint">]</span>
          </p>
          {actions}
        </div>
      </div>
    </header>
  );
}
