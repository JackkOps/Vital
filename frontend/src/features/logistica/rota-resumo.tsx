import type { RotaResponse } from "@/api/types";
import { cn } from "@/lib/utils";

export function minutosTexto(minutos: number) {
  if (minutos < 60) return `${minutos} min`;
  const horas = Math.floor(minutos / 60);
  const resto = minutos % 60;
  return resto ? `${horas} h ${resto} min` : `${horas} h`;
}

/** Resumo da rota: números grandes + linha de paradas com horário de chegada. */
export function RotaResumo({ rota, inverse = false }: { rota: RotaResponse; inverse?: boolean }) {
  const chegada = new Map(rota.entregas.map((e) => [e.localId, e]));
  const muted = inverse ? "text-inverse-muted" : "text-ink-muted";

  return (
    <div className="mt-3">
      <dl className="flex flex-wrap gap-x-7 gap-y-2">
        {[
          { valor: minutosTexto(rota.tempoTotalMinutos), rotulo: "tempo total" },
          { valor: `${rota.distanciaTotalKm.toLocaleString("pt-BR")} km`, rotulo: "distância" },
          { valor: minutosTexto(rota.janelaMinutos), rotulo: "janela fria" },
        ].map((item) => (
          <div key={item.rotulo} className="flex flex-col">
            <dt className={cn("serif-accent text-[13px]", muted)}>{item.rotulo}</dt>
            <dd className="display-num order-first text-[1.6rem]">{item.valor}</dd>
          </div>
        ))}
      </dl>

      <ol className="mt-4 flex flex-col gap-0 sm:flex-row sm:items-start" aria-label="Paradas da rota">
        {rota.caminho.map((local, i) => {
          const entrega = chegada.get(local.id);
          const fora = entrega && !entrega.dentroDaJanela;
          const ultimo = i === rota.caminho.length - 1;
          return (
            <li key={`${local.id}-${i}`} className="relative flex min-w-0 gap-3 pb-3 sm:flex-1 sm:flex-col sm:gap-2 sm:pb-0">
              {!ultimo && (
                <span
                  aria-hidden="true"
                  className={cn(
                    "absolute left-[5px] top-3 h-full w-px sm:left-3 sm:top-[5px] sm:h-px sm:w-full",
                    inverse ? "bg-inverse-line" : "bg-border-strong",
                  )}
                />
              )}
              <span
                className={cn(
                  "relative z-10 mt-1 size-[11px] flex-none rounded-full border-2 sm:mt-0",
                  i === 0
                    ? inverse ? "border-inverse-ink bg-inverse-ink" : "border-ink bg-ink"
                    : fora
                      ? "border-emergencia bg-emergencia"
                      : inverse ? "border-inverse-ink bg-inverse" : "border-brand bg-surface",
                )}
              />
              <div className="min-w-0 sm:pr-3">
                <p className="text-[13px] font-medium leading-tight">{local.nome}</p>
                <p className={cn("meta mt-0.5", fora ? "font-semibold text-emergencia" : muted)}>
                  {i === 0 ? "saída" : entrega ? `${minutosTexto(entrega.chegadaMinutos)}${fora ? " · fora da janela" : ""}` : "passagem"}
                </p>
              </div>
            </li>
          );
        })}
      </ol>
    </div>
  );
}
