package com.jackops.rotavital.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.server.ResponseStatusException;

import com.jackops.rotavital.dto.FiltroIndicadores;
import com.jackops.rotavital.dto.SituacaoComponenteResponse;
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

    @Test
    void deveInformarDadosInsuficientesQuandoNaoHaMovimentacaoNoPeriodo() {
        criarBolsa("FORA-DO-PERIODO", LocalDate.now().plusDays(10), StatusBolsa.DISPONIVEL);
        LocalDate inicio = LocalDate.now().minusYears(2);

        var resposta = indicadoresService.consultar(
                new FiltroIndicadores(inicio, inicio.plusDays(30), null, null));

        assertFalse(resposta.dadosSuficientes());
        assertEquals("Dados insuficientes para o período", resposta.mensagem());
        assertTrue(resposta.estoqueDisponivel().isEmpty());
        assertTrue(resposta.situacaoComponentes().isEmpty());
        assertNull(resposta.taxaDescarteVencimento());
        assertNull(resposta.tempoMedioAtendimentoHoras());
    }

    @Test
    void deveCalcularTaxaDeDescarteDasBolsasQueVenceramNoPeriodo() {
        LocalDate ontem = LocalDate.now().minusDays(1);
        criarBolsa("VENCIDA-DESCARTADA", ontem, StatusBolsa.DESCARTADA);
        criarBolsa("VENCIDA-USADA", ontem, StatusBolsa.ALOCADA);
        criarBolsa("VENCIDA-OUTRA-DESCARTADA", ontem.minusDays(2), StatusBolsa.DESCARTADA);
        criarBolsa("VENCIDA-SEM-BAIXA", ontem.minusDays(3), StatusBolsa.DISPONIVEL);
        criarBolsa("AINDA-VALIDA", LocalDate.now().plusDays(5), StatusBolsa.DISPONIVEL);

        var resposta = indicadoresService.consultar();

        assertTrue(resposta.dadosSuficientes());
        assertEquals(0.5, resposta.taxaDescarteVencimento());
    }

    @Test
    void deveCalcularTempoMedioDeAtendimentoEmHoras() {
        LocalDateTime agora = LocalDateTime.now();
        criarSolicitacaoAtendida(agora.minusHours(3), agora);
        criarSolicitacaoAtendida(agora.minusHours(5), agora);
        criarSolicitacao(1, StatusSolicitacao.PENDENTE);

        var resposta = indicadoresService.consultar();

        assertEquals(4.0, resposta.tempoMedioAtendimentoHoras());
    }

    @Test
    void deveDestacarComponenteComEstoqueAbaixoDoNivelCritico() {
        criarBolsa("HEM-1", LocalDate.now().plusDays(10), StatusBolsa.DISPONIVEL);
        criarBolsa("HEM-2", LocalDate.now().plusDays(10), StatusBolsa.DISPONIVEL);
        criarSolicitacao(3, StatusSolicitacao.PENDENTE);

        var resposta = indicadoresService.consultar();

        SituacaoComponenteResponse hemacias = situacao(resposta.situacaoComponentes(), TipoComponente.HEMACIAS);
        assertEquals(2, hemacias.bolsasDisponiveis());
        assertEquals(3, hemacias.demandaEmAberto());
        assertTrue(hemacias.estoqueCritico());
        assertEquals(1.0, hemacias.probabilidadeDesabastecimento());
        assertEquals(TipoComponente.values().length, resposta.situacaoComponentes().size());
    }

    @Test
    void deveFiltrarPorComponenteEHospital() {
        criarBolsa("HEM-1", LocalDate.now().plusDays(10), StatusBolsa.DISPONIVEL);
        criarSolicitacao(2, StatusSolicitacao.PENDENTE);
        SolicitacaoSangue outroHospital = SolicitacaoSangue.builder()
                .nomeHospital("Hospital Outro")
                .tipoSanguineo(TipoSanguineo.O_POSITIVO)
                .tipoComponente(TipoComponente.HEMACIAS)
                .quantidade(4)
                .build();
        solicitacaoRepository.save(outroHospital);

        var porHospital = indicadoresService.consultar(
                new FiltroIndicadores(null, null, "hospital outro", TipoComponente.HEMACIAS));
        assertEquals(1, porHospital.demandaEmAberto().size());
        assertEquals("Hospital Outro", porHospital.demandaEmAberto().get(0).nomeHospital());
        assertEquals(1, porHospital.situacaoComponentes().size());

        var porPlasma = indicadoresService.consultar(
                new FiltroIndicadores(null, null, null, TipoComponente.PLASMA));
        assertFalse(porPlasma.dadosSuficientes());
    }

    @Test
    void deveRecusarPeriodoComInicioDepoisDoFim() {
        LocalDate hoje = LocalDate.now();

        assertThrows(ResponseStatusException.class, () -> indicadoresService.consultar(
                new FiltroIndicadores(hoje, hoje.minusDays(1), null, null)));
    }

    @Test
    void deveCalcularProbabilidadeDeDesabastecimentoPorPoisson() {
        assertEquals(1.0, IndicadoresService.probabilidadeDesabastecimento(-1, 2.0));
        assertEquals(0.0, IndicadoresService.probabilidadeDesabastecimento(3, 0.0));
        // P(D > 0) com λ = 2: 1 - e^-2
        assertEquals(0.8647, IndicadoresService.probabilidadeDesabastecimento(0, 2.0));
        // P(D > 2) com λ = 1: 1 - e^-1 (1 + 1 + 1/2)
        assertEquals(0.0803, IndicadoresService.probabilidadeDesabastecimento(2, 1.0));
    }

    private SituacaoComponenteResponse situacao(java.util.List<SituacaoComponenteResponse> lista,
            TipoComponente componente) {
        return lista.stream().filter(item -> item.tipoComponente() == componente).findFirst().orElseThrow();
    }

    private void criarSolicitacaoAtendida(LocalDateTime criadaEm, LocalDateTime atendidaEm) {
        SolicitacaoSangue solicitacao = SolicitacaoSangue.builder()
                .nomeHospital("Hospital Indicadores")
                .tipoSanguineo(TipoSanguineo.O_POSITIVO)
                .tipoComponente(TipoComponente.HEMACIAS)
                .quantidade(1)
                .build();
        solicitacao.setStatus(StatusSolicitacao.ATENDIDA);
        solicitacao.setCriadaEm(criadaEm);
        solicitacao.setAtendidaEm(atendidaEm);
        solicitacaoRepository.save(solicitacao);
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