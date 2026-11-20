import * as React from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { ChoiceGroup } from "@/components/ui/choice-group";
import {
  DESCRICAO_COMPONENTE,
  DESCRICAO_TIPO_SANGUINEO,
  TIPOS_COMPONENTE,
  TIPOS_SANGUINEOS,
  type BolsaResponse,
  type CadastroBolsaRequest,
  type TipoComponente,
  type TipoSanguineo,
} from "@/api/types";
import { useAtualizarBolsa, useCadastrarBolsa } from "@/hooks/use-api";
import { Plus, Save, X } from "lucide-react";

const vazio: CadastroBolsaRequest = {
  identificador: "",
  tipoSanguineo: "" as TipoSanguineo,
  tipoComponente: "" as TipoComponente,
  dataColeta: "",
  dataValidade: "",
  volume: 300,
};

export function BolsaForm({
  emEdicao,
  onCancelarEdicao,
}: {
  emEdicao: BolsaResponse | null;
  onCancelarEdicao: () => void;
}) {
  const [form, setForm] = React.useState<CadastroBolsaRequest>(vazio);
  const [erro, setErro] = React.useState<string | null>(null);
  const cadastrar = useCadastrarBolsa();
  const atualizar = useAtualizarBolsa();

  React.useEffect(() => {
    if (emEdicao) {
      setForm({
        identificador: emEdicao.identificador,
        tipoSanguineo: emEdicao.tipoSanguineo,
        tipoComponente: emEdicao.tipoComponente,
        dataColeta: emEdicao.dataColeta,
        dataValidade: emEdicao.dataValidade,
        volume: emEdicao.volume,
      });
      setErro(null);
    }
  }, [emEdicao]);

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setErro(null);
    if (!form.tipoSanguineo || !form.tipoComponente) {
      setErro("Selecione o tipo sanguíneo e o componente.");
      return;
    }
    if (emEdicao) {
      atualizar.mutate(
        { id: emEdicao.id, body: form },
        {
          onSuccess: () => {
            setForm(vazio);
            onCancelarEdicao();
          },
          onError: (e) => setErro(e.message),
        },
      );
    } else {
      cadastrar.mutate(form, {
        onSuccess: () => setForm(vazio),
        onError: (e) => setErro(e.message),
      });
    }
  }

  const salvando = cadastrar.isPending || atualizar.isPending;

  return (
    <form
      onSubmit={handleSubmit}
      className="rounded-panel border border-border bg-surface p-5"
      noValidate
    >
      <div className="mb-4 flex flex-wrap items-center gap-2">
        <h2 className="text-[0.95rem] font-semibold text-ink">
          {emEdicao ? "Editar bolsa" : "Cadastrar bolsa"}
        </h2>
        {emEdicao && (
          <span className="rounded-control bg-info-soft px-2 py-0.5 text-xs font-semibold text-info">
            {emEdicao.identificador}
          </span>
        )}
        <p className="ml-auto text-xs text-ink-faint">* campos obrigatórios</p>
      </div>

      {erro && (
        <p role="alert" className="mb-4 rounded-control bg-emergencia-soft px-3 py-2 text-sm text-emergencia">
          {erro}
        </p>
      )}

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <div className="space-y-1.5 lg:col-span-1">
          <Label htmlFor="bolsa-identificador">Identificador *</Label>
          <Input
            id="bolsa-identificador"
            required
            placeholder="BOLSA-2026-0001"
            className="font-mono text-[13.5px]"
            value={form.identificador}
            onChange={(e) => setForm((f) => ({ ...f, identificador: e.target.value }))}
          />
        </div>

        <div className="space-y-1.5 sm:col-span-2 lg:col-span-1">
          <Label>Tipo sanguíneo *</Label>
          <ChoiceGroup
            name="tipoSanguineo"
            value={form.tipoSanguineo}
            onChange={(v) => setForm((f) => ({ ...f, tipoSanguineo: v }))}
            opcoes={TIPOS_SANGUINEOS.map((t) => ({ value: t, label: DESCRICAO_TIPO_SANGUINEO[t] }))}
          />
        </div>

        <div className="space-y-1.5 sm:col-span-2 lg:col-span-2">
          <Label>Componente *</Label>
          <ChoiceGroup
            name="tipoComponente"
            value={form.tipoComponente}
            onChange={(v) => setForm((f) => ({ ...f, tipoComponente: v }))}
            opcoes={TIPOS_COMPONENTE.map((c) => ({ value: c, label: DESCRICAO_COMPONENTE[c] }))}
          />
        </div>

        <div className="space-y-1.5">
          <Label htmlFor="bolsa-coleta">Data de coleta *</Label>
          <Input
            id="bolsa-coleta"
            type="date"
            required
            value={form.dataColeta}
            onChange={(e) => setForm((f) => ({ ...f, dataColeta: e.target.value }))}
          />
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="bolsa-validade">Data de validade *</Label>
          <Input
            id="bolsa-validade"
            type="date"
            required
            value={form.dataValidade}
            onChange={(e) => setForm((f) => ({ ...f, dataValidade: e.target.value }))}
          />
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="bolsa-volume">Volume (mL) *</Label>
          <Input
            id="bolsa-volume"
            type="number"
            min={1}
            required
            value={form.volume}
            onChange={(e) => setForm((f) => ({ ...f, volume: Number(e.target.value) }))}
          />
        </div>

        <div className="flex items-end justify-end gap-2 lg:col-span-1">
          {emEdicao && (
            <Button
              type="button"
              variant="secondary"
              onClick={() => {
                setForm(vazio);
                setErro(null);
                onCancelarEdicao();
              }}
            >
              <X /> Cancelar
            </Button>
          )}
          <Button type="submit" disabled={salvando}>
            {emEdicao ? <Save /> : <Plus />}
            {emEdicao ? "Salvar" : "Cadastrar"}
          </Button>
        </div>
      </div>
    </form>
  );
}
