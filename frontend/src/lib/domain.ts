import {
  AlertTriangle,
  CheckCircle2,
  CircleDot,
  Clock,
  PackageX,
  Truck,
  XCircle,
} from "lucide-react";
import type {
  CondicaoRemessa,
  NivelUrgencia,
  StatusBolsa,
  StatusRemessa,
  StatusRota,
  StatusSolicitacao,
  TipoComponente,
} from "@/api/types";
import type { BadgeProps } from "@/components/ui/badge";

type Tone = NonNullable<BadgeProps["tone"]>;

export const TONE_POR_COMPONENTE: Record<TipoComponente, Tone> = {
  HEMACIAS: "hem",
  PLASMA: "pla",
  PLAQUETAS: "plq",
  SANGUE_TOTAL: "st",
};

export const STATUS_BOLSA_INFO: Record<
  StatusBolsa,
  { label: string; tone: Tone; icon: typeof CheckCircle2 }
> = {
  DISPONIVEL: { label: "Disponível", tone: "ok", icon: CheckCircle2 },
  ALOCADA: { label: "Alocada", tone: "info", icon: CircleDot },
  INDISPONIVEL: { label: "Indisponível", tone: "neutral", icon: XCircle },
  DESCARTADA: { label: "Descartada", tone: "emergencia", icon: PackageX },
};

export const STATUS_SOLICITACAO_INFO: Record<
  StatusSolicitacao,
  { label: string; tone: Tone; icon: typeof CheckCircle2 }
> = {
  PENDENTE: { label: "Pendente", tone: "alerta", icon: Clock },
  EM_ATENDIMENTO: { label: "Em atendimento", tone: "info", icon: CircleDot },
  ATENDIDA: { label: "Atendida", tone: "ok", icon: CheckCircle2 },
  CANCELADA: { label: "Cancelada", tone: "neutral", icon: XCircle },
};

export const URGENCIA_INFO: Record<NivelUrgencia, { label: string; tone: Tone }> = {
  EMERGENCIA: { label: "Emergência", tone: "emergencia" },
  URGENTE: { label: "Urgente", tone: "alerta" },
  ELETIVA: { label: "Eletiva", tone: "neutral" },
};

export const STATUS_ROTA_INFO: Record<StatusRota, { label: string; tone: Tone }> = {
  CONFIRMADA: { label: "Confirmada", tone: "ok" },
  AGUARDANDO_CONFIRMACAO: { label: "Aguardando confirmação", tone: "alerta" },
};

export const STATUS_REMESSA_INFO: Record<
  StatusRemessa,
  { label: string; tone: Tone; icon: typeof Truck }
> = {
  EM_TRANSITO: { label: "Em trânsito", tone: "info", icon: Truck },
  ENTREGUE: { label: "Entregue", tone: "ok", icon: CheckCircle2 },
};

export const CONDICAO_REMESSA_INFO: Record<
  CondicaoRemessa,
  { label: string; tone: Tone; icon: typeof CheckCircle2 }
> = {
  CONFORME: { label: "Conforme", tone: "ok", icon: CheckCircle2 },
  SOB_RISCO: { label: "Sob risco", tone: "emergencia", icon: AlertTriangle },
};

/** Nível crítico padrão de estoque (espelha rotavital.indicadores.nivel-critico). */
export const NIVEL_CRITICO_ESTOQUE = 5;

/** Dias antes do vencimento em que cada componente entra em alerta (espelha TipoComponente.java). */
export const JANELA_ALERTA_DIAS: Record<TipoComponente, number> = {
  HEMACIAS: 7,
  PLASMA: 30,
  PLAQUETAS: 2,
  SANGUE_TOTAL: 7,
};
