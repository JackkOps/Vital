package com.jackops.rotavital.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.jackops.rotavital.dto.CadastroBolsaRequest;
import com.jackops.rotavital.estrutura.ListaEstoque;
import com.jackops.rotavital.estrutura.PilhaHistorico;
import com.jackops.rotavital.model.enums.*;
import com.jackops.rotavital.repository.BolsaRepository;
import jakarta.validation.Validation;

class BolsaValidationTest {
    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {0, -1})
    void rejeitaVolumeInvalido(Integer volume) {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var request = request(LocalDate.of(2026, 10, 20), volume);
            assertTrue(factory.getValidator().validate(request).stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("volume")));
        }
    }

    @Test
    void rejeitaCamposObrigatoriosAusentes() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            assertEquals(6, factory.getValidator().validate(
                    new CadastroBolsaRequest("", null, null, null, null, null)).size());
        }
    }

    @Test
    void aceitaValidadeIgualAColeta() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            assertTrue(factory.getValidator().validate(request(LocalDate.of(2026, 10, 20), 450)).isEmpty());
        }
    }

    @Test
    void validadeAnteriorDeveSerBadRequestSemPersistenciaOuHistorico() {
        var repository = mock(BolsaRepository.class);
        var pilha = new PilhaHistorico();
        var service = new BolsaService(repository, new ListaEstoque(), pilha);
        var erro = assertThrows(ResponseStatusException.class,
                () -> service.cadastrar(request(LocalDate.of(2026, 10, 19), 450)));
        assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
        verify(repository, never()).save(any());
        assertTrue(pilha.vazia());
    }

    private CadastroBolsaRequest request(LocalDate validade, Integer volume) {
        return new CadastroBolsaRequest("TESTE", TipoSanguineo.O_POSITIVO, TipoComponente.HEMACIAS,
                LocalDate.of(2026, 10, 20), validade, volume);
    }
}
