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
    <ul className="flex flex-col">
      {hospitais.map(([nome, info], i) => (
        <li key={nome} className="grid grid-cols-[3.5rem_minmax(0,1fr)] items-center gap-4 border-b border-border py-3 last:border-0">
          <p className={i === 0 ? "display-num text-[2.2rem] text-brand" : "display-num text-[2.2rem] text-ink"}>{info.total}</p>
          <div className="min-w-0">
            <div className="mb-1.5 flex items-baseline justify-between gap-3">
              <p className="truncate text-[14px] font-medium text-ink">{nome}</p>
              <span className="serif-accent truncate text-[13.5px] text-ink-muted">{info.pedidos.join(", ")}</span>
            </div>
            <div className="h-[3px] rounded-full bg-surface-muted">
              <div className="h-full rounded-full bg-brand transition-[width] duration-500" style={{ width: `${Math.max(4, (info.total / maior) * 100)}%` }} />
            </div>
          </div>
        </li>
      ))}
    </ul>
  );
}
