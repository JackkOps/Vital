import * as React from "react";
import { Button, ButtonArrow } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Card, CardHeader } from "@/components/ui/card";
import { Input, Select } from "@/components/ui/input";
import { FieldLegend, Label } from "@/components/ui/label";
import { FormError } from "@/components/ui/stat";
import { EmptyState } from "@/components/layout/empty-state";
import { RotaResumo } from "./rota-resumo";
import { STATUS_ROTA_INFO } from "@/lib/domain";
import { cn } from "@/lib/utils";
import { useCalcularRota, useConfirmarRota, useRedeDistribuicao, useRotas } from "@/hooks/use-api";
import { AlertTriangle, Check, CheckCircle2, Route } from "lucide-react";

export function RotaCard() {
  const { data: rede } = useRedeDistribuicao();
  const { data: rotas } = useRotas();
  const calcular = useCalcularRota();
  const confirmar = useConfirmarRota();

  const hemocentros = (rede?.locais ?? []).filter((l) => l.tipo === "HEMOCENTRO");
  const hospitais = (rede?.locais ?? []).filter((l) => l.tipo === "HOSPITAL");

  const [origemId, setOrigemId] = React.useState<string>("");
  const [janela, setJanela] = React.useState<string>("");
  const [destinos, setDestinos] = React.useState<Set<number>>(new Set());
  const [resultadoId, setResultadoId] = React.useState<number | null>(null);
  const [erroCalculo, setErroCalculo] = React.useState<string | null>(null);

  React.useEffect(() => {
    if (!origemId && hemocentros.length) setOrigemId(String(hemocentros[0].id));
  }, [hemocentros, origemId]);

  function alternarDestino(id: number) {
    setDestinos((prev) => {
      const novo = new Set(prev);
      if (novo.has(id)) novo.delete(id);
      else novo.add(id);
      return novo;
    });
  }

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setErroCalculo(null);
    if (destinos.size === 0) {
      setErroCalculo("Marque ao menos um hospital de destino.");
      return;
    }
    calcular.mutate(
      { origemId: Number(origemId), destinoIds: [...destinos], janelaMinutos: janela ? Number(janela) : undefined },
      { onSuccess: (rota) => setResultadoId(rota.id), onError: (e) => setErroCalculo(e.message) },
    );
  }

  const resultado = (rotas ?? []).find((r) => r.id === resultadoId);
  const recentes = [...(rotas ?? [])].reverse().slice(0, 6);

  return (
    <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_minmax(0,1fr)] xl:items-start">
      <Card>
        <CardHeader index="01" title="Calcular" accent="rota" />
        <form onSubmit={handleSubmit} className="p-5" noValidate>
          <p className="mb-5 text-xs text-ink-muted">
            <span className="text-ink-faint">[</span>Menor tempo pelo grafo da rede (Dijkstra). Sem janela informada, valem 120 minutos.<span className="text-ink-faint">]</span>
          </p>
          {erroCalculo && <FormError>{erroCalculo}</FormError>}

          <div className="grid gap-5 sm:grid-cols-[minmax(0,1fr)_11rem]">
            <div className="space-y-2">
              <Label htmlFor="rota-origem">Saída</Label>
              <Select id="rota-origem" value={origemId} onChange={(e) => setOrigemId(e.target.value)}>
                {hemocentros.map((l) => (
                  <option key={l.id} value={l.id}>{l.nome}</option>
                ))}
              </Select>
            </div>
            <div className="space-y-2">
              <Label htmlFor="rota-janela">Janela fria (min)</Label>
              <Input id="rota-janela" type="number" min={1} inputMode="numeric" placeholder="120" value={janela} onChange={(e) => setJanela(e.target.value)} />
            </div>
            <fieldset className="space-y-2 sm:col-span-2">
              <div className="flex items-baseline justify-between">
                <FieldLegend>Hospitais de destino</FieldLegend>
                {hospitais.length > 0 && (
                  <button
                    type="button"
                    className="text-xs text-brand hover:underline"
                    onClick={() => setDestinos(destinos.size === hospitais.length ? new Set() : new Set(hospitais.map((h) => h.id)))}
                  >
                    {destinos.size === hospitais.length ? "Limpar" : "Marcar todos"}
                  </button>
                )}
              </div>
              <div className="flex flex-wrap gap-1.5">
                {hospitais.map((l) => {
                  const marcado = destinos.has(l.id);
                  return (
                    <label
                      key={l.id}
                      className={cn(
                        "inline-flex cursor-pointer items-center gap-1.5 rounded-full border px-3 py-1.5 text-[13px] font-medium transition-colors has-[:focus-visible]:outline-2 has-[:focus-visible]:outline-brand",
                        marcado ? "border-brand bg-brand text-brand-foreground" : "border-border-strong text-ink-muted hover:border-ink hover:text-ink",
                      )}
                    >
                      <input type="checkbox" className="sr-only" checked={marcado} onChange={() => alternarDestino(l.id)} />
                      {marcado && <Check className="size-3.5" />}
                      {l.nome}
                    </label>
                  );
                })}
              </div>
            </fieldset>
          </div>

          <div className="mt-6 flex items-center justify-between gap-3 border-t border-border pt-5">
            <p className="meta text-ink-muted">{destinos.size} destino(s) marcados</p>
            <Button type="submit" disabled={calcular.isPending}>
              {calcular.isPending ? "Calculando…" : "Calcular rota"} <ButtonArrow />
            </Button>
          </div>

          {resultado && (
            <div
              role={resultado.cumpreJanela ? "status" : "alert"}
              className={cn(
                "mt-5 animate-rise rounded-panel p-5",
                resultado.cumpreJanela ? "bg-inverse text-inverse-ink" : "border border-emergencia/40 bg-emergencia-soft/60 text-ink",
              )}
            >
              <p className="flex items-center gap-2 text-[15px] font-medium">
                {resultado.cumpreJanela ? <CheckCircle2 className="size-4" /> : <AlertTriangle className="size-4 text-emergencia" />}
                {resultado.cumpreJanela ? "Rota confirmada" : "Risco de atraso"}
                <span className={cn("serif-accent text-[16px]", resultado.cumpreJanela ? "text-inverse-muted" : "text-emergencia")}>
                  rota {resultado.id}
                </span>
              </p>
              {resultado.alerta && <p className="mt-1 text-[13px] text-emergencia">{resultado.alerta}</p>}
              <RotaResumo rota={resultado} inverse={resultado.cumpreJanela} />
              {!resultado.cumpreJanela && (
                <Button size="sm" variant="secondary" className="mt-4" onClick={() => confirmar.mutate(resultado.id)}>
                  Confirmar mesmo assim
                </Button>
              )}
            </div>
          )}
        </form>
      </Card>

      <Card>
        <CardHeader index="02" title="Rotas" accent="calculadas" meta={(rotas ?? []).length} />
        {recentes.length === 0 ? (
          <EmptyState icon={Route} title="Nenhuma rota calculada" description="Escolha os hospitais e calcule a primeira rota." />
        ) : (
          recentes.map((r) => {
            const status = STATUS_ROTA_INFO[r.status];
            return (
              <article key={r.id} className="border-b border-border px-5 py-4 last:border-0">
                <div className="flex flex-wrap items-center gap-2">
                  <span className="font-mono text-[11px] text-brand">ROTA-{String(r.id).padStart(3, "0")}</span>
                  <Badge tone={status.tone}>{status.label}</Badge>
                  {r.status === "AGUARDANDO_CONFIRMACAO" && (
                    <Button size="sm" variant="secondary" className="ml-auto" onClick={() => confirmar.mutate(r.id)}>
                      Confirmar mesmo assim
                    </Button>
                  )}
                </div>
                <RotaResumo rota={r} />
                {r.alerta && <p className="mt-2 text-xs text-emergencia">{r.alerta}</p>}
              </article>
            );
          })
        )}
      </Card>
    </div>
  );
}
