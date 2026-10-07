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
    void deveManterOrdemFifoECasosDeFilaVazia() {
        FilaRequisicoes fila = new FilaRequisicoes();

        assertTrue(fila.vazia());
        assertNull(fila.frente());
        assertNull(fila.desenfileirar());

        fila.enfileirar(solicitacao("Hospital 1"));
        fila.enfileirar(solicitacao("Hospital 2"));
        fila.enfileirar(solicitacao("Hospital 3"));

        assertEquals("Hospital 1", fila.frente().getNomeHospital());
        assertEquals("Hospital 1", fila.desenfileirar().getNomeHospital());
        assertEquals("Hospital 2", fila.desenfileirar().getNomeHospital());
        assertEquals("Hospital 3", fila.desenfileirar().getNomeHospital());
        assertTrue(fila.vazia());

        fila.limpar();
        assertTrue(fila.vazia());
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
