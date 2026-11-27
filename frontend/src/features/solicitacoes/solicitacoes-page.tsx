import { PageHeader } from "@/components/layout/page-header";
import { Stat, StatRow } from "@/components/ui/stat";
import { SolicitacaoForm } from "./solicitacao-form";
import { FilaCard, EmAtendimentoCard } from "./fila-atendimento";
import { SolicitacoesTable } from "./solicitacoes-table";
import { useSolicitacoes } from "@/hooks/use-api";

export function SolicitacoesPage() {
  const { data, isLoading } = useSolicitacoes();
  const solicitacoes = data ?? [];
  const pendentes = solicitacoes.filter((s) => s.status === "PENDENTE");
  const emAtendimento = solicitacoes.filter((s) => s.status === "EM_ATENDIMENTO");
  const emergencias = pendentes.filter((s) => s.nivelUrgencia === "EMERGENCIA").length;
  const bolsasFaltando = [...pendentes, ...emAtendimento].reduce(
    (soma, s) => soma + Math.max(0, s.quantidade - s.bolsasAlocadas),
    0,
  );
  const v = (n: number) => (isLoading ? "—" : n);

  return (
    <div>
      <PageHeader
        title="Solicitações"
        accent="hospitalares."
        description="Registre pedidos, atenda a fila por urgência e aloque bolsas compatíveis."
      />

      <StatRow className="mb-8 animate-rise">
        <Stat value={v(pendentes.length)} label="Aguardando na fila" tone="ink" />
        <Stat value={v(emAtendimento.length)} label="Em atendimento" />
        <Stat value={v(emergencias)} label="Emergências na fila" tone={emergencias ? "emergencia" : "ink"} />
        <Stat value={v(bolsasFaltando)} label="Bolsas ainda por alocar" />
      </StatRow>

      <div className="flex flex-col gap-6">
        <div className="grid gap-6 lg:grid-cols-2 2xl:grid-cols-[340px_minmax(0,1fr)_minmax(0,1.1fr)] 2xl:items-start">
          <SolicitacaoForm />
          <FilaCard />
          <div className="lg:col-span-2 2xl:col-span-1">
            <EmAtendimentoCard />
          </div>
        </div>
        <SolicitacoesTable solicitacoes={solicitacoes} />
      </div>
    </div>
  );
}
