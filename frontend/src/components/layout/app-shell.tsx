import * as React from "react";
import { NavLink, Outlet } from "react-router-dom";
import { Droplet } from "lucide-react";
import { useQuery } from "@tanstack/react-query";
import { cn } from "@/lib/utils";
import { NAV_ITEMS } from "./nav-items";
import { StockStrip } from "./stock-strip";
import { useSolicitacoes } from "@/hooks/use-api";

function useRelogio() {
  const [agora, setAgora] = React.useState(() => new Date());
  React.useEffect(() => {
    const id = window.setInterval(() => setAgora(new Date()), 30_000);
    return () => window.clearInterval(id);
  }, []);
  return agora;
}

function useStatusConexao() {
  return useQuery({
    queryKey: ["status-conexao"],
    queryFn: async () => {
      const resposta = await fetch("/api/bolsas", { method: "HEAD" }).catch(() => null);
      if (resposta) return true;
      const alternativa = await fetch("/api/bolsas").catch(() => null);
      return Boolean(alternativa?.ok);
    },
    refetchInterval: 15_000,
    retry: false,
  });
}

function Marca() {
  return (
    <div className="flex items-center gap-2.5 px-2">
      <span className="grid size-8 flex-none place-items-center rounded-control bg-brand text-brand-foreground">
        <Droplet className="size-4" fill="currentColor" strokeWidth={0} />
      </span>
      <div className="leading-tight">
        <p className="text-[0.95rem] font-bold text-ink">Rota Vital</p>
        <p className="text-[11px] text-ink-muted">Hemocentro Regional</p>
      </div>
    </div>
  );
}

function Navegacao({ onNavigate }: { onNavigate?: () => void }) {
  const { data: fila } = useSolicitacoes();
  const pendentes = (fila ?? []).filter((s) => s.status === "PENDENTE").length;

  return (
    <nav className="flex flex-col gap-0.5" aria-label="Áreas do sistema">
      {NAV_ITEMS.map(({ to, label, icon: Icon }) => (
        <NavLink
          key={to}
          to={to}
          onClick={onNavigate}
          className={({ isActive }) =>
            cn(
              "flex items-center gap-2.5 rounded-control px-2.5 py-2 text-sm font-medium text-ink-muted hover:bg-surface-muted hover:text-ink",
              isActive && "bg-brand-soft text-brand hover:bg-brand-soft hover:text-brand",
            )
          }
        >
          <Icon className="size-4" />
          {label}
          {label === "Solicitações" && pendentes > 0 && (
            <span className="ml-auto rounded-control bg-alerta-soft px-1.5 py-0.5 text-[11px] font-bold text-alerta tabular">
              {pendentes}
            </span>
          )}
        </NavLink>
      ))}
    </nav>
  );
}

function IndicadorConexao() {
  const { data: conectado, isLoading } = useStatusConexao();
  const estado = isLoading ? "verificando" : conectado ? "ok" : "falha";
  return (
    <div className="flex items-center gap-2 border-t border-border px-2.5 pt-3 text-xs text-ink-muted">
      <span
        className={cn(
          "size-2 flex-none rounded-full",
          estado === "ok" && "bg-ok",
          estado === "falha" && "bg-emergencia",
          estado === "verificando" && "bg-alerta",
        )}
      />
      <span>
        {estado === "ok" && "API conectada"}
        {estado === "falha" && "Sem conexão com a API"}
        {estado === "verificando" && "Conectando…"}
      </span>
    </div>
  );
}

export function AppShell() {
  const agora = useRelogio();
  const [menuAberto, setMenuAberto] = React.useState(false);

  return (
    <div className="min-h-screen md:grid md:grid-cols-[232px_minmax(0,1fr)]">
      {/* Sidebar — desktop */}
      <aside className="sticky top-0 hidden h-screen flex-col gap-5 border-r border-border bg-surface p-4 md:flex">
        <Marca />
        <StockStrip />
        <Navegacao />
        <IndicadorConexao />
      </aside>

      {/* Topo — mobile */}
      <header className="sticky top-0 z-40 border-b border-border bg-surface/95 backdrop-blur md:hidden">
        <div className="flex items-center justify-between px-4 py-3">
          <Marca />
          <button
            type="button"
            onClick={() => setMenuAberto((v) => !v)}
            className="rounded-control border border-border-strong px-3 py-1.5 text-sm font-medium"
            aria-expanded={menuAberto}
          >
            Menu
          </button>
        </div>
        {menuAberto && (
          <div className="border-t border-border p-3">
            <StockStrip />
            <div className="mt-3">
              <Navegacao onNavigate={() => setMenuAberto(false)} />
            </div>
          </div>
        )}
        <div className="flex gap-1 overflow-x-auto border-t border-border px-2 py-1.5">
          {NAV_ITEMS.map(({ to, label }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) =>
                cn(
                  "flex-none rounded-control px-2.5 py-1.5 text-sm font-medium text-ink-muted",
                  isActive && "bg-brand-soft text-brand",
                )
              }
            >
              {label}
            </NavLink>
          ))}
        </div>
      </header>

      <main className="min-w-0">
        <div className="mx-auto max-w-[1280px] px-4 py-6 md:px-8 md:py-7">
          <Outlet context={{ agora } satisfies { agora: Date }} />
        </div>
      </main>
    </div>
  );
}
