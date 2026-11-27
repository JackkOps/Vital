import * as React from "react";
import { PageHeader } from "@/components/layout/page-header";
import { Stat, StatRow } from "@/components/ui/stat";
import { BolsaForm } from "./bolsa-form";
import { BolsaTable } from "./bolsa-table";
import { useBolsas } from "@/hooks/use-api";
import type { BolsaResponse } from "@/api/types";

export function EstoquePage() {
  const { data: bolsas, isLoading } = useBolsas();
  const [emEdicao, setEmEdicao] = React.useState<BolsaResponse | null>(null);
  const formRef = React.useRef<HTMLDivElement>(null);

  const lista = bolsas ?? [];
  const disponiveis = lista.filter((b) => b.status === "DISPONIVEL");
  const alocadas = lista.filter((b) => b.status === "ALOCADA").length;
  const volumeLitros = disponiveis.reduce((s, b) => s + b.volume, 0) / 1000;
  const traco = isLoading ? "—" : undefined;

  function editar(bolsa: BolsaResponse) {
    setEmEdicao(bolsa);
    formRef.current?.scrollIntoView({ behavior: "smooth", block: "start" });
  }

  return (
    <div>
      <PageHeader
        title="Estoque de"
        accent="bolsas."
        description="Cadastre, edite e consulte as bolsas de hemocomponentes do hemocentro."
      />

      <StatRow className="mb-8 animate-rise">
        <Stat value={traco ?? lista.length} label="Bolsas registradas" tone="ink" />
        <Stat value={traco ?? disponiveis.length} label="Disponíveis para uso" />
        <Stat value={traco ?? alocadas} label="Alocadas a pedidos" tone="ink" />
        <Stat
          value={traco ?? volumeLitros.toLocaleString("pt-BR", { maximumFractionDigits: 1 })}
          unit="L"
          label="Volume disponível"
        />
      </StatRow>

      <div className="flex flex-col gap-6">
        <div ref={formRef} className="scroll-mt-28">
          <BolsaForm emEdicao={emEdicao} onCancelarEdicao={() => setEmEdicao(null)} />
        </div>
        {isLoading ? (
          <div className="h-64 animate-pulse rounded-panel border border-border bg-surface" />
        ) : (
          <BolsaTable bolsas={lista} onEditar={editar} emEdicaoId={emEdicao?.id ?? null} />
        )}
      </div>
    </div>
  );
}
