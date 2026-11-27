import * as React from "react";
import { Button, ButtonArrow } from "@/components/ui/button";
import { Card, CardHeader } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { FieldLegend, Label } from "@/components/ui/label";
import { FormError } from "@/components/ui/stat";
import { ChoiceGroup } from "@/components/ui/choice-group";
import {
  DESCRICAO_COMPONENTE,
  DESCRICAO_TIPO_SANGUINEO,
  DESCRICAO_URGENCIA,
  NIVEIS_URGENCIA,
  TIPOS_COMPONENTE,
  TIPOS_SANGUINEOS,
  type CadastroSolicitacaoRequest,
  type NivelUrgencia,
  type TipoComponente,
  type TipoSanguineo,
} from "@/api/types";
import { useCadastrarSolicitacao, useSolicitacoes } from "@/hooks/use-api";
import { cn } from "@/lib/utils";
import { Minus, Plus } from "lucide-react";

const vazio: CadastroSolicitacaoRequest = {
  nomeHospital: "",
  tipoSanguineo: "" as TipoSanguineo,
  tipoComponente: "" as TipoComponente,
  quantidade: 1,
  nivelUrgencia: "" as NivelUrgencia,
};

const COR_URGENCIA: Record<NivelUrgencia, { ativo: string; ponto: string }> = {
  EMERGENCIA: { ativo: "border-emergencia bg-emergencia text-white", ponto: "bg-emergencia" },
  URGENTE: { ativo: "border-alerta bg-alerta text-white", ponto: "bg-alerta" },
  ELETIVA: { ativo: "border-ink bg-ink text-background", ponto: "bg-ink-faint" },
};

export function SolicitacaoForm() {
  const [form, setForm] = React.useState<CadastroSolicitacaoRequest>(vazio);
  const [erro, setErro] = React.useState<string | null>(null);
  const cadastrar = useCadastrarSolicitacao();
  const { data: todas } = useSolicitacoes();
  const hospitaisConhecidos = [...new Set((todas ?? []).map((s) => s.nomeHospital))].sort((a, b) =>
    a.localeCompare(b, "pt-BR"),
  );

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setErro(null);
    if (!form.nomeHospital.trim()) {
      setErro("Informe o hospital solicitante.");
      return;
    }
    if (!form.tipoSanguineo || !form.tipoComponente || !form.nivelUrgencia) {
      setErro("Selecione o tipo sanguíneo, o componente e a urgência.");
      return;
    }
    cadastrar.mutate(form, {
      onSuccess: () => setForm(vazio),
      onError: (e) => setErro(e.message),
    });
  }

  const quantidade = (delta: number) =>
    setForm((f) => ({ ...f, quantidade: Math.min(10, Math.max(1, f.quantidade + delta)) }));

  return (
    <Card>
      <CardHeader index="01" title="Registrar" accent="pedido" />
      <form onSubmit={handleSubmit} className="space-y-5 p-5" noValidate>
        {erro && <FormError>{erro}</FormError>}

        <div className="space-y-2">
          <Label htmlFor="solic-hospital">Hospital *</Label>
          <Input
            id="solic-hospital"
            required
            list="hospitais-conhecidos"
            autoComplete="off"
            placeholder="Nome do hospital"
            value={form.nomeHospital}
            onChange={(e) => setForm((f) => ({ ...f, nomeHospital: e.target.value }))}
          />
          <datalist id="hospitais-conhecidos">
            {hospitaisConhecidos.map((h) => (
              <option key={h} value={h} />
            ))}
          </datalist>
        </div>

        <div className="space-y-2">
          <FieldLegend>Urgência *</FieldLegend>
          <div role="radiogroup" aria-label="Urgência" className="grid grid-cols-3 gap-1.5">
            {NIVEIS_URGENCIA.map((n) => {
              const ativo = form.nivelUrgencia === n;
              return (
                <button
                  key={n}
                  type="button"
                  role="radio"
                  aria-checked={ativo}
                  onClick={() => setForm((f) => ({ ...f, nivelUrgencia: n }))}
                  className={cn(
                    "flex h-9 items-center justify-center gap-1.5 rounded-full border text-[13px] font-medium transition-colors",
                    ativo ? COR_URGENCIA[n].ativo : "border-border-strong text-ink-muted hover:border-ink hover:text-ink",
                  )}
                >
                  {!ativo && <span className={cn("size-1.5 rounded-full", COR_URGENCIA[n].ponto)} />}
                  {DESCRICAO_URGENCIA[n]}
                </button>
              );
            })}
          </div>
        </div>

        <div className="space-y-2">
          <FieldLegend>Tipo sanguíneo do paciente *</FieldLegend>
          <ChoiceGroup
            name="Tipo sanguíneo do paciente"
            mono
            value={form.tipoSanguineo}
            onChange={(v) => setForm((f) => ({ ...f, tipoSanguineo: v }))}
            opcoes={TIPOS_SANGUINEOS.map((t) => ({ value: t, label: DESCRICAO_TIPO_SANGUINEO[t] }))}
          />
        </div>

        <div className="space-y-2">
          <FieldLegend>Componente *</FieldLegend>
          <ChoiceGroup
            name="Componente"
            value={form.tipoComponente}
            onChange={(v) => setForm((f) => ({ ...f, tipoComponente: v }))}
            opcoes={TIPOS_COMPONENTE.map((c) => ({ value: c, label: DESCRICAO_COMPONENTE[c] }))}
          />
        </div>

        <div className="flex items-end justify-between gap-4 border-t border-border pt-5">
          <div className="space-y-2">
            <Label htmlFor="solic-quantidade">Bolsas *</Label>
            <div className="flex items-center rounded-full border border-border-strong">
              <button type="button" onClick={() => quantidade(-1)} className="grid size-9 place-items-center rounded-full text-ink-muted hover:text-ink disabled:opacity-30" disabled={form.quantidade <= 1} aria-label="Diminuir quantidade">
                <Minus className="size-3.5" />
              </button>
              <input
                id="solic-quantidade"
                type="number"
                min={1}
                max={10}
                inputMode="numeric"
                className="w-8 bg-transparent text-center text-[15px] font-medium tabular outline-none [appearance:textfield] [&::-webkit-inner-spin-button]:appearance-none"
                value={form.quantidade}
                onChange={(e) => setForm((f) => ({ ...f, quantidade: Math.min(10, Math.max(1, Number(e.target.value) || 1)) }))}
              />
              <button type="button" onClick={() => quantidade(1)} className="grid size-9 place-items-center rounded-full text-ink-muted hover:text-ink disabled:opacity-30" disabled={form.quantidade >= 10} aria-label="Aumentar quantidade">
                <Plus className="size-3.5" />
              </button>
            </div>
          </div>
          <Button type="submit" disabled={cadastrar.isPending}>
            {cadastrar.isPending ? "Registrando…" : "Registrar"}
            <ButtonArrow />
          </Button>
        </div>
      </form>
    </Card>
  );
}
