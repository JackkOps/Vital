package com.jackops.rotavital.service;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.jackops.rotavital.estrutura.PilhaHistorico;

class HistoricoServiceTest {
    @Test
    void consultaPreservaPilhaEPopRevelaOperacaoAnterior() {
        var pilha = new PilhaHistorico();
        var service = new HistoricoService(pilha);
        pilha.empilhar("CRIACAO");
        pilha.empilhar("ATUALIZACAO");
        pilha.empilhar("EXCLUSAO");
        var esperado = List.of("EXCLUSAO", "ATUALIZACAO", "CRIACAO");
        assertEquals(esperado, service.listar().stream().map(r -> r.operacao()).toList());
        assertEquals(esperado, service.listar().stream().map(r -> r.operacao()).toList());
        assertEquals("EXCLUSAO", service.removerUltima().operacao());
        assertEquals("ATUALIZACAO", pilha.topo());
    }

    @Test
    void pilhaVaziaRetorna404() {
        var service = new HistoricoService(new PilhaHistorico());
        assertTrue(service.listar().isEmpty());
        assertEquals(HttpStatus.NOT_FOUND,
                assertThrows(ResponseStatusException.class, service::removerUltima).getStatusCode());
    }
}
