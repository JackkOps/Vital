import {
  BarChart3,
  Boxes,
  CalendarClock,
  ClipboardList,
  History,
  Truck,
} from "lucide-react";

export const NAV_ITEMS = [
  { to: "/estoque", label: "Estoque", hint: "Bolsas e cadastro", icon: Boxes },
  { to: "/solicitacoes", label: "Solicitações", hint: "Fila por urgência", icon: ClipboardList },
  { to: "/validade", label: "Validade", hint: "Vencimentos e baixas", icon: CalendarClock },
  { to: "/logistica", label: "Logística", hint: "Rotas e cadeia fria", icon: Truck },
  { to: "/indicadores", label: "Indicadores", hint: "Estoque e demanda", icon: BarChart3 },
  { to: "/historico", label: "Histórico", hint: "Pilha de operações", icon: History },
] as const;

export function indiceDaRota(pathname: string) {
  const i = NAV_ITEMS.findIndex((item) => pathname.startsWith(item.to));
  return i < 0 ? 0 : i;
}

export const doisDigitos = (n: number) => String(n).padStart(2, "0");
