package com.jackops.rotavital.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql(
        statements = "DELETE FROM solicitacao_sangue",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class SolicitacaoSangueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveCadastrarSolicitacaoComUrgenciaEStatusPendente() throws Exception {
        mockMvc.perform(post("/api/solicitacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json("Hospital Emergencia", "EMERGENCIA")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomeHospital").value("Hospital Emergencia"))
                .andExpect(jsonPath("$.nivelUrgencia").value("EMERGENCIA"))
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    @Test
    void deveRejeitarSolicitacaoSemUrgencia() throws Exception {
        String corpo = """
                {
                    "nomeHospital": "Hospital Teste",
                    "tipoSanguineo": "O_POSITIVO",
                    "tipoComponente": "HEMACIAS",
                    "quantidade": 1
                }
                """;

        mockMvc.perform(post("/api/solicitacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").exists());

        mockMvc.perform(get("/api/solicitacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deveListarFilaEmOrdemDeChegada() throws Exception {
        cadastrarSemRetorno("Hospital Eletiva", "ELETIVA");
        cadastrarSemRetorno("Hospital Primeira Emergencia", "EMERGENCIA");
        cadastrarSemRetorno("Hospital Urgente", "URGENTE");
        cadastrarSemRetorno("Hospital Segunda Emergencia", "EMERGENCIA");

        mockMvc.perform(get("/api/solicitacoes/fila"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].nomeHospital")
                        .value("Hospital Eletiva"))
                .andExpect(jsonPath("$[1].nomeHospital")
                        .value("Hospital Primeira Emergencia"))
                .andExpect(jsonPath("$[2].nomeHospital")
                        .value("Hospital Urgente"))
                .andExpect(jsonPath("$[3].nomeHospital")
                        .value("Hospital Segunda Emergencia"));
    }

    @Test
    void deveChamarProximaSolicitacaoDaFila() throws Exception {
        cadastrarSemRetorno("Hospital Primeiro", "ELETIVA");
        cadastrarSemRetorno("Hospital Segundo", "EMERGENCIA");

        mockMvc.perform(post("/api/solicitacoes/fila/proxima"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeHospital").value("Hospital Primeiro"))
                .andExpect(jsonPath("$.status").value("EM_ATENDIMENTO"));

        mockMvc.perform(get("/api/solicitacoes/fila"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nomeHospital").value("Hospital Segundo"));
    }

    @Test
    void deveRetornar404AoChamarProximaComFilaVazia() throws Exception {
        mockMvc.perform(post("/api/solicitacoes/fila/proxima"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Fila de solicitações vazia"));
    }

    @Test
    void deveBuscarSolicitacaoPendentePelaFila() throws Exception {
        long id = cadastrar("Hospital Busca", "URGENTE");

        mockMvc.perform(get("/api/solicitacoes/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeHospital").value("Hospital Busca"))
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    @Test
    void deveRemoverSolicitacaoPendenteDaFilaAoExcluir() throws Exception {
        long id = cadastrar("Hospital Remover", "ELETIVA");

        mockMvc.perform(delete("/api/solicitacoes/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/solicitacoes/fila"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deveRetornar404AoBuscarSolicitacaoInexistente() throws Exception {
        mockMvc.perform(get("/api/solicitacoes/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Solicitação não encontrada"));
    }

    private void cadastrarSemRetorno(String nomeHospital, String nivelUrgencia)
            throws Exception {
        mockMvc.perform(post("/api/solicitacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(nomeHospital, nivelUrgencia)))
                .andExpect(status().isCreated());
    }

    private long cadastrar(String nomeHospital, String nivelUrgencia) throws Exception {
        String location = mockMvc.perform(post("/api/solicitacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(nomeHospital, nivelUrgencia)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return Long.parseLong(location.replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    private String json(String nomeHospital, String nivelUrgencia) {
        return """
                {
                    "nomeHospital": "%s",
                    "tipoSanguineo": "O_POSITIVO",
                    "tipoComponente": "HEMACIAS",
                    "quantidade": 1,
                    "nivelUrgencia": "%s"
                }
                """.formatted(nomeHospital, nivelUrgencia);
    }
}
