package com.jackops.rotavital.estrutura;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.jackops.rotavital.model.Bolsa;
import com.jackops.rotavital.model.enums.StatusBolsa;
import com.jackops.rotavital.model.enums.TipoComponente;
import com.jackops.rotavital.model.enums.TipoSanguineo;

class ListaEstoqueTest {

    @Test
    void deveInserirEmListaVazia() {
        ListaEstoque lista = new ListaEstoque();

        assertTrue(lista.inserir(bolsa("B001")));

        assertEquals("B001", lista.buscar("B001").getIdentificador());
    }

    @Test
    void deveRejeitarIdentificadorDuplicado() {
        ListaEstoque lista = new ListaEstoque();

        assertTrue(lista.inserir(bolsa("B001")));

        assertFalse(lista.inserir(bolsa("B001")));
    }

    @Test
    void deveBuscarBolsaExistente() {
        ListaEstoque lista = new ListaEstoque();
        lista.inserir(bolsa("B001"));

        assertNotNull(lista.buscar("B001"));
    }

    @Test
    void deveRetornarNullAoBuscarBolsaInexistente() {
        ListaEstoque lista = new ListaEstoque();

        assertNull(lista.buscar("B001"));
    }

    @Test
    void deveRemoverPrimeiroNo() {
        ListaEstoque lista = listaComTresBolsas();

        assertTrue(lista.remover("B001"));

        assertNull(lista.buscar("B001"));
        assertNotNull(lista.buscar("B002"));
        assertNotNull(lista.buscar("B003"));
    }

    @Test
    void deveRemoverNoDoMeio() {
        ListaEstoque lista = listaComTresBolsas();

        assertTrue(lista.remover("B002"));

        assertNotNull(lista.buscar("B001"));
        assertNull(lista.buscar("B002"));
        assertNotNull(lista.buscar("B003"));
    }

    @Test
    void deveRemoverUltimoNo() {
        ListaEstoque lista = listaComTresBolsas();

        assertTrue(lista.remover("B003"));

        assertNotNull(lista.buscar("B001"));
        assertNotNull(lista.buscar("B002"));
        assertNull(lista.buscar("B003"));
    }

    @Test
    void deveRetornarFalseAoRemoverInexistente() {
        ListaEstoque lista = listaComTresBolsas();

        assertFalse(lista.remover("NAO_EXISTE"));
    }

    @Test
    void deveListarListaVazia() {
        ListaEstoque lista = new ListaEstoque();

        assertEquals(0, lista.listar().length);
    }

    @Test
    void deveLimparTodosOsNos() {
        ListaEstoque lista = listaComTresBolsas();

        lista.limpar();

        assertEquals(0, lista.listar().length);
    }

    private ListaEstoque listaComTresBolsas() {
        ListaEstoque lista = new ListaEstoque();
        lista.inserir(bolsa("B001"));
        lista.inserir(bolsa("B002"));
        lista.inserir(bolsa("B003"));
        return lista;
    }

    private Bolsa bolsa(String identificador) {
        return Bolsa.builder()
                .identificador(identificador)
                .tipoSanguineo(TipoSanguineo.O_POSITIVO)
                .tipoComponente(TipoComponente.HEMACIAS)
                .dataColeta(LocalDate.of(2026, 10, 1))
                .dataValidade(LocalDate.of(2026, 11, 1))
                .volume(450)
                .status(StatusBolsa.DISPONIVEL)
                .build();
    }
}
