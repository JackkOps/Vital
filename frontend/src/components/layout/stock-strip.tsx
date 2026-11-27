import { useBolsas } from "@/hooks/use-api";
import { DESCRICAO_TIPO_SANGUINEO, TIPOS_SANGUINEOS } from "@/api/types";
import { NIVEL_CRITICO_ESTOQUE } from "@/lib/domain";
import { cn } from "@/lib/utils";

/**
 * Faixa de estoque ao vivo por tipo sanguíneo — o primeiro número que a
 * equipe quer ver ao abrir o sistema. Vive dentro da lâmina azul da barra
 * lateral, em creme; tipos abaixo do nível crítico ganham o tom de alerta.
 */
export function StockStrip() {
  const { data: bolsas, isLoading } = useBolsas();

  const contagens = TIPOS_SANGUINEOS.map((tipo) => {
    const total = (bolsas ?? []).filter(
      (b) => b.tipoSanguineo === tipo && b.status === "DISPONIVEL",
    ).length;
    return { tipo, total };
  });

  const maior = Math.max(1, ...contagens.map((c) => c.total));
  const soma = contagens.reduce((s, c) => s + c.total, 0);
  const criticos = contagens.filter((c) => c.total <= NIVEL_CRITICO_ESTOQUE).length;

  return (
    <section aria-label="Bolsas disponíveis por tipo sanguíneo">
      <div className="mb-3 flex items-end justify-between gap-2">
        <div>
          <p className="serif-accent text-[15px] leading-none text-inverse-muted">Disponível agora</p>
          <p className="display-num mt-1.5 text-[2.6rem] text-inverse-ink">
            {isLoading ? "—" : soma}
            <span className="ml-1.5 align-baseline text-xs font-normal tracking-normal text-inverse-muted">
              bolsas
            </span>
          </p>
        </div>
        {criticos > 0 && !isLoading && (
          <span className="mb-1 rounded-full bg-[#ffd2c7] px-2 py-0.5 text-[11px] font-semibold text-[#8f1d0e]">
            {criticos} crítico{criticos > 1 ? "s" : ""}
          </span>
        )}
      </div>

      <ul className="grid grid-cols-2 gap-x-4 gap-y-2">
        {contagens.map(({ tipo, total }) => {
          const critico = total <= NIVEL_CRITICO_ESTOQUE;
          return (
            <li
              key={tipo}
              className="flex items-center gap-2 text-xs"
              title={`${DESCRICAO_TIPO_SANGUINEO[tipo]}: ${total} disponível(is)${critico ? " — abaixo do nível crítico" : ""}`}
            >
              <span className="w-7 font-mono font-medium text-inverse-ink">
                {DESCRICAO_TIPO_SANGUINEO[tipo]}
              </span>
              <span className="relative h-[3px] flex-1 overflow-hidden rounded-full bg-inverse-line">
                <span
                  className={cn(
                    "absolute inset-y-0 left-0 rounded-full transition-[width] duration-500",
                    critico ? "bg-[#ffb4a2]" : "bg-inverse-ink",
                  )}
                  style={{ width: `${Math.max(4, (total / maior) * 100)}%` }}
                />
              </span>
              <span
                className={cn(
                  "w-5 text-right font-mono tabular",
                  critico ? "font-semibold text-[#ffc6b8]" : "text-inverse-muted",
                )}
              >
                {total}
              </span>
            </li>
          );
        })}
      </ul>
      <p className="meta mt-3 text-inverse-muted">
        Crítico: até {NIVEL_CRITICO_ESTOQUE} bolsas por tipo
      </p>
    </section>
  );
}
