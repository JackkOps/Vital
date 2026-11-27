import { PageHeader } from "@/components/layout/page-header";
import { Stat, StatRow } from "@/components/ui/stat";
import { RotaCard } from "./rota-card";
import { RemessaCard } from "./remessa-card";
import { useRemessas, useRotas } from "@/hooks/use-api";

export function LogisticaPage() {
  const { data: remessas } = useRemessas();
  const { data: rotas } = useRotas();
  const emTransito = (remessas ?? []).filter((r) => r.status === "EM_TRANSITO");
  const sobRisco = emTransito.filter((r) => r.condicao === "SOB_RISCO").length;
  const confirmadas = (rotas ?? []).filter((r) => r.status === "CONFIRMADA").length;
  const aguardando = (rotas ?? []).filter((r) => r.status === "AGUARDANDO_CONFIRMACAO").length;

  return (
    <div>
      <PageHeader
        title="Logística e"
        accent="cadeia fria."
        description="Calcule a rota de menor tempo até os hospitais e acompanhe a temperatura das remessas em trânsito."
      />
      <StatRow className="mb-8 animate-rise">
        <Stat value={emTransito.length} label="Remessas em trânsito" />
        <Stat value={sobRisco} label="Sob risco térmico" tone={sobRisco ? "emergencia" : "ink"} />
        <Stat value={confirmadas} label="Rotas confirmadas" tone="ink" />
        <Stat value={aguardando} label="Aguardando confirmação" tone={aguardando ? "alerta" : "ink"} />
      </StatRow>
      <div className="flex flex-col gap-6">
        <RotaCard />
        <RemessaCard />
      </div>
    </div>
  );
}
