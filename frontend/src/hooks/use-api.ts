import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api, ApiError } from "@/api/client";
import type {
  AtualizacaoBolsaRequest,
  AtualizacaoSolicitacaoRequest,
  CadastroBolsaRequest,
  CadastroRemessaRequest,
  CadastroSolicitacaoRequest,
  CalculoRotaRequest,
  FiltroIndicadores,
} from "@/api/types";
import { useToast } from "@/hooks/use-toast";

function mensagemDeErro(erro: unknown): string {
  if (erro instanceof ApiError) return erro.message;
  return "Não foi possível completar a operação. Tente novamente.";
}

// ---------- Estoque ----------

export function useBolsas() {
  return useQuery({ queryKey: ["bolsas"], queryFn: api.bolsas.listar });
}

export function useCadastrarBolsa() {
  const qc = useQueryClient();
  const { toast } = useToast();
  return useMutation({
    mutationFn: (body: CadastroBolsaRequest) => api.bolsas.cadastrar(body),
    onSuccess: (bolsa) => {
      qc.invalidateQueries({ queryKey: ["bolsas"] });
      qc.invalidateQueries({ queryKey: ["historico"] });
      toast({ title: "Bolsa cadastrada", description: bolsa.identificador, tone: "ok" });
    },
    onError: (erro) => toast({ title: "Não foi possível cadastrar", description: mensagemDeErro(erro), tone: "erro" }),
  });
}

export function useAtualizarBolsa() {
  const qc = useQueryClient();
  const { toast } = useToast();
  return useMutation({
    mutationFn: ({ id, body }: { id: number; body: AtualizacaoBolsaRequest }) =>
      api.bolsas.atualizar(id, body),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["bolsas"] });
      toast({ title: "Bolsa atualizada", tone: "ok" });
    },
    onError: (erro) => toast({ title: "Não foi possível atualizar", description: mensagemDeErro(erro), tone: "erro" }),
  });
}

export function useExcluirBolsa() {
  const qc = useQueryClient();
  const { toast } = useToast();
  return useMutation({
    mutationFn: (id: number) => api.bolsas.excluir(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["bolsas"] });
      toast({ title: "Bolsa removida", tone: "info" });
    },
    onError: (erro) => toast({ title: "Não foi possível remover", description: mensagemDeErro(erro), tone: "erro" }),
  });
}

// ---------- Solicitações ----------

export function useSolicitacoes() {
  return useQuery({ queryKey: ["solicitacoes"], queryFn: api.solicitacoes.listar });
}

export function useFilaSolicitacoes() {
  return useQuery({ queryKey: ["solicitacoes", "fila"], queryFn: api.solicitacoes.fila });
}

export function useCadastrarSolicitacao() {
  const qc = useQueryClient();
  const { toast } = useToast();
  return useMutation({
    mutationFn: (body: CadastroSolicitacaoRequest) => api.solicitacoes.cadastrar(body),
    onSuccess: (solicitacao) => {
      qc.invalidateQueries({ queryKey: ["solicitacoes"] });
      qc.invalidateQueries({ queryKey: ["historico"] });
      toast({
        title: "Solicitação registrada",
        description: solicitacao.nomeHospital,
        tone: "ok",
      });
    },
    onError: (erro) => toast({ title: "Não foi possível registrar", description: mensagemDeErro(erro), tone: "erro" }),
  });
}

export function useAtualizarSolicitacao() {
  const qc = useQueryClient();
  const { toast } = useToast();
  return useMutation({
    mutationFn: ({ id, body }: { id: number; body: AtualizacaoSolicitacaoRequest }) =>
      api.solicitacoes.atualizar(id, body),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["solicitacoes"] });
      toast({ title: "Solicitação atualizada", tone: "ok" });
    },
    onError: (erro) => toast({ title: "Não foi possível atualizar", description: mensagemDeErro(erro), tone: "erro" }),
  });
}

export function useCancelarSolicitacao() {
  const qc = useQueryClient();
  const { toast } = useToast();
  return useMutation({
    mutationFn: (id: number) => api.solicitacoes.cancelar(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["solicitacoes"] });
      toast({ title: "Solicitação cancelada", tone: "info" });
    },
    onError: (erro) => toast({ title: "Não foi possível cancelar", description: mensagemDeErro(erro), tone: "erro" }),
  });
}

