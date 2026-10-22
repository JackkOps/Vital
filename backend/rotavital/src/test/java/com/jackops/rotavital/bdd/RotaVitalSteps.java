package com.jackops.rotavital.bdd;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import com.jackops.rotavital.repository.BolsaRepository;
import com.jackops.rotavital.repository.SolicitacaoSangueRepository;
import tools.jackson.databind.ObjectMapper;

public class RotaVitalSteps {
    @Autowired MockMvc mvc;
    @Autowired BolsaRepository bolsas;
    @Autowired SolicitacaoSangueRepository solicitacoes;
    @Autowired PilhaHistorico pilha;
    @Autowired FilaRequisicoes fila;
    @Autowired ListaEstoque estoque;
    private final ObjectMapper json = new ObjectMapper();
    private Map<String, Object> bolsa;
    private Map<String, Object> solicitacao;
    private ResultActions resposta;
    private String ultimaDescricao;

    @Before
    public void limpar() {
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
}
