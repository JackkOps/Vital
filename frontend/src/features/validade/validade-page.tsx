import * as React from "react";
import { PageHeader } from "@/components/layout/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
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
import {
  AlertTriangle,
  CalendarClock,
  CheckCircle2,
  PackageX,
  ShieldAlert,
  Trash2,
} from "lucide-react";

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

  const hoje = new Date().toISOString().slice(0, 10);
  const vencidas = (bolsas ?? []).filter((b) => b.status === "DISPONIVEL" && b.dataValidade < hoje);

  return (
    <div>
      <PageHeader
        title="Validade das bolsas"
        description="Acompanhe vencimentos e registre baixas antes que uma bolsa vencida pareça disponível."
      />

      <div className="grid gap-5 lg:grid-cols-2 lg:items-start">
        <div className="rounded-panel border border-border bg-surface p-5">
          <div className="flex items-start gap-3">
            <span className="grid size-9 flex-none place-items-center rounded-control bg-emergencia-soft text-emergencia">
              <CalendarClock className="size-4" />
            </span>
            <div>
              <p className="text-sm font-semibold text-ink">Verificar validade agora</p>
              <p className="text-sm text-ink-muted">
                Dá baixa por vencimento em todas as bolsas disponíveis com validade anterior a hoje. A
                verificação automática roda todo dia à 01:00.
              </p>
            </div>
          </div>

          {vencidas.length > 0 && (
            <ul className="mt-4 flex flex-col gap-2">
              {vencidas.map((b) => (
                <li
                  key={b.id}
                  className="flex items-center gap-3 rounded-control border border-emergencia/25 bg-emergencia-soft/40 px-3 py-2"
                >
                  <PackageX className="size-4 flex-none text-emergencia" />
                  <span className="font-mono text-xs font-semibold text-ink">{b.identificador}</span>
                  <span className="text-xs text-ink-muted">
                    {DESCRICAO_TIPO_SANGUINEO[b.tipoSanguineo]} · venceu em {formatarData(b.dataValidade)}
                  </span>
                  <Button
                    size="sm"
                    variant="secondary"
                    className="ml-auto hover:border-emergencia hover:text-emergencia"
                    onClick={() => darBaixa.mutate(b.id)}
                  >
                    Dar baixa manual
                  </Button>
                </li>
              ))}
            </ul>
          )}

          <Button
            variant="danger"
            className="mt-4 w-full"
            disabled={verificar.isPending}
            onClick={() => setConfirmarVerificacao(true)}
          >
            <Trash2 /> Verificar e dar baixa nas vencidas
          </Button>
        </div>

        <div className="rounded-panel border border-border bg-surface">
          <div className="flex items-center gap-3 border-b border-border px-5 py-4">
            <h2 className="text-[0.95rem] font-semibold text-ink">Próximas do vencimento</h2>
            <span className="ml-auto text-xs tabular text-ink-muted">{(alertas ?? []).length}</span>
          </div>
          <div className="flex flex-wrap gap-1.5 border-b border-border px-5 py-3">
            <span className="text-xs text-ink-muted">Janela de alerta:</span>
            {TIPOS_COMPONENTE.map((c) => (
              <Badge key={c} tone={TONE_POR_COMPONENTE[c]}>
                {DESCRICAO_COMPONENTE[c]}: {JANELA_ALERTA_DIAS[c]} {JANELA_ALERTA_DIAS[c] === 1 ? "dia" : "dias"}
              </Badge>
            ))}
          </div>
          <div className="p-4">
            {(alertas ?? []).length === 0 ? (
              <EmptyState
                icon={ShieldAlert}
                title="Nada próximo do vencimento"
                description="Bolsas disponíveis aparecem aqui quando entram na janela de alerta do seu componente."
              />
            ) : (
              <ul className="flex flex-col gap-2">
                {(alertas ?? []).map((a) => (
                  <li
                    key={a.bolsaId}
                    className="flex items-center gap-3 rounded-control border border-border px-3 py-2"
                  >
                    <AlertTriangle
                      className={`size-4 flex-none ${a.diasParaVencer <= 1 ? "text-emergencia" : "text-alerta"}`}
                    />
                    <span className="font-mono text-xs font-semibold text-ink">{a.identificador}</span>
                    <span className="text-xs text-ink-muted">
                      {DESCRICAO_TIPO_SANGUINEO[a.tipoSanguineo]} · {DESCRICAO_COMPONENTE[a.tipoComponente]} ·
                      validade {formatarData(a.dataValidade)}
                    </span>
                    <Badge tone={a.diasParaVencer <= 1 ? "emergencia" : "alerta"} className="ml-auto">
                      {a.diasParaVencer === 0 ? "vence hoje" : a.diasParaVencer === 1 ? "vence amanhã" : `${a.diasParaVencer} dias`}
                    </Badge>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>
      </div>

      <div className="mt-5 rounded-panel border border-border bg-surface">
        <div className="flex items-center gap-3 border-b border-border px-5 py-4">
          <h2 className="text-[0.95rem] font-semibold text-ink">Baixas registradas</h2>
          <span className="text-xs tabular text-ink-muted">{(baixas ?? []).length}</span>
        </div>
        {(baixas ?? []).length === 0 ? (
          <EmptyState icon={Trash2} title="Nenhuma baixa registrada" description="Baixas por vencimento, manuais ou automáticas, aparecem aqui." />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full whitespace-nowrap text-left text-sm">
              <thead>
                <tr className="border-b border-border text-xs text-ink-muted">
                  <th className="px-5 py-2.5 font-medium">Bolsa</th>
                  <th className="px-5 py-2.5 font-medium">Motivo</th>
                  <th className="px-5 py-2.5 font-medium">Data da baixa</th>
                  <th className="px-5 py-2.5 font-medium">Origem</th>
                </tr>
              </thead>
              <tbody>
                {[...(baixas ?? [])].reverse().map((b) => (
                  <tr key={b.id} className="border-b border-border last:border-0">
                    <td className="px-5 py-2.5 font-mono text-[13px] font-semibold text-ink">{b.identificadorBolsa}</td>
                    <td className="px-5 py-2.5 text-ink-muted">Vencimento</td>
                    <td className="px-5 py-2.5 tabular text-ink-muted">{formatarData(b.dataBaixa)}</td>
                    <td className="px-5 py-2.5">
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
        )}
      </div>

      <ConfirmDialog
        open={confirmarVerificacao}
        onOpenChange={setConfirmarVerificacao}
        title={
          vencidas.length === 1
            ? "Dar baixa em 1 bolsa vencida?"
            : `Dar baixa em ${vencidas.length} bolsa(s) vencida(s)?`
        }
        description="O status passa para DESCARTADA e a baixa é registrada como automática. Não é possível desfazer."
        confirmLabel={vencidas.length === 1 ? "Dar baixa em 1 bolsa" : `Dar baixa em ${vencidas.length} bolsas`}
        onConfirm={() => verificar.mutate()}
      />
    </div>
  );
}
