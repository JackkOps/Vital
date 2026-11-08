package com.jackops.rotavital.estrutura;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HeapMinimoTest {

    @Test
    void deveRemoverEmOrdemCrescenteDeDistancia() {
        HeapMinimo heap = new HeapMinimo(2);
        long[] distancias = {40, 5, 27, 5, 100, 0, 13};
        for (int i = 0; i < distancias.length; i++) {
            heap.inserir(i, distancias[i]);
        }

        assertEquals(7, heap.tamanho());
        long anterior = Long.MIN_VALUE;
        while (!heap.vazio()) {
            long atual = heap.menorDistancia();
            heap.removerMinimo();
            assertTrue(atual >= anterior);
            anterior = atual;
        }
    }

    @Test
    void deveRetornarVerticeDeMenorDistancia() {
        HeapMinimo heap = new HeapMinimo(4);
        heap.inserir(7, 30);
        heap.inserir(3, 10);
        heap.inserir(9, 20);

        assertEquals(3, heap.removerMinimo());
        assertEquals(9, heap.removerMinimo());
        assertEquals(7, heap.removerMinimo());
        assertTrue(heap.vazio());
    }

    @Test
    void deveFalharAoRemoverDeHeapVazio() {
        HeapMinimo heap = new HeapMinimo(0);

        assertThrows(IllegalStateException.class, heap::removerMinimo);
        assertThrows(IllegalStateException.class, heap::menorDistancia);
    }
}
