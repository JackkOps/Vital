import * as React from "react";
import { Badge } from "@/components/ui/badge";
import { Input, Select } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Card, CardHeader } from "@/components/ui/card";
import { tabela } from "@/components/ui/stat";
import { Paginacao, usePaginacao } from "@/components/ui/pagination";
import { ConfirmDialog } from "@/components/ui/confirm-dialog";
import { EmptyState } from "@/components/layout/empty-state";
import {
  DESCRICAO_COMPONENTE,
  DESCRICAO_TIPO_SANGUINEO,
  TIPOS_COMPONENTE,
  TIPOS_SANGUINEOS,
  type BolsaResponse,
  type StatusBolsa,
} from "@/api/types";
import { STATUS_BOLSA_INFO, TONE_POR_COMPONENTE } from "@/lib/domain";
import { formatarData } from "@/lib/format";
import { useExcluirBolsa } from "@/hooks/use-api";
import { cn } from "@/lib/utils";
import { Boxes, Pencil, Search, Trash2 } from "lucide-react";

const STATUS_OPCOES: StatusBolsa[] = ["DISPONIVEL", "ALOCADA", "INDISPONIVEL", "DESCARTADA"];

function diasAte(iso: string) {
  const hoje = new Date();
  hoje.setHours(0, 0, 0, 0);
  const alvo = new Date(`${iso.split("T")[0]}T00:00:00`);
  return Math.round((alvo.getTime() - hoje.getTime()) / 86_400_000);
}

function Validade({ iso, ativa }: { iso: string; ativa: boolean }) {
  const dias = diasAte(iso);
  const tom = !ativa ? "" : dias < 0 ? "text-emergencia" : dias <= 3 ? "text-alerta" : "";
  return (
    <span className="inline-flex flex-col leading-tight">
      <span className={cn("tabular text-ink", tom)}>{formatarData(iso)}</span>
      {ativa && (
        <span className={cn("meta text-ink-faint", tom)}>
          {dias < 0 ? `venceu há ${-dias} d` : dias === 0 ? "vence hoje" : `em ${dias} d`}
        </span>
      )}
    </span>
  );
}

