import * as React from "react";
import { NavLink, Outlet, useLocation } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { Menu, X } from "lucide-react";
import { cn } from "@/lib/utils";
import { NAV_ITEMS, doisDigitos } from "./nav-items";
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

function usePendentes() {
  const { data: fila } = useSolicitacoes();
  return (fila ?? []).filter((s) => s.status === "PENDENTE").length;
}

/** Marca tipográfica: grotesca + serifada itálica, como os títulos da referência. */
function Marca({ inverse = false }: { inverse?: boolean }) {
  return (
    <NavLink to="/estoque" className="group inline-flex items-baseline gap-1 leading-none" aria-label="Rota Vital — início">
      <span
        className={cn(
          "text-[1.6rem] font-medium tracking-[-0.04em]",
          inverse ? "text-inverse-ink" : "text-ink",
        )}
      >
        Rota
      </span>
      <span
        className={cn(
          "serif-accent text-[1.85rem]",
          inverse ? "text-inverse-muted" : "text-brand",
        )}
      >
        Vital
      </span>
      <svg
        viewBox="0 0 10 14"
        aria-hidden="true"
        className={cn("mb-auto ml-0.5 h-2.5 w-2", inverse ? "fill-[#ffb4a2]" : "fill-emergencia")}
      >
        <path d="M5 0C5 0 0 6 0 9a5 5 0 0 0 10 0C10 6 5 0 5 0Z" />
      </svg>
    </NavLink>
  );
}

function NavegacaoLateral() {
  const pendentes = usePendentes();

  return (
    <nav aria-label="Áreas do sistema">
      <ol className="border-t border-inverse-line">
        {NAV_ITEMS.map(({ to, label, hint }, i) => (
          <li key={to} className="border-b border-inverse-line">
            <NavLink
              to={to}
              className={({ isActive }) =>
                cn(
                  "group relative -mx-2 my-1 flex items-center gap-3 rounded-full px-3 py-2 transition-colors",
                  isActive
                    ? "bg-inverse-ink text-inverse"
                    : "text-inverse-ink hover:bg-white/10",
                )
              }
            >
              {({ isActive }) => (
                <>
                  <span
                    className={cn(
                      "w-5 font-mono text-[11px]",
                      isActive ? "text-inverse" : "text-inverse-muted",
                    )}
                  >
                    {doisDigitos(i + 1)}
                  </span>
                  <span className="flex-1 leading-tight">
                    <span className="block text-[14.5px] font-medium tracking-[-0.01em]">{label}</span>
                    <span
                      className={cn(
                        "serif-accent block text-[13px] leading-tight",
                        isActive ? "text-inverse/70" : "text-inverse-muted",
                      )}
                    >
                      {hint}
                    </span>
                  </span>
                  {label === "Solicitações" && pendentes > 0 && (
                    <span
                      className={cn(
                        "grid min-w-6 place-items-center rounded-full px-1.5 py-0.5 text-[11px] font-semibold tabular",
                        isActive ? "bg-inverse text-inverse-ink" : "bg-inverse-ink text-inverse",
                      )}
                      aria-label={`${pendentes} pendente(s)`}
                    >
                      {pendentes}
                    </span>
                  )}
                </>
              )}
            </NavLink>
          </li>
        ))}
      </ol>
    </nav>
  );
}

function IndicadorConexao({ inverse = true }: { inverse?: boolean }) {
  const { data: conectado, isLoading } = useStatusConexao();
  const estado = isLoading ? "verificando" : conectado ? "ok" : "falha";
  return (
    <p
      className={cn("meta flex items-center gap-2", inverse ? "text-inverse-muted" : "text-ink-muted")}
      role="status"
    >
      <span className="relative flex size-2">
        {estado === "ok" && (
          <span className="absolute inset-0 animate-ping rounded-full bg-[#7ee2b0] opacity-60" />
        )}
        <span
          className={cn(
            "relative size-2 rounded-full",
            estado === "ok" && "bg-[#7ee2b0]",
            estado === "falha" && "bg-[#ff8a73]",
            estado === "verificando" && "bg-[#f0c36a]",
          )}
        />
      </span>
      {estado === "ok" && "API conectada"}
      {estado === "falha" && "Sem conexão com a API"}
      {estado === "verificando" && "Conectando…"}
    </p>
  );
}