export function useExcluirSolicitacao() {
  const qc = useQueryClient();
  const { toast } = useToast();
  return useMutation({
    mutationFn: (id: number) => api.solicitacoes.excluir(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["solicitacoes"] });
      toast({ title: "Solicitação removida", tone: "info" });
    },
    onError: (erro) => toast({ title: "Não foi possível remover", description: mensagemDeErro(erro), tone: "erro" }),
  });
}

export function useBolsasAlocadas(solicitacaoId: number) {
  return useQuery({
    queryKey: ["alocacoes", solicitacaoId],
    queryFn: () => api.alocacoes.listarPorSolicitacao(solicitacaoId),
  });
}

export function useChamarProximaDaFila() {
  const qc = useQueryClient();
  const { toast } = useToast();
  return useMutation({
    mutationFn: api.solicitacoes.chamarProxima,
    onSuccess: (solicitacao) => {
      qc.invalidateQueries({ queryKey: ["solicitacoes"] });
      qc.invalidateQueries({ queryKey: ["historico"] });
      toast({
        title: "Chamada da fila",
        description: `${solicitacao.nomeHospital} está em atendimento`,
        tone: "info",
      });
    },
    onError: (erro) => toast({ title: "Não foi possível chamar a próxima", description: mensagemDeErro(erro), tone: "erro" }),
  });
}

export function useCancelarAlocacao() {
  const qc = useQueryClient();
  const { toast } = useToast();
  return useMutation({
    mutationFn: (bolsaId: number) => api.alocacoes.cancelar(bolsaId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["solicitacoes"] });
      qc.invalidateQueries({ queryKey: ["bolsas"] });
      qc.invalidateQueries({ queryKey: ["alocacoes"] });
      toast({ title: "Alocação cancelada", tone: "info" });
    },
    onError: (erro) => toast({ title: "Não foi possível cancelar a alocação", description: mensagemDeErro(erro), tone: "erro" }),
  });
}

export function useAlocarBolsa() {
  const qc = useQueryClient();
  const { toast } = useToast();
  return useMutation({
    mutationFn: (solicitacaoId: number) => api.alocacoes.alocar(solicitacaoId),
    onSuccess: (resposta) => {
      qc.invalidateQueries({ queryKey: ["solicitacoes"] });
      qc.invalidateQueries({ queryKey: ["bolsas"] });
      qc.invalidateQueries({ queryKey: ["historico"] });
      toast({
        title: resposta.alocada ? "Bolsa alocada" : "Sem bolsa compatível",
        description: resposta.mensagem,
        tone: resposta.alocada ? "ok" : "erro",
      });
    },
    onError: (erro) => toast({ title: "Não foi possível alocar", description: mensagemDeErro(erro), tone: "erro" }),
  });
}

// ---------- Validade ----------

export function useAlertasValidade() {
  return useQuery({ queryKey: ["validade", "alertas"], queryFn: api.validade.alertas });
}

export function useBaixas() {
  return useQuery({ queryKey: ["validade", "baixas"], queryFn: api.validade.baixas });
}

export function useVerificarValidade() {
  const qc = useQueryClient();
  const { toast } = useToast();
  return useMutation({
    mutationFn: api.validade.verificar,
    onSuccess: (resultado) => {
      qc.invalidateQueries({ queryKey: ["bolsas"] });
      qc.invalidateQueries({ queryKey: ["validade"] });
      qc.invalidateQueries({ queryKey: ["historico"] });
      toast({
        title: "Verificação concluída",
        description: `${resultado.baixasRealizadas.length} bolsa(s) baixada(s) por vencimento`,
        tone: resultado.baixasRealizadas.length ? "info" : "ok",
      });
    },
    onError: (erro) => toast({ title: "Não foi possível verificar", description: mensagemDeErro(erro), tone: "erro" }),
  });
}

// ---------- Logística: rotas ----------

export function useRedeDistribuicao() {
  return useQuery({ queryKey: ["rotas", "rede"], queryFn: api.rotas.rede });
}

export function useRotas() {
  return useQuery({ queryKey: ["rotas"], queryFn: api.rotas.listar });
}

export function useCalcularRota() {
  const qc = useQueryClient();
  const { toast } = useToast();
  return useMutation({
    mutationFn: (body: CalculoRotaRequest) => api.rotas.calcular(body),
    onSuccess: (rota) => {
      qc.invalidateQueries({ queryKey: ["rotas"] });
      toast({
        title: "Rota calculada",
        description: `${rota.tempoTotalMinutos} min · ${rota.distanciaTotalKm.toFixed(1)} km`,
        tone: rota.cumpreJanela ? "ok" : "info",
      });
    },
    onError: (erro) => toast({ title: "Não foi possível calcular a rota", description: mensagemDeErro(erro), tone: "erro" }),
  });
}

