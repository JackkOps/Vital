import * as React from "react";
import { PageHeader } from "@/components/layout/page-header";
import { Input, Select } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { KpiCard } from "./kpi-strip";
import { EstoqueHeatmap } from "./estoque-heatmap";
import { DemandaHospitais } from "./demanda-hospitais";
import { PeriodoPanel } from "./periodo-panel";
import { DESCRICAO_COMPONENTE, TIPOS_COMPONENTE, type TipoComponente } from "@/api/types";
import { formatarData } from "@/lib/format";
import {
  useAlertasValidade,
  useBaixas,
  useBolsas,
  useIndicadores,
  useSolicitacoes,
} from "@/hooks/use-api";
import { AlertTriangle, CheckCircle2, ClipboardList, Trash2 } from "lucide-react";

export function IndicadoresPage() {
  const [dataInicio, setDataInicio] = React.useState("");
  const [dataFim, setDataFim] = React.useState("");
  const [hospital, setHospital] = React.useState("");
  const [tipoComponente, setTipoComponente] = React.useState<TipoComponente | "">("");

  const { data: bolsas } = useBolsas();
  const { data: alertas } = useAlertasValidade();
  const { data: solicitacoes } = useSolicitacoes();
  const { data: baixas } = useBaixas();
  const { data: indicadores } = useIndicadores({
    dataInicio: dataInicio || undefined,
    dataFim: dataFim || undefined,
    hospital: hospital || undefined,
    tipoComponente: tipoComponente || undefined,
  });

  const hospitais = [...new Set((solicitacoes ?? []).map((s) => s.nomeHospital))].sort((a, b) =>
    a.localeCompare(b, "pt-BR"),
  );

  // KPIs — combinam dados já carregados nas outras páginas (como no app original).
  const totalDisponivel = (bolsas ?? []).filter((b) => b.status === "DISPONIVEL").length;
  const vencemHoje = (alertas ?? []).filter((a) => a.diasParaVencer === 0).length;
  const ateDoisDias = (alertas ?? []).filter((a) => a.diasParaVencer > 0 && a.diasParaVencer <= 2).length;

  const abertas = (solicitacoes ?? []).filter((s) => s.status === "PENDENTE" || s.status === "EM_ATENDIMENTO");
  const naFila = abertas.filter((s) => s.status === "PENDENTE").length;
  const bolsasPendentes = abertas.reduce(
    (soma, s) => soma + Math.max(0, s.quantidade - s.bolsasAlocadas),
    0,
  );

  const mesAtual = new Date().toISOString().slice(0, 7);
  const baixasMes = (baixas ?? []).filter((b) => b.dataBaixa.startsWith(mesAtual));
  const automaticas = baixasMes.filter((b) => b.automatica).length;

  const componentesFiltro = tipoComponente ? [tipoComponente] : TIPOS_COMPONENTE;

  return (
    <div>
      <PageHeader
        title="Indicadores"
        description="Situação do estoque e da demanda hospitalar."
      />

      <div className="mb-5 rounded-panel border border-border bg-surface p-4">
        <div className="flex flex-wrap items-end gap-3">
          <div className="space-y-1.5">
            <Label htmlFor="ind-inicio">De</Label>
            <Input id="ind-inicio" type="date" className="h-9 w-36" value={dataInicio} onChange={(e) => setDataInicio(e.target.value)} />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="ind-fim">Até</Label>
            <Input id="ind-fim" type="date" className="h-9 w-36" value={dataFim} onChange={(e) => setDataFim(e.target.value)} />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="ind-hospital">Hospital</Label>
            <Select id="ind-hospital" className="h-9 w-48" value={hospital} onChange={(e) => setHospital(e.target.value)}>
              <option value="">Todos</option>
              {hospitais.map((h) => (
                <option key={h} value={h}>{h}</option>
              ))}
            </Select>
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="ind-componente">Componente</Label>
            <Select
              id="ind-componente"
              className="h-9 w-44"
              value={tipoComponente}
              onChange={(e) => setTipoComponente(e.target.value as TipoComponente | "")}
            >
              <option value="">Todos</option>
              {TIPOS_COMPONENTE.map((c) => (
                <option key={c} value={c}>{DESCRICAO_COMPONENTE[c]}</option>
              ))}
            </Select>
          </div>
        </div>
      </div>

      <div className="mb-5 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <KpiCard
          icon={CheckCircle2}
          title="Total disponível"
          value={totalDisponivel}
          unit={totalDisponivel === 1 ? "bolsa" : "bolsas"}
          detail={TIPOS_COMPONENTE.map((c) => `${(bolsas ?? []).filter((b) => b.status === "DISPONIVEL" && b.tipoComponente === c).length} ${DESCRICAO_COMPONENTE[c].toLowerCase()}`).join(" · ")}
        />
        <KpiCard
          icon={AlertTriangle}
          title="Próximas do vencimento"
          value={(alertas ?? []).length}
          unit={(alertas ?? []).length === 1 ? "bolsa" : "bolsas"}
          detail={`${vencemHoje} vence${vencemHoje === 1 ? "" : "m"} hoje · ${ateDoisDias} em até 2 dias`}
          tone={(alertas ?? []).length ? "alerta" : "neutral"}
        />
        <KpiCard
          icon={ClipboardList}
          title="Solicitações em aberto"
          value={abertas.length}
          unit={abertas.length === 1 ? "pedido" : "pedidos"}
          detail={`${abertas.length - naFila} em atendimento · ${naFila} na fila · ${bolsasPendentes} bolsa(s) pendente(s)`}
        />
        <KpiCard
          icon={Trash2}
          title="Baixas no mês"
          value={baixasMes.length}
          unit={baixasMes.length === 1 ? "bolsa" : "bolsas"}
          detail={`${automaticas} automáticas · ${baixasMes.length - automaticas} manuais · motivo: vencimento`}
          tone={baixasMes.length ? "emergencia" : "neutral"}
        />
      </div>

      <div className="mb-5 rounded-panel border border-border bg-surface p-5">
        <div className="mb-3 flex items-center justify-between gap-2">
          <h2 className="text-[0.95rem] font-semibold text-ink">Indicadores do período</h2>
          {indicadores && (
            <p className="text-xs text-ink-muted">
              De {formatarData(indicadores.periodoInicio)} a {formatarData(indicadores.periodoFim)}.
            </p>
          )}
        </div>
        {!indicadores || !indicadores.dadosSuficientes ? (
          <p className="flex items-center gap-2 rounded-control bg-info-soft px-3 py-2 text-sm text-info">
            <AlertTriangle className="size-4" />
            {indicadores?.mensagem ?? "Carregando indicadores…"}
          </p>
        ) : (
          <PeriodoPanel indicadores={indicadores} />
        )}
      </div>

      <div className="grid gap-5 lg:grid-cols-2">
        <div className="rounded-panel border border-border bg-surface p-5">
          <h2 className="mb-3 text-[0.95rem] font-semibold text-ink">
            Bolsas disponíveis por tipo sanguíneo e componente
          </h2>
          {!indicadores || !indicadores.dadosSuficientes ? (
            <p className="text-sm text-ink-muted">{indicadores?.mensagem ?? "Carregando…"}</p>
          ) : (
            <EstoqueHeatmap estoque={indicadores.estoqueDisponivel} componentes={componentesFiltro} />
          )}
        </div>
        <div className="rounded-panel border border-border bg-surface p-5">
          <h2 className="mb-3 text-[0.95rem] font-semibold text-ink">Demanda em aberto por hospital</h2>
          {!indicadores || !indicadores.dadosSuficientes ? (
            <p className="text-sm text-ink-muted">{indicadores?.mensagem ?? "Carregando…"}</p>
          ) : (
            <DemandaHospitais itens={indicadores.demandaEmAberto} />
          )}
        </div>
      </div>
    </div>
  );
}
