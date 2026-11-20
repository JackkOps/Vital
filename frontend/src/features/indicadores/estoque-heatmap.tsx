import type { CSSProperties } from "react";
import {
  DESCRICAO_COMPONENTE,
  DESCRICAO_TIPO_SANGUINEO,
  TIPOS_SANGUINEOS,
  type EstoquePorTipoResponse,
  type TipoComponente,
} from "@/api/types";

function corCelula(n: number): CSSProperties {
  if (n === 0) return { border: "1.5px solid var(--color-emergencia)", background: "transparent" };
  const pct = Math.min(100, 12 + n * 3);
  return { background: `color-mix(in srgb, var(--color-ok) ${pct}%, var(--color-surface))` };
}

export function EstoqueHeatmap({
  estoque,
  componentes,
}: {
  estoque: EstoquePorTipoResponse[];
  componentes: TipoComponente[];
}) {
  const quantidade = (componente?: TipoComponente, tipo?: string) =>
    estoque
      .filter((e) => (!componente || e.tipoComponente === componente) && (!tipo || e.tipoSanguineo === tipo))
      .reduce((soma, e) => soma + e.quantidadeBolsasDisponiveis, 0);

  const totalGeral = quantidade();

  return (
    <div>
      <div className="overflow-x-auto">
        <table className="w-full border-separate border-spacing-1 text-sm">
          <thead>
            <tr className="text-xs text-ink-muted">
              <th className="px-3 py-2 text-left font-medium"></th>
              {componentes.map((c) => (
                <th key={c} className="px-3 py-2 text-center font-medium">{DESCRICAO_COMPONENTE[c]}</th>
              ))}
              <th className="px-3 py-2 text-center font-medium">Total</th>
            </tr>
          </thead>
          <tbody>
            {TIPOS_SANGUINEOS.map((tipo) => {
              const total = componentes.reduce((soma, c) => soma + quantidade(c, tipo), 0);
              return (
                <tr key={tipo}>
                  <th scope="row" className="px-3 py-1.5 text-left font-mono text-sm font-semibold text-ink-muted">
                    {DESCRICAO_TIPO_SANGUINEO[tipo]}
                  </th>
                  {componentes.map((c) => {
                    const n = quantidade(c, tipo);
                    return (
                      <td
                        key={c}
                        style={corCelula(n)}
                        className="rounded-control px-3 py-1.5 text-center text-sm font-semibold tabular text-ink"
                        aria-label={`${DESCRICAO_TIPO_SANGUINEO[tipo]}, ${DESCRICAO_COMPONENTE[c]}: ${n}`}
                      >
                        {n === 0 ? "×" : n <= 2 ? `↓ ${n}` : n}
                      </td>
                    );
                  })}
                  <td className="px-3 py-1.5 text-center text-sm font-bold tabular text-ink">{total}</td>
                </tr>
              );
            })}
            <tr>
              <th scope="row" className="px-3 py-2 text-left text-xs font-semibold text-ink-muted">Total</th>
              {componentes.map((c) => (
                <td key={c} className="px-3 py-2 text-center text-sm font-bold tabular text-ink">{quantidade(c)}</td>
              ))}
              <td className="px-3 py-2 text-center text-sm font-bold tabular text-ink">{totalGeral}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div className="mt-3 flex flex-wrap items-center gap-4 text-xs text-ink-muted">
        <span className="flex items-center gap-1.5">
          <i className="size-3 rounded-sm" style={{ border: "1.5px solid var(--color-emergencia)" }} />
          Sem estoque
        </span>
        <span className="flex items-center gap-1.5">
          <i className="size-3 rounded-sm" style={{ background: "color-mix(in srgb, var(--color-ok) 20%, var(--color-surface))" }} />
          Baixo (1–2)
        </span>
        <span className="flex items-center gap-1.5">
          Mais bolsas
          {[30, 55, 75, 95].map((p) => (
            <i key={p} className="size-3 rounded-sm" style={{ background: `color-mix(in srgb, var(--color-ok) ${p}%, var(--color-surface))` }} />
          ))}
          3 → 30+
        </span>
      </div>
    </div>
  );
}
