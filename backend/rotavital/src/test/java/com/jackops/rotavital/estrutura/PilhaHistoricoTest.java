package com.jackops.rotavital.estrutura;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PilhaHistoricoTest {

    @Test
    void deveManterOrdemLifo() {
        PilhaHistorico pilha = pilhaComTresOperacoes();

        assertEquals("remoção B001", pilha.desempilhar());
        assertEquals("cadastro B002", pilha.desempilhar());
        assertEquals("cadastro B001", pilha.desempilhar());
    }

    @Test
    void deveIndicarPilhaVazia() {
        PilhaHistorico pilha = new PilhaHistorico();

        assertTrue(pilha.vazia());
        assertNull(pilha.desempilhar());
    }

    @Test
    void deveConsultarTopoSemRemover() {
        PilhaHistorico pilha = pilhaComTresOperacoes();

        assertEquals("remoção B001", pilha.topo());
        assertEquals("remoção B001", pilha.topo());
        assertEquals("remoção B001", pilha.desempilhar());
    }

    @Test
    void deveRetornarNullQuandoTopoVazio() {
        PilhaHistorico pilha = new PilhaHistorico();

        assertNull(pilha.topo());
    }

    @Test
    void deveLimparPilha() {
        PilhaHistorico pilha = pilhaComTresOperacoes();

        pilha.limpar();

        assertTrue(pilha.vazia());
    }

    private PilhaHistorico pilhaComTresOperacoes() {
        PilhaHistorico pilha = new PilhaHistorico();
        pilha.empilhar("cadastro B001");
        pilha.empilhar("cadastro B002");
        pilha.empilhar("remoção B001");
        return pilha;
    }
}
