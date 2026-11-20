import { useBolsas } from "@/hooks/use-api";
import { DESCRICAO_TIPO_SANGUINEO, TIPOS_SANGUINEOS } from "@/api/types";
import { NIVEL_CRITICO_ESTOQUE } from "@/lib/domain";
import { cn } from "@/lib/utils";

/**
 * Faixa de estoque ao vivo por tipo sanguíneo — o primeiro número que a
 * equipe quer ver ao abrir o sistema. É o elemento de destaque do painel
 * lateral; o resto da navegação fica deliberadamente quieto ao redor dele.
 */
export function StockStrip({ collapsed = false }: { collapsed?: boolean }) {
  const { data: bolsas } = useBolsas();

  const contagens = TIPOS_SANGUINEOS.map((tipo) => {
    const total = (bolsas ?? []).filter(
      (b) => b.tipoSanguineo === tipo && b.status === "DISPONIVEL",
    ).length;
    return { tipo, total };
  });

  const maior = Math.max(1, ...contagens.map((c) => c.total));

  if (collapsed) return null;

  return (
    <div className="flex flex-col gap-1.5 px-1">
      <p className="px-2 text-xs font-medium text-ink-muted">Disponível agora</p>
      {contagens.map(({ tipo, total }) => {
        const critico = total <= NIVEL_CRITICO_ESTOQUE;
        return (
          <div key={tipo} className="flex items-center gap-2 px-2 py-0.5 text-xs">
            <span className="w-7 font-mono font-semibold text-ink-muted">
              {DESCRICAO_TIPO_SANGUINEO[tipo]}
            </span>
            <span className="relative h-1.5 flex-1 overflow-hidden rounded-full bg-surface-muted">
              <span
                className={cn(
                  "absolute inset-y-0 left-0 rounded-full",
                  critico ? "bg-emergencia" : "bg-ok",
                )}
                style={{ width: `${Math.max(6, (total / maior) * 100)}%` }}
              />
            </span>
            <span
              className={cn(
                "w-6 text-right font-mono tabular font-semibold",
                critico ? "text-emergencia" : "text-ink-muted",
              )}
            >
              {total}
            </span>
          </div>
        );
      })}
    </div>
  );
}
