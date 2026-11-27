import * as React from "react";
import { Button, ButtonArrow } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Card, CardHeader } from "@/components/ui/card";
import { ConfirmDialog } from "@/components/ui/confirm-dialog";
import { EmptyState } from "@/components/layout/empty-state";
import {
  DESCRICAO_COMPONENTE,
  DESCRICAO_TIPO_SANGUINEO,
  type NivelUrgencia,
  type SolicitacaoResponse,
} from "@/api/types";
import { URGENCIA_INFO } from "@/lib/domain";
import { cn } from "@/lib/utils";
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
import { Link2, MoreHorizontal, X } from "lucide-react";

export function codigoSolicitacao(id: number) {
  return `SOLIC-${String(id).padStart(4, "0")}`;
}

/** Selo de urgência legível sobre a lâmina azul. */
const URGENCIA_INVERSA: Record<NivelUrgencia, string> = {
  EMERGENCIA: "bg-[#ffd2c7] text-[#8f1d0e]",
  URGENTE: "bg-[#f7e0b0] text-[#6b4200]",
  ELETIVA: "border border-inverse-line text-inverse-muted",
};

function ItemFila({ solicitacao, posicao }: { solicitacao: SolicitacaoResponse; posicao: number }) {
  const urgencia = URGENCIA_INFO[solicitacao.nivelUrgencia];
  const primeiro = posicao === 1;
  return (
    <li className="flex items-start gap-4 border-b border-inverse-line py-3.5 last:border-0">
      <span className="w-7 pt-0.5 font-mono text-[11px] text-inverse-muted">({String(posicao).padStart(2, "0")})</span>
      <div className="min-w-0 flex-1">
        <p className={cn("truncate font-medium leading-tight", primeiro ? "text-[1.05rem]" : "text-[14.5px]")}>
          {solicitacao.nomeHospital}
        </p>
        <p className="serif-accent mt-0.5 text-[14px] text-inverse-muted">
          {DESCRICAO_TIPO_SANGUINEO[solicitacao.tipoSanguineo]} · {DESCRICAO_COMPONENTE[solicitacao.tipoComponente]} ·{" "}
          {solicitacao.quantidade} {solicitacao.quantidade === 1 ? "bolsa" : "bolsas"}
        </p>
      </div>
      <span className={cn("rounded-full px-2.5 py-0.5 text-[11.5px] font-medium", URGENCIA_INVERSA[solicitacao.nivelUrgencia])}>
        {urgencia.label}
      </span>
    </li>
  );
}

export function FilaCard() {
  const { data: fila, isLoading } = useFilaSolicitacoes();
  const chamarProxima = useChamarProximaDaFila();
  const lista = fila ?? [];

  return (
    <Card inverse className="flex flex-col">
      <div className="flex items-start justify-between gap-3 px-5 pb-2 pt-5">
        <div>
          <p className="meta font-mono text-inverse-muted">(02)</p>
          <h2 className="mt-2 text-[1.7rem] font-normal leading-none tracking-[-0.03em]">Fila de</h2>
          <p className="serif-accent pl-6 text-[1.9rem] leading-none text-inverse-muted">atendimento</p>
        </div>
        <div className="text-right">
          <p className="display-num text-[3.2rem]">{isLoading ? "—" : lista.length}</p>
          <p className="serif-accent text-[14px] text-inverse-muted">na fila</p>
        </div>
      </div>
      <p className="px-5 text-xs text-inverse-muted">
        <span className="opacity-70">[</span>Por urgência; no mesmo nível, ordem de chegada<span className="opacity-70">]</span>
      </p>

      <div className="flex-1 px-5 pt-3">
        {lista.length === 0 ? (
          <div className="py-10 text-center">
            <p className="serif-accent text-xl">Fila vazia</p>
            <p className="mt-1 text-[13px] text-inverse-muted">Novos pedidos pendentes aparecem aqui, ordenados por urgência.</p>
          </div>
        ) : (
          <ol className="border-t border-inverse-line">
            {lista.map((s, i) => (
              <ItemFila key={s.id} solicitacao={s} posicao={i + 1} />
            ))}
          </ol>
        )}
      </div>

      <div className="flex items-center justify-between gap-3 border-t border-inverse-line px-5 py-4">
        <p className="text-xs text-inverse-muted">
          {lista[0] ? <>Próximo: <span className="text-inverse-ink">{lista[0].nomeHospital}</span></> : "Nada aguardando"}
        </p>
        <Button
          variant="inverse"
          disabled={!lista.length || chamarProxima.isPending}
          onClick={() => chamarProxima.mutate()}
        >
          Chamar próxima <ButtonArrow />
        </Button>
      </div>
    </Card>
  );
}

