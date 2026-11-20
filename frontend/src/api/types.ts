// Tipos espelhando os DTOs/enums do backend (com.jackops.rotavital).
// Mantidos em sincronia manual com backend/rotavital/src/main/java/.../dto e .../model/enums.

export type TipoSanguineo =
  | "A_POSITIVO"
  | "A_NEGATIVO"
  | "B_POSITIVO"
  | "B_NEGATIVO"
  | "AB_POSITIVO"
  | "AB_NEGATIVO"
  | "O_POSITIVO"
  | "O_NEGATIVO";

export const TIPOS_SANGUINEOS: TipoSanguineo[] = [
  "A_POSITIVO",
  "A_NEGATIVO",
  "B_POSITIVO",
  "B_NEGATIVO",
  "AB_POSITIVO",
  "AB_NEGATIVO",
  "O_POSITIVO",
  "O_NEGATIVO",
];

export const DESCRICAO_TIPO_SANGUINEO: Record<TipoSanguineo, string> = {
  A_POSITIVO: "A+",
  A_NEGATIVO: "A-",
  B_POSITIVO: "B+",
  B_NEGATIVO: "B-",
  AB_POSITIVO: "AB+",
  AB_NEGATIVO: "AB-",
  O_POSITIVO: "O+",
  O_NEGATIVO: "O-",
};

export type TipoComponente = "HEMACIAS" | "PLASMA" | "PLAQUETAS" | "SANGUE_TOTAL";

export const TIPOS_COMPONENTE: TipoComponente[] = [
  "HEMACIAS",
  "PLASMA",
  "PLAQUETAS",
  "SANGUE_TOTAL",
];

export const DESCRICAO_COMPONENTE: Record<TipoComponente, string> = {
  HEMACIAS: "Hemácias",
  PLASMA: "Plasma",
  PLAQUETAS: "Plaquetas",
  SANGUE_TOTAL: "Sangue total",
};

export type StatusBolsa = "DISPONIVEL" | "ALOCADA" | "INDISPONIVEL" | "DESCARTADA";

export type NivelUrgencia = "ELETIVA" | "URGENTE" | "EMERGENCIA";

export const NIVEIS_URGENCIA: NivelUrgencia[] = ["EMERGENCIA", "URGENTE", "ELETIVA"];

export const DESCRICAO_URGENCIA: Record<NivelUrgencia, string> = {
  EMERGENCIA: "Emergência",
  URGENTE: "Urgente",
  ELETIVA: "Eletiva",
};

export type StatusSolicitacao = "PENDENTE" | "EM_ATENDIMENTO" | "ATENDIDA" | "CANCELADA";

export type StatusRota = "CONFIRMADA" | "AGUARDANDO_CONFIRMACAO";

export type StatusRemessa = "EM_TRANSITO" | "ENTREGUE";

export type CondicaoRemessa = "CONFORME" | "SOB_RISCO";

export type MotivoBaixa = "VENCIMENTO";

export type TipoAlertaRemessa =
  | "EXCURSAO_TERMICA"
  | "SENSOR_INDISPONIVEL"
  | "LEITURAS_PERDIDAS";

export type TipoLocal = "HEMOCENTRO" | "HOSPITAL";

// ---------- Estoque (bolsas) ----------

export interface BolsaResponse {
  id: number;
  identificador: string;
  tipoSanguineo: TipoSanguineo;
  tipoComponente: TipoComponente;
  dataColeta: string;
  dataValidade: string;
  volume: number;
  status: StatusBolsa;
  solicitacaoId: number | null;
}

export interface CadastroBolsaRequest {
  identificador: string;
  tipoSanguineo: TipoSanguineo;
  tipoComponente: TipoComponente;
  dataColeta: string;
  dataValidade: string;
  volume: number;
}

export type AtualizacaoBolsaRequest = CadastroBolsaRequest;

// ---------- Solicitações ----------

export interface SolicitacaoResponse {
  id: number;
  nomeHospital: string;
  tipoSanguineo: TipoSanguineo;
  tipoComponente: TipoComponente;
  quantidade: number;
  nivelUrgencia: NivelUrgencia;
  status: StatusSolicitacao;
  bolsasAlocadas: number;
}

export interface CadastroSolicitacaoRequest {
  nomeHospital: string;
  tipoSanguineo: TipoSanguineo;
  tipoComponente: TipoComponente;
  quantidade: number;
  nivelUrgencia: NivelUrgencia;
}

export interface AtualizacaoSolicitacaoRequest {
  nomeHospital: string;
  tipoSanguineo: TipoSanguineo;
  tipoComponente: TipoComponente;
  quantidade: number;
}

export interface RespostaAlocacaoBolsa {
  bolsaId: number | null;
  alocada: boolean;
  mensagem: string;
  bolsasAlocadas: number;
  statusSolicitacao: StatusSolicitacao;
}

