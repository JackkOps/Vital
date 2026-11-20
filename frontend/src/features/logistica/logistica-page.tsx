import { PageHeader } from "@/components/layout/page-header";
import { RotaCard } from "./rota-card";
import { RemessaCard } from "./remessa-card";

export function LogisticaPage() {
  return (
    <div>
      <PageHeader
        title="Logística"
        description="Calcule a rota de menor tempo até os hospitais e acompanhe a temperatura das remessas em trânsito."
      />
      <div className="flex flex-col gap-5">
        <RotaCard />
        <RemessaCard />
      </div>
    </div>
  );
}
