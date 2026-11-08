package com.jackops.rotavital.bdd;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.pt.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import com.jackops.rotavital.estrutura.FilaRequisicoes;
import com.jackops.rotavital.estrutura.ListaEstoque;
import com.jackops.rotavital.estrutura.PilhaHistorico;
import com.jackops.rotavital.model.Remessa;
import com.jackops.rotavital.model.enums.TipoComponente;
import com.jackops.rotavital.repository.AlertaRemessaRepository;
import com.jackops.rotavital.repository.BolsaRepository;
import com.jackops.rotavital.repository.LeituraTemperaturaRepository;
import com.jackops.rotavital.repository.LocalRedeRepository;
import com.jackops.rotavital.repository.RemessaRepository;
import com.jackops.rotavital.repository.RotaRepository;
import com.jackops.rotavital.repository.SolicitacaoSangueRepository;
import tools.jackson.databind.ObjectMapper;

public class RotaVitalSteps {
    @Autowired MockMvc mvc;
    @Autowired BolsaRepository bolsas;
    @Autowired SolicitacaoSangueRepository solicitacoes;
    @Autowired PilhaHistorico pilha;
    @Autowired FilaRequisicoes fila;
    @Autowired ListaEstoque estoque;
    @Autowired LocalRedeRepository locais;
    @Autowired RotaRepository rotas;
    @Autowired RemessaRepository remessas;
    @Autowired LeituraTemperaturaRepository leituras;
    @Autowired AlertaRemessaRepository alertasRemessa;
    private final List<Long> destinosRota = new ArrayList<>();
    private Long remessaId;
    private final ObjectMapper json = new ObjectMapper();
    private Map<String, Object> bolsa;
    private Map<String, Object> solicitacao;
    private ResultActions resposta;
    private String ultimaDescricao;

    @Before
    public void limpar() {
        alertasRemessa.deleteAll();
        leituras.deleteAll();
        remessas.deleteAll();
        rotas.deleteAll();
        destinosRota.clear();
        bolsas.deleteAll();
        solicitacoes.deleteAll();
        pilha.limpar();
        fila.limpar();
        estoque.limpar();
        bolsa = novaBolsa("BDD-001");
    }

    private Map<String, Object> novaBolsa(String codigo) {
        return new LinkedHashMap<>(Map.of("identificador", codigo, "tipoSanguineo", "O_POSITIVO",
                "tipoComponente", "HEMACIAS", "volume", 450,
                "dataColeta", "2026-10-20", "dataValidade", "2026-11-24"));
    }