// ---------- Validade ----------

export interface AlertaValidadeResponse {
  bolsaId: number;
  identificador: string;
  tipoSanguineo: TipoSanguineo;
  tipoComponente: TipoComponente;
  dataValidade: string;
  diasParaVencer: number;
}

export interface BaixaBolsaResponse {
  id: number;
  bolsaId: number;
  identificadorBolsa: string;
  motivo: MotivoBaixa;
  dataBaixa: string;
  automatica: boolean;
}

export interface VerificacaoValidadeResponse {
  dataReferencia: string;
  baixasRealizadas: BaixaBolsaResponse[];
  alertas: AlertaValidadeResponse[];
}

// ---------- Logística: rede e rotas ----------

export interface LocalRedeResponse {
  id: number;
  nome: string;
  tipo: TipoLocal;
}

export interface TrechoResponse {
  id: number;
  origemId: number;
  destinoId: number;
  tempoMinutos: number;
  distanciaKm: number;
}

export interface RedeDistribuicaoResponse {
  locais: LocalRedeResponse[];
  trechos: TrechoResponse[];
}

export interface EntregaRotaResponse {
  localId: number;
  nome: string;
  chegadaMinutos: number;
  dentroDaJanela: boolean;
}

export interface RotaResponse {
  id: number;
  origem: LocalRedeResponse;
  caminho: LocalRedeResponse[];
  entregas: EntregaRotaResponse[];
  tempoTotalMinutos: number;
  distanciaTotalKm: number;
  janelaMinutos: number;
  cumpreJanela: boolean;
  status: StatusRota;
  alerta: string | null;
  criadaEm: string;
}

export interface CalculoRotaRequest {
  origemId: number;
  destinoIds?: number[];
  solicitacaoIds?: number[];
  janelaMinutos?: number;
}

// ---------- Logística: remessas / cadeia fria ----------

export interface AlertaRemessaResponse {
  id: number;
  remessaId: number;
  tipo: TipoAlertaRemessa;
  mensagem: string;
  criadoEm: string;
}

export interface RemessaResponse {
  id: number;
  rotaId: number | null;
  tipoComponente: TipoComponente;
  temperaturaMinima: number;
  temperaturaMaxima: number;
  sensorId: string;
  status: StatusRemessa;
  condicao: CondicaoRemessa;
  sensorIndisponivel: boolean;
  iniciadaEm: string;
  ultimaLeituraEm: string | null;
  ultimaTemperatura: number | null;
  totalLeituras: number;
  entregueEm: string | null;
  alertas: AlertaRemessaResponse[];
}

export interface CadastroRemessaRequest {
  rotaId?: number | null;
  tipoComponente: TipoComponente;
  sensorId: string;
}

export interface LeituraTemperaturaResponse {
  id: number;
  leituraId: string;
  sequencia: number;
  temperatura: number;
  dataHora: string;
  recebidaEm: string;
  dentroDaFaixa: boolean;
}

export interface ResultadoLeituraResponse {
  duplicada: boolean;
  leitura: LeituraTemperaturaResponse;
  condicaoRemessa: CondicaoRemessa;
  alertasGerados: AlertaRemessaResponse[];
}

export interface VerificacaoSensoresResponse {
  verificadoEm: string;
  alertasGerados: AlertaRemessaResponse[];
}

// ---------- Indicadores ----------

export interface EstoquePorTipoResponse {
  tipoComponente: TipoComponente;
  tipoSanguineo: TipoSanguineo;
  quantidadeBolsasDisponiveis: number;
}

export interface DemandaPorHospitalResponse {
  nomeHospital: string;
  tipoComponente: TipoComponente;
  tipoSanguineo: TipoSanguineo;
  quantidadeBolsasSolicitadasEmAberto: number;
}

export interface SituacaoComponenteResponse {
  tipoComponente: TipoComponente;
  bolsasDisponiveis: number;
  demandaEmAberto: number;
  nivelCritico: number;
  estoqueCritico: boolean;
  probabilidadeDesabastecimento: number;
}

export interface IndicadoresResponse {
  dataReferencia: string;
  periodoInicio: string;
  periodoFim: string;
  dadosSuficientes: boolean;
  mensagem: string | null;
  descricaoEstoque: string | null;
  descricaoDemanda: string | null;
  estoqueDisponivel: EstoquePorTipoResponse[];
  demandaEmAberto: DemandaPorHospitalResponse[];
  taxaDescarteVencimento: number | null;
  tempoMedioAtendimentoHoras: number | null;
  horizonteDesabastecimentoDias: number;
  situacaoComponentes: SituacaoComponenteResponse[];
}

export interface FiltroIndicadores {
  dataInicio?: string;
  dataFim?: string;
  hospital?: string;
  tipoComponente?: TipoComponente;
}

// ---------- Histórico ----------

export interface OperacaoHistoricoResponse {
  operacao: string;
}
