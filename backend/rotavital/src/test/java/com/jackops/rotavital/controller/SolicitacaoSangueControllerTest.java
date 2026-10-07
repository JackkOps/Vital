package com.jackops.rotavital.controller;

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
        cadastrar("Hospital Eletiva", "ELETIVA");
        cadastrar("Hospital Primeira Emergencia", "EMERGENCIA");
        cadastrar("Hospital Urgente", "URGENTE");
        cadastrar("Hospital Segunda Emergencia", "EMERGENCIA");

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

    private void cadastrar(String nomeHospital, String nivelUrgencia)
            throws Exception {
        mockMvc.perform(post("/api/solicitacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(nomeHospital, nivelUrgencia)))
                .andExpect(status().isCreated());
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
