import * as React from "react";
import { Button, ButtonArrow } from "@/components/ui/button";
import { Card, CardHeader } from "@/components/ui/card";
import { FormError } from "@/components/ui/stat";
import { Input } from "@/components/ui/input";
import { FieldLegend, Label } from "@/components/ui/label";
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
import { X } from "lucide-react";

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
    if (!form.identificador.trim()) {
      setErro("Informe o identificador da bolsa.");
      return;
    }
    if (!form.tipoSanguineo || !form.tipoComponente) {
      setErro("Selecione o tipo sanguíneo e o componente.");
      return;
    }
    if (!form.dataColeta || !form.dataValidade) {
      setErro("Informe as datas de coleta e de validade.");
      return;
    }
    if (form.dataValidade < form.dataColeta) {
      setErro("A validade não pode ser anterior à data de coleta.");
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
    <Card className={emEdicao ? "border-brand ring-1 ring-brand" : undefined}>
      <CardHeader
        index="01"
        title={emEdicao ? "Editar" : "Cadastrar"}
        accent={emEdicao ? "bolsa" : "nova bolsa"}
        meta={emEdicao ? emEdicao.identificador : undefined}
      >
        <p className="meta ml-auto text-ink-faint">* obrigatórios</p>
      </CardHeader>

      <form onSubmit={handleSubmit} className="p-5" noValidate>
        {erro && <FormError>{erro}</FormError>}

        <div className="grid gap-x-5 gap-y-5 sm:grid-cols-2 lg:grid-cols-12">
          <div className="space-y-2 sm:col-span-2 lg:col-span-4">
            <Label htmlFor="bolsa-identificador">Identificador *</Label>
            <Input
              id="bolsa-identificador"
              required
              autoComplete="off"
              placeholder="BOLSA-2026-0001"
              className="font-mono text-[13.5px]"
              value={form.identificador}
              onChange={(e) => setForm((f) => ({ ...f, identificador: e.target.value }))}
            />
          </div>
          <div className="space-y-2 lg:col-span-3">
            <Label htmlFor="bolsa-coleta">Data de coleta *</Label>
            <Input
              id="bolsa-coleta"
              type="date"
              required
              value={form.dataColeta}
              onChange={(e) => setForm((f) => ({ ...f, dataColeta: e.target.value }))}
            />
          </div>
          <div className="space-y-2 lg:col-span-3">
            <Label htmlFor="bolsa-validade">Data de validade *</Label>
            <Input
              id="bolsa-validade"
              type="date"
              required
              min={form.dataColeta || undefined}
              value={form.dataValidade}
              onChange={(e) => setForm((f) => ({ ...f, dataValidade: e.target.value }))}
            />
          </div>
          <div className="space-y-2 lg:col-span-2">
            <Label htmlFor="bolsa-volume">Volume (mL) *</Label>
            <Input
              id="bolsa-volume"
              type="number"
              min={1}
              step={10}
              inputMode="numeric"
              required
              className="tabular"
              value={form.volume}
              onChange={(e) => setForm((f) => ({ ...f, volume: Number(e.target.value) }))}
            />
          </div>

          <div className="space-y-2 sm:col-span-2 lg:col-span-5">
            <FieldLegend>Tipo sanguíneo *</FieldLegend>
            <ChoiceGroup
              name="Tipo sanguíneo"
              mono
              value={form.tipoSanguineo}
              onChange={(v) => setForm((f) => ({ ...f, tipoSanguineo: v }))}
              opcoes={TIPOS_SANGUINEOS.map((t) => ({ value: t, label: DESCRICAO_TIPO_SANGUINEO[t] }))}
            />
          </div>

          <div className="space-y-2 sm:col-span-2 lg:col-span-7">
            <FieldLegend>Componente *</FieldLegend>
            <ChoiceGroup
              name="Componente"
              value={form.tipoComponente}
              onChange={(v) => setForm((f) => ({ ...f, tipoComponente: v }))}
              opcoes={TIPOS_COMPONENTE.map((c) => ({ value: c, label: DESCRICAO_COMPONENTE[c] }))}
            />
          </div>
        </div>

        <div className="mt-6 flex flex-wrap items-center justify-end gap-2 border-t border-border pt-5">
          {emEdicao && (
            <Button
              type="button"
              variant="ghost"
              onClick={() => {
                setForm(vazio);
                setErro(null);
                onCancelarEdicao();
              }}
            >
              <X /> Cancelar edição
            </Button>
          )}
          <Button type="submit" disabled={salvando}>
            {salvando ? "Salvando…" : emEdicao ? "Salvar alterações" : "Cadastrar bolsa"}
            <ButtonArrow />
          </Button>
        </div>
      </form>
    </Card>
  );
}
