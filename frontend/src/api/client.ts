// Cliente HTTP fino para a API do Rota Vital.
// Em dev, o Vite repassa /api para o backend Spring Boot (ver vite.config.ts).
// Erros do backend sempre chegam como { "mensagem": "..." } (ApiExceptionHandler).

export class ApiError extends Error {
  status: number;
  constructor(status: number, mensagem: string) {
    super(mensagem);
    this.status = status;
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`/api${path}`, {
    ...init,
    headers: {
      ...(init?.body ? { "Content-Type": "application/json" } : {}),
      ...init?.headers,
    },
  });

  if (!response.ok) {
    let mensagem = `Falha na requisição (${response.status})`;
    try {
      const corpo = await response.json();
      if (corpo?.mensagem) mensagem = corpo.mensagem;
    } catch {
      // corpo vazio ou não-JSON: mantém a mensagem padrão
    }
    throw new ApiError(response.status, mensagem);
  }

  if (response.status === 204) return undefined as T;
  const texto = await response.text();
  return (texto ? JSON.parse(texto) : undefined) as T;
}

const get = <T>(path: string) => request<T>(path);
const post = <T>(path: string, body?: unknown) =>
  request<T>(path, { method: "POST", body: body !== undefined ? JSON.stringify(body) : undefined });
const put = <T>(path: string, body?: unknown) =>
  request<T>(path, { method: "PUT", body: JSON.stringify(body) });
const del = <T>(path: string) => request<T>(path, { method: "DELETE" });

function query(params: Record<string, string | number | undefined | null>) {
  const search = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== null && value !== "") search.set(key, String(value));
  }
  const qs = search.toString();
  return qs ? `?${qs}` : "";
}

import type {
  AlertaValidadeResponse,
  AtualizacaoBolsaRequest,
  AtualizacaoSolicitacaoRequest,
  BaixaBolsaResponse,
  BolsaResponse,
  CadastroBolsaRequest,
  CadastroRemessaRequest,
  CadastroSolicitacaoRequest,
  CalculoRotaRequest,
  FiltroIndicadores,
  IndicadoresResponse,
  LeituraTemperaturaResponse,
  OperacaoHistoricoResponse,
  RedeDistribuicaoResponse,
  RemessaResponse,
  RespostaAlocacaoBolsa,
  ResultadoLeituraResponse,
  RotaResponse,
  SolicitacaoResponse,
  VerificacaoSensoresResponse,
  VerificacaoValidadeResponse,
} from "./types";

export const api = {
  bolsas: {
    listar: () => get<BolsaResponse[]>("/bolsas"),
    buscar: (id: number) => get<BolsaResponse>(`/bolsas/${id}`),
    cadastrar: (body: CadastroBolsaRequest) => post<BolsaResponse>("/bolsas", body),
    atualizar: (id: number, body: AtualizacaoBolsaRequest) =>
      put<BolsaResponse>(`/bolsas/${id}`, body),
    excluir: (id: number) => del<void>(`/bolsas/${id}`),
  },
  solicitacoes: {
    listar: () => get<SolicitacaoResponse[]>("/solicitacoes"),
    buscar: (id: number) => get<SolicitacaoResponse>(`/solicitacoes/${id}`),
    fila: () => get<SolicitacaoResponse[]>("/solicitacoes/fila"),
    cadastrar: (body: CadastroSolicitacaoRequest) =>
      post<SolicitacaoResponse>("/solicitacoes", body),
    atualizar: (id: number, body: AtualizacaoSolicitacaoRequest) =>
      put<SolicitacaoResponse>(`/solicitacoes/${id}`, body),
    cancelar: (id: number) => post<SolicitacaoResponse>(`/solicitacoes/${id}/cancelar`),
    chamarProxima: () => post<SolicitacaoResponse>("/solicitacoes/fila/proxima"),
    excluir: (id: number) => del<void>(`/solicitacoes/${id}`),
  },
  alocacoes: {
    alocar: (solicitacaoId: number) =>
      post<RespostaAlocacaoBolsa>(`/alocacoes/solicitacoes/${solicitacaoId}/alocar`),
    cancelar: (bolsaId: number) => post<BolsaResponse>(`/alocacoes/bolsas/${bolsaId}/cancelar`),
    listarPorSolicitacao: (solicitacaoId: number) =>
      get<BolsaResponse[]>(`/alocacoes/solicitacoes/${solicitacaoId}`),
  },
  validade: {
    alertas: () => get<AlertaValidadeResponse[]>("/validade/alertas"),
    verificar: () => post<VerificacaoValidadeResponse>("/validade/verificar"),
    darBaixa: (bolsaId: number) =>
      post<BaixaBolsaResponse>(`/validade/bolsas/${bolsaId}/baixa`),
    baixas: () => get<BaixaBolsaResponse[]>("/validade/baixas"),
  },
  rotas: {
    rede: () => get<RedeDistribuicaoResponse>("/rotas/rede"),
    calcular: (body: CalculoRotaRequest) => post<RotaResponse>("/rotas", body),
    listar: () => get<RotaResponse[]>("/rotas"),
    buscar: (id: number) => get<RotaResponse>(`/rotas/${id}`),
    confirmar: (id: number) => post<RotaResponse>(`/rotas/${id}/confirmar`),
  },
  remessas: {
    despachar: (body: CadastroRemessaRequest) => post<RemessaResponse>("/remessas", body),
    listar: () => get<RemessaResponse[]>("/remessas"),
    buscar: (id: number) => get<RemessaResponse>(`/remessas/${id}`),
    leituras: (id: number) => get<LeituraTemperaturaResponse[]>(`/remessas/${id}/leituras`),
    registrarLeitura: (id: number, body: unknown) =>
      post<ResultadoLeituraResponse>(`/remessas/${id}/leituras`, body),
    entregar: (id: number) => post<RemessaResponse>(`/remessas/${id}/entregar`),
    verificarSensores: () => post<VerificacaoSensoresResponse>("/remessas/verificar-sensores"),
  },
  indicadores: {
    consultar: (filtro: FiltroIndicadores) =>
      get<IndicadoresResponse>(
        `/indicadores${query({
          dataInicio: filtro.dataInicio,
          dataFim: filtro.dataFim,
          hospital: filtro.hospital,
          tipoComponente: filtro.tipoComponente,
        })}`,
      ),
  },
  historico: {
    listar: () => get<OperacaoHistoricoResponse[]>("/historico"),
    removerUltima: () => del<OperacaoHistoricoResponse>("/historico/ultima"),
  },
};
