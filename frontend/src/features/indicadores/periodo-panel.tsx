import { Badge } from "@/components/ui/badge";
import { DESCRICAO_COMPONENTE, type IndicadoresResponse } from "@/api/types";
import { formatarHoras, formatarPercentual } from "@/lib/format";
import { cn } from "@/lib/utils";
import { AlertTriangle, CheckCircle2 } from "lucide-react";

export function PeriodoPanel({ indicadores }: { indicadores: IndicadoresResponse }) {
  const criticos = indicadores.situacaoComponentes.filter((c) => c.estoqueCritico);

  return (
    <div>
      <div className="mb-4 grid gap-4 sm:grid-cols-2">
        <div>
          <p className="text-xs text-ink-muted">Taxa de descarte por vencimento</p>
          <p className="text-xl font-bold tabular text-ink">
            {formatarPercentual(indicadores.taxaDescarteVencimento, 1)}
          </p>
        </div>
        <div>
          <p className="text-xs text-ink-muted">Tempo médio de atendimento</p>
          <p className="text-xl font-bold tabular text-ink">
            {formatarHoras(indicadores.tempoMedioAtendimentoHoras)}
          </p>
        </div>
      </div>

      {criticos.length > 0 && (
        <p className="mb-3 flex items-start gap-2 rounded-control border border-emergencia/25 bg-emergencia-soft/50 px-3 py-2 text-sm text-emergencia">
          <AlertTriangle className="mt-0.5 size-4 flex-none" />
          <span>
            <strong>Estoque crítico:</strong> {criticos.map((c) => DESCRICAO_COMPONENTE[c.tipoComponente]).join(", ")}{" "}
            abaixo de {criticos[0].nivelCritico} bolsas disponíveis.
          </span>
        </p>
      )}

      <div className="overflow-x-auto">
        <table className="w-full whitespace-nowrap text-left text-sm">
          <thead>
            <tr className="border-b border-border text-xs text-ink-muted">
              <th className="px-3 py-2 font-medium">Componente</th>
              <th className="px-3 py-2 text-right font-medium">Disponíveis</th>
              <th className="px-3 py-2 text-right font-medium">Faltam alocar</th>
              <th className="px-3 py-2 font-medium">Situação</th>
              <th className="px-3 py-2 text-right font-medium">
                Risco em {indicadores.horizonteDesabastecimentoDias}d
              </th>
            </tr>
          </thead>
          <tbody>
            {indicadores.situacaoComponentes.map((c) => (
              <tr
                key={c.tipoComponente}
                className={cn("border-b border-border last:border-0", c.estoqueCritico && "bg-emergencia-soft/30")}
              >
                <td className="px-3 py-2 text-ink">{DESCRICAO_COMPONENTE[c.tipoComponente]}</td>
                <td className="px-3 py-2 text-right tabular text-ink-muted">{c.bolsasDisponiveis}</td>
                <td className="px-3 py-2 text-right tabular text-ink-muted">{c.demandaEmAberto}</td>
                <td className="px-3 py-2">
                  <Badge tone={c.estoqueCritico ? "emergencia" : "ok"}>
                    {c.estoqueCritico ? <AlertTriangle className="size-3" /> : <CheckCircle2 className="size-3" />}
                    {c.estoqueCritico ? "Estoque crítico" : "Adequado"}
                  </Badge>
                </td>
                <td className="px-3 py-2 text-right tabular text-ink-muted">
                  {formatarPercentual(c.probabilidadeDesabastecimento, 1)}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
