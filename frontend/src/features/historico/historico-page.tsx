import { PageHeader } from "@/components/layout/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { EmptyState } from "@/components/layout/empty-state";
import { useHistorico, useRemoverUltimoHistorico } from "@/hooks/use-api";
import { interpretarOperacao } from "@/lib/historico";
import { History, Trash2 } from "lucide-react";

export function HistoricoPage() {
  const { data: historico } = useHistorico();
  const remover = useRemoverUltimoHistorico();

  const operacoes = (historico ?? []).map((o) => interpretarOperacao(o.operacao));

  return (
    <div>
      <PageHeader
        title="Histórico de operações"
        description="Pilha de registros: a operação mais recente fica no topo."
      />
      <div className="grid gap-5 lg:grid-cols-[minmax(0,1fr)_320px]">
        <div className="rounded-panel border border-border bg-surface">
          <div className="flex items-center gap-3 border-b border-border px-5 py-4">
            <h2 className="text-[0.95rem] font-semibold text-ink">Linha do tempo</h2>
            <span className="text-xs tabular text-ink-muted">{operacoes.length}</span>
          </div>
          <div className="p-4">
            {operacoes.length === 0 ? (
              <EmptyState
                icon={History}
                title="Histórico vazio"
                description="Cadastros, edições, chamadas da fila e baixas aparecem aqui, do mais recente para o mais antigo."
              />
            ) : (
              <ol className="flex flex-col gap-3">
                {operacoes.map((op, indice) => (
                  <li key={indice} className="flex gap-3">
                    <span
                      className="mt-0.5 grid size-7 flex-none place-items-center rounded-full"
                      style={{
                        background:
                          op.tone === "neutral" ? "var(--color-surface-muted)" : `var(--color-${op.tone}-soft)`,
                        color: op.tone === "neutral" ? "var(--color-ink-muted)" : `var(--color-${op.tone})`,
                      }}
                    >
                      <op.icon className="size-3.5" />
                    </span>
                    <div className="min-w-0 flex-1 border-b border-border pb-3 last:border-0 last:pb-0">
                      <div className="mb-1 flex items-center gap-2">
                        {indice === 0 && <Badge tone="brand">Topo · mais recente</Badge>}
                        <Badge tone={op.tone}>{op.categoria}</Badge>
                      </div>
                      <p className="text-sm text-ink">{op.texto}</p>
                    </div>
                  </li>
                ))}
              </ol>
            )}
          </div>
        </div>

        <div className="rounded-panel border border-border bg-surface p-5">
          <h2 className="mb-2 text-[0.95rem] font-semibold text-ink">Remover última operação</h2>
          <p className="mb-4 rounded-control bg-info-soft px-3 py-2 text-sm text-info">
            <strong>Só apaga o registro do histórico.</strong> A operação no estoque não é desfeita: bolsas cadastradas, alocadas ou baixadas continuam como estão.
          </p>
          <Button
            variant="danger"
            className="w-full"
            disabled={operacoes.length === 0 || remover.isPending}
            onClick={() => remover.mutate()}
          >
            <Trash2 /> Remover registro do topo
          </Button>
        </div>
      </div>
    </div>
  );
}
