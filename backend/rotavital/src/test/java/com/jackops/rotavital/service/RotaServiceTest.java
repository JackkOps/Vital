package com.jackops.rotavital.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.jackops.rotavital.dto.CalculoRotaRequest;
import com.jackops.rotavital.dto.LocalRedeResponse;
import com.jackops.rotavital.dto.RotaResponse;
import com.jackops.rotavital.exception.RecursoNaoEncontradoException;
import com.jackops.rotavital.exception.RegraDeNegocioException;
import com.jackops.rotavital.model.SolicitacaoSangue;
import com.jackops.rotavital.model.enums.StatusRota;
import com.jackops.rotavital.model.enums.StatusSolicitacao;
import com.jackops.rotavital.model.enums.TipoComponente;
import com.jackops.rotavital.model.enums.TipoSanguineo;
import com.jackops.rotavital.repository.SolicitacaoSangueRepository;

/**
 * Usa a rede sintética da migration V4 (1 = HEMOPE, 2 = Restauração, 3 = Jayme
 * da Fonte, 4 = Agamenon, 5 = Getúlio Vargas, 9 = Arcoverde, 10 = Noronha).
 */
@SpringBootTest
@Transactional
@Sql(statements = {"DELETE FROM bolsa", "DELETE FROM solicitacao_sangue"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class RotaServiceTest {

    private static final long HEMOPE = 1;
    private static final long RESTAURACAO = 2;
    private static final long JAYME = 3;
    private static final long AGAMENON = 4;
    private static final long GETULIO = 5;
    private static final long ARCOVERDE = 9;
    private static final long NORONHA = 10;

    @Autowired
    private RotaService rotaService;

    @Autowired
    private SolicitacaoSangueRepository solicitacaoRepository;

    @Test
    void deveCalcularMenorCaminhoEConfirmarQuandoCumpreAJanela() {
        RotaResponse rota = rotaService.calcular(pedido(List.of(GETULIO), null));

        // Via Agamenon (12 + 9 = 21) é mais rápido que via Restauração (8 + 14 = 22).
        assertEquals(List.of(HEMOPE, AGAMENON, GETULIO), ids(rota.caminho()));
        assertEquals(21, rota.tempoTotalMinutos());
        assertEquals(8.8, rota.distanciaTotalKm());
        assertTrue(rota.cumpreJanela());
        assertEquals(StatusRota.CONFIRMADA, rota.status());
        assertNull(rota.alerta());
    }

    @Test
    void deveEscolherAOrdemDeVisitaDeMenorTempoTotal() {
        RotaResponse rota = rotaService.calcular(pedido(List.of(GETULIO, JAYME), null));

        // Jayme primeiro (10) e depois Getúlio via Restauração (6 + 14): 30 min, contra 41 na ordem inversa.
        assertEquals(List.of(HEMOPE, JAYME, RESTAURACAO, GETULIO), ids(rota.caminho()));
        assertEquals(30, rota.tempoTotalMinutos());
        assertEquals(JAYME, rota.entregas().get(0).localId());
        assertEquals(10, rota.entregas().get(0).chegadaMinutos());
        assertEquals(30, rota.entregas().get(1).chegadaMinutos());
    }

    @Test
    void naoDeveConfirmarAutomaticamenteRotaQuePassaDaJanela() {
        RotaResponse rota = rotaService.calcular(pedido(List.of(ARCOVERDE), null));

        assertEquals(225, rota.tempoTotalMinutos());
        assertFalse(rota.cumpreJanela());
        assertEquals(StatusRota.AGUARDANDO_CONFIRMACAO, rota.status());
        assertTrue(rota.alerta().startsWith("Risco de atraso"));
        assertFalse(rota.entregas().get(0).dentroDaJanela());
    }

    @Test
    void deveRespeitarJanelaInformadaNoPedido() {
        RotaResponse rota = rotaService.calcular(new CalculoRotaRequest(HEMOPE, List.of(ARCOVERDE), null, 240));

        assertEquals(StatusRota.CONFIRMADA, rota.status());
        assertEquals(240, rota.janelaMinutos());
    }

    @Test
    void operadorPodeConfirmarRotaComRiscoUmaVez() {
        RotaResponse rota = rotaService.calcular(pedido(List.of(ARCOVERDE), null));

        RotaResponse confirmada = rotaService.confirmar(rota.id());

        assertEquals(StatusRota.CONFIRMADA, confirmada.status());
        assertNotNull(confirmada.alerta());
        assertThrows(RegraDeNegocioException.class, () -> rotaService.confirmar(rota.id()));
    }

    @Test
    void deveAlertarQuandoNaoHaCaminhoAteODestino() {
        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> rotaService.calcular(pedido(List.of(NORONHA), null)));

        assertEquals(422, erro.getStatusCode().value());
        assertTrue(erro.getReason().startsWith("Não há rota disponível"));
    }

    @Test
    void deveUsarOHospitalDasSolicitacoesComoDestino() {
        SolicitacaoSangue solicitacao = solicitar("hospital jayme da fonte", StatusSolicitacao.ATENDIDA);

        RotaResponse rota = rotaService.calcular(pedido(null, List.of(solicitacao.getId())));

        assertEquals(List.of(HEMOPE, JAYME), ids(rota.caminho()));
    }

    @Test
    void deveRecusarSolicitacaoDeHospitalForaDaRedeOuCancelada() {
        SolicitacaoSangue foraDaRede = solicitar("Hospital Inexistente", StatusSolicitacao.PENDENTE);
        SolicitacaoSangue cancelada = solicitar("Hospital Jayme da Fonte", StatusSolicitacao.CANCELADA);

        assertThrows(RegraDeNegocioException.class,
                () -> rotaService.calcular(pedido(null, List.of(foraDaRede.getId()))));
        assertThrows(RegraDeNegocioException.class,
                () -> rotaService.calcular(pedido(null, List.of(cancelada.getId()))));
    }

    @Test
    void deveRecusarPedidoSemDestinoOuComLocalInexistente() {
        assertThrows(ResponseStatusException.class, () -> rotaService.calcular(pedido(List.of(HEMOPE), null)));
        assertThrows(RecursoNaoEncontradoException.class,
                () -> rotaService.calcular(pedido(List.of(999L), null)));
        assertThrows(RecursoNaoEncontradoException.class,
                () -> rotaService.calcular(new CalculoRotaRequest(999L, List.of(JAYME), null, null)));
    }

    @Test
    void deveExporARedeDeDistribuicao() {
        var rede = rotaService.rede();

        assertEquals(10, rede.locais().size());
        assertEquals(12, rede.trechos().size());
    }

    private CalculoRotaRequest pedido(List<Long> destinos, List<Long> solicitacoes) {
        return new CalculoRotaRequest(HEMOPE, destinos, solicitacoes, null);
    }

    private List<Long> ids(List<LocalRedeResponse> locais) {
        return locais.stream().map(LocalRedeResponse::id).toList();
    }

    private SolicitacaoSangue solicitar(String hospital, StatusSolicitacao status) {
        SolicitacaoSangue solicitacao = SolicitacaoSangue.builder()
                .nomeHospital(hospital)
                .tipoSanguineo(TipoSanguineo.O_POSITIVO)
                .tipoComponente(TipoComponente.HEMACIAS)
                .quantidade(1)
                .build();
        solicitacao.setStatus(status);
        return solicitacaoRepository.save(solicitacao);
    }
}
