package com.jackops.rotavital.repository;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import com.jackops.rotavital.model.Bolsa;
import com.jackops.rotavital.model.enums.*;

@DataJpaTest
class BolsaRepositoryIntegrationTest {
    @Autowired BolsaRepository repository;

    @Test
    void bancoBloqueiaDuplicidadeMesmoSemService() {
        repository.saveAndFlush(bolsa());
        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(bolsa()));
    }

    private Bolsa bolsa() {
        return Bolsa.builder().identificador("UNICO").tipoSanguineo(TipoSanguineo.O_POSITIVO)
                .tipoComponente(TipoComponente.HEMACIAS).dataColeta(LocalDate.of(2026, 10, 20))
                .dataValidade(LocalDate.of(2026, 11, 20)).volume(450).status(StatusBolsa.DISPONIVEL).build();
    }
}
