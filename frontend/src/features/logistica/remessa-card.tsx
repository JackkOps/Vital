import * as React from "react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Card, CardHeader } from "@/components/ui/card";
import { Input, Select } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { EmptyState } from "@/components/layout/empty-state";
import { DESCRICAO_COMPONENTE, TIPOS_COMPONENTE, type RemessaResponse, type TipoComponente } from "@/api/types";
import { CONDICAO_REMESSA_INFO, STATUS_REMESSA_INFO, TONE_POR_COMPONENTE } from "@/lib/domain";
import { formatarDataHora } from "@/lib/format";
import { cn } from "@/lib/utils";
import {
  useDespacharRemessa,
  useEntregarRemessa,
  useRegistrarLeitura,
  useRemessas,
  useRotas,
  useVerificarSensores,
} from "@/hooks/use-api";
import { AlertTriangle, Plus, RadioTower, Thermometer, Truck, WifiOff } from "lucide-react";

const fmt = (n: number) => n.toLocaleString("pt-BR", { maximumFractionDigits: 1 });

function LeituraSimulada({ remessaId, sensorId, totalLeituras }: { remessaId: number; sensorId: string; totalLeituras: number }) {
  const [valor, setValor] = React.useState("");
  const registrar = useRegistrarLeitura();

  function enviar(e: React.FormEvent) {
    e.preventDefault();
    const temperatura = Number(valor.replace(",", "."));
    if (valor === "" || Number.isNaN(temperatura)) return;
    registrar.mutate(
      { id: remessaId, sensorId, sequencia: totalLeituras + 1, temperatura },
      { onSuccess: () => setValor("") },
    );
  }

  return (
    <form onSubmit={enviar} className="flex items-center rounded-full border border-border-strong bg-background/60 pl-3">
      <Thermometer className="size-3.5 text-ink-faint" />
      <input
        type="number"
        step="0.1"
        inputMode="decimal"
        placeholder="°C"
        aria-label={`Temperatura lida da remessa ${remessaId}`}
        className="h-8 w-16 bg-transparent px-2 text-[13px] tabular outline-none [appearance:textfield] [&::-webkit-inner-spin-button]:appearance-none"
        value={valor}
        onChange={(e) => setValor(e.target.value)}
      />
      <Button type="submit" size="sm" variant="ghost" className="rounded-l-none border-l border-border-strong" disabled={registrar.isPending || valor === ""}>
        Registrar
      </Button>
    </form>
  );
}

/** Termômetro horizontal: faixa segura destacada e marcador na última leitura. */
function Termometro({ remessa }: { remessa: RemessaResponse }) {
  const { temperaturaMinima: min, temperaturaMaxima: max, ultimaTemperatura: atual } = remessa;
  const folga = Math.max(2, (max - min) * 0.6);
  const inicio = min - folga;
  const fim = max + folga;
  const pos = (t: number) => Math.min(100, Math.max(0, ((t - inicio) / (fim - inicio)) * 100));
  const fora = atual !== null && (atual < min || atual > max);

  return (
    <div className="w-full">
      <div className="relative h-2 rounded-full bg-surface-muted">
        <div className="absolute inset-y-0 rounded-full bg-ok-soft ring-1 ring-ok/40" style={{ left: `${pos(min)}%`, right: `${100 - pos(max)}%` }} />
        {atual !== null && (
          <span
            className={cn("absolute top-1/2 size-3.5 -translate-x-1/2 -translate-y-1/2 rounded-full border-2 border-surface", fora ? "bg-emergencia" : "bg-brand")}
            style={{ left: `${pos(atual)}%` }}
          />
        )}
      </div>
      <div className="meta relative mt-1.5 h-4 text-ink-faint">
        <span className="absolute -translate-x-1/2" style={{ left: `${pos(min)}%` }}>{fmt(min)}°</span>
        <span className="absolute -translate-x-1/2" style={{ left: `${pos(max)}%` }}>{fmt(max)}°</span>
      </div>
    </div>
  );
}

function ItemRemessa({ remessa: r }: { remessa: RemessaResponse }) {
  const entregar = useEntregarRemessa();
  const status = STATUS_REMESSA_INFO[r.status];
  const condicao = CONDICAO_REMESSA_INFO[r.condicao];
  const emTransito = r.status === "EM_TRANSITO";
  const fora = r.ultimaTemperatura !== null && (r.ultimaTemperatura < r.temperaturaMinima || r.ultimaTemperatura > r.temperaturaMaxima);

  return (
    <article className={cn("grid gap-5 border-b border-border px-5 py-5 last:border-0 lg:grid-cols-[minmax(0,1.2fr)_minmax(0,1fr)_20rem] lg:items-center", !emTransito && "opacity-70")}>
      <div className="min-w-0">
        <div className="flex flex-wrap items-center gap-2">
          <span className="font-mono text-[11px] text-brand">REM-{String(r.id).padStart(3, "0")}</span>
          <Badge tone={TONE_POR_COMPONENTE[r.tipoComponente]}>{DESCRICAO_COMPONENTE[r.tipoComponente]}</Badge>
          {emTransito ? (
            <Badge tone={condicao.tone}><condicao.icon className="size-3" />{condicao.label}</Badge>
          ) : (
            <Badge tone={status.tone}><status.icon className="size-3" />{status.label}</Badge>
          )}
          {r.sensorIndisponivel && <Badge tone="alerta"><WifiOff className="size-3" />Sensor sem sinal</Badge>}
        </div>
        <p className="mt-2 text-[13px] text-ink-muted">
          <span className="font-mono text-ink">{r.sensorId}</span> · {r.totalLeituras} leitura(s)
          {r.rotaId ? ` · rota ${r.rotaId}` : " · sem rota"}
        </p>
        {r.alertas.length > 0 && (
          <ul className="mt-2 space-y-1">
            {r.alertas.map((a) => (
              <li key={a.id} className="flex items-start gap-1.5 text-xs text-emergencia">
                <AlertTriangle className="mt-px size-3 flex-none" />
                <span><span className="tabular">{formatarDataHora(a.criadoEm)}</span> · {a.mensagem}</span>
              </li>
            ))}
          </ul>
        )}
      </div>

      <div className="flex items-center gap-4">
        <div className="w-24 flex-none">
          <p className={cn("display-num text-[2.1rem]", r.ultimaTemperatura === null ? "text-ink-faint" : fora ? "text-emergencia" : "text-brand")}>
            {r.ultimaTemperatura === null ? "—" : fmt(r.ultimaTemperatura)}
            <span className="ml-0.5 text-sm tracking-normal text-ink-muted">°C</span>
          </p>
          <p className="meta mt-1 text-ink-muted">
            {r.ultimaLeituraEm ? formatarDataHora(r.ultimaLeituraEm) : "sem leituras"}
          </p>
        </div>
        <Termometro remessa={r} />
      </div>

      <div className="flex flex-wrap items-center gap-2 lg:justify-end">
        {emTransito ? (
          <>
            <LeituraSimulada remessaId={r.id} sensorId={r.sensorId} totalLeituras={r.totalLeituras} />
            <Button size="sm" variant="secondary" onClick={() => entregar.mutate(r.id)} disabled={entregar.isPending}>
              Marcar entregue
            </Button>
          </>
        ) : (
          <p className="meta text-ink-muted">Entregue {formatarDataHora(r.entregueEm)}</p>
        )}
      </div>
    </article>
  );
}

export function RemessaCard() {
  const { data: remessas } = useRemessas();
  const { data: rotas } = useRotas();
  const despachar = useDespacharRemessa();
  const verificarSensores = useVerificarSensores();

  const [componente, setComponente] = React.useState<TipoComponente | "">("");
  const [sensorId, setSensorId] = React.useState("");
  const [rotaId, setRotaId] = React.useState("");

  const rotasConfirmadas = (rotas ?? []).filter((r) => r.status === "CONFIRMADA");
  const lista = [...(remessas ?? [])].sort((a, b) =>
    a.status === b.status ? b.id - a.id : a.status === "EM_TRANSITO" ? -1 : 1,
  );
  const podeDespachar = Boolean(componente && sensorId.trim());

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!componente || !sensorId.trim()) return;
    despachar.mutate(
      { tipoComponente: componente, sensorId: sensorId.trim(), rotaId: rotaId ? Number(rotaId) : undefined },
      { onSuccess: () => { setComponente(""); setSensorId(""); setRotaId(""); } },
    );
  }

  return (
    <Card>
      <CardHeader index="03" title="Remessas &" accent="cadeia fria" meta={lista.filter((r) => r.status === "EM_TRANSITO").length + " em trânsito"}>
        <Button size="sm" variant="secondary" className="ml-auto" onClick={() => verificarSensores.mutate()} disabled={verificarSensores.isPending}>
          <RadioTower /> Verificar sensores
        </Button>
      </CardHeader>

      <form onSubmit={handleSubmit} className="flex flex-wrap items-end gap-3 border-b border-border bg-background/40 px-5 py-4">
        <div className="space-y-2">
          <Label htmlFor="remessa-componente">Componente</Label>
          <Select id="remessa-componente" className="w-40" value={componente} onChange={(e) => setComponente(e.target.value as TipoComponente)}>
            <option value="">Selecione</option>
            {TIPOS_COMPONENTE.map((c) => (
              <option key={c} value={c}>{DESCRICAO_COMPONENTE[c]}</option>
            ))}
          </Select>
        </div>
        <div className="space-y-2">
          <Label htmlFor="remessa-sensor">Sensor</Label>
          <Input id="remessa-sensor" className="w-36 font-mono text-[13px]" placeholder="SENSOR-01" value={sensorId} onChange={(e) => setSensorId(e.target.value)} />
        </div>
        <div className="min-w-48 flex-1 space-y-2 sm:max-w-xs">
          <Label htmlFor="remessa-rota">Rota confirmada</Label>
          <Select id="remessa-rota" value={rotaId} onChange={(e) => setRotaId(e.target.value)}>
            <option value="">Sem rota</option>
            {rotasConfirmadas.map((r) => (
              <option key={r.id} value={r.id}>
                Rota {r.id} · {r.entregas.map((e) => e.nome).join(", ")}
              </option>
            ))}
          </Select>
        </div>
        <Button type="submit" disabled={!podeDespachar || despachar.isPending}>
          <Plus /> Despachar remessa
        </Button>
      </form>

      {lista.length === 0 ? (
        <EmptyState icon={Truck} title="Nenhuma remessa despachada" description="Despache uma remessa para acompanhar as leituras de temperatura do sensor." />
      ) : (
        lista.map((r) => <ItemRemessa key={r.id} remessa={r} />)
      )}
    </Card>
  );
}
