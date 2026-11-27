import * as React from "react";
import { Badge } from "@/components/ui/badge";
import { Card, CardHeader } from "@/components/ui/card";
import { tabela } from "@/components/ui/stat";
import { Paginacao, usePaginacao } from "@/components/ui/pagination";
import { EmptyState } from "@/components/layout/empty-state";
import {
  DESCRICAO_COMPONENTE,
  DESCRICAO_TIPO_SANGUINEO,
  type SolicitacaoResponse,
  type StatusSolicitacao,
} from "@/api/types";
import { STATUS_SOLICITACAO_INFO, URGENCIA_INFO } from "@/lib/domain";
import { cn } from "@/lib/utils";
import { ClipboardList } from "lucide-react";
import { codigoSolicitacao } from "./fila-atendimento";

const ABAS: { valor: StatusSolicitacao | ""; label: string }[] = [
  { valor: "", label: "Todas" },
  { valor: "PENDENTE", label: "Pendentes" },
  { valor: "EM_ATENDIMENTO", label: "Em atendimento" },
  { valor: "ATENDIDA", label: "Atendidas" },
  { valor: "CANCELADA", label: "Canceladas" },
];

export function SolicitacoesTable({ solicitacoes }: { solicitacoes: SolicitacaoResponse[] }) {
  const [aba, setAba] = React.useState<StatusSolicitacao | "">("");
  const [pagina, setPagina] = React.useState(1);
  const ordenadas = [...solicitacoes].sort((a, b) => b.id - a.id);
  const lista = aba ? ordenadas.filter((s) => s.status === aba) : ordenadas;
  const paginacao = usePaginacao(lista, pagina, 10);
  const contar = (v: StatusSolicitacao | "") => (v ? solicitacoes.filter((s) => s.status === v).length : solicitacoes.length);

  return (
    <Card>
      <CardHeader index="04" title="Todas as" accent="solicitações" />
      <div role="tablist" aria-label="Filtrar por status" className="scrollbar-none flex gap-1.5 overflow-x-auto border-b border-border bg-background/40 px-5 py-3">
        {ABAS.map((a) => (
          <button
            key={a.label}
            type="button"
            role="tab"
            aria-selected={aba === a.valor}
            onClick={() => { setAba(a.valor); setPagina(1); }}
            className={cn(
              "flex flex-none items-center gap-1.5 rounded-full border px-3 py-1 text-[13px] font-medium transition-colors",
              aba === a.valor ? "border-ink bg-ink text-background" : "border-border-strong text-ink-muted hover:border-ink hover:text-ink",
            )}
          >
            {a.label}
            <span className="font-mono text-[10.5px] opacity-60">{contar(a.valor)}</span>
          </button>
        ))}
      </div>

      {lista.length === 0 ? (
        <EmptyState
          icon={ClipboardList}
          title={aba ? "Nada nesta etapa" : "Nenhuma solicitação registrada"}
          description={aba ? "Escolha outra aba para ver os demais pedidos." : 'Use "Registrar pedido" para colocar o primeiro pedido na fila.'}
        />
      ) : (
        <>
          <div className="overflow-x-auto">
            <table className={tabela.table}>
              <thead>
                <tr className={tabela.headRow}>
                  <th className={tabela.th}>Código</th>
                  <th className={tabela.th}>Hospital</th>
                  <th className={tabela.th}>Tipo</th>
                  <th className={tabela.th}>Componente</th>
                  <th className={tabela.th}>Urgência</th>
                  <th className={cn(tabela.th, "text-right")}>Alocadas</th>
                  <th className={tabela.th}>Status</th>
                </tr>
              </thead>
              <tbody>
                {paginacao.visiveis.map((s) => {
                  const status = STATUS_SOLICITACAO_INFO[s.status];
                  const urgencia = URGENCIA_INFO[s.nivelUrgencia];
                  return (
                    <tr key={s.id} className={tabela.row}>
                      <td className={cn(tabela.td, "font-mono text-[12.5px] text-brand")}>{codigoSolicitacao(s.id)}</td>
                      <td className={cn(tabela.td, "font-medium text-ink")}>{s.nomeHospital}</td>
                      <td className={cn(tabela.td, "font-mono text-[13px] text-ink")}>{DESCRICAO_TIPO_SANGUINEO[s.tipoSanguineo]}</td>
                      <td className={cn(tabela.td, "text-ink-muted")}>{DESCRICAO_COMPONENTE[s.tipoComponente]}</td>
                      <td className={tabela.td}><Badge tone={urgencia.tone}>{urgencia.label}</Badge></td>
                      <td className={cn(tabela.td, "text-right tabular text-ink-muted")}>{s.bolsasAlocadas}/{s.quantidade}</td>
                      <td className={tabela.td}>
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
          <Paginacao {...paginacao} total={lista.length} onChange={setPagina} />
        </>
      )}
    </Card>
  );
}
