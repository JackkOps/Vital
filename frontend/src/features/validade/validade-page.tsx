import * as React from "react";
import { PageHeader } from "@/components/layout/page-header";
import { Badge } from "@/components/ui/badge";
import { Button, ButtonArrow } from "@/components/ui/button";
import { Card, CardHeader } from "@/components/ui/card";
import { Stat, StatRow, tabela } from "@/components/ui/stat";
import { Paginacao, usePaginacao } from "@/components/ui/pagination";
import { cn } from "@/lib/utils";
import { ConfirmDialog } from "@/components/ui/confirm-dialog";
import { EmptyState } from "@/components/layout/empty-state";
import {
  DESCRICAO_COMPONENTE,
  DESCRICAO_TIPO_SANGUINEO,
  TIPOS_COMPONENTE,
} from "@/api/types";
import { JANELA_ALERTA_DIAS, TONE_POR_COMPONENTE } from "@/lib/domain";
import { formatarData } from "@/lib/format";
import {
  useAlertasValidade,
  useBaixas,
  useBolsas,
  useVerificarValidade,
} from "@/hooks/use-api";
import { api } from "@/api/client";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useToast } from "@/hooks/use-toast";
import { CheckCircle2, ShieldAlert, ShieldCheck, Trash2 } from "lucide-react";

function useDarBaixaManual() {
  const qc = useQueryClient();
  const { toast } = useToast();
  return useMutation({
    mutationFn: (bolsaId: number) => api.validade.darBaixa(bolsaId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["bolsas"] });
      qc.invalidateQueries({ queryKey: ["validade"] });
      qc.invalidateQueries({ queryKey: ["historico"] });
      toast({ title: "Baixa registrada", tone: "info" });
    },
    onError: (erro) =>
      toast({
        title: "Não foi possível dar baixa",
        description: erro instanceof Error ? erro.message : undefined,
        tone: "erro",
      }),
  });
}