export function BolsaTable({
  bolsas,
  onEditar,
  emEdicaoId,
}: {
  bolsas: BolsaResponse[];
  onEditar: (bolsa: BolsaResponse) => void;
  emEdicaoId: number | null;
}) {
  const [busca, setBusca] = React.useState("");
  const [filtroTipo, setFiltroTipo] = React.useState("");
  const [filtroComponente, setFiltroComponente] = React.useState("");
  const [filtroStatus, setFiltroStatus] = React.useState("");
  const [paraExcluir, setParaExcluir] = React.useState<BolsaResponse | null>(null);
  const excluir = useExcluirBolsa();

  const termo = busca.trim().toLowerCase();
  const filtradas = bolsas.filter(
    (b) =>
      (!termo || b.identificador.toLowerCase().includes(termo)) &&
      (!filtroTipo || b.tipoSanguineo === filtroTipo) &&
      (!filtroComponente || b.tipoComponente === filtroComponente) &&
      (!filtroStatus || b.status === filtroStatus),
  );
  const filtrando = Boolean(termo || filtroTipo || filtroComponente || filtroStatus);
  const [pagina, setPagina] = React.useState(1);
  const paginacao = usePaginacao(filtradas, pagina, 12);
  React.useEffect(() => setPagina(1), [termo, filtroTipo, filtroComponente, filtroStatus]);

  function limpar() {
    setBusca("");
    setFiltroTipo("");
    setFiltroComponente("");
    setFiltroStatus("");
  }

  return (
    <Card>
      <CardHeader index="02" title="Bolsas" accent="cadastradas" meta={`${filtradas.length} de ${bolsas.length}`} />

      <div className="flex flex-wrap items-center gap-2 border-b border-border bg-background/40 px-5 py-3">
        <div className="relative min-w-52 flex-1 sm:max-w-xs">
          <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-ink-faint" />
          <Input
            type="search"
            placeholder="Buscar identificador"
            aria-label="Buscar bolsa por identificador"
            className="h-9 rounded-full pl-9 font-mono text-[13px]"
            value={busca}
            onChange={(e) => setBusca(e.target.value)}
          />
        </div>
        <Select aria-label="Filtrar por tipo sanguíneo" value={filtroTipo} onChange={(e) => setFiltroTipo(e.target.value)} className="h-9 w-auto rounded-full text-[13px]">
          <option value="">Todos os tipos</option>
          {TIPOS_SANGUINEOS.map((t) => (
            <option key={t} value={t}>{DESCRICAO_TIPO_SANGUINEO[t]}</option>
          ))}
        </Select>
        <Select aria-label="Filtrar por componente" value={filtroComponente} onChange={(e) => setFiltroComponente(e.target.value)} className="h-9 w-auto rounded-full text-[13px]">
          <option value="">Todos os componentes</option>
          {TIPOS_COMPONENTE.map((c) => (
            <option key={c} value={c}>{DESCRICAO_COMPONENTE[c]}</option>
          ))}
        </Select>
        <Select aria-label="Filtrar por status" value={filtroStatus} onChange={(e) => setFiltroStatus(e.target.value)} className="h-9 w-auto rounded-full text-[13px]">
          <option value="">Todos os status</option>
          {STATUS_OPCOES.map((s) => (
            <option key={s} value={s}>{STATUS_BOLSA_INFO[s].label}</option>
          ))}
        </Select>
        {filtrando && (
          <Button variant="link" size="sm" onClick={limpar} className="ml-1 h-9">
            Limpar filtros
          </Button>
        )}
      </div>

      {filtradas.length === 0 ? (
        <EmptyState
          icon={Boxes}
          title={filtrando ? "Nada com esses filtros" : "Nenhuma bolsa ainda"}
          description={
            filtrando
              ? "Ajuste a busca ou os filtros para ver outras bolsas."
              : "Cadastre a primeira bolsa no formulário acima para começar o estoque."
          }
        />
      ) : (
        <>
        <div className="overflow-x-auto">
          <table className={tabela.table}>
            <thead>
              <tr className={tabela.headRow}>
                <th className={tabela.th}>Identificador</th>
                <th className={tabela.th}>Tipo</th>
                <th className={tabela.th}>Componente</th>
                <th className={tabela.th}>Validade</th>
                <th className={cn(tabela.th, "text-right")}>Volume</th>
                <th className={tabela.th}>Status</th>
                <th className={cn(tabela.th, "text-right")}>
                  <span className="sr-only">Ações</span>
                </th>
              </tr>
            </thead>
            <tbody>
              {paginacao.visiveis.map((bolsa) => {
                const statusInfo = STATUS_BOLSA_INFO[bolsa.status];
                const ativa = bolsa.status === "DISPONIVEL";
                return (
                  <tr
                    key={bolsa.id}
                    className={cn(tabela.row, "group", emEdicaoId === bolsa.id && "bg-brand-soft/60 hover:bg-brand-soft/60")}
                  >
                    <td className={cn(tabela.td, "font-mono text-[13px] font-medium text-ink")}>
                      {bolsa.identificador}
                    </td>
                    <td className={tabela.td}>
                      <span className="inline-grid min-w-9 place-items-center rounded-full border border-ink/70 px-1.5 font-mono text-[12px] font-semibold leading-6 text-ink">
                        {DESCRICAO_TIPO_SANGUINEO[bolsa.tipoSanguineo]}
                      </span>
                    </td>
                    <td className={tabela.td}>
                      <Badge tone={TONE_POR_COMPONENTE[bolsa.tipoComponente]}>
                        {DESCRICAO_COMPONENTE[bolsa.tipoComponente]}
                      </Badge>
                    </td>
                    <td className={tabela.td}>
                      <Validade iso={bolsa.dataValidade} ativa={ativa} />
                    </td>
                    <td className={cn(tabela.td, "text-right tabular text-ink-muted")}>{bolsa.volume} mL</td>
                    <td className={tabela.td}>
                      <Badge tone={statusInfo.tone}>
                        <statusInfo.icon className="size-3" />
                        {statusInfo.label}
                      </Badge>
                    </td>
                    <td className={cn(tabela.td, "text-right")}>
                      <div className="inline-flex gap-1 opacity-70 transition-opacity group-hover:opacity-100 group-focus-within:opacity-100">
                        <Button variant="ghost" size="icon" onClick={() => onEditar(bolsa)} aria-label={`Editar ${bolsa.identificador}`}>
                          <Pencil />
                        </Button>
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => setParaExcluir(bolsa)}
                          aria-label={`Excluir ${bolsa.identificador}`}
                          className="hover:bg-emergencia-soft hover:text-emergencia"
                        >
                          <Trash2 />
                        </Button>
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
        <Paginacao {...paginacao} total={filtradas.length} onChange={setPagina} />
        </>
      )}

      <ConfirmDialog
        open={Boolean(paraExcluir)}
        onOpenChange={(open) => !open && setParaExcluir(null)}
        title={`Excluir ${paraExcluir?.identificador ?? ""}?`}
        description="Essa ação remove o registro da bolsa permanentemente do estoque."
        confirmLabel="Excluir bolsa"
        onConfirm={() => paraExcluir && excluir.mutate(paraExcluir.id)}
      />
    </Card>
  );
}
