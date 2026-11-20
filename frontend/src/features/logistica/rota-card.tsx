import * as React from "react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input, Select } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { EmptyState } from "@/components/layout/empty-state";
import { RotaResumo } from "./rota-resumo";
import { STATUS_ROTA_INFO } from "@/lib/domain";
import {
  useCalcularRota,
  useConfirmarRota,
  useRedeDistribuicao,
  useRotas,
} from "@/hooks/use-api";
import { AlertTriangle, CheckCircle2, Route } from "lucide-react";

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
      {
        origemId: Number(origemId),
        destinoIds: [...destinos],
        janelaMinutos: janela ? Number(janela) : undefined,
      },
      {
        onSuccess: (rota) => setResultadoId(rota.id),
        onError: (e) => setErroCalculo(e.message),
      },
    );
  }

  const resultado = (rotas ?? []).find((r) => r.id === resultadoId);

  return (
    <div className="grid gap-5 lg:grid-cols-2">
      <form onSubmit={handleSubmit} className="rounded-panel border border-border bg-surface p-5" noValidate>
        <div className="mb-4">
          <h2 className="text-[0.95rem] font-semibold text-ink">Calcular rota</h2>
          <p className="text-xs text-ink-muted">
            Menor tempo pelo grafo da rede (Dijkstra). Sem janela informada, valem 120 minutos.
          </p>
        </div>

        {erroCalculo && (
          <p role="alert" className="mb-4 rounded-control bg-emergencia-soft px-3 py-2 text-sm text-emergencia">
            {erroCalculo}
          </p>
        )}

        <div className="grid gap-4 sm:grid-cols-2">
          <div className="space-y-1.5">
            <Label htmlFor="rota-origem">Saída</Label>
            <Select id="rota-origem" value={origemId} onChange={(e) => setOrigemId(e.target.value)}>
              {hemocentros.map((l) => (
                <option key={l.id} value={l.id}>{l.nome}</option>
              ))}
            </Select>
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="rota-janela">Janela da cadeia fria (min)</Label>
            <Input
              id="rota-janela"
              type="number"
              min={1}
              placeholder="120"
              value={janela}
              onChange={(e) => setJanela(e.target.value)}
            />
          </div>
          <fieldset className="space-y-1.5 sm:col-span-2">
            <legend className="text-sm font-medium text-ink">Hospitais de destino</legend>
            <div className="flex flex-wrap gap-2">
              {hospitais.map((l) => {
                const marcado = destinos.has(l.id);
                return (
                  <label
                    key={l.id}
                    className={`flex items-center gap-1.5 rounded-control border px-2.5 py-1.5 text-sm ${
                      marcado ? "border-brand bg-brand-soft text-brand" : "border-border-strong text-ink-muted"
                    }`}
                  >
                    <input
                      type="checkbox"
                      className="accent-current"
                      checked={marcado}
                      onChange={() => alternarDestino(l.id)}
                    />
                    {l.nome}
                  </label>
                );
              })}
            </div>
          </fieldset>
        </div>

        <div className="mt-4 flex justify-end">
          <Button type="submit" disabled={calcular.isPending}>
            <Route /> Calcular rota
          </Button>
        </div>

        {resultado && (
          <div
            role={resultado.cumpreJanela ? "status" : "alert"}
            className={`mt-4 rounded-control border p-3.5 ${
              resultado.cumpreJanela ? "border-ok/30 bg-ok-soft/40" : "border-emergencia/30 bg-emergencia-soft/40"
            }`}
          >
            <p className="flex items-center gap-2 text-sm font-semibold text-ink">
              {resultado.cumpreJanela ? (
                <CheckCircle2 className="size-4 text-ok" />
              ) : (
                <AlertTriangle className="size-4 text-emergencia" />
              )}
              {resultado.cumpreJanela ? "Rota confirmada" : "Risco de atraso"}
            </p>
            {resultado.alerta && <p className="mt-1 text-sm text-ink-muted">{resultado.alerta}</p>}
            <RotaResumo rota={resultado} />
            {!resultado.cumpreJanela && (
              <Button
                size="sm"
                variant="secondary"
                className="mt-3"
                onClick={() => confirmar.mutate(resultado.id)}
              >
                Confirmar mesmo assim
              </Button>
            )}
          </div>
        )}
      </form>

      <div className="rounded-panel border border-border bg-surface">
        <div className="flex items-center gap-3 border-b border-border px-5 py-4">
          <h2 className="text-[0.95rem] font-semibold text-ink">Rotas calculadas</h2>
          <span className="ml-auto text-xs tabular text-ink-muted">{(rotas ?? []).length}</span>
        </div>
        <div className="flex flex-col gap-3 p-4">
          {(rotas ?? []).length === 0 ? (
            <EmptyState icon={Route} title="Nenhuma rota calculada" description="Escolha os hospitais e calcule a primeira rota." />
          ) : (
            [...(rotas ?? [])].reverse().slice(0, 8).map((r) => {
              const status = STATUS_ROTA_INFO[r.status];
              return (
                <div key={r.id} className="rounded-control border border-border p-3">
                  <div className="flex items-center gap-2">
                    <strong className="text-sm text-ink">Rota {r.id}</strong>
                    <Badge tone={status.tone}>{status.label}</Badge>
                    {r.status === "AGUARDANDO_CONFIRMACAO" && (
                      <Button size="sm" variant="secondary" className="ml-auto" onClick={() => confirmar.mutate(r.id)}>
                        Confirmar mesmo assim
                      </Button>
                    )}
                  </div>
                  <RotaResumo rota={r} />
                  {r.alerta && <p className="mt-1 text-xs text-ink-muted">{r.alerta}</p>}
                </div>
              );
            })
          )}
        </div>
      </div>
    </div>
  );
}
