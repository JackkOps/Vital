import * as React from "react";
import { PageHeader } from "@/components/layout/page-header";
import { Input, Select } from "@/components/ui/input";
import { Card, CardHeader } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
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

  const filtrando = Boolean(dataInicio || dataFim || hospital || tipoComponente);
  const carregandoOuSemDados = !indicadores || !indicadores.dadosSuficientes;
  const avisoSemDados = (
    <p className="flex items-center gap-2 rounded-panel bg-info-soft px-3.5 py-2.5 text-[13px] text-info">
      <AlertTriangle className="size-4 flex-none" />
      {indicadores?.mensagem ?? "Carregando indicadores…"}
    </p>
  );

  return (
    <div>
      <PageHeader
        title="Indicadores"
        accent="do hemocentro."
        description="Situação do estoque e da demanda hospitalar, com filtros por período, hospital e componente."
      />

      <div className="mb-6 grid gap-4 animate-rise sm:grid-cols-2 xl:grid-cols-4">
        <KpiCard
          inverse
          index="01"
          icon={CheckCircle2}
          title="Total disponível"
          value={totalDisponivel}
          unit={totalDisponivel === 1 ? "bolsa" : "bolsas"}
          detail={TIPOS_COMPONENTE.map((c) => `${(bolsas ?? []).filter((b) => b.status === "DISPONIVEL" && b.tipoComponente === c).length} ${DESCRICAO_COMPONENTE[c].toLowerCase()}`).join(" · ")}
        />
        <KpiCard
          index="02"
          icon={AlertTriangle}
          title="Próximas do vencimento"
          value={(alertas ?? []).length}
          unit={(alertas ?? []).length === 1 ? "bolsa" : "bolsas"}
          detail={`${vencemHoje} vence${vencemHoje === 1 ? "" : "m"} hoje · ${ateDoisDias} em até 2 dias`}
          tone={(alertas ?? []).length ? "alerta" : "neutral"}
        />
        <KpiCard
          index="03"
          icon={ClipboardList}
          title="Solicitações em aberto"
          value={abertas.length}
          unit={abertas.length === 1 ? "pedido" : "pedidos"}
          detail={`${abertas.length - naFila} em atendimento · ${naFila} na fila · ${bolsasPendentes} bolsa(s) pendente(s)`}
        />
        <KpiCard
          index="04"
          icon={Trash2}
          title="Baixas no mês"
          value={baixasMes.length}
          unit={baixasMes.length === 1 ? "bolsa" : "bolsas"}
          detail={`${automaticas} automáticas · ${baixasMes.length - automaticas} manuais · motivo: vencimento`}
          tone={baixasMes.length ? "emergencia" : "neutral"}
        />
      </div>

      <Card className="mb-6">
        <CardHeader index="05" title="Indicadores do" accent="período">
          {indicadores && (
            <p className="meta ml-auto text-ink-muted">
              {formatarData(indicadores.periodoInicio)} — {formatarData(indicadores.periodoFim)}
            </p>
          )}
        </CardHeader>
        <div className="flex flex-wrap items-end gap-3 border-b border-border bg-background/40 px-5 py-4">
          <div className="space-y-2">
            <Label htmlFor="ind-inicio">De</Label>
            <Input id="ind-inicio" type="date" className="w-40" value={dataInicio} max={dataFim || undefined} onChange={(e) => setDataInicio(e.target.value)} />
          </div>
          <div className="space-y-2">
            <Label htmlFor="ind-fim">Até</Label>
            <Input id="ind-fim" type="date" className="w-40" value={dataFim} min={dataInicio || undefined} onChange={(e) => setDataFim(e.target.value)} />
          </div>
          <div className="space-y-2">
            <Label htmlFor="ind-hospital">Hospital</Label>
            <Select id="ind-hospital" className="w-52" value={hospital} onChange={(e) => setHospital(e.target.value)}>
              <option value="">Todos</option>
              {hospitais.map((h) => (
                <option key={h} value={h}>{h}</option>
              ))}
            </Select>
          </div>
          <div className="space-y-2">
            <Label htmlFor="ind-componente">Componente</Label>
            <Select
              id="ind-componente"
              className="w-44"
              value={tipoComponente}
              onChange={(e) => setTipoComponente(e.target.value as TipoComponente | "")}
            >
              <option value="">Todos</option>
              {TIPOS_COMPONENTE.map((c) => (
                <option key={c} value={c}>{DESCRICAO_COMPONENTE[c]}</option>
              ))}
            </Select>
          </div>
          {filtrando && (
            <Button
              variant="link"
              size="sm"
              className="h-10"
              onClick={() => {
                setDataInicio("");
                setDataFim("");
                setHospital("");
                setTipoComponente("");
              }}
            >
              Limpar filtros
            </Button>
          )}
        </div>
        <div className="p-5">{carregandoOuSemDados ? avisoSemDados : <PeriodoPanel indicadores={indicadores} />}</div>
      </Card>

      <div className="grid gap-6 xl:grid-cols-[minmax(0,1.2fr)_minmax(0,1fr)] xl:items-start">
        <Card>
          <CardHeader index="06" title="Disponível por" accent="tipo e componente" />
          <div className="p-5">
            {carregandoOuSemDados ? avisoSemDados : <EstoqueHeatmap estoque={indicadores.estoqueDisponivel} componentes={componentesFiltro} />}
          </div>
        </Card>
        <Card>
          <CardHeader index="07" title="Demanda em" accent="aberto" />
          <div className="px-5 py-2">
            {carregandoOuSemDados ? <div className="py-3">{avisoSemDados}</div> : <DemandaHospitais itens={indicadores.demandaEmAberto} />}
          </div>
        </Card>
      </div>
    </div>
  );
}
