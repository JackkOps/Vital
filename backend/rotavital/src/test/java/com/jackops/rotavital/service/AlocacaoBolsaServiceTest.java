package com.jackops.rotavital.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import com.jackops.rotavital.dto.BolsaResponse;
import com.jackops.rotavital.dto.RespostaAlocacaoBolsa;
import com.jackops.rotavital.exception.RecursoNaoEncontradoException;
import com.jackops.rotavital.exception.RegraDeNegocioException;
import com.jackops.rotavital.model.Bolsa;
import com.jackops.rotavital.model.SolicitacaoSangue;
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
                .identificador("BOLSA-TESTE-PRINCIPAL")
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

    @Test
    void deveLancarNaoEncontradoParaSolicitacaoInexistente() {
        RecursoNaoEncontradoException excecao = assertThrows(RecursoNaoEncontradoException.class,
                () -> alocacaoBolsaService.alocarBolsaCompativel(999999L));

        assertEquals("Solicitação não encontrada", excecao.getMessage());
    }

    @Test
    void deveGravarVinculoEntreBolsaESolicitacao() {
        SolicitacaoSangue solicitacao = criarSolicitacao(TipoSanguineo.O_POSITIVO);
        Bolsa bolsa = criarBolsa(TipoSanguineo.O_POSITIVO, LocalDate.now().plusDays(10), StatusBolsa.DISPONIVEL);

        alocacaoBolsaService.alocarBolsaCompativel(solicitacao.getId());

        assertEquals(solicitacao.getId(),
                bolsaRepository.findById(bolsa.getId()).orElseThrow().getSolicitacao().getId());
        List<BolsaResponse> alocadas = alocacaoBolsaService.listarBolsasAlocadas(solicitacao.getId());
        assertEquals(1, alocadas.size());
        assertEquals(bolsa.getId(), alocadas.get(0).id());
        assertEquals(solicitacao.getId(), alocadas.get(0).solicitacaoId());
    }

    @Test
    void devePassarParaEmAtendimentoAteCompletarEDepoisParaAtendida() {
        SolicitacaoSangue solicitacao = criarSolicitacao(TipoSanguineo.O_POSITIVO, 2);
        criarBolsa(TipoSanguineo.O_POSITIVO, LocalDate.now().plusDays(10), StatusBolsa.DISPONIVEL);
        criarBolsa(TipoSanguineo.O_POSITIVO, LocalDate.now().plusDays(12), StatusBolsa.DISPONIVEL);

        RespostaAlocacaoBolsa primeira = alocacaoBolsaService.alocarBolsaCompativel(solicitacao.getId());
        assertEquals(1, primeira.getBolsasAlocadas());
        assertEquals(StatusSolicitacao.EM_ATENDIMENTO, primeira.getStatusSolicitacao());

        RespostaAlocacaoBolsa segunda = alocacaoBolsaService.alocarBolsaCompativel(solicitacao.getId());
        assertEquals(2, segunda.getBolsasAlocadas());
        assertEquals(StatusSolicitacao.ATENDIDA, segunda.getStatusSolicitacao());
        assertEquals(StatusSolicitacao.ATENDIDA,
                solicitacaoSangueRepository.findById(solicitacao.getId()).orElseThrow().getStatus());
    }

    @Test
    void deveRecusarAlocacaoAlemDaQuantidadePedida() {
        SolicitacaoSangue solicitacao = criarSolicitacao(TipoSanguineo.O_POSITIVO);
        criarBolsa(TipoSanguineo.O_POSITIVO, LocalDate.now().plusDays(10), StatusBolsa.DISPONIVEL);
        Bolsa sobra = criarBolsa(TipoSanguineo.O_POSITIVO, LocalDate.now().plusDays(12), StatusBolsa.DISPONIVEL);
        alocacaoBolsaService.alocarBolsaCompativel(solicitacao.getId());

        RegraDeNegocioException excecao = assertThrows(RegraDeNegocioException.class,
                () -> alocacaoBolsaService.alocarBolsaCompativel(solicitacao.getId()));

        assertEquals("Solicitação já recebeu a quantidade de bolsas pedida", excecao.getMessage());
        assertEquals(StatusBolsa.DISPONIVEL, bolsaRepository.findById(sobra.getId()).orElseThrow().getStatus());
    }

    @Test
    void deveRecusarAlocacaoParaSolicitacaoCancelada() {
        SolicitacaoSangue solicitacao = criarSolicitacao(TipoSanguineo.O_POSITIVO);
        solicitacao.setStatus(StatusSolicitacao.CANCELADA);
        solicitacaoSangueRepository.save(solicitacao);
        criarBolsa(TipoSanguineo.O_POSITIVO, LocalDate.now().plusDays(10), StatusBolsa.DISPONIVEL);

        assertThrows(RegraDeNegocioException.class,
                () -> alocacaoBolsaService.alocarBolsaCompativel(solicitacao.getId()));
    }

    @Test
    void naoDeveMudarStatusQuandoNaoHaBolsaCompativel() {
        SolicitacaoSangue solicitacao = criarSolicitacao(TipoSanguineo.AB_NEGATIVO);

        RespostaAlocacaoBolsa resposta = alocacaoBolsaService.alocarBolsaCompativel(solicitacao.getId());

        assertEquals(0, resposta.getBolsasAlocadas());
        assertEquals(StatusSolicitacao.PENDENTE, resposta.getStatusSolicitacao());
        assertNull(resposta.getBolsaId());
    }

    @Test
    void deveLancarNaoEncontradoAoListarAlocacoesDeSolicitacaoInexistente() {
        assertThrows(RecursoNaoEncontradoException.class,
                () -> alocacaoBolsaService.listarBolsasAlocadas(999999L));
    }

    private SolicitacaoSangue criarSolicitacao(TipoSanguineo tipoSanguineo) {
        return criarSolicitacao(tipoSanguineo, 1);
    }

    private SolicitacaoSangue criarSolicitacao(TipoSanguineo tipoSanguineo, int quantidade) {
        return solicitacaoSangueRepository.save(SolicitacaoSangue.builder()
                .nomeHospital("Hospital Central")
                .tipoSanguineo(tipoSanguineo)
                .tipoComponente(TipoComponente.HEMACIAS)
                .quantidade(quantidade)
                .build());
    }

    private Bolsa criarBolsa(TipoSanguineo tipoSanguineo, LocalDate dataValidade, StatusBolsa status) {
        return bolsaRepository.save(Bolsa.builder()
                .identificador("BOLSA-TESTE-" + java.util.UUID.randomUUID())
                .tipoSanguineo(tipoSanguineo)
                .tipoComponente(TipoComponente.HEMACIAS)
                .dataColeta(LocalDate.now().minusDays(20))
                .dataValidade(dataValidade)
                .volume(450)
                .status(status)
                .build());
    }
}
