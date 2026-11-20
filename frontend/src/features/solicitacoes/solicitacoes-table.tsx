import { Badge } from "@/components/ui/badge";
import { EmptyState } from "@/components/layout/empty-state";
import { DESCRICAO_COMPONENTE, DESCRICAO_TIPO_SANGUINEO, type SolicitacaoResponse } from "@/api/types";
import { STATUS_SOLICITACAO_INFO, URGENCIA_INFO } from "@/lib/domain";
import { ClipboardList } from "lucide-react";

function codigo(id: number) {
  return `SOLIC-${String(id).padStart(4, "0")}`;
}

export function SolicitacoesTable({ solicitacoes }: { solicitacoes: SolicitacaoResponse[] }) {
  const lista = [...solicitacoes].sort((a, b) => b.id - a.id);

  return (
    <div className="rounded-panel border border-border bg-surface">
      <div className="flex items-center gap-3 border-b border-border px-5 py-4">
        <h2 className="text-[0.95rem] font-semibold text-ink">Todas as solicitações</h2>
        <span className="text-xs tabular text-ink-muted">{lista.length}</span>
      </div>

      {lista.length === 0 ? (
        <EmptyState
          icon={ClipboardList}
          title="Nenhuma solicitação registrada"
          description='Use o formulário "Registrar solicitação" para colocar o primeiro pedido na fila.'
        />
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full whitespace-nowrap text-left text-sm">
            <thead>
              <tr className="border-b border-border text-xs text-ink-muted">
                <th className="px-5 py-2.5 font-medium">Código</th>
                <th className="px-5 py-2.5 font-medium">Hospital</th>
                <th className="px-5 py-2.5 font-medium">Tipo</th>
                <th className="px-5 py-2.5 font-medium">Componente</th>
                <th className="px-5 py-2.5 font-medium">Urgência</th>
                <th className="px-5 py-2.5 font-medium">Alocadas</th>
                <th className="px-5 py-2.5 font-medium">Status</th>
              </tr>
            </thead>
            <tbody>
              {lista.map((s) => {
                const status = STATUS_SOLICITACAO_INFO[s.status];
                const urgencia = URGENCIA_INFO[s.nivelUrgencia];
                return (
                  <tr key={s.id} className="border-b border-border last:border-0 hover:bg-surface-muted/60">
                    <td className="px-5 py-2.5 font-mono text-[13px] font-semibold text-info">{codigo(s.id)}</td>
                    <td className="px-5 py-2.5 text-ink">{s.nomeHospital}</td>
                    <td className="px-5 py-2.5 text-ink-muted">{DESCRICAO_TIPO_SANGUINEO[s.tipoSanguineo]}</td>
                    <td className="px-5 py-2.5 text-ink-muted">{DESCRICAO_COMPONENTE[s.tipoComponente]}</td>
                    <td className="px-5 py-2.5"><Badge tone={urgencia.tone}>{urgencia.label}</Badge></td>
                    <td className="px-5 py-2.5 tabular text-ink-muted">{s.bolsasAlocadas}/{s.quantidade}</td>
                    <td className="px-5 py-2.5">
                      <Badge tone={status.tone}>
                        <status.icon className="size-3" />
                        {status.label}
                      </Badge>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
