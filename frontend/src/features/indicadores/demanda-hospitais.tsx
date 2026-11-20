import { EmptyState } from "@/components/layout/empty-state";
import { DESCRICAO_COMPONENTE, DESCRICAO_TIPO_SANGUINEO, type DemandaPorHospitalResponse } from "@/api/types";
import { ClipboardList } from "lucide-react";

export function DemandaHospitais({ itens }: { itens: DemandaPorHospitalResponse[] }) {
  if (itens.length === 0) {
    return (
      <EmptyState
        icon={ClipboardList}
        title="Nenhuma demanda em aberto"
        description="Pedidos pendentes ou em atendimento aparecem aqui, agrupados por hospital."
      />
    );
  }

  const porHospital = new Map<string, { total: number; pedidos: string[] }>();
  for (const item of itens) {
    const atual = porHospital.get(item.nomeHospital) ?? { total: 0, pedidos: [] };
    atual.total += item.quantidadeBolsasSolicitadasEmAberto;
    atual.pedidos.push(`${DESCRICAO_TIPO_SANGUINEO[item.tipoSanguineo]} ${DESCRICAO_COMPONENTE[item.tipoComponente].toLowerCase()}`);
    porHospital.set(item.nomeHospital, atual);
  }
  const hospitais = [...porHospital.entries()].sort((a, b) => b[1].total - a[1].total);
  const maior = hospitais[0]?.[1].total || 1;

  return (
    <ul className="flex flex-col gap-2.5">
      {hospitais.map(([nome, info]) => (
        <li key={nome}>
          <div className="mb-1 flex items-baseline justify-between gap-2">
            <p className="text-sm font-semibold text-ink">{nome}</p>
            <span className="text-xs text-ink-muted">{info.pedidos.join(", ")}</span>
          </div>
          <div className="relative h-5 overflow-hidden rounded-control bg-surface-muted">
            <div
              className="flex h-full items-center rounded-control bg-brand px-2 text-xs font-semibold tabular text-brand-foreground"
              style={{ width: `${Math.max(8, (info.total / maior) * 92)}%` }}
            >
              {info.total}
            </div>
          </div>
        </li>
      ))}
    </ul>
  );
}
