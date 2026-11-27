import * as React from "react";
import { PageHeader } from "@/components/layout/page-header";
import { Badge } from "@/components/ui/badge";
import { Button, ButtonArrow } from "@/components/ui/button";
import { Card, CardHeader } from "@/components/ui/card";
import { ConfirmDialog } from "@/components/ui/confirm-dialog";
import { Paginacao, usePaginacao } from "@/components/ui/pagination";
import { EmptyState } from "@/components/layout/empty-state";
import { useHistorico, useRemoverUltimoHistorico } from "@/hooks/use-api";
import { interpretarOperacao } from "@/lib/historico";
import { cn } from "@/lib/utils";
import { History } from "lucide-react";

const doisDigitos = (n: number) => String(n).padStart(2, "0");

export function HistoricoPage() {
  const { data: historico } = useHistorico();
  const remover = useRemoverUltimoHistorico();
  const [confirmando, setConfirmando] = React.useState(false);
  const [pagina, setPagina] = React.useState(1);

  const operacoes = (historico ?? []).map((o) => interpretarOperacao(o.operacao));
  const paginacao = usePaginacao(operacoes, pagina, 12);
  const deslocamento = (paginacao.atual - 1) * 12;
  const topo = operacoes[0];

  return (
    <div>
      <PageHeader
        title="Histórico de"
        accent="operações."
        description="Pilha de registros: a operação mais recente fica no topo."
      />

      <div className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_340px] lg:items-start">
        <Card>
          <CardHeader index="01" title="Linha do" accent="tempo" meta={operacoes.length} />
          {operacoes.length === 0 ? (
            <EmptyState
              icon={History}
              title="Histórico vazio"
              description="Cadastros, edições, chamadas da fila e baixas aparecem aqui, do mais recente para o mais antigo."
            />
          ) : (
            <>
              <ol>
                {paginacao.visiveis.map((op, i) => {
                  const posicao = deslocamento + i;
                  const noTopo = posicao === 0;
                  return (
                    <li
                      key={posicao}
                      className={cn(
                        "grid grid-cols-[3.25rem_minmax(0,1fr)] items-center gap-4 border-b border-border px-5 py-4 last:border-0 sm:grid-cols-[4rem_minmax(0,1fr)_auto]",
                        noTopo && "bg-brand-soft/50",
                      )}
                    >
                      <span className={cn("display-num text-[2rem]", noTopo ? "text-brand" : "text-ink-faint")}>
                        {doisDigitos(posicao + 1)}
                      </span>
                      <div className="min-w-0">
                        <p className={cn("serif-accent text-[15px] leading-tight", noTopo ? "text-brand" : "text-ink-muted")}>
                          {op.categoria}
                        </p>
                        <p className="mt-0.5 text-[14px] leading-snug text-ink">{op.texto}</p>
                      </div>
                      <div className="col-start-2 flex items-center gap-2 sm:col-start-auto">
                        {noTopo && <Badge tone="brand">Topo</Badge>}
                        <span
                          className="grid size-8 place-items-center rounded-full"
                          style={{
                            background: op.tone === "neutral" ? "var(--color-surface-muted)" : `var(--color-${op.tone}-soft)`,
                            color: op.tone === "neutral" ? "var(--color-ink-muted)" : `var(--color-${op.tone})`,
                          }}
                          aria-hidden="true"
                        >
                          <op.icon className="size-3.5" />
                        </span>
                      </div>
                    </li>
                  );
                })}
              </ol>
              <Paginacao {...paginacao} total={operacoes.length} onChange={setPagina} />
            </>
          )}
        </Card>

        <Card inverse className="lg:sticky lg:top-8">
          <div className="p-6">
            <p className="meta font-mono text-inverse-muted">(02)</p>
            <h2 className="mt-5 text-[1.8rem] font-normal leading-none tracking-[-0.03em]">Remover</h2>
            <p className="serif-accent pl-6 text-[2rem] leading-none text-inverse-muted">o registro do topo</p>

            <div className="mt-6 border-t border-inverse-line pt-4">
              <p className="meta text-inverse-muted">Registro atual no topo</p>
              <p className="mt-1.5 text-[14px] leading-snug">{topo ? topo.texto : "Nenhum registro"}</p>
            </div>

            <p className="mt-5 text-[13px] leading-relaxed text-inverse-muted">
              <span className="text-inverse-ink">Só apaga o registro do histórico.</span> A operação no estoque não é desfeita:
              bolsas cadastradas, alocadas ou baixadas continuam como estão.
            </p>

            <Button
              variant="inverse"
              className="mt-6 w-full"
              disabled={operacoes.length === 0 || remover.isPending}
              onClick={() => setConfirmando(true)}
            >
              Remover registro do topo <ButtonArrow />
            </Button>
          </div>
        </Card>
      </div>

      <ConfirmDialog
        open={confirmando}
        onOpenChange={setConfirmando}
        title="Remover o registro do topo?"
        description={`"${topo?.texto ?? ""}" sai do histórico. A operação em si não é desfeita.`}
        confirmLabel="Remover registro"
        onConfirm={() => remover.mutate()}
      />
    </div>
  );
}
