import * as React from "react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input, Select } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { EmptyState } from "@/components/layout/empty-state";
import { DESCRICAO_COMPONENTE, TIPOS_COMPONENTE, type TipoComponente } from "@/api/types";
import { CONDICAO_REMESSA_INFO, STATUS_REMESSA_INFO, TONE_POR_COMPONENTE } from "@/lib/domain";
import { formatarDataHora } from "@/lib/format";
import {
  useDespacharRemessa,
  useEntregarRemessa,
  useRegistrarLeitura,
  useRemessas,
  useRotas,
  useVerificarSensores,
} from "@/hooks/use-api";
import { PlusCircle, RadioTower, Thermometer, Truck } from "lucide-react";

function LeituraSimulada({ remessaId, sensorId, totalLeituras }: { remessaId: number; sensorId: string; totalLeituras: number }) {
  const [valor, setValor] = React.useState("");
  const registrar = useRegistrarLeitura();

  function enviar() {
    const temperatura = Number(valor.replace(",", "."));
    if (valor === "" || Number.isNaN(temperatura)) return;
    registrar.mutate(
      { id: remessaId, sensorId, sequencia: totalLeituras + 1, temperatura },
      { onSuccess: () => setValor("") },
    );
  }

  return (
    <div className="flex items-center gap-1.5">
      <Input
        type="number"
        step="0.1"
        inputMode="decimal"
        placeholder="°C"
        aria-label={`Temperatura lida da remessa ${remessaId}`}
        className="h-8 w-20"
        value={valor}
        onChange={(e) => setValor(e.target.value)}
      />
      <Button size="sm" variant="secondary" onClick={enviar} disabled={registrar.isPending}>
        <Thermometer /> Registrar leitura
      </Button>
    </div>
  );
}

export function RemessaCard() {
  const { data: remessas } = useRemessas();
  const { data: rotas } = useRotas();
  const despachar = useDespacharRemessa();
  const verificarSensores = useVerificarSensores();
  const entregar = useEntregarRemessa();

  const [componente, setComponente] = React.useState<TipoComponente | "">("");
  const [sensorId, setSensorId] = React.useState("");
  const [rotaId, setRotaId] = React.useState("");

  const rotasConfirmadas = (rotas ?? []).filter((r) => r.status === "CONFIRMADA");

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!componente || !sensorId.trim()) return;
    despachar.mutate(
      { tipoComponente: componente, sensorId: sensorId.trim(), rotaId: rotaId ? Number(rotaId) : undefined },
      { onSuccess: () => { setComponente(""); setSensorId(""); setRotaId(""); } },
    );
  }

  return (
    <div className="rounded-panel border border-border bg-surface">
      <div className="flex items-center gap-3 border-b border-border px-5 py-4">
        <h2 className="text-[0.95rem] font-semibold text-ink">Remessas em trânsito</h2>
        <Button
          size="sm"
          variant="secondary"
          className="ml-auto"
          onClick={() => verificarSensores.mutate()}
          disabled={verificarSensores.isPending}
        >
          <RadioTower /> Verificar sensores
        </Button>
      </div>

      <form onSubmit={handleSubmit} className="flex flex-wrap items-end gap-3 border-b border-border px-5 py-4">
        <div className="space-y-1.5">
          <Label htmlFor="remessa-componente">Componente</Label>
          <Select
            id="remessa-componente"
            className="h-9 w-40"
            value={componente}
            onChange={(e) => setComponente(e.target.value as TipoComponente)}
          >
            <option value="">Selecione</option>
            {TIPOS_COMPONENTE.map((c) => (
              <option key={c} value={c}>{DESCRICAO_COMPONENTE[c]}</option>
            ))}
          </Select>
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="remessa-sensor">Sensor</Label>
          <Input
            id="remessa-sensor"
            className="h-9 w-36"
            placeholder="SENSOR-01"
            value={sensorId}
            onChange={(e) => setSensorId(e.target.value)}
          />
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="remessa-rota">Rota</Label>
          <Select id="remessa-rota" className="h-9 w-48" value={rotaId} onChange={(e) => setRotaId(e.target.value)}>
            <option value="">Sem rota</option>
            {rotasConfirmadas.map((r) => (
              <option key={r.id} value={r.id}>
                Rota {r.id} · {r.entregas.map((e) => e.nome).join(", ")}
              </option>
            ))}
          </Select>
        </div>
        <Button type="submit" disabled={despachar.isPending}>
          <PlusCircle /> Despachar remessa
        </Button>
      </form>

      <div className="flex flex-col gap-3 p-4">
        {(remessas ?? []).length === 0 ? (
          <EmptyState icon={Truck} title="Nenhuma remessa despachada" description="Despache uma remessa para acompanhar as leituras de temperatura do sensor." />
        ) : (
          [...(remessas ?? [])].reverse().map((r) => {
            const status = STATUS_REMESSA_INFO[r.status];
            const condicao = CONDICAO_REMESSA_INFO[r.condicao];
            const emTransito = r.status === "EM_TRANSITO";
            return (
              <div key={r.id} className="rounded-control border border-border p-3.5">
                <div className="flex flex-wrap items-center gap-2">
                  <strong className="text-sm text-ink">Remessa {r.id}</strong>
                  <Badge tone={TONE_POR_COMPONENTE[r.tipoComponente]}>{DESCRICAO_COMPONENTE[r.tipoComponente]}</Badge>
                  <Badge tone={condicao.tone}><condicao.icon className="size-3" />{condicao.label}</Badge>
                  {r.sensorIndisponivel && <Badge tone="alerta">Sensor sem sinal</Badge>}
                  {!emTransito && <Badge tone={status.tone}><status.icon className="size-3" />{status.label}</Badge>}
                  {emTransito && (
                    <div className="ml-auto flex items-center gap-2">
                      <LeituraSimulada remessaId={r.id} sensorId={r.sensorId} totalLeituras={r.totalLeituras} />
                      <Button size="sm" variant="secondary" onClick={() => entregar.mutate(r.id)}>
                        Marcar entregue
                      </Button>
                    </div>
                  )}
                </div>
                <p className="mt-1.5 text-xs text-ink-muted">
                  Sensor {r.sensorId} · faixa segura {r.temperaturaMinima.toLocaleString("pt-BR")} a{" "}
                  {r.temperaturaMaxima.toLocaleString("pt-BR")} °C · última leitura:{" "}
                  {r.ultimaTemperatura === null
                    ? "sem leituras"
                    : `${r.ultimaTemperatura.toLocaleString("pt-BR")} °C às ${formatarDataHora(r.ultimaLeituraEm)}`}{" "}
                  · {r.totalLeituras} leitura(s){r.rotaId ? ` · rota ${r.rotaId}` : ""}
                </p>
                {r.alertas.length > 0 && (
                  <ul className="mt-2 flex flex-col gap-1">
                    {r.alertas.map((a) => (
                      <li key={a.id} className="text-xs text-emergencia">
                        {formatarDataHora(a.criadoEm)} · {a.mensagem}
                      </li>
                    ))}
                  </ul>
                )}
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}
