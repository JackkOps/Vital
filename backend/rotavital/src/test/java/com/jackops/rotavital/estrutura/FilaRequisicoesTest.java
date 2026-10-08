package com.jackops.rotavital.estrutura;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.jackops.rotavital.model.SolicitacaoSangue;
import com.jackops.rotavital.model.enums.TipoComponente;
import com.jackops.rotavital.model.enums.TipoSanguineo;

class FilaRequisicoesTest {

    @Test
    void deveManterOrdemFifo() {
        FilaRequisicoes fila = filaComTresSolicitacoes();

        assertEquals("Hospital 1", fila.desenfileirar().getNomeHospital());
        assertEquals("Hospital 2", fila.desenfileirar().getNomeHospital());
        assertEquals("Hospital 3", fila.desenfileirar().getNomeHospital());
    }

    @Test
    void deveIndicarFilaVazia() {
        FilaRequisicoes fila = new FilaRequisicoes();

        assertTrue(fila.vazia());
        assertNull(fila.desenfileirar());
    }

    @Test
    void deveConsultarFrenteSemRemover() {
        FilaRequisicoes fila = filaComTresSolicitacoes();

        assertEquals("Hospital 1", fila.frente().getNomeHospital());
        assertEquals("Hospital 1", fila.frente().getNomeHospital());
        assertEquals("Hospital 1", fila.desenfileirar().getNomeHospital());
    }

    @Test
    void deveRetornarNullQuandoFrenteVazia() {
        FilaRequisicoes fila = new FilaRequisicoes();

        assertNull(fila.frente());
    }

    @Test
    void deveLimparFila() {
        FilaRequisicoes fila = filaComTresSolicitacoes();

        fila.limpar();

        assertTrue(fila.vazia());
    }

    private FilaRequisicoes filaComTresSolicitacoes() {
        FilaRequisicoes fila = new FilaRequisicoes();
        fila.enfileirar(solicitacao("Hospital 1"));
        fila.enfileirar(solicitacao("Hospital 2"));
        fila.enfileirar(solicitacao("Hospital 3"));
        return fila;
    }

    private SolicitacaoSangue solicitacao(String hospital) {
        return SolicitacaoSangue.builder()
                .nomeHospital(hospital)
                .tipoSanguineo(TipoSanguineo.O_POSITIVO)
                .tipoComponente(TipoComponente.HEMACIAS)
                .quantidade(1)
                .build();
    }
}
