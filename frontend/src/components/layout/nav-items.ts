import {
  BarChart3,
  Boxes,
  CalendarClock,
  ClipboardList,
  History,
  Truck,
} from "lucide-react";

export const NAV_ITEMS = [
  { to: "/estoque", label: "Estoque", icon: Boxes },
  { to: "/solicitacoes", label: "Solicitações", icon: ClipboardList },
  { to: "/validade", label: "Validade", icon: CalendarClock },
  { to: "/logistica", label: "Logística", icon: Truck },
  { to: "/indicadores", label: "Indicadores", icon: BarChart3 },
  { to: "/historico", label: "Histórico", icon: History },
] as const;