function CartaoEmAtendimento({ solicitacao }: { solicitacao: SolicitacaoResponse }) {
  const { data: alocadas } = useBolsasAlocadas(solicitacao.id);
  const alocar = useAlocarBolsa();
  const cancelarAlocacao = useCancelarAlocacao();
  const cancelarSolicitacao = useCancelarSolicitacao();
  const excluir = useExcluirSolicitacao();
  const [confirmando, setConfirmando] = React.useState<"cancelar" | "excluir" | null>(null);
  const [menu, setMenu] = React.useState(false);
  const completa = solicitacao.bolsasAlocadas >= solicitacao.quantidade;
  const urgencia = URGENCIA_INFO[solicitacao.nivelUrgencia];
  const progresso = Math.min(100, (solicitacao.bolsasAlocadas / solicitacao.quantidade) * 100);

  return (
    <article className="border-b border-border px-5 py-5 last:border-0">
      <div className="flex items-start gap-4">
        <div className="min-w-0 flex-1">
          <div className="flex flex-wrap items-center gap-2">
            <span className="font-mono text-[11px] text-brand">{codigoSolicitacao(solicitacao.id)}</span>
            <Badge tone={urgencia.tone}>{urgencia.label}</Badge>
          </div>
          <p className="mt-1.5 text-[1.05rem] font-medium leading-tight text-ink">{solicitacao.nomeHospital}</p>
          <p className="serif-accent text-[15px] text-brand">
            {DESCRICAO_TIPO_SANGUINEO[solicitacao.tipoSanguineo]} · {DESCRICAO_COMPONENTE[solicitacao.tipoComponente]}
          </p>
        </div>
        <div className="text-right">
          <p className={cn("display-num text-[2.3rem]", completa ? "text-ok" : "text-ink")}>
            {solicitacao.bolsasAlocadas}
            <span className="text-ink-faint">/{solicitacao.quantidade}</span>
          </p>
          <p className="meta text-ink-muted">alocadas</p>
        </div>
      </div>

      <div className="mt-3 h-[3px] overflow-hidden rounded-full bg-surface-muted" role="progressbar" aria-valuemin={0} aria-valuemax={solicitacao.quantidade} aria-valuenow={solicitacao.bolsasAlocadas} aria-label="Bolsas alocadas">
        <div className={cn("h-full rounded-full transition-[width] duration-500", completa ? "bg-ok" : "bg-brand")} style={{ width: `${progresso}%` }} />
      </div>

      {alocadas && alocadas.length > 0 && (
        <ul className="mt-3 flex flex-wrap gap-1.5" aria-label="Bolsas alocadas">
          {alocadas.map((b) => (
            <li key={b.id}>
              <button
                type="button"
                onClick={() => cancelarAlocacao.mutate(b.id)}
                className="group/chip inline-flex items-center gap-1.5 rounded-full border border-border-strong py-0.5 pl-2.5 pr-1.5 font-mono text-[11px] text-ink-muted transition-colors hover:border-emergencia hover:bg-emergencia-soft hover:text-emergencia"
                title="Cancelar alocação desta bolsa"
                aria-label={`Cancelar alocação de ${b.identificador}`}
              >
                {b.identificador}
                <X className="size-3 opacity-50 group-hover/chip:opacity-100" />
              </button>
            </li>
          ))}
        </ul>
      )}

      <div className="mt-4 flex flex-wrap items-center gap-2">
        <Button size="sm" disabled={completa || alocar.isPending} onClick={() => alocar.mutate(solicitacao.id)}>
          <Link2 /> {completa ? "Quantidade atingida" : "Alocar bolsa compatível"}
        </Button>
        <div className="relative ml-auto">
          <Button size="icon" variant="ghost" aria-label="Mais ações" aria-expanded={menu} onClick={() => setMenu((v) => !v)}>
            <MoreHorizontal />
          </Button>
          {menu && (
            <div
              className="absolute right-0 top-9 z-10 w-52 animate-rise overflow-hidden rounded-panel border border-border bg-surface py-1 shadow-[0_16px_40px_-12px_rgba(11,20,64,0.3)]"
              onMouseLeave={() => setMenu(false)}
            >
              <button type="button" className="block w-full px-3.5 py-2 text-left text-[13px] text-ink hover:bg-surface-muted" onClick={() => { setMenu(false); setConfirmando("cancelar"); }}>
                Cancelar solicitação
              </button>
              <button type="button" className="block w-full px-3.5 py-2 text-left text-[13px] text-emergencia hover:bg-emergencia-soft" onClick={() => { setMenu(false); setConfirmando("excluir"); }}>
                Excluir registro
              </button>
            </div>
          )}
        </div>
      </div>

      <ConfirmDialog
        open={confirmando === "cancelar"}
        onOpenChange={(open) => !open && setConfirmando(null)}
        title={`Cancelar o pedido de ${solicitacao.nomeHospital}?`}
        description="As bolsas já alocadas para este pedido voltam a ficar disponíveis no estoque."
        confirmLabel="Cancelar solicitação"
        onConfirm={() => cancelarSolicitacao.mutate(solicitacao.id)}
      />
      <ConfirmDialog
        open={confirmando === "excluir"}
        onOpenChange={(open) => !open && setConfirmando(null)}
        title={`Excluir ${codigoSolicitacao(solicitacao.id)}?`}
        description="O registro da solicitação é removido permanentemente."
        confirmLabel="Excluir"
        onConfirm={() => excluir.mutate(solicitacao.id)}
      />
    </article>
  );
}

export function EmAtendimentoCard() {
  const { data: todas } = useSolicitacoes();
  const emAtendimento = (todas ?? [])
    .filter((s) => s.status === "EM_ATENDIMENTO")
    .sort((a, b) => b.id - a.id);

  return (
    <Card>
      <CardHeader index="03" title="Em" accent="atendimento" meta={emAtendimento.length} />
      <p className="border-b border-border px-5 py-2.5 text-xs text-ink-muted">
        <span className="text-ink-faint">[</span>Compatibilidade ABO/Rh · vence primeiro, sai primeiro<span className="text-ink-faint">]</span>
      </p>
      {emAtendimento.length === 0 ? (
        <EmptyState
          icon={Link2}
          title="Nada em atendimento"
          description='Use "Chamar próxima" para tirar o primeiro pedido da fila e começar a alocar bolsas.'
        />
      ) : (
        emAtendimento.map((s) => <CartaoEmAtendimento key={s.id} solicitacao={s} />)
      )}
    </Card>
  );
}
