package com.jackops.rotavital.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import com.jackops.rotavital.dto.CadastroBolsaRequest;
import com.jackops.rotavital.dto.BolsaResponse;
import com.jackops.rotavital.estrutura.ListaEstoque;
import com.jackops.rotavital.model.enums.TipoComponente;
import com.jackops.rotavital.model.enums.TipoSanguineo;
import com.jackops.rotavital.repository.BolsaRepository;

@SpringBootTest
@Sql(statements = "DELETE FROM bolsa", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class BolsaServiceEstruturaIntegracaoTest {
    @Autowired
    private BolsaService bolsaService;

    @Autowired
    private BolsaRepository bolsaRepository;

    @Autowired
    private ListaEstoque listaEstoque;

    @Test
    void deveCadastrarNaListaENoBancoEExcluirDosDois() {
        BolsaResponse response = bolsaService.cadastrar(new CadastroBolsaRequest(
                "BOLSA-INT-001",
                TipoSanguineo.O_POSITIVO,
                TipoComponente.HEMACIAS,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 11, 1),
                450));

        assertNotNull(listaEstoque.buscar("BOLSA-INT-001"));
        assertEquals(1, bolsaRepository.count());

        bolsaService.excluir(response.id());

        assertNull(listaEstoque.buscar("BOLSA-INT-001"));
        assertFalse(bolsaRepository.existsById(response.id()));
    }
}