export function useConfirmarRota() {
  const qc = useQueryClient();
  const { toast } = useToast();
  return useMutation({
    mutationFn: (id: number) => api.rotas.confirmar(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["rotas"] });
      toast({ title: "Rota confirmada", tone: "ok" });
    },
    onError: (erro) => toast({ title: "Não foi possível confirmar", description: mensagemDeErro(erro), tone: "erro" }),
  });
}

// ---------- Logística: remessas ----------

export function useRemessas() {
  return useQuery({ queryKey: ["remessas"], queryFn: api.remessas.listar });
}

export function useDespacharRemessa() {
  const qc = useQueryClient();
  const { toast } = useToast();
  return useMutation({
    mutationFn: (body: CadastroRemessaRequest) => api.remessas.despachar(body),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["remessas"] });
      toast({ title: "Remessa despachada", tone: "ok" });
    },
    onError: (erro) => toast({ title: "Não foi possível despachar", description: mensagemDeErro(erro), tone: "erro" }),
  });
}

export function useVerificarSensores() {
  const qc = useQueryClient();
  const { toast } = useToast();
  return useMutation({
    mutationFn: api.remessas.verificarSensores,
    onSuccess: (resultado) => {
      qc.invalidateQueries({ queryKey: ["remessas"] });
      toast({
        title: "Sensores verificados",
        description: `${resultado.alertasGerados.length} alerta(s) novo(s)`,
        tone: resultado.alertasGerados.length ? "erro" : "ok",
      });
    },
    onError: (erro) => toast({ title: "Não foi possível verificar", description: mensagemDeErro(erro), tone: "erro" }),
  });
}

export function useRegistrarLeitura() {
  const qc = useQueryClient();
  const { toast } = useToast();
  return useMutation({
    mutationFn: ({
      id,
      sensorId,
      sequencia,
      temperatura,
    }: {
      id: number;
      sensorId: string;
      sequencia: number;
      temperatura: number;
    }) => {
      const agora = new Date();
      const local = new Date(agora.getTime() - agora.getTimezoneOffset() * 60000);
      return api.remessas.registrarLeitura(id, {
        leituraId: `WEB-${id}-${Date.now()}`,
        sensorId,
        sequencia,
        temperatura,
        dataHora: local.toISOString().slice(0, 19),
      });
    },
    onSuccess: (resultado) => {
      qc.invalidateQueries({ queryKey: ["remessas"] });
      const alertas = resultado.alertasGerados ?? [];
      toast({
        title: alertas.length ? "Leitura fora da faixa" : "Leitura registrada",
        description: alertas.length
          ? alertas.map((a) => a.mensagem).join(" · ")
          : `${resultado.leitura.temperatura.toLocaleString("pt-BR")} °C`,
        tone: alertas.length ? "erro" : "ok",
      });
    },
    onError: (erro) => toast({ title: "Não foi possível registrar a leitura", description: mensagemDeErro(erro), tone: "erro" }),
  });
}

export function useEntregarRemessa() {
  const qc = useQueryClient();
  const { toast } = useToast();
  return useMutation({
    mutationFn: (id: number) => api.remessas.entregar(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["remessas"] });
      toast({ title: "Remessa entregue", tone: "ok" });
    },
    onError: (erro) => toast({ title: "Não foi possível confirmar entrega", description: mensagemDeErro(erro), tone: "erro" }),
  });
}

// ---------- Indicadores ----------

export function useIndicadores(filtro: FiltroIndicadores) {
  return useQuery({
    queryKey: ["indicadores", filtro],
    queryFn: () => api.indicadores.consultar(filtro),
  });
}

// ---------- Histórico ----------

export function useHistorico() {
  return useQuery({ queryKey: ["historico"], queryFn: api.historico.listar });
}

export function useRemoverUltimoHistorico() {
  const qc = useQueryClient();
  const { toast } = useToast();
  return useMutation({
    mutationFn: api.historico.removerUltima,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["historico"] });
      toast({ title: "Registro removido do histórico", tone: "info" });
    },
    onError: (erro) => toast({ title: "Não foi possível remover", description: mensagemDeErro(erro), tone: "erro" }),
  });
}
