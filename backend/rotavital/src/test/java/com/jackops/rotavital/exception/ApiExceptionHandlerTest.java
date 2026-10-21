package com.jackops.rotavital.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiExceptionHandlerTest {
    private static final String MENSAGEM_CORPO_INVALIDO =
            "Corpo da requisição inválido: verifique o formato do JSON e os valores informados";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveRetornar400ComMensagemParaJsonMalformado() throws Exception {
        mockMvc.perform(post("/api/bolsas").contentType(MediaType.APPLICATION_JSON).content("{\"identificador\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(MENSAGEM_CORPO_INVALIDO));
    }

    @Test
    void deveRetornar400ComMensagemParaValorDeEnumInvalido() throws Exception {
        mockMvc.perform(post("/api/solicitacoes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nomeHospital\":\"Hospital Central\",\"tipoSanguineo\":\"X\","
                                + "\"tipoComponente\":\"HEMACIAS\",\"quantidade\":1,\"nivelUrgencia\":\"ELETIVA\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(MENSAGEM_CORPO_INVALIDO));
    }

    @Test
    void deveRetornar400ComMensagemParaIdNaoNumerico() throws Exception {
        mockMvc.perform(post("/api/validade/bolsas/{id}/baixa", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Valor inválido para o parâmetro: bolsaId"));
    }
}
