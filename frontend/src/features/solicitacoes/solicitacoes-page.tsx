import { PageHeader } from "@/components/layout/page-header";
import { SolicitacaoForm } from "./solicitacao-form";
import { FilaCard, EmAtendimentoCard } from "./fila-atendimento";
import { SolicitacoesTable } from "./solicitacoes-table";
import { useSolicitacoes } from "@/hooks/use-api";

export function SolicitacoesPage() {
  const { data: solicitacoes } = useSolicitacoes();

  return (
    <div>
      <PageHeader
        title="Solicitações hospitalares"
        description="Registre pedidos, atenda a fila por urgência e aloque bolsas compatíveis."
      />
      <div className="flex flex-col gap-5">
        <div className="grid gap-5 xl:grid-cols-[320px_minmax(0,1fr)_minmax(0,1.15fr)] xl:items-start">
          <SolicitacaoForm />
          <FilaCard />
          <EmAtendimentoCard />
        </div>
        <SolicitacoesTable solicitacoes={solicitacoes ?? []} />
      </div>
    </div>
  );
}
