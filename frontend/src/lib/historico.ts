import {
  CirclePlay,
  ClipboardList,
  History,
  Pencil,
  Plus,
  Trash2,
  type LucideIcon,
} from "lucide-react";
import type { BadgeProps } from "@/components/ui/badge";

type Tone = NonNullable<BadgeProps["tone"]>;

interface Regra {
  padrao: RegExp;
  categoria: string;
  tone: Tone;
  icon: LucideIcon;
}

const REGRAS: Regra[] = [
  { padrao: /^Cadastro de bolsa/, categoria: "Cadastro de bolsa", tone: "ok", icon: Plus },
  { padrao: /^Atualização de bolsa/, categoria: "Edição de bolsa", tone: "neutral", icon: Pencil },
  { padrao: /^Exclusão de bolsa/, categoria: "Exclusão de bolsa", tone: "emergencia", icon: Trash2 },
  { padrao: /^Baixa por vencimento/, categoria: "Baixa", tone: "emergencia", icon: Trash2 },
  { padrao: /^Cadastro de solicitação/, categoria: "Solicitação", tone: "alerta", icon: ClipboardList },
  { padrao: /^Atualização de solicitação/, categoria: "Solicitação", tone: "alerta", icon: Pencil },
  { padrao: /^Exclusão de solicitação/, categoria: "Solicitação", tone: "emergencia", icon: Trash2 },
  { padrao: /^Chamada de solicitação/, categoria: "Atendimento", tone: "info", icon: CirclePlay },
];

export function interpretarOperacao(texto: string) {
  for (const regra of REGRAS) {
    if (regra.padrao.test(texto)) {
      return { categoria: regra.categoria, tone: regra.tone, icon: regra.icon, texto };
    }
  }
  return { categoria: "Operação", tone: "neutral" as Tone, icon: History, texto };
}
