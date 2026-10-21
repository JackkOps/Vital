package com.jackops.rotavital.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.jackops.rotavital.model.Bolsa;
import com.jackops.rotavital.model.enums.StatusBolsa;
import com.jackops.rotavital.model.enums.TipoComponente;
import com.jackops.rotavital.model.enums.TipoSanguineo;
import com.jackops.rotavital.repository.BolsaRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql(
        statements = {
                "DELETE FROM baixa_bolsa",
                "DELETE FROM bolsa"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class ValidadeBolsaControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BolsaRepository bolsaRepository;

    @Test
    void deveListarAlertasDeVencimento() throws Exception {
        criarBolsa("BOLSA-ALERTA", TipoComponente.PLAQUETAS, LocalDate.now().plusDays(1), StatusBolsa.DISPONIVEL);

        mockMvc.perform(get("/api/validade/alertas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].identificador").value("BOLSA-ALERTA"))
                .andExpect(jsonPath("$[0].diasParaVencer").value(1));
    }

    @Test
    void deveDescartarBolsaVencidaNaVerificacao() throws Exception {
        criarBolsa("BOLSA-VENCIDA", TipoComponente.HEMACIAS, LocalDate.now().minusDays(1), StatusBolsa.DISPONIVEL);

        mockMvc.perform(post("/api/validade/verificar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.baixasRealizadas[0].identificadorBolsa").value("BOLSA-VENCIDA"))
                .andExpect(jsonPath("$.baixasRealizadas[0].motivo").value("VENCIMENTO"))
                .andExpect(jsonPath("$.baixasRealizadas[0].automatica").value(true));

        mockMvc.perform(get("/api/bolsas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("DESCARTADA"));
    }

    @Test
    void deveRegistrarBaixaManualDeBolsaVencida() throws Exception {
        Bolsa vencida = criarBolsa("BOLSA-MANUAL", TipoComponente.PLASMA, LocalDate.now().minusDays(3),
                StatusBolsa.DISPONIVEL);

        mockMvc.perform(post("/api/validade/bolsas/{id}/baixa", vencida.getId()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bolsaId").value(vencida.getId()))
                .andExpect(jsonPath("$.automatica").value(false));

        mockMvc.perform(get("/api/validade/baixas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].identificadorBolsa").value("BOLSA-MANUAL"));
    }

    @Test
    void deveBloquearBaixaDeBolsaAlocada() throws Exception {
        Bolsa alocada = criarBolsa("BOLSA-ALOCADA", TipoComponente.HEMACIAS, LocalDate.now().minusDays(1),
                StatusBolsa.ALOCADA);

        mockMvc.perform(post("/api/validade/bolsas/{id}/baixa", alocada.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("Cancele a alocação antes de dar baixa"));
    }

    @Test
    void deveRetornar404AoDarBaixaEmBolsaInexistente() throws Exception {
        mockMvc.perform(post("/api/validade/bolsas/{id}/baixa", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Bolsa não encontrada"));
    }

    private Bolsa criarBolsa(String identificador, TipoComponente tipoComponente, LocalDate dataValidade,
            StatusBolsa status) {
        return bolsaRepository.save(Bolsa.builder()
                .identificador(identificador)
                .tipoSanguineo(TipoSanguineo.A_POSITIVO)
                .tipoComponente(tipoComponente)
                .dataColeta(dataValidade.minusDays(30))
                .dataValidade(dataValidade)
                .volume(450)
                .status(status)
                .build());
    }
}
