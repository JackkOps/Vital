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
    void deveCobrirInserirBuscarListarDuplicidadeERemocoes() {
        ListaEstoque lista = new ListaEstoque();

        assertNull(lista.buscar("B001"));
        assertFalse(lista.remover("B001"));

        assertTrue(lista.inserir(bolsa("B001")));
        assertTrue(lista.inserir(bolsa("B002")));
        assertTrue(lista.inserir(bolsa("B003")));
        assertFalse(lista.inserir(bolsa("B002")));

        assertNotNull(lista.buscar("B001"));
        assertEquals("B002", lista.buscar("B002").getIdentificador());
        assertEquals(3, lista.listar().length);

        assertTrue(lista.remover("B001"));
        assertNull(lista.buscar("B001"));
        assertTrue(lista.remover("B003"));
        assertNull(lista.buscar("B003"));
        assertTrue(lista.inserir(bolsa("B004")));
        assertTrue(lista.inserir(bolsa("B005")));
        assertTrue(lista.remover("B004"));
        assertNotNull(lista.buscar("B002"));
        assertNotNull(lista.buscar("B005"));
        assertFalse(lista.remover("NAO_EXISTE"));

        lista.limpar();
        assertEquals(0, lista.listar().length);
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
