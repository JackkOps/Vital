import * as React from "react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { ConfirmDialog } from "@/components/ui/confirm-dialog";
import { EmptyState } from "@/components/layout/empty-state";
import {
  DESCRICAO_COMPONENTE,
  DESCRICAO_TIPO_SANGUINEO,
  type SolicitacaoResponse,
} from "@/api/types";
import { URGENCIA_INFO } from "@/lib/domain";
import {
  useAlocarBolsa,
  useBolsasAlocadas,
  useCancelarAlocacao,
  useCancelarSolicitacao,
  useChamarProximaDaFila,
  useExcluirSolicitacao,
  useFilaSolicitacoes,
  useSolicitacoes,
} from "@/hooks/use-api";
import { ArrowRight, Link2, ListChecks, Unlink } from "lucide-react";

function codigo(id: number) {
  return `SOLIC-${String(id).padStart(4, "0")}`;
}

function CartaoFila({ solicitacao, posicao }: { solicitacao: SolicitacaoResponse; posicao: number }) {
  const urgencia = URGENCIA_INFO[solicitacao.nivelUrgencia];
  return (
    <li className="flex items-center gap-3 rounded-control border border-border px-3 py-2.5">
      <span className="grid size-6 flex-none place-items-center rounded-full bg-surface-muted text-xs font-bold tabular text-ink-muted">
        {posicao}
      </span>
      <div className="min-w-0 flex-1">
        <p className="truncate text-sm font-semibold text-ink">{solicitacao.nomeHospital}</p>
        <p className="text-xs text-ink-muted">
          {DESCRICAO_TIPO_SANGUINEO[solicitacao.tipoSanguineo]} · {DESCRICAO_COMPONENTE[solicitacao.tipoComponente]} ·{" "}
          {solicitacao.quantidade} bolsa(s)
        </p>
      </div>
      <Badge tone={urgencia.tone}>{urgencia.label}</Badge>
    </li>
  );
}

function CartaoEmAtendimento({ solicitacao }: { solicitacao: SolicitacaoResponse }) {
  const { data: alocadas } = useBolsasAlocadas(solicitacao.id);
  const alocar = useAlocarBolsa();
  const cancelarAlocacao = useCancelarAlocacao();
  const cancelarSolicitacao = useCancelarSolicitacao();
  const excluir = useExcluirSolicitacao();
  const [confirmando, setConfirmando] = React.useState<"cancelar" | "excluir" | null>(null);
  const completa = solicitacao.bolsasAlocadas >= solicitacao.quantidade;
  const urgencia = URGENCIA_INFO[solicitacao.nivelUrgencia];

  return (
    <div className="rounded-control border border-border p-3.5">
      <div className="flex flex-wrap items-center gap-2">
        <span className="font-mono text-xs font-semibold text-info">{codigo(solicitacao.id)}</span>
        <p className="text-sm font-semibold text-ink">{solicitacao.nomeHospital}</p>
        <Badge tone={urgencia.tone}>{urgencia.label}</Badge>
        <span className="ml-auto text-xs tabular text-ink-muted">
          {solicitacao.bolsasAlocadas}/{solicitacao.quantidade} alocada(s)
        </span>
      </div>
      <p className="mt-1 text-xs text-ink-muted">
        {DESCRICAO_TIPO_SANGUINEO[solicitacao.tipoSanguineo]} · {DESCRICAO_COMPONENTE[solicitacao.tipoComponente]}
      </p>

      {alocadas && alocadas.length > 0 && (
        <ul className="mt-2.5 flex flex-wrap gap-1.5">
          {alocadas.map((b) => (
            <li key={b.id}>
              <button
                type="button"
                onClick={() => cancelarAlocacao.mutate(b.id)}
                className="inline-flex items-center gap-1.5 rounded-control border border-border-strong bg-surface-muted px-2 py-1 font-mono text-[11px] font-semibold text-ink-muted hover:border-emergencia hover:text-emergencia"
                title="Cancelar alocação desta bolsa"
              >
                {b.identificador}
                <Unlink className="size-3" />
              </button>
            </li>
          ))}
        </ul>
      )}

      <div className="mt-3 flex flex-wrap gap-2">
        <Button
          size="sm"
          disabled={completa || alocar.isPending}
          onClick={() => alocar.mutate(solicitacao.id)}
        >
          <Link2 /> {completa ? "Quantidade atingida" : "Alocar bolsa compatível"}
        </Button>
        <Button size="sm" variant="secondary" onClick={() => setConfirmando("cancelar")}>
          Cancelar solicitação
        </Button>
        <Button size="sm" variant="ghost" onClick={() => setConfirmando("excluir")}>
          Excluir solicitação
        </Button>
      </div>

      <ConfirmDialog
        open={confirmando === "cancelar"}
        onOpenChange={(open) => !open && setConfirmando(null)}
        title={`Cancelar a solicitação de ${solicitacao.nomeHospital}?`}
        description="As bolsas já alocadas para este pedido voltam a ficar disponíveis no estoque."
        confirmLabel="Cancelar solicitação"
        onConfirm={() => cancelarSolicitacao.mutate(solicitacao.id)}
      />
      <ConfirmDialog
        open={confirmando === "excluir"}
        onOpenChange={(open) => !open && setConfirmando(null)}
        title={`Excluir ${codigo(solicitacao.id)}?`}
        description="O registro da solicitação é removido permanentemente."
        confirmLabel="Excluir"
        onConfirm={() => excluir.mutate(solicitacao.id)}
      />
    </div>
  );
}

