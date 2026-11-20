import * as React from "react";
import { Button } from "@/components/ui/button";
import { Input, Select } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
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
import { useCadastrarSolicitacao } from "@/hooks/use-api";
import { Plus } from "lucide-react";

const vazio: CadastroSolicitacaoRequest = {
  nomeHospital: "",
  tipoSanguineo: "" as TipoSanguineo,
  tipoComponente: "" as TipoComponente,
  quantidade: 1,
  nivelUrgencia: "" as NivelUrgencia,
};

export function SolicitacaoForm() {
  const [form, setForm] = React.useState<CadastroSolicitacaoRequest>(vazio);
  const [erro, setErro] = React.useState<string | null>(null);
  const cadastrar = useCadastrarSolicitacao();

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setErro(null);
    if (!form.tipoSanguineo || !form.tipoComponente || !form.nivelUrgencia) {
      setErro("Selecione o tipo sanguíneo, o componente e a urgência.");
      return;
    }
    cadastrar.mutate(form, {
      onSuccess: () => setForm(vazio),
      onError: (e) => setErro(e.message),
    });
  }

  return (
    <form onSubmit={handleSubmit} className="rounded-panel border border-border bg-surface p-5" noValidate>
      <div className="mb-4">
        <h2 className="text-[0.95rem] font-semibold text-ink">Registrar solicitação</h2>
        <p className="mt-0.5 text-xs text-ink-muted">
          Emergências entram à frente das urgentes e eletivas; no mesmo nível vale a ordem de chegada.
        </p>
      </div>

      {erro && (
        <p role="alert" className="mb-4 rounded-control bg-emergencia-soft px-3 py-2 text-sm text-emergencia">
          {erro}
        </p>
      )}

      <div className="space-y-4">
        <div className="space-y-1.5">
          <Label htmlFor="solic-hospital">Hospital *</Label>
          <Input
            id="solic-hospital"
            required
            placeholder="Nome do hospital"
            value={form.nomeHospital}
            onChange={(e) => setForm((f) => ({ ...f, nomeHospital: e.target.value }))}
          />
        </div>

        <div className="space-y-1.5">
          <Label>Tipo sanguíneo do paciente *</Label>
          <ChoiceGroup
            name="tipoSanguineo"
            value={form.tipoSanguineo}
            onChange={(v) => setForm((f) => ({ ...f, tipoSanguineo: v }))}
            opcoes={TIPOS_SANGUINEOS.map((t) => ({ value: t, label: DESCRICAO_TIPO_SANGUINEO[t] }))}
          />
        </div>

        <div className="space-y-1.5">
          <Label htmlFor="solic-componente">Componente *</Label>
          <Select
            id="solic-componente"
            required
            value={form.tipoComponente}
            onChange={(e) =>
              setForm((f) => ({ ...f, tipoComponente: e.target.value as TipoComponente }))
            }
          >
            <option value="" disabled>Selecione o componente</option>
            {TIPOS_COMPONENTE.map((c) => (
              <option key={c} value={c}>{DESCRICAO_COMPONENTE[c]}</option>
            ))}
          </Select>
        </div>

        <div className="space-y-1.5">
          <Label htmlFor="solic-quantidade">Quantidade de bolsas *</Label>
          <Input
            id="solic-quantidade"
            type="number"
            min={1}
            max={10}
            required
            className="w-28"
            value={form.quantidade}
            onChange={(e) => setForm((f) => ({ ...f, quantidade: Number(e.target.value) }))}
          />
        </div>

        <div className="space-y-1.5">
          <Label>Urgência *</Label>
          <ChoiceGroup
            name="nivelUrgencia"
            value={form.nivelUrgencia}
            onChange={(v) => setForm((f) => ({ ...f, nivelUrgencia: v }))}
            opcoes={NIVEIS_URGENCIA.map((n) => ({ value: n, label: DESCRICAO_URGENCIA[n] }))}
          />
        </div>

        <Button type="submit" className="w-full" disabled={cadastrar.isPending}>
          <Plus /> Registrar solicitação
        </Button>
      </div>
    </form>
  );
}