export function ValidadePage() {
  const { data: bolsas } = useBolsas();
  const { data: alertas } = useAlertasValidade();
  const { data: baixas } = useBaixas();
  const verificar = useVerificarValidade();
  const darBaixa = useDarBaixaManual();
  const [confirmarVerificacao, setConfirmarVerificacao] = React.useState(false);
  const [pagina, setPagina] = React.useState(1);
  const [verTodos, setVerTodos] = React.useState(false);

  const hoje = new Date().toISOString().slice(0, 10);
  const vencidas = (bolsas ?? []).filter((b) => b.status === "DISPONIVEL" && b.dataValidade < hoje);
  const listaAlertas = [...(alertas ?? [])].sort((a, b) => a.diasParaVencer - b.diasParaVencer);
  const vencemHoje = listaAlertas.filter((a) => a.diasParaVencer === 0).length;
  const mesAtual = hoje.slice(0, 7);
  const baixasOrdenadas = [...(baixas ?? [])].reverse();
  const baixasMes = baixasOrdenadas.filter((b) => b.dataBaixa.startsWith(mesAtual)).length;
  const paginacao = usePaginacao(baixasOrdenadas, pagina, 8);

  return (
    <div>
      <PageHeader
        title="Validade das"
        accent="bolsas."
        description="Acompanhe vencimentos e registre baixas antes que uma bolsa vencida pareça disponível."
      />

      <StatRow className="mb-8 animate-rise">
        <Stat
          value={vencidas.length}
          label="Vencidas ainda disponíveis"
          tone={vencidas.length ? "emergencia" : "ink"}
        />
        <Stat value={vencemHoje} label="Vencem hoje" tone={vencemHoje ? "alerta" : "ink"} />
        <Stat value={listaAlertas.length} label="Na janela de alerta" />
        <Stat value={baixasMes} label="Baixas neste mês" tone="ink" />
      </StatRow>

      <div className="grid gap-6 lg:grid-cols-[minmax(0,0.9fr)_minmax(0,1.1fr)] lg:items-start">
        <Card inverse={vencidas.length === 0} className={cn("lg:sticky lg:top-8", vencidas.length > 0 && "border-emergencia/40")}>
          {vencidas.length === 0 ? (
            <div className="flex min-h-[19rem] flex-col p-6">
              <p className="meta font-mono text-inverse-muted">(01)</p>
              <ShieldCheck className="mt-6 size-7 text-inverse-muted" strokeWidth={1.25} />
              <h2 className="mt-4 text-[1.9rem] font-normal leading-none tracking-[-0.03em]">Nenhuma bolsa</h2>
              <p className="serif-accent pl-6 text-[2.1rem] leading-none text-inverse-muted">vencida em uso</p>
              <p className="mt-5 max-w-sm text-[13px] leading-relaxed text-inverse-muted">
                A verificação automática roda todo dia à 01:00 e dá baixa nas bolsas disponíveis com validade anterior a hoje.
              </p>
              <div className="mt-auto pt-6">
                <Button variant="inverse" disabled={verificar.isPending} onClick={() => setConfirmarVerificacao(true)}>
                  Verificar agora <ButtonArrow />
                </Button>
              </div>
            </div>
          ) : (
            <>
              <CardHeader index="01" title="Vencidas" accent="ainda disponíveis" meta={vencidas.length} />
              <p className="border-b border-border bg-emergencia-soft/50 px-5 py-3 text-[13px] text-emergencia">
                Estas bolsas passaram da validade e ainda aparecem como disponíveis. Dê baixa antes que sejam alocadas.
              </p>
              <ul>
                {vencidas.map((b) => (
                  <li key={b.id} className="flex flex-wrap items-center gap-x-4 gap-y-2 border-b border-border px-5 py-3.5 last:border-0">
                    <div className="min-w-0 flex-1">
                      <p className="font-mono text-[13px] font-medium text-ink">{b.identificador}</p>
                      <p className="serif-accent text-[14px] text-emergencia">
                        {DESCRICAO_TIPO_SANGUINEO[b.tipoSanguineo]} · venceu em {formatarData(b.dataValidade)}
                      </p>
                    </div>
                    <Button
                      size="sm"
                      variant="secondary"
                      className="hover:border-emergencia hover:bg-emergencia hover:text-white"
                      onClick={() => darBaixa.mutate(b.id)}
                    >
                      Dar baixa
                    </Button>
                  </li>
                ))}
              </ul>
              <div className="flex flex-wrap items-center justify-between gap-3 border-t border-border px-5 py-4">
                <p className="text-xs text-ink-muted">Rotina automática diária às 01:00</p>
                <Button variant="danger" disabled={verificar.isPending} onClick={() => setConfirmarVerificacao(true)}>
                  <Trash2 /> Dar baixa em todas
                </Button>
              </div>
            </>
          )}
        </Card>

        <Card>
          <CardHeader index="02" title="Próximas do" accent="vencimento" meta={listaAlertas.length} />
          <div className="flex flex-wrap items-center gap-1.5 border-b border-border bg-background/40 px-5 py-3">
            <span className="mr-1 text-xs text-ink-muted">Janela de alerta</span>
            {TIPOS_COMPONENTE.map((c) => (
              <Badge key={c} tone={TONE_POR_COMPONENTE[c]}>
                {DESCRICAO_COMPONENTE[c]} · {JANELA_ALERTA_DIAS[c]} {JANELA_ALERTA_DIAS[c] === 1 ? "dia" : "dias"}
              </Badge>
            ))}
          </div>
          {listaAlertas.length === 0 ? (
            <EmptyState
              icon={ShieldAlert}
              title="Nada próximo do vencimento"
              description="Bolsas disponíveis aparecem aqui quando entram na janela de alerta do seu componente."
            />
          ) : (
            <>
            <ul>
              {(verTodos ? listaAlertas : listaAlertas.slice(0, 8)).map((a) => {
                const critico = a.diasParaVencer <= 1;
                return (
                  <li key={a.bolsaId} className="grid grid-cols-[4.5rem_minmax(0,1fr)_auto] items-center gap-4 border-b border-border px-5 py-3.5 last:border-0">
                    <p className={cn("display-num text-[2.2rem]", critico ? "text-emergencia" : "text-brand")}>
                      {a.diasParaVencer}
                      <span className="ml-1 text-[11px] tracking-normal text-ink-muted">{a.diasParaVencer === 1 ? "dia" : "dias"}</span>
                    </p>
                    <div className="min-w-0">
                      <p className="truncate font-mono text-[13px] font-medium text-ink">{a.identificador}</p>
                      <p className="truncate text-xs text-ink-muted">
                        {DESCRICAO_TIPO_SANGUINEO[a.tipoSanguineo]} · {DESCRICAO_COMPONENTE[a.tipoComponente]} · validade {formatarData(a.dataValidade)}
                      </p>
                    </div>
                    <Badge tone={critico ? "emergencia" : "alerta"}>
                      {a.diasParaVencer === 0 ? "Vence hoje" : a.diasParaVencer === 1 ? "Vence amanhã" : "Em alerta"}
                    </Badge>
                  </li>
                );
              })}
            </ul>
            {listaAlertas.length > 8 && (
              <div className="border-t border-border px-5 py-3 text-center">
                <Button variant="link" size="sm" onClick={() => setVerTodos((v) => !v)}>
                  {verTodos ? "Mostrar menos" : `Ver todas as ${listaAlertas.length} bolsas em alerta`}
                </Button>
              </div>
            )}
            </>
          )}
        </Card>
      </div>

      <Card className="mt-6">
        <CardHeader index="03" title="Baixas" accent="registradas" meta={baixasOrdenadas.length} />
        {baixasOrdenadas.length === 0 ? (
          <EmptyState icon={Trash2} title="Nenhuma baixa registrada" description="Baixas por vencimento, manuais ou automáticas, aparecem aqui." />
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className={tabela.table}>
                <thead>
                  <tr className={tabela.headRow}>
                    <th className={tabela.th}>Bolsa</th>
                    <th className={tabela.th}>Motivo</th>
                    <th className={tabela.th}>Data da baixa</th>
                    <th className={tabela.th}>Origem</th>
                  </tr>
                </thead>
                <tbody>
                  {paginacao.visiveis.map((b) => (
                    <tr key={b.id} className={tabela.row}>
                      <td className={cn(tabela.td, "font-mono text-[13px] font-medium text-ink")}>{b.identificadorBolsa}</td>
                      <td className={cn(tabela.td, "text-ink-muted")}>Vencimento</td>
                      <td className={cn(tabela.td, "tabular text-ink-muted")}>{formatarData(b.dataBaixa)}</td>
                      <td className={tabela.td}>
                        <Badge tone={b.automatica ? "info" : "neutral"}>
                          <CheckCircle2 className="size-3" />
                          {b.automatica ? "Automática" : "Manual"}
                        </Badge>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <Paginacao {...paginacao} total={baixasOrdenadas.length} onChange={setPagina} />
          </>
        )}
      </Card>

      <ConfirmDialog
        open={confirmarVerificacao}
        onOpenChange={setConfirmarVerificacao}
        title={
          vencidas.length === 0
            ? "Rodar a verificação de validade agora?"
            : vencidas.length === 1
              ? "Dar baixa em 1 bolsa vencida?"
              : `Dar baixa em ${vencidas.length} bolsas vencidas?`
        }
        description={
          vencidas.length === 0
            ? "Nenhuma bolsa vencida foi encontrada no estoque carregado; a verificação confirma isso no servidor."
            : "O status passa para DESCARTADA e a baixa é registrada como automática. Não é possível desfazer."
        }
        danger={vencidas.length > 0}
        confirmLabel={
          vencidas.length === 0 ? "Verificar" : vencidas.length === 1 ? "Dar baixa em 1 bolsa" : `Dar baixa em ${vencidas.length} bolsas`
        }
        onConfirm={() => verificar.mutate()}
      />
    </div>
  );
}
