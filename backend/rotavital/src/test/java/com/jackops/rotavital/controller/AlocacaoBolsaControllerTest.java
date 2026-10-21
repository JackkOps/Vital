package com.jackops.rotavital.controller;

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
import com.jackops.rotavital.model.SolicitacaoSangue;
import com.jackops.rotavital.model.enums.StatusBolsa;
import com.jackops.rotavital.model.enums.TipoComponente;
import com.jackops.rotavital.model.enums.TipoSanguineo;
import com.jackops.rotavital.repository.BolsaRepository;
import com.jackops.rotavital.repository.SolicitacaoSangueRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql(
        statements = {
                "DELETE FROM baixa_bolsa",
                "DELETE FROM bolsa",
                "DELETE FROM solicitacao_sangue"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class AlocacaoBolsaControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BolsaRepository bolsaRepository;

    @Autowired
    private SolicitacaoSangueRepository solicitacaoSangueRepository;

    @Test
    void deveAlocarBolsaCompativelComStatus200() throws Exception {
        SolicitacaoSangue solicitacao = criarSolicitacao();
        Bolsa bolsa = bolsaRepository.save(Bolsa.builder()
                .identificador("BOLSA-ALOCACAO")
                .tipoSanguineo(TipoSanguineo.O_POSITIVO)
                .tipoComponente(TipoComponente.HEMACIAS)
                .dataColeta(LocalDate.now().minusDays(10))
                .dataValidade(LocalDate.now().plusDays(20))
                .volume(450)
                .status(StatusBolsa.DISPONIVEL)
                .build());

        mockMvc.perform(post("/api/alocacoes/solicitacoes/{id}/alocar", solicitacao.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alocada").value(true))
                .andExpect(jsonPath("$.bolsaId").value(bolsa.getId()));
    }

    @Test
    void deveRetornar202QuandoNaoHaBolsaCompativel() throws Exception {
        SolicitacaoSangue solicitacao = criarSolicitacao();

        mockMvc.perform(post("/api/alocacoes/solicitacoes/{id}/alocar", solicitacao.getId()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.alocada").value(false))
                .andExpect(jsonPath("$.mensagem").value("Nenhuma bolsa compatível disponível no momento."));
    }

    @Test
    void deveRetornar404AoAlocarSolicitacaoInexistente() throws Exception {
        mockMvc.perform(post("/api/alocacoes/solicitacoes/{id}/alocar", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Solicitação não encontrada"));
    }

    private SolicitacaoSangue criarSolicitacao() {
        return solicitacaoSangueRepository.save(SolicitacaoSangue.builder()
                .nomeHospital("Hospital Central")
                .tipoSanguineo(TipoSanguineo.O_POSITIVO)
                .tipoComponente(TipoComponente.HEMACIAS)
                .quantidade(1)
                .build());
    }
}
