import { ChevronLeft, ChevronRight } from "lucide-react";
import { cn } from "@/lib/utils";

export function usePaginacao<T>(itens: T[], pagina: number, porPagina: number) {
  const totalPaginas = Math.max(1, Math.ceil(itens.length / porPagina));
  const atual = Math.min(pagina, totalPaginas);
  return {
    atual,
    totalPaginas,
    visiveis: itens.slice((atual - 1) * porPagina, atual * porPagina),
    inicio: itens.length === 0 ? 0 : (atual - 1) * porPagina + 1,
    fim: Math.min(atual * porPagina, itens.length),
  };
}

/** Paginação em pílulas numeradas "01 02 03", no espírito do índice da referência. */
export function Paginacao({
  atual,
  totalPaginas,
  inicio,
  fim,
  total,
  onChange,
}: {
  atual: number;
  totalPaginas: number;
  inicio: number;
  fim: number;
  total: number;
  onChange: (pagina: number) => void;
}) {
  if (totalPaginas <= 1) return null;
  const paginas = Array.from({ length: totalPaginas }, (_, i) => i + 1).filter(
    (p) => p === 1 || p === totalPaginas || Math.abs(p - atual) <= 1,
  );
  const botao =
    "grid h-8 min-w-8 place-items-center rounded-full px-2 font-mono text-[12px] transition-colors";

  return (
    <nav
      aria-label="Paginação"
      className="flex flex-wrap items-center justify-between gap-3 border-t border-border px-5 py-3"
    >
      <p className="meta text-ink-muted">
        {inicio}–{fim} de {total}
      </p>
      <div className="flex items-center gap-1">
        <button
          type="button"
          className={cn(botao, "text-ink-muted hover:bg-surface-muted disabled:opacity-30")}
          onClick={() => onChange(atual - 1)}
          disabled={atual === 1}
          aria-label="Página anterior"
        >
          <ChevronLeft className="size-4" />
        </button>
        {paginas.map((p, i) => (
          <span key={p} className="flex items-center gap-1">
            {i > 0 && p - paginas[i - 1] > 1 && <span className="px-1 text-ink-faint">…</span>}
            <button
              type="button"
              onClick={() => onChange(p)}
              aria-current={p === atual ? "page" : undefined}
              className={cn(
                botao,
                p === atual ? "bg-brand text-brand-foreground" : "text-ink-muted hover:bg-surface-muted",
              )}
            >
              {String(p).padStart(2, "0")}
            </button>
          </span>
        ))}
        <button
          type="button"
          className={cn(botao, "text-ink-muted hover:bg-surface-muted disabled:opacity-30")}
          onClick={() => onChange(atual + 1)}
          disabled={atual === totalPaginas}
          aria-label="Próxima página"
        >
          <ChevronRight className="size-4" />
        </button>
      </div>
    </nav>
  );
}
