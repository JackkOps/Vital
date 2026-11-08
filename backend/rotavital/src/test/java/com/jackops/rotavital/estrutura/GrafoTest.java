package com.jackops.rotavital.estrutura;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GrafoTest {

    @Test
    void deveEncontrarCaminhoDeMenorTempoMesmoComMaisTrechos() {
        // 0 -> 2 direto leva 50; passando por 1 leva 10 + 15 = 25.
        Grafo grafo = new Grafo(3);
        grafo.adicionarArestaDupla(0, 2, 50, 20.0);
        grafo.adicionarArestaDupla(0, 1, 10, 8.0);
        grafo.adicionarArestaDupla(1, 2, 15, 9.0);

        Grafo.CaminhosMinimos caminhos = grafo.menoresCaminhos(0);

        assertEquals(25, caminhos.tempos()[2]);
        assertEquals(17.0, caminhos.distancias()[2]);
        assertArrayEquals(new int[] {0, 1, 2}, caminhos.caminhoAte(2));
    }

    @Test
    void deveDesempatarTempoPelaMenorDistancia() {
        Grafo grafo = new Grafo(4);
        grafo.adicionarArestaDupla(0, 1, 10, 9.0);
        grafo.adicionarArestaDupla(1, 3, 10, 9.0);
        grafo.adicionarArestaDupla(0, 2, 10, 2.0);
        grafo.adicionarArestaDupla(2, 3, 10, 2.0);

        Grafo.CaminhosMinimos caminhos = grafo.menoresCaminhos(0);

        assertEquals(20, caminhos.tempos()[3]);
        assertArrayEquals(new int[] {0, 2, 3}, caminhos.caminhoAte(3));
    }

    @Test
    void deveMarcarVerticeSemConexaoComoInalcancavel() {
        Grafo grafo = new Grafo(3);
        grafo.adicionarArestaDupla(0, 1, 5, 1.0);

        Grafo.CaminhosMinimos caminhos = grafo.menoresCaminhos(0);

        assertFalse(caminhos.alcancavel(2));
        assertEquals(Grafo.INALCANCAVEL, caminhos.tempos()[2]);
        assertEquals(0, caminhos.caminhoAte(2).length);
    }

    @Test
    void deveRespeitarSentidoDaArestaSimples() {
        Grafo grafo = new Grafo(2);
        grafo.adicionarAresta(0, 1, 5, 1.0);

        assertTrue(grafo.menoresCaminhos(0).alcancavel(1));
        assertFalse(grafo.menoresCaminhos(1).alcancavel(0));
    }

    @Test
    void deveRetornarCaminhoDaOrigemParaElaMesma() {
        Grafo grafo = new Grafo(1);

        Grafo.CaminhosMinimos caminhos = grafo.menoresCaminhos(0);

        assertEquals(0, caminhos.tempos()[0]);
        assertArrayEquals(new int[] {0}, caminhos.caminhoAte(0));
        assertNull(grafo.adjacentes(0));
    }

    @Test
    void deveRecusarVerticeInexistenteETempoNegativo() {
        Grafo grafo = new Grafo(2);

        assertThrows(IllegalArgumentException.class, () -> grafo.adicionarAresta(0, 5, 1, 1.0));
        assertThrows(IllegalArgumentException.class, () -> grafo.adicionarAresta(0, 1, -1, 1.0));
        assertThrows(IllegalArgumentException.class, () -> grafo.menoresCaminhos(2));
    }
}
