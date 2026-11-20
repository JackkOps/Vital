import * as React from "react";
import { PageHeader } from "@/components/layout/page-header";
import { BolsaForm } from "./bolsa-form";
import { BolsaTable } from "./bolsa-table";
import { useBolsas } from "@/hooks/use-api";
import type { BolsaResponse } from "@/api/types";

export function EstoquePage() {
  const { data: bolsas, isLoading } = useBolsas();
  const [emEdicao, setEmEdicao] = React.useState<BolsaResponse | null>(null);

  return (
    <div>
      <PageHeader
        title="Estoque de bolsas"
        description="Cadastre, edite e consulte as bolsas de hemocomponentes do hemocentro."
      />
      <div className="flex flex-col gap-5">
        <BolsaForm emEdicao={emEdicao} onCancelarEdicao={() => setEmEdicao(null)} />
        {isLoading ? (
          <p className="text-sm text-ink-muted">Carregando estoque…</p>
        ) : (
          <BolsaTable bolsas={bolsas ?? []} onEditar={setEmEdicao} />
        )}
      </div>
    </div>
  );
}