export function FilaCard() {
  const { data: fila } = useFilaSolicitacoes();
  const chamarProxima = useChamarProximaDaFila();

  return (
    <div className="rounded-panel border border-border bg-surface">
      <div className="border-b border-border px-5 py-4">
        <div className="flex items-center gap-2">
          <h2 className="text-[0.95rem] font-semibold text-ink">Fila de atendimento</h2>
          <span className="ml-auto whitespace-nowrap rounded-control bg-surface-muted px-2 py-0.5 text-xs font-semibold tabular text-ink-muted">
            {(fila ?? []).length} na fila
          </span>
        </div>
        <p className="mt-0.5 text-xs text-ink-muted">Por urgência; no mesmo nível, ordem de chegada</p>
      </div>
      <div className="p-4">
        <Button
          variant="secondary"
          className="mb-3 w-full"
          disabled={!(fila ?? []).length || chamarProxima.isPending}
          onClick={() => chamarProxima.mutate()}
        >
          <ArrowRight /> Chamar próxima
        </Button>
        {(fila ?? []).length === 0 ? (
          <EmptyState
            icon={ListChecks}
            title="Fila vazia"
            description="Novos pedidos pendentes aparecem aqui, ordenados por urgência."
          />
        ) : (
          <ol className="flex flex-col gap-2">
            {(fila ?? []).map((s, i) => (
              <CartaoFila key={s.id} solicitacao={s} posicao={i + 1} />
            ))}
          </ol>
        )}
      </div>
    </div>
  );
}

export function EmAtendimentoCard() {
  const { data: todas } = useSolicitacoes();
  const emAtendimento = (todas ?? [])
    .filter((s) => s.status === "EM_ATENDIMENTO")
    .sort((a, b) => b.id - a.id);

  return (
    <div className="rounded-panel border border-border bg-surface">
      <div className="border-b border-border px-5 py-4">
        <h2 className="text-[0.95rem] font-semibold text-ink">Em atendimento</h2>
        <p className="mt-0.5 text-xs text-ink-muted">Compatibilidade ABO/Rh e vence primeiro, sai primeiro</p>
      </div>
      <div className="flex flex-col gap-3 p-4">
        {emAtendimento.length === 0 ? (
          <EmptyState
            icon={Link2}
            title="Nada em atendimento"
            description='Use "Chamar próxima" para tirar o primeiro pedido da fila e começar a alocar bolsas.'
          />
        ) : (
          emAtendimento.map((s) => <CartaoEmAtendimento key={s.id} solicitacao={s} />)
        )}
      </div>
    </div>
  );
}
