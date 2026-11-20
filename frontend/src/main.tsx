import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import "./index.css";

import { AppShell } from "@/components/layout/app-shell";
import { ToastProvider } from "@/hooks/use-toast";
import { EstoquePage } from "@/features/estoque/estoque-page";
import { SolicitacoesPage } from "@/features/solicitacoes/solicitacoes-page";
import { ValidadePage } from "@/features/validade/validade-page";
import { LogisticaPage } from "@/features/logistica/logistica-page";
import { IndicadoresPage } from "@/features/indicadores/indicadores-page";
import { HistoricoPage } from "@/features/historico/historico-page";

// Tema escuro segue a preferência do sistema operacional (como no app original).
const temaEscuro = window.matchMedia("(prefers-color-scheme: dark)");
const aplicarTema = () => document.documentElement.classList.toggle("dark", temaEscuro.matches);
aplicarTema();
temaEscuro.addEventListener("change", aplicarTema);

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 15_000,
      refetchOnWindowFocus: false,
    },
  },
});

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <ToastProvider>
        <BrowserRouter>
          <Routes>
            <Route element={<AppShell />}>
              <Route index element={<Navigate to="/estoque" replace />} />
              <Route path="estoque" element={<EstoquePage />} />
              <Route path="solicitacoes" element={<SolicitacoesPage />} />
              <Route path="validade" element={<ValidadePage />} />
              <Route path="logistica" element={<LogisticaPage />} />
              <Route path="indicadores" element={<IndicadoresPage />} />
              <Route path="historico" element={<HistoricoPage />} />
              <Route path="*" element={<Navigate to="/estoque" replace />} />
            </Route>
          </Routes>
        </BrowserRouter>
      </ToastProvider>
    </QueryClientProvider>
  </StrictMode>,
);
