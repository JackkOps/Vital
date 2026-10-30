package com.jackops.rotavital.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
                .andExpect(jsonPath("$.bolsaId").value(bolsa.getId()))
                .andExpect(jsonPath("$.bolsasAlocadas").value(1))
                .andExpect(jsonPath("$.statusSolicitacao").value("ATENDIDA"));
    }

    @Test
    void deveExporAlocacaoNasRotasDeSolicitacoesEBolsas() throws Exception {
        SolicitacaoSangue solicitacao = criarSolicitacao();
        Bolsa bolsa = criarBolsaDisponivel("BOLSA-VINCULO");
        mockMvc.perform(post("/api/alocacoes/solicitacoes/{id}/alocar", solicitacao.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/alocacoes/solicitacoes/{id}", solicitacao.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bolsa.getId()))
                .andExpect(jsonPath("$[0].solicitacaoId").value(solicitacao.getId()));
        mockMvc.perform(get("/api/solicitacoes/{id}", solicitacao.getId()))
                .andExpect(jsonPath("$.bolsasAlocadas").value(1))
                .andExpect(jsonPath("$.status").value("ATENDIDA"));
        mockMvc.perform(get("/api/bolsas/{id}", bolsa.getId()))
                .andExpect(jsonPath("$.solicitacaoId").value(solicitacao.getId()));
    }

    @Test
    void deveRetornar409AoAlocarParaSolicitacaoJaAtendida() throws Exception {
        SolicitacaoSangue solicitacao = criarSolicitacao();
        criarBolsaDisponivel("BOLSA-PRIMEIRA");
        criarBolsaDisponivel("BOLSA-SOBRA");
        mockMvc.perform(post("/api/alocacoes/solicitacoes/{id}/alocar", solicitacao.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/alocacoes/solicitacoes/{id}/alocar", solicitacao.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("Solicitação já recebeu a quantidade de bolsas pedida"));
    }

    @Test
    void deveExcluirSolicitacaoComBolsaAlocadaMantendoBolsaAlocada() throws Exception {
        SolicitacaoSangue solicitacao = criarSolicitacao();
        Bolsa bolsa = criarBolsaDisponivel("BOLSA-EXCLUSAO");
        mockMvc.perform(post("/api/alocacoes/solicitacoes/{id}/alocar", solicitacao.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/solicitacoes/{id}", solicitacao.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/bolsas/{id}", bolsa.getId()))
                .andExpect(jsonPath("$.status").value("ALOCADA"))
                .andExpect(jsonPath("$.solicitacaoId").doesNotExist());
    }

    @Test
    void deveRetornar404AoListarAlocacoesDeSolicitacaoInexistente() throws Exception {
        mockMvc.perform(get("/api/alocacoes/solicitacoes/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Solicitação não encontrada"));
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

    @Test
    void deveRetornar400ParaIdentificadorZeroOuNegativo() throws Exception {
        for (long id : new long[] {0, -1}) {
            mockMvc.perform(post("/api/alocacoes/solicitacoes/{id}/alocar", id))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensagem").value(
                            "Identificador da solicitação inválido: informe um número inteiro maior que zero"));
        }
    }

    @Test
    void deveRetornar400ParaIdentificadorNaoNumerico() throws Exception {
        for (String id : new String[] {"abc", "99999999999999999999"}) {
            mockMvc.perform(post("/api/alocacoes/solicitacoes/{id}/alocar", id))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensagem").value("Valor inválido para o parâmetro: solicitacaoId"));
        }
    }

    private Bolsa criarBolsaDisponivel(String identificador) {
        return bolsaRepository.save(Bolsa.builder()
                .identificador(identificador)
                .tipoSanguineo(TipoSanguineo.O_POSITIVO)
                .tipoComponente(TipoComponente.HEMACIAS)
                .dataColeta(LocalDate.now().minusDays(10))
                .dataValidade(LocalDate.now().plusDays(20))
                .volume(450)
                .status(StatusBolsa.DISPONIVEL)
                .build());
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
