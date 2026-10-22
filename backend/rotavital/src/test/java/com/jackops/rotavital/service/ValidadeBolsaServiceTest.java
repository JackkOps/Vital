package com.jackops.rotavital.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import com.jackops.rotavital.dto.AlertaValidadeResponse;
import com.jackops.rotavital.dto.BaixaBolsaResponse;
import com.jackops.rotavital.dto.RespostaAlocacaoBolsa;
import com.jackops.rotavital.dto.VerificacaoValidadeResponse;
import com.jackops.rotavital.exception.RegraDeNegocioException;
import com.jackops.rotavital.model.Bolsa;
import com.jackops.rotavital.model.SolicitacaoSangue;
import com.jackops.rotavital.model.enums.MotivoBaixa;
import com.jackops.rotavital.model.enums.StatusBolsa;
import com.jackops.rotavital.model.enums.TipoComponente;
import com.jackops.rotavital.model.enums.TipoSanguineo;
import com.jackops.rotavital.repository.BaixaBolsaRepository;
import com.jackops.rotavital.repository.BolsaRepository;
import com.jackops.rotavital.repository.SolicitacaoSangueRepository;

@SpringBootTest
@Transactional
@Sql(
        statements = {
                "DELETE FROM baixa_bolsa",
                "DELETE FROM bolsa",
                "DELETE FROM solicitacao_sangue"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class ValidadeBolsaServiceTest {

    @Autowired
    private ValidadeBolsaService validadeBolsaService;

    @Autowired
    private AlocacaoBolsaService alocacaoBolsaService;

    @Autowired
    private BolsaRepository bolsaRepository;

    @Autowired
    private BaixaBolsaRepository baixaBolsaRepository;

    @Autowired
    private SolicitacaoSangueRepository solicitacaoSangueRepository;

    @Test
    void deveSinalizarBolsaDentroDaJanelaDeAlerta() {
        LocalDate hoje = LocalDate.now();
        Bolsa plaquetas = criarBolsa(TipoComponente.PLAQUETAS, hoje.plusDays(1), StatusBolsa.DISPONIVEL);
        criarBolsa(TipoComponente.HEMACIAS, hoje.plusDays(40), StatusBolsa.DISPONIVEL);

        List<AlertaValidadeResponse> alertas = validadeBolsaService.listarAlertas();

        assertEquals(1, alertas.size());
        assertEquals(plaquetas.getId(), alertas.get(0).bolsaId());
        assertEquals(1, alertas.get(0).diasParaVencer());
    }

    @Test
    void naoDeveSinalizarBolsaAlocada() {
        criarBolsa(TipoComponente.PLAQUETAS, LocalDate.now().plusDays(1), StatusBolsa.ALOCADA);

        assertTrue(validadeBolsaService.listarAlertas().isEmpty());
    }

    @Test
    void deveDarBaixaAutomaticaEmBolsaVencida() {
        Bolsa vencida = criarBolsa(TipoComponente.HEMACIAS, LocalDate.now().minusDays(1), StatusBolsa.DISPONIVEL);

        VerificacaoValidadeResponse resposta = validadeBolsaService.verificarValidade();

        assertEquals(1, resposta.baixasRealizadas().size());
        BaixaBolsaResponse baixa = resposta.baixasRealizadas().get(0);
        assertEquals(vencida.getId(), baixa.bolsaId());
        assertEquals(MotivoBaixa.VENCIMENTO, baixa.motivo());
        assertTrue(baixa.automatica());
        assertEquals(StatusBolsa.DESCARTADA, bolsaRepository.findById(vencida.getId()).orElseThrow().getStatus());
        assertEquals(1, baixaBolsaRepository.count());
    }

    @Test
    void bolsaDescartadaNaoDeveSerConsideradaNaAlocacao() {
        criarBolsa(TipoComponente.HEMACIAS, LocalDate.now().minusDays(1), StatusBolsa.DISPONIVEL);
        validadeBolsaService.verificarValidade();
        SolicitacaoSangue solicitacao = solicitacaoSangueRepository.save(SolicitacaoSangue.builder()
                .nomeHospital("Hospital Central")
                .tipoSanguineo(TipoSanguineo.O_POSITIVO)
                .tipoComponente(TipoComponente.HEMACIAS)
                .quantidade(1)
                .build());

        RespostaAlocacaoBolsa resposta = alocacaoBolsaService.alocarBolsaCompativel(solicitacao.getId());

        assertFalse(resposta.isAlocada());
    }

    @Test
    void naoDeveDarBaixaAutomaticaEmBolsaAlocada() {
        Bolsa alocada = criarBolsa(TipoComponente.HEMACIAS, LocalDate.now().minusDays(1), StatusBolsa.ALOCADA);

        VerificacaoValidadeResponse resposta = validadeBolsaService.verificarValidade();

        assertTrue(resposta.baixasRealizadas().isEmpty());
        assertEquals(StatusBolsa.ALOCADA, bolsaRepository.findById(alocada.getId()).orElseThrow().getStatus());
    }

    @Test
    void deveBloquearBaixaManualDeBolsaAlocada() {
        Bolsa alocada = criarBolsa(TipoComponente.HEMACIAS, LocalDate.now().minusDays(1), StatusBolsa.ALOCADA);

        RegraDeNegocioException excecao = assertThrows(RegraDeNegocioException.class,
                () -> validadeBolsaService.darBaixaPorVencimento(alocada.getId()));

        assertEquals("Cancele a alocação antes de dar baixa", excecao.getMessage());
        assertEquals(0, baixaBolsaRepository.count());
    }

    @Test
    void deveDarBaixaManualEmBolsaVencida() {
        Bolsa vencida = criarBolsa(TipoComponente.PLASMA, LocalDate.now().minusDays(2), StatusBolsa.DISPONIVEL);

        BaixaBolsaResponse baixa = validadeBolsaService.darBaixaPorVencimento(vencida.getId());

        assertFalse(baixa.automatica());
        assertEquals(StatusBolsa.DESCARTADA, bolsaRepository.findById(vencida.getId()).orElseThrow().getStatus());
        assertEquals(1, validadeBolsaService.listarBaixas().size());
    }

    @Test
    void deveBloquearBaixaManualDeBolsaDentroDaValidade() {
        Bolsa valida = criarBolsa(TipoComponente.HEMACIAS, LocalDate.now().plusDays(10), StatusBolsa.DISPONIVEL);

        RegraDeNegocioException excecao = assertThrows(RegraDeNegocioException.class,
                () -> validadeBolsaService.darBaixaPorVencimento(valida.getId()));

        assertEquals("Bolsa ainda está dentro da validade", excecao.getMessage());
    }

    @Test
    void deveBloquearSegundaBaixaDaMesmaBolsa() {
        Bolsa vencida = criarBolsa(TipoComponente.HEMACIAS, LocalDate.now().minusDays(1), StatusBolsa.DISPONIVEL);
        validadeBolsaService.darBaixaPorVencimento(vencida.getId());

        RegraDeNegocioException excecao = assertThrows(RegraDeNegocioException.class,
                () -> validadeBolsaService.darBaixaPorVencimento(vencida.getId()));

        assertEquals("Bolsa já foi descartada", excecao.getMessage());
        assertEquals(1, baixaBolsaRepository.count());
    }

    private Bolsa criarBolsa(TipoComponente tipoComponente, LocalDate dataValidade, StatusBolsa status) {
        return bolsaRepository.save(Bolsa.builder()
                .identificador("BOLSA-TESTE-" + UUID.randomUUID())
                .tipoSanguineo(TipoSanguineo.O_POSITIVO)
                .tipoComponente(tipoComponente)
                .dataColeta(dataValidade.minusDays(30))
                .dataValidade(dataValidade)
                .volume(450)
                .status(status)
                .build());
    }
}
