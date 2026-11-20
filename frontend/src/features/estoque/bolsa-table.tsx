import * as React from "react";
import { Badge } from "@/components/ui/badge";
import { Select } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
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
import { Boxes, Pencil, Trash2 } from "lucide-react";

const STATUS_OPCOES: StatusBolsa[] = ["DISPONIVEL", "ALOCADA", "INDISPONIVEL", "DESCARTADA"];

export function BolsaTable({
  bolsas,
  onEditar,
}: {
  bolsas: BolsaResponse[];
  onEditar: (bolsa: BolsaResponse) => void;
}) {
  const [filtroTipo, setFiltroTipo] = React.useState("");
  const [filtroComponente, setFiltroComponente] = React.useState("");
  const [filtroStatus, setFiltroStatus] = React.useState("");
  const [paraExcluir, setParaExcluir] = React.useState<BolsaResponse | null>(null);
  const excluir = useExcluirBolsa();

  const filtradas = bolsas.filter(
    (b) =>
      (!filtroTipo || b.tipoSanguineo === filtroTipo) &&
      (!filtroComponente || b.tipoComponente === filtroComponente) &&
      (!filtroStatus || b.status === filtroStatus),
  );

  return (
    <div className="rounded-panel border border-border bg-surface">
      <div className="flex flex-wrap items-center gap-3 border-b border-border px-5 py-4">
        <h2 className="text-[0.95rem] font-semibold text-ink">Bolsas cadastradas</h2>
        <span className="text-xs tabular text-ink-muted">{filtradas.length} de {bolsas.length}</span>
        <div className="ml-auto flex flex-wrap gap-2">
          <Select value={filtroTipo} onChange={(e) => setFiltroTipo(e.target.value)} className="h-8 w-auto text-xs">
            <option value="">Todos os tipos</option>
            {TIPOS_SANGUINEOS.map((t) => (
              <option key={t} value={t}>{DESCRICAO_TIPO_SANGUINEO[t]}</option>
            ))}
          </Select>
          <Select value={filtroComponente} onChange={(e) => setFiltroComponente(e.target.value)} className="h-8 w-auto text-xs">
            <option value="">Todos os componentes</option>
            {TIPOS_COMPONENTE.map((c) => (
              <option key={c} value={c}>{DESCRICAO_COMPONENTE[c]}</option>
            ))}
          </Select>
          <Select value={filtroStatus} onChange={(e) => setFiltroStatus(e.target.value)} className="h-8 w-auto text-xs">
            <option value="">Todos os status</option>
            {STATUS_OPCOES.map((s) => (
              <option key={s} value={s}>{STATUS_BOLSA_INFO[s].label}</option>
            ))}
          </Select>
        </div>
      </div>

      {filtradas.length === 0 ? (
        <EmptyState
          icon={Boxes}
          title="Nenhuma bolsa encontrada"
          description="Ajuste os filtros ou cadastre uma nova bolsa no formulário acima."
        />
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full whitespace-nowrap text-left text-sm">
            <thead>
              <tr className="border-b border-border text-xs text-ink-muted">
                <th className="px-5 py-2 font-medium">Identificador</th>
                <th className="px-5 py-2 font-medium">Tipo</th>
                <th className="px-5 py-2 font-medium">Componente</th>
                <th className="px-5 py-2 font-medium">Validade</th>
                <th className="px-5 py-2 font-medium">Volume</th>
                <th className="px-5 py-2 font-medium">Status</th>
                <th className="px-5 py-2 font-medium text-right">Ações</th>
              </tr>
            </thead>
            <tbody>
              {filtradas.map((bolsa) => {
                const statusInfo = STATUS_BOLSA_INFO[bolsa.status];
                return (
                  <tr key={bolsa.id} className="border-b border-border last:border-0 hover:bg-surface-muted/60">
                    <td className="px-5 py-2 font-mono text-[13px] font-semibold text-ink">
                      {bolsa.identificador}
                    </td>
                    <td className="px-5 py-2 font-mono text-[13px] font-semibold text-ink">
                      {DESCRICAO_TIPO_SANGUINEO[bolsa.tipoSanguineo]}
                    </td>
                    <td className="px-5 py-2">
                      <Badge tone={TONE_POR_COMPONENTE[bolsa.tipoComponente]}>
                        {DESCRICAO_COMPONENTE[bolsa.tipoComponente]}
                      </Badge>
                    </td>
                    <td className="px-5 py-2 tabular text-ink-muted">{formatarData(bolsa.dataValidade)}</td>
                    <td className="px-5 py-2 tabular text-ink-muted">{bolsa.volume} mL</td>
                    <td className="px-5 py-2">
                      <Badge tone={statusInfo.tone}>
                        <statusInfo.icon className="size-3" />
                        {statusInfo.label}
                      </Badge>
                    </td>
                    <td className="px-5 py-2 text-right">
                      <div className="inline-flex gap-1">
                        <Button variant="ghost" size="sm" className="size-8 px-0" onClick={() => onEditar(bolsa)} aria-label="Editar bolsa">
                          <Pencil />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => setParaExcluir(bolsa)}
                          aria-label="Excluir bolsa"
                          className="size-8 px-0 hover:text-emergencia"
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
      )}

      <ConfirmDialog
        open={Boolean(paraExcluir)}
        onOpenChange={(open) => !open && setParaExcluir(null)}
        title={`Excluir ${paraExcluir?.identificador ?? ""}?`}
        description="Essa ação remove o registro da bolsa permanentemente do estoque."
        confirmLabel="Excluir"
        onConfirm={() => paraExcluir && excluir.mutate(paraExcluir.id)}
      />
    </div>
  );
}
