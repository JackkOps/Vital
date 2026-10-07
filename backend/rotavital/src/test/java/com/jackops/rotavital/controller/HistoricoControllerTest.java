package com.jackops.rotavital.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import com.jackops.rotavital.estrutura.PilhaHistorico;

@SpringBootTest
@AutoConfigureMockMvc
class HistoricoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PilhaHistorico pilhaHistorico;

    @BeforeEach
    void limparPilha() {
        pilhaHistorico.limpar();
    }

    @Test
    void deveListarHistoricoDoTopoParaBaseSemRemover() throws Exception {
        pilhaHistorico.empilhar("Cadastro de bolsa B001");
        pilhaHistorico.empilhar("Exclusão de bolsa B001");

        mockMvc.perform(get("/api/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].operacao").value("Exclusão de bolsa B001"))
                .andExpect(jsonPath("$[1].operacao").value("Cadastro de bolsa B001"));

        mockMvc.perform(get("/api/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void deveRemoverUltimaOperacaoDoHistorico() throws Exception {
        pilhaHistorico.empilhar("Cadastro de solicitação 1");
        pilhaHistorico.empilhar("Chamada de solicitação 1");

        mockMvc.perform(delete("/api/historico/ultima"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operacao").value("Chamada de solicitação 1"));

        mockMvc.perform(get("/api/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].operacao").value("Cadastro de solicitação 1"));
    }

    @Test
    void deveRetornar404AoRemoverHistoricoVazio() throws Exception {
        mockMvc.perform(delete("/api/historico/ultima"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Histórico vazio"));
    }

    @Test
    void deveListarHistoricoVazio() throws Exception {
        mockMvc.perform(get("/api/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
