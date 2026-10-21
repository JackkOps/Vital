package com.jackops.rotavital.exception;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.jackops.rotavital.service.IndicadoresService;

@SpringBootTest
@AutoConfigureMockMvc
class ErroInesperadoTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IndicadoresService indicadoresService;

    @Test
    void deveRetornar500ComMensagemUniformeParaFalhaInesperada() throws Exception {
        when(indicadoresService.consultar()).thenThrow(new IllegalStateException("falha simulada"));

        mockMvc.perform(get("/api/indicadores"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensagem").value("Erro interno no servidor"));
    }
}