function BarraLateral() {
  return (
    <aside className="sticky top-0 hidden h-screen flex-col overflow-y-auto bg-inverse px-6 pb-6 pt-5 text-inverse-ink lg:flex">
      <div className="meta mb-8 flex items-center justify-between text-inverse-muted">
        <span>Hemocentro Regional</span>
        <span className="font-mono">{new Date().getFullYear()}</span>
      </div>

      <Marca inverse />
      <p className="mt-3 max-w-[15rem] text-[12.5px] leading-snug text-inverse-muted">
        Estoque, fila hospitalar e cadeia fria em um só lugar.
      </p>

      <div className="mt-8">
        <NavegacaoLateral />
      </div>

      <div className="mt-8 rounded-panel border border-inverse-line p-4">
        <StockStrip />
      </div>

      <div className="mt-auto pt-6">
        <IndicadorConexao />
      </div>
    </aside>
  );
}

function TopoMovel() {
  const [aberto, setAberto] = React.useState(false);
  const { pathname } = useLocation();
  const pendentes = usePendentes();
  const trilhaRef = React.useRef<HTMLDivElement>(null);

  React.useEffect(() => setAberto(false), [pathname]);

  // Mantém a pílula ativa visível na trilha horizontal.
  React.useEffect(() => {
    trilhaRef.current
      ?.querySelector<HTMLElement>("[aria-current=page]")
      ?.scrollIntoView({ inline: "center", block: "nearest" });
  }, [pathname]);

  return (
    <header className="sticky top-0 z-40 border-b border-border bg-background/90 backdrop-blur-md lg:hidden">
      <div className="flex items-center justify-between px-4 pb-2 pt-3">
        <Marca />
        <button
          type="button"
          onClick={() => setAberto((v) => !v)}
          className="inline-flex h-9 items-center gap-2 rounded-full border border-ink/80 px-3.5 text-[13px] font-medium"
          aria-expanded={aberto}
          aria-controls="painel-estoque-movel"
        >
          {aberto ? <X className="size-4" /> : <Menu className="size-4" />}
          Estoque
        </button>
      </div>

      {aberto && (
        <div id="painel-estoque-movel" className="mx-3 mb-3 animate-rise rounded-panel bg-inverse p-4 text-inverse-ink">
          <StockStrip />
          <div className="mt-4 border-t border-inverse-line pt-3">
            <IndicadorConexao />
          </div>
        </div>
      )}

      <div ref={trilhaRef} className="scrollbar-none flex gap-1.5 overflow-x-auto px-3 pb-2.5">
        {NAV_ITEMS.map(({ to, label }, i) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) =>
              cn(
                "flex flex-none items-center gap-1.5 rounded-full border px-3 py-1.5 text-[13px] font-medium transition-colors",
                isActive
                  ? "border-brand bg-brand text-brand-foreground"
                  : "border-border-strong text-ink-muted",
              )
            }
          >
            <span className="font-mono text-[10px] opacity-70">{doisDigitos(i + 1)}</span>
            {label}
            {label === "Solicitações" && pendentes > 0 && (
              <span className="rounded-full bg-emergencia px-1.5 text-[10.5px] font-semibold text-white tabular">
                {pendentes}
              </span>
            )}
          </NavLink>
        ))}
      </div>
    </header>
  );
}

export function AppShell() {
  const agora = useRelogio();

  return (
    <div className="min-h-screen lg:grid lg:grid-cols-[272px_minmax(0,1fr)]">
      <a
        href="#conteudo"
        className="sr-only z-50 rounded-full bg-brand px-4 py-2 text-brand-foreground focus:not-sr-only focus:fixed focus:left-4 focus:top-4"
      >
        Pular para o conteúdo
      </a>
      <BarraLateral />
      <TopoMovel />

      <main id="conteudo" className="min-w-0">
        <div className="mx-auto max-w-[1320px] px-4 pb-16 pt-6 sm:px-6 lg:px-10 lg:pt-8">
          <Outlet context={{ agora } satisfies { agora: Date }} />
        </div>
      </main>
    </div>
  );
}
