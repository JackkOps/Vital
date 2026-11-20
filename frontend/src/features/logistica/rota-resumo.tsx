import type { RotaResponse } from "@/api/types";

function minutosTexto(minutos: number) {
  if (minutos < 60) return `${minutos} min`;
  const horas = Math.floor(minutos / 60);
  const resto = minutos % 60;
  return resto ? `${horas} h ${resto} min` : `${horas} h`;
}

export function RotaResumo({ rota }: { rota: RotaResponse }) {
  const caminho = rota.caminho.map((l) => l.nome).join(" → ");
  return (
    <div className="mt-2 space-y-1 text-sm">
      <p className="font-semibold text-ink">
        {minutosTexto(rota.tempoTotalMinutos)} · {rota.distanciaTotalKm.toLocaleString("pt-BR")} km · janela{" "}
        {minutosTexto(rota.janelaMinutos)}
      </p>
      <p className="text-ink-muted">{caminho}</p>
      <p className="text-xs text-ink-faint">
        Entregas:{" "}
        {rota.entregas
          .map((e) => `${e.nome} em ${minutosTexto(e.chegadaMinutos)}${e.dentroDaJanela ? "" : " (fora da janela)"}`)
          .join(" · ")}
      </p>
    </div>
  );
}
