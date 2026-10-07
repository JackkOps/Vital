package com.jackops.rotavital.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(statements = "DELETE FROM bolsa", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class BolsaControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveCadastrarBolsaComStatusDisponivel() throws Exception {
        mockMvc.perform(post("/api/bolsas").contentType(MediaType.APPLICATION_JSON).content(json("BOLSA-2026-0001", "2026-09-01", "2026-10-01", 450)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.identificador").value("BOLSA-2026-0001"))
                .andExpect(jsonPath("$.status").value("DISPONIVEL"));
    }

    @Test
    void deveBloquearIdentificadorDuplicado() throws Exception {
        String corpo = json("BOLSA-2026-0001", "2026-09-01", "2026-10-01", 450);
        mockMvc.perform(post("/api/bolsas").contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().isCreated());
        mockMvc.perform(post("/api/bolsas").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.mensagem").value("Bolsa já existe"));
    }

    @Test
    void deveBloquearValidadeAnteriorAColeta() throws Exception {
        mockMvc.perform(post("/api/bolsas").contentType(MediaType.APPLICATION_JSON).content(json("BOLSA-2026-0002", "2026-10-01", "2026-09-01", 450)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("Data de validade não pode ser anterior à data de coleta"));
    }

    @Test
    void deveValidarCampoObrigatorioEVolume() throws Exception {
        mockMvc.perform(post("/api/bolsas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identificador\":\"\",\"tipoComponente\":\"HEMACIAS\",\"dataColeta\":\"2026-09-01\",\"dataValidade\":\"2026-10-01\",\"volume\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").exists());
    }

    @Test
    void deveExibirBolsaNoEstoque() throws Exception {
        mockMvc.perform(post("/api/bolsas").contentType(MediaType.APPLICATION_JSON).content(json("BOLSA-2026-0003", "2026-09-01", "2026-10-01", 450))).andExpect(status().isCreated());
        mockMvc.perform(get("/api/bolsas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].identificador").value("BOLSA-2026-0003"));
    }

    private String json(String identificador, String coleta, String validade, int volume) {
        return "{\"identificador\":\"%s\",\"tipoSanguineo\":\"O_POSITIVO\",\"tipoComponente\":\"HEMACIAS\",\"dataColeta\":\"%s\",\"dataValidade\":\"%s\",\"volume\":%d}"
                .formatted(identificador, coleta, validade, volume);
    }
}
