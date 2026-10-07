package com.jackops.rotavital.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import com.jackops.rotavital.model.Bolsa;
import com.jackops.rotavital.model.SolicitacaoSangue;
import com.jackops.rotavital.model.enums.NivelUrgencia;
import com.jackops.rotavital.model.enums.StatusBolsa;
import com.jackops.rotavital.model.enums.StatusSolicitacao;
import com.jackops.rotavital.model.enums.TipoComponente;
import com.jackops.rotavital.model.enums.TipoSanguineo;
import com.jackops.rotavital.repository.BolsaRepository;
import com.jackops.rotavital.repository.SolicitacaoSangueRepository;

@SpringBootTest
@Transactional
@Sql(
        statements = {
                "DELETE FROM bolsa",
                "DELETE FROM solicitacao_sangue"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class IndicadoresServiceTest {

    @Autowired
    private IndicadoresService indicadoresService;

    @Autowired
    private BolsaRepository bolsaRepository;

    @Autowired
    private SolicitacaoSangueRepository solicitacaoRepository;

    @Test
    void deveRetornarListasVaziasQuandoNaoHaRegistros() {
        var resposta = indicadoresService.consultar();

        assertTrue(resposta.estoqueDisponivel().isEmpty());
        assertTrue(resposta.demandaEmAberto().isEmpty());
    }

    @Test
    void deveContarSomenteBolsasDisponiveisEValidas() {
        LocalDate hoje = LocalDate.now();

        criarBolsa("VALIDA-HOJE", hoje, StatusBolsa.DISPONIVEL);
        criarBolsa("VALIDA-FUTURA", hoje.plusDays(10), StatusBolsa.DISPONIVEL);
        criarBolsa("VENCIDA", hoje.minusDays(1), StatusBolsa.DISPONIVEL);
        criarBolsa("ALOCADA", hoje.plusDays(10), StatusBolsa.ALOCADA);
        criarBolsa("INDISPONIVEL", hoje.plusDays(10), StatusBolsa.INDISPONIVEL);

        var resposta = indicadoresService.consultar();

        assertEquals(1, resposta.estoqueDisponivel().size());

        var estoque = resposta.estoqueDisponivel().get(0);

        assertEquals(TipoComponente.HEMACIAS, estoque.tipoComponente());
        assertEquals(TipoSanguineo.O_POSITIVO, estoque.tipoSanguineo());
        assertEquals(Long.valueOf(2), estoque.quantidadeBolsasDisponiveis());
    }

    @Test
    void deveSomarSomenteDemandaPendenteEEmAtendimento() {
        criarSolicitacao(2, StatusSolicitacao.PENDENTE);
        criarSolicitacao(3, StatusSolicitacao.EM_ATENDIMENTO);
        criarSolicitacao(20, StatusSolicitacao.ATENDIDA);
        criarSolicitacao(40, StatusSolicitacao.CANCELADA);

        var resposta = indicadoresService.consultar();

        assertEquals(1, resposta.demandaEmAberto().size());

        var demanda = resposta.demandaEmAberto().get(0);

        assertEquals("Hospital Indicadores", demanda.nomeHospital());
        assertEquals(TipoComponente.HEMACIAS, demanda.tipoComponente());
        assertEquals(TipoSanguineo.O_POSITIVO, demanda.tipoSanguineo());
        assertEquals(
                Long.valueOf(5),
                demanda.quantidadeBolsasSolicitadasEmAberto());
    }

    private void criarBolsa(
            String identificador,
            LocalDate validade,
            StatusBolsa status) {
        bolsaRepository.save(Bolsa.builder()
                .identificador(identificador)
                .tipoSanguineo(TipoSanguineo.O_POSITIVO)
                .tipoComponente(TipoComponente.HEMACIAS)
                .dataColeta(LocalDate.now().minusDays(20))
                .dataValidade(validade)
                .volume(450)
                .status(status)
                .build());
    }

    private void criarSolicitacao(
            Integer quantidade,
            StatusSolicitacao status) {
        SolicitacaoSangue solicitacao = SolicitacaoSangue.builder()
                .nomeHospital("Hospital Indicadores")
                .tipoSanguineo(TipoSanguineo.O_POSITIVO)
                .tipoComponente(TipoComponente.HEMACIAS)
                .quantidade(quantidade)
                .build();

        solicitacao.setNivelUrgencia(NivelUrgencia.ELETIVA);
        solicitacao.setStatus(status);

        solicitacaoRepository.save(solicitacao);
    }
}