package com.jackops.rotavital.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.jackops.rotavital.dto.RespostaAlocacaoBolsa;
import com.jackops.rotavital.model.Bolsa;
import com.jackops.rotavital.model.SolicitacaoSangue;
import com.jackops.rotavital.model.enums.StatusBolsa;
import com.jackops.rotavital.model.enums.TipoComponente;
import com.jackops.rotavital.model.enums.TipoSanguineo;
import com.jackops.rotavital.repository.BolsaRepository;
import com.jackops.rotavital.repository.SolicitacaoSangueRepository;

@SpringBootTest
@Transactional
class AlocacaoBolsaServiceTest {

    @Autowired
    private AlocacaoBolsaService alocacaoBolsaService;

    @Autowired
    private BolsaRepository bolsaRepository;

    @Autowired
    private SolicitacaoSangueRepository solicitacaoSangueRepository;

    @Test
    void deveAlocarBolsaCompativel() {
        SolicitacaoSangue solicitacao = criarSolicitacao(TipoSanguineo.O_POSITIVO);

        Bolsa bolsa = bolsaRepository.save(Bolsa.builder()
                .tipoSanguineo(TipoSanguineo.O_POSITIVO)
                .tipoComponente(TipoComponente.HEMACIAS)
                .dataColeta(LocalDate.now().minusDays(10))
                .dataValidade(LocalDate.now().plusDays(30))
                .volume(450)
                .status(StatusBolsa.DISPONIVEL)
                .build());

        RespostaAlocacaoBolsa resposta = alocacaoBolsaService.alocarBolsaCompativel(solicitacao.getId());

        assertTrue(resposta.isAlocada());
        assertEquals(bolsa.getId(), resposta.getBolsaId());
        assertEquals(StatusBolsa.ALOCADA, bolsaRepository.findById(bolsa.getId()).orElseThrow().getStatus());
    }

    @Test
    void deveEscolherBolsaComValidadeMaisProxima() {
        SolicitacaoSangue solicitacao = criarSolicitacao(TipoSanguineo.O_POSITIVO);

        Bolsa bolsaComValidadeMaisProxima = criarBolsa(
                TipoSanguineo.O_POSITIVO,
                LocalDate.now().plusDays(5),
                StatusBolsa.DISPONIVEL
        );
        criarBolsa(
                TipoSanguineo.O_POSITIVO,
                LocalDate.now().plusDays(10),
                StatusBolsa.DISPONIVEL
        );

        RespostaAlocacaoBolsa resposta = alocacaoBolsaService.alocarBolsaCompativel(solicitacao.getId());

        assertTrue(resposta.isAlocada());
        assertEquals(bolsaComValidadeMaisProxima.getId(), resposta.getBolsaId());
        assertEquals(
                StatusBolsa.ALOCADA,
                bolsaRepository.findById(bolsaComValidadeMaisProxima.getId()).orElseThrow().getStatus()
        );
    }

    @Test
    void deveIgnorarBolsaVencida() {
        SolicitacaoSangue solicitacao = criarSolicitacao(TipoSanguineo.O_POSITIVO);
        criarBolsa(
                TipoSanguineo.O_POSITIVO,
                LocalDate.now().minusDays(1),
                StatusBolsa.DISPONIVEL
        );

        RespostaAlocacaoBolsa resposta = alocacaoBolsaService.alocarBolsaCompativel(solicitacao.getId());

        assertFalse(resposta.isAlocada());
        assertEquals("Nenhuma bolsa compatível disponível no momento.", resposta.getMensagem());
    }

    @Test
    void deveIgnorarBolsaIncompativel() {
        SolicitacaoSangue solicitacao = criarSolicitacao(TipoSanguineo.A_POSITIVO);
        criarBolsa(
                TipoSanguineo.B_POSITIVO,
                LocalDate.now().plusDays(20),
                StatusBolsa.DISPONIVEL
        );

        RespostaAlocacaoBolsa resposta = alocacaoBolsaService.alocarBolsaCompativel(solicitacao.getId());

        assertFalse(resposta.isAlocada());
    }

    @Test
    void deveRetornarSemAlocacaoQuandoNaoHaBolsaCompativel() {
        SolicitacaoSangue solicitacao = criarSolicitacao(TipoSanguineo.AB_NEGATIVO);
        Bolsa bolsa = criarBolsa(
                TipoSanguineo.O_POSITIVO,
                LocalDate.now().plusDays(5),
                StatusBolsa.DISPONIVEL
        );

        RespostaAlocacaoBolsa resposta = alocacaoBolsaService.alocarBolsaCompativel(solicitacao.getId());

        assertFalse(resposta.isAlocada());
        assertEquals(StatusBolsa.DISPONIVEL, bolsaRepository.findById(bolsa.getId()).orElseThrow().getStatus());
    }

    @Test
    void deveIgnorarBolsaJaAlocada() {
        SolicitacaoSangue solicitacao = criarSolicitacao(TipoSanguineo.A_NEGATIVO);
        Bolsa bolsa = criarBolsa(
                TipoSanguineo.A_NEGATIVO,
                LocalDate.now().plusDays(20),
                StatusBolsa.ALOCADA
        );

        RespostaAlocacaoBolsa resposta = alocacaoBolsaService.alocarBolsaCompativel(solicitacao.getId());

        assertFalse(resposta.isAlocada());
        assertEquals(StatusBolsa.ALOCADA, bolsaRepository.findById(bolsa.getId()).orElseThrow().getStatus());
    }

    private SolicitacaoSangue criarSolicitacao(TipoSanguineo tipoSanguineo) {
        return solicitacaoSangueRepository.save(SolicitacaoSangue.builder()
                .nomeHospital("Hospital Central")
                .tipoSanguineo(tipoSanguineo)
                .tipoComponente(TipoComponente.HEMACIAS)
                .quantidade(1)
                .build());
    }

    private Bolsa criarBolsa(TipoSanguineo tipoSanguineo, LocalDate dataValidade, StatusBolsa status) {
        return bolsaRepository.save(Bolsa.builder()
                .tipoSanguineo(tipoSanguineo)
                .tipoComponente(TipoComponente.HEMACIAS)
                .dataColeta(LocalDate.now().minusDays(20))
                .dataValidade(dataValidade)
                .volume(450)
                .status(status)
                .build());
    }
}
