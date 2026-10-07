package com.jackops.rotavital.estrutura;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PilhaHistoricoTest {

    @Test
    void deveManterOrdemLifoECasosDePilhaVazia() {
        PilhaHistorico pilha = new PilhaHistorico();

        assertTrue(pilha.vazia());
        assertNull(pilha.topo());
        assertNull(pilha.desempilhar());

        pilha.empilhar("cadastro B001");
        pilha.empilhar("cadastro B002");
        pilha.empilhar("remocao B001");

        assertEquals("remocao B001", pilha.topo());
        assertEquals("remocao B001", pilha.desempilhar());
        assertEquals("cadastro B002", pilha.desempilhar());
        assertEquals("cadastro B001", pilha.desempilhar());
        assertTrue(pilha.vazia());

        pilha.limpar();
        assertTrue(pilha.vazia());
    }
}