    private ResultActions enviarBolsa() throws Exception {
        return mvc.perform(post("/api/bolsas").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(bolsa)));
    }

    private ResultActions enviarSolicitacao(String hospital, String tipo, int quantidade) throws Exception {
        solicitacao = new LinkedHashMap<>(Map.of("nomeHospital", hospital, "tipoSanguineo", tipo,
                "tipoComponente", "HEMACIAS", "quantidade", quantidade, "nivelUrgencia", "ELETIVA"));
        return enviarSolicitacao();
    }

    private ResultActions enviarSolicitacao() throws Exception {
        return mvc.perform(post("/api/solicitacoes").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(solicitacao)));
    }

    @Dado("que o sistema de hemocentro está ativo e pronto para receber cadastros")
    @Dado("que a fila de solicitações hospitalares está inicializada")
    @Dado("que o módulo de auditoria e histórico de operações está ativo")
    public void sistemaAtivo() { assertTrue(pilha.vazia()); }

    @Dado("que informo os dados da bolsa:")
    public void dadosBolsa(DataTable tabela) {
        Map<String, String> dados = tabela.asMap();
        bolsa.put("identificador", dados.get("codigoIdentificador"));
        bolsa.put("tipoSanguineo", dados.get("tipoSanguineo") + "_" + dados.get("fatorRh"));
        bolsa.put("tipoComponente", dados.get("tipoHemocomponente"));
        bolsa.put("volume", Integer.parseInt(dados.get("volumeMl")));
        bolsa.put("dataColeta", dados.get("dataColeta"));
        bolsa.put("dataValidade", dados.get("dataValidade"));
    }

    @Quando("submeto o cadastro da bolsa")
    public void cadastrarBolsa() throws Exception { resposta = enviarBolsa(); }

    @Então("a bolsa é persistida com status {string}")
    public void bolsaPersistida(String status) throws Exception {
        resposta.andExpect(jsonPath("$.status").value(status));
        assertEquals(1, bolsas.count());
        assertEquals(status, bolsas.findAll().getFirst().getStatus().name());
    }

    @Então("o sistema retorna código HTTP 201 Created")
    public void criada() throws Exception { resposta.andExpect(status().isCreated()); }

    @Dado("que já existe uma bolsa cadastrada com o código {string}")
    public void bolsaExistente(String codigo) throws Exception {
        bolsa = novaBolsa(codigo);
        enviarBolsa().andExpect(status().isCreated());
    }

    @Quando("tento cadastrar uma nova bolsa com o mesmo código {string}")
    public void duplicar(String codigo) throws Exception {
        bolsa = novaBolsa(codigo);
        cadastrarBolsa();
    }

    @Então("o sistema deve rejeitar o cadastro com código HTTP 409 Conflict")
    public void conflito() throws Exception {
        resposta.andExpect(status().isConflict());
        assertEquals(1, bolsas.count());
    }

    @Então("a mensagem de erro deve indicar duplicidade de identificador")
    public void mensagemDuplicidade() throws Exception {
        resposta.andExpect(jsonPath("$.mensagem").value("Bolsa já existe"));
    }

    @Dado("que informo uma bolsa com data de coleta {string} e validade {string}")
    public void datas(String coleta, String validade) {
        bolsa.put("dataColeta", coleta);
        bolsa.put("dataValidade", validade);
    }

    @Então("o sistema deve rejeitar a requisição com código HTTP 400 Bad Request")
    public void bolsaInvalida() throws Exception {
        resposta.andExpect(status().isBadRequest());
        assertEquals(0, bolsas.count());
        assertTrue(pilha.vazia());
    }

    @Então("deve retornar mensagem informando que a validade não pode ser anterior à coleta")
    public void mensagemDatas() throws Exception {
        resposta.andExpect(jsonPath("$.mensagem").value("Data de validade não pode ser anterior à data de coleta"));
    }

    @Dado("que informo uma bolsa com volumeMl menor ou igual a zero")
    public void volumeInvalido() { bolsa.put("volume", 0); }

    @Então("deve apontar violação de validação no campo volumeMl")
    public void mensagemVolume() throws Exception {
        resposta.andExpect(jsonPath("$.mensagem", containsString("Volume")));
    }

    @Dado("que o hospital {string} submete uma solicitação para {string} com {int} unidades")
    @Dado("em seguida o hospital {string} submete uma solicitação para {string} com {int} unidade")
    public void pedido(String hospital, String tipo, int quantidade) throws Exception {
        String tipoApi = tipo.replace("+", "_POSITIVO").replace("-", "_NEGATIVO");
        enviarSolicitacao(hospital, tipoApi, quantidade).andExpect(status().isCreated());
    }

    @Quando("o operador solicita a próxima solicitação a ser atendida")
    @Quando("o operador solicita a próxima solicitação")
    public void proxima() throws Exception {
        resposta = mvc.perform(post("/api/solicitacoes/fila/proxima"));
    }

    @Então("a solicitação retornada deve ser a do {string}")
    public void primeiroHospital(String hospital) throws Exception {
        resposta.andExpect(status().isOk()).andExpect(jsonPath("$.nomeHospital").value(hospital))
                .andExpect(jsonPath("$.status").value("EM_ATENDIMENTO"));
        assertEquals("EM_ATENDIMENTO", solicitacoes.findAllByOrderByIdAsc().getFirst().getStatus().name());
    }

    @Então("a solicitação do {string} deve passar a ser a primeira da fila")
    public void hospitalSeguinte(String hospital) throws Exception {
        mvc.perform(get("/api/solicitacoes/fila")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].nomeHospital").value(hospital));
    }

    @Dado("que um hospital envia solicitação com quantidadeSolicitada menor ou igual a 0")
    public void quantidadeInvalida() {
        solicitacao = new LinkedHashMap<>(Map.of("nomeHospital", "Hospital BDD", "tipoSanguineo", "O_POSITIVO",
                "tipoComponente", "HEMACIAS", "quantidade", 0, "nivelUrgencia", "ELETIVA"));
    }

    @Quando("submete a solicitação")
    public void cadastrarSolicitacao() throws Exception { resposta = enviarSolicitacao(); }

    @Então("o sistema deve recusar a solicitação com código HTTP 400 Bad Request")
    public void pedidoInvalido() throws Exception { resposta.andExpect(status().isBadRequest()); }

    @Então("nenhuma requisição deve ser adicionada à fila")
    public void filaSemPedidos() throws Exception {
        assertEquals(0, solicitacoes.count());
        mvc.perform(get("/api/solicitacoes/fila")).andExpect(jsonPath("$.length()").value(0));
    }

    @Dado("que não existem solicitações pendentes na fila")
    public void filaVazia() { assertEquals(0, solicitacoes.count()); }

    @Então("o sistema deve informar que não há solicitações para processamento com HTTP 204 No Content ou 404")
    public void semProxima() throws Exception { resposta.andExpect(status().isNotFound()); }

    @Dado("que foram executadas em sequência as ações {string} e {string}")
    public void criarEAtualizar(String criacao, String atualizacao) throws Exception {
        assertEquals("CRIACAO_BOLSA_01", criacao);
        assertEquals("ATUALIZACAO_BOLSA_01", atualizacao);
        bolsa = novaBolsa("01");
        enviarBolsa().andExpect(status().isCreated());
        long id = bolsas.findAll().getFirst().getId();
        bolsa.put("volume", 400);
        mvc.perform(put("/api/bolsas/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(bolsa))).andExpect(status().isOk());
    }

    @Quando("consulto o histórico de operações")
    public void historico() throws Exception { resposta = mvc.perform(get("/api/historico")); }

    @Então("a primeira operação listada deve ser {string} \\(topo da pilha)")
    public void topoHistorico(String operacao) throws Exception {
        assertEquals("ATUALIZACAO_BOLSA_01", operacao);
        resposta.andExpect(status().isOk()).andExpect(jsonPath("$[0].operacao").value("Atualização de bolsa 01"));
    }

    @Então("a operação seguinte deve ser {string}")
    public void segundaOperacao(String operacao) throws Exception {
        assertEquals("CRIACAO_BOLSA_01", operacao);
        resposta.andExpect(jsonPath("$[1].operacao").value("Cadastro de bolsa 01"));
        assertEquals("Atualização de bolsa 01", pilha.topo());
    }

    @Dado("que a última operação registrada na pilha foi {string}")
    public void cadastroSolicitacao(String operacao) throws Exception {
        assertEquals("CADASTRO_SOLICITACAO_10", operacao);
        enviarBolsa().andExpect(status().isCreated());
        enviarSolicitacao("Hospital BDD", "O_POSITIVO", 1).andExpect(status().isCreated());
        ultimaDescricao = "Cadastro de solicitação " + solicitacoes.findAll().getFirst().getId();
        assertEquals(ultimaDescricao, pilha.topo());
    }

    @Quando("solicito a remoção do último registro do histórico")
    @Quando("tento remover a última operação")
    public void removerUltima() throws Exception { resposta = mvc.perform(delete("/api/historico/ultima")); }

    @Então("a operação {string} é desempilhada")
    public void desempilhada(String operacao) throws Exception {
        assertEquals("CADASTRO_SOLICITACAO_10", operacao);
        resposta.andExpect(status().isOk()).andExpect(jsonPath("$.operacao").value(ultimaDescricao));
    }

    @Então("o cadastro da solicitação permanece no banco de dados")
    public void cadastroPreservado() {
        assertEquals(1, solicitacoes.count());
        assertEquals("Hospital BDD", solicitacoes.findAll().getFirst().getNomeHospital());
    }

    @Então("a operação anterior passa a ocupar o topo da pilha")
    public void topoAnterior() throws Exception {
        mvc.perform(get("/api/historico")).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].operacao").value("Cadastro de bolsa BDD-001"));
    }

    @Dado("que o histórico de operações está vazio")
    public void historicoVazio() { assertTrue(pilha.vazia()); }

    @Então("o sistema deve retornar status informando que não há registros a serem removidos")
    public void semOperacoes() throws Exception {
        resposta.andExpect(status().isNotFound()).andExpect(jsonPath("$.mensagem").value("Histórico vazio"));
    }

    /* ---------- História 4: prioridade por urgência, limite e cancelamento ---------- */

    private ResultActions enviarSolicitacaoComUrgencia(String hospital, String urgencia) throws Exception {
        solicitacao = new LinkedHashMap<>(Map.of("nomeHospital", hospital, "tipoSanguineo", "O_POSITIVO",
                "tipoComponente", "HEMACIAS", "quantidade", 1, "nivelUrgencia", urgencia));
        return enviarSolicitacao();
    }

    @Dado("que existem as solicitações eletivas pendentes dos hospitais {string} e {string}")
    public void eletivasPendentes(String primeiro, String segundo) throws Exception {
        enviarSolicitacaoComUrgencia(primeiro, "ELETIVA").andExpect(status().isCreated());
        enviarSolicitacaoComUrgencia(segundo, "ELETIVA").andExpect(status().isCreated());
    }

    @Quando("o hospital {string} registra uma solicitação com urgência {string}")
    public void registrarComUrgencia(String hospital, String urgencia) throws Exception {
        enviarSolicitacaoComUrgencia(hospital, urgencia).andExpect(status().isCreated());
    }

    @Então("a fila de atendimento fica na ordem {string}, {string}, {string}")
    public void ordemDaFila(String primeiro, String segundo, String terceiro) throws Exception {
        mvc.perform(get("/api/solicitacoes/fila")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].nomeHospital").value(primeiro))
                .andExpect(jsonPath("$[1].nomeHospital").value(segundo))
                .andExpect(jsonPath("$[2].nomeHospital").value(terceiro));
    }

    @Dado("que um hospital envia solicitação com {int} bolsas")
    public void quantidadeAcimaDoLimite(int quantidade) {
        solicitacao = new LinkedHashMap<>(Map.of("nomeHospital", "Hospital BDD", "tipoSanguineo", "O_POSITIVO",
                "tipoComponente", "HEMACIAS", "quantidade", quantidade, "nivelUrgencia", "ELETIVA"));
    }

    @Dado("que existe uma solicitação em atendimento com uma bolsa alocada")
    public void solicitacaoComBolsaAlocada() throws Exception {
        bolsa.put("dataColeta", LocalDate.now().minusDays(5).toString());
        bolsa.put("dataValidade", LocalDate.now().plusDays(20).toString());
        enviarBolsa().andExpect(status().isCreated());
        enviarSolicitacao("Hospital BDD", "O_POSITIVO", 2).andExpect(status().isCreated());
        long id = solicitacoes.findAll().getFirst().getId();
        mvc.perform(post("/api/alocacoes/solicitacoes/{id}/alocar", id)).andExpect(status().isOk());
    }

    @Quando("o hospital cancela a solicitação")
    public void cancelarSolicitacao() throws Exception {
        long id = solicitacoes.findAll().getFirst().getId();
        resposta = mvc.perform(post("/api/solicitacoes/{id}/cancelar", id));
    }

    @Então("a solicitação passa a ter status {string}")
    public void statusSolicitacao(String status) throws Exception {
        resposta.andExpect(status().isOk()).andExpect(jsonPath("$.status").value(status));
        assertEquals(status, solicitacoes.findAll().getFirst().getStatus().name());
    }

    @Então("a bolsa alocada volta a ter status {string}")
    public void bolsaDevolvida(String status) {
        var devolvida = bolsas.findAll().getFirst();
        assertEquals(status, devolvida.getStatus().name());
        assertNull(devolvida.getSolicitacao());
    }

    /* ---------- História 7: painel de indicadores ---------- */

    @Dado("que não houve movimentação entre {string} e {string}")
    public void semMovimentacao(String inicio, String fim) {
        assertEquals(0, bolsas.count());
        assertEquals(0, solicitacoes.count());
    }

    @Quando("acesso o painel de indicadores do período de {string} a {string}")
    public void painelDoPeriodo(String inicio, String fim) throws Exception {
        resposta = mvc.perform(get("/api/indicadores").param("dataInicio", inicio).param("dataFim", fim));
    }

    @Quando("acesso o painel de indicadores")
    public void painel() throws Exception {
        resposta = mvc.perform(get("/api/indicadores"));
    }

    @Então("o painel exibe a mensagem {string}")
    public void mensagemPainel(String mensagem) throws Exception {
        resposta.andExpect(status().isOk())
                .andExpect(jsonPath("$.dadosSuficientes").value(false))
                .andExpect(jsonPath("$.mensagem").value(mensagem));
    }

    @Então("nenhum indicador é calculado")
    public void semIndicadores() throws Exception {
        resposta.andExpect(jsonPath("$.estoqueDisponivel.length()").value(0))
                .andExpect(jsonPath("$.situacaoComponentes.length()").value(0))
                .andExpect(jsonPath("$.taxaDescarteVencimento").doesNotExist())
                .andExpect(jsonPath("$.tempoMedioAtendimentoHoras").doesNotExist());
    }

    @Dado("que há {int} bolsas de {string} disponíveis e uma solicitação em aberto de {int} bolsas")
    public void estoqueBaixo(int quantidadeBolsas, String componente, int pedidas) throws Exception {
        for (int i = 1; i <= quantidadeBolsas; i++) {
            bolsa = novaBolsa("BDD-CRIT-" + i);
            bolsa.put("tipoComponente", componente);
            bolsa.put("dataColeta", LocalDate.now().minusDays(2).toString());
            bolsa.put("dataValidade", LocalDate.now().plusDays(20).toString());
            enviarBolsa().andExpect(status().isCreated());
        }
        enviarSolicitacao("Hospital BDD", "O_POSITIVO", pedidas).andExpect(status().isCreated());
    }

    @Então("o componente {string} aparece como estoque crítico")
    public void componenteCritico(String componente) throws Exception {
        resposta.andExpect(status().isOk())
                .andExpect(jsonPath("$.situacaoComponentes[?(@.tipoComponente == '" + componente + "')].estoqueCritico")
                        .value(true));
    }

    @Então("a probabilidade de desabastecimento de {string} é apresentada")
    public void probabilidadeApresentada(String componente) throws Exception {
        resposta.andExpect(jsonPath("$.situacaoComponentes[?(@.tipoComponente == '" + componente
                + "')].probabilidadeDesabastecimento").value(1.0));
    }

    /* ---------- História 3: cálculo de rota ---------- */

    private long localPorNome(String nome) {
        return locais.findByNomeIgnoreCase(nome).orElseThrow().getId();
    }

    @Dado("que existem entregas pendentes para {string} e {string}")
    public void entregasPendentes(String primeiro, String segundo) {
        destinosRota.add(localPorNome(primeiro));
        destinosRota.add(localPorNome(segundo));
    }

    @Dado("que existem entregas pendentes para {string}")
    public void entregaPendente(String hospital) {
        destinosRota.add(localPorNome(hospital));
    }

    @Quando("o sistema calcula a rota a partir do {string}")
    public void calcularRota(String origem) throws Exception {
        resposta = mvc.perform(post("/api/rotas").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("origemId", localPorNome(origem), "destinoIds", destinosRota))));
    }

    @Então("o caminho de menor custo é retornado em {int} minutos")
    public void caminhoDeMenorCusto(int minutos) throws Exception {
        resposta.andExpect(status().isCreated()).andExpect(jsonPath("$.tempoTotalMinutos").value(minutos));
    }

    @Então("a rota cumpre a janela de tempo exigida")
    public void cumpreJanela() throws Exception {
        resposta.andExpect(jsonPath("$.cumpreJanela").value(true));
    }

    @Então("a rota é confirmada automaticamente")
    public void confirmadaAutomaticamente() throws Exception {
        resposta.andExpect(jsonPath("$.status").value("CONFIRMADA"));
    }

    @Então("o sistema alerta que não há rota disponível")
    public void semRota() throws Exception {
        resposta.andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.mensagem", containsString("Não há rota disponível")));
        assertEquals(0, rotas.count());
    }

    @Então("o operador é alertado sobre o risco de atraso")
    public void riscoDeAtraso() throws Exception {
        resposta.andExpect(status().isCreated()).andExpect(jsonPath("$.cumpreJanela").value(false))
                .andExpect(jsonPath("$.alerta", containsString("Risco de atraso")));
    }

    @Então("a rota não é confirmada automaticamente")
    public void naoConfirmada() throws Exception {
        resposta.andExpect(jsonPath("$.status").value("AGUARDANDO_CONFIRMACAO"));
    }

    /* ---------- História 6: cadeia fria ---------- */

    @Dado("que existe uma remessa de {string} em trânsito com sensor ativo")
    public void remessaEmTransito(String componente) throws Exception {
        String corpo = mvc.perform(post("/api/remessas").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("tipoComponente", componente, "sensorId", "SENSOR-BDD"))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        remessaId = json.readTree(corpo).get("id").asLong();
    }

    @Dado("que existe uma remessa de {string} em trânsito sem leitura há {int} minutos")
    public void remessaSemLeitura(String componente, int minutos) {
        Remessa remessa = new Remessa(null, TipoComponente.valueOf(componente), "SENSOR-BDD",
                LocalDateTime.now().minusMinutes(minutos));
        remessaId = remessas.save(remessa).getId();
    }

    @Quando("o sistema recebe uma leitura de {double} graus")
    public void receberLeitura(double temperatura) throws Exception {
        resposta = mvc.perform(post("/api/remessas/{id}/leituras", remessaId).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("leituraId", "BDD-1", "sensorId", "SENSOR-BDD",
                        "sequencia", 1, "temperatura", temperatura, "dataHora", LocalDateTime.now().withNano(0).toString()))));
    }

    @Então("a leitura é registrada no histórico da remessa")
    public void leituraNoHistorico() throws Exception {
        resposta.andExpect(status().isCreated());
        mvc.perform(get("/api/remessas/{id}/leituras", remessaId)).andExpect(jsonPath("$.length()").value(1));
    }

    @Então("a remessa permanece com condição {string}")
    public void condicaoRemessa(String condicao) throws Exception {
        mvc.perform(get("/api/remessas/{id}", remessaId)).andExpect(jsonPath("$.condicao").value(condicao));
    }

    @Então("o operador recebe um alerta {string}")
    public void alertaRemessa(String tipo) throws Exception {
        mvc.perform(get("/api/remessas/{id}", remessaId))
                .andExpect(jsonPath("$.alertas.length()").value(1))
                .andExpect(jsonPath("$.alertas[0].tipo").value(tipo));
    }

    @Quando("o sistema verifica os sensores")
    public void verificarSensores() throws Exception {
        resposta = mvc.perform(post("/api/remessas/verificar-sensores"));
    }

    @Então("o sistema registra falha de comunicação")
    public void falhaDeComunicacao() throws Exception {
        resposta.andExpect(status().isOk()).andExpect(jsonPath("$.alertasGerados.length()").value(1));
        mvc.perform(get("/api/remessas/{id}", remessaId)).andExpect(jsonPath("$.sensorIndisponivel").value(true));
    }
}
