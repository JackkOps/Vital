package com.jackops.rotavital.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Garante que o schema vem das migrations do Flyway (e não do ddl-auto) e que a
 * V2 cria o vínculo bolsa → solicitação com chave estrangeira.
 */
@SpringBootTest
class MigracaoSchemaTest {

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void flywayAplicaTodasAsMigrations() {
        List<String> versoes = jdbc.queryForList(
                "SELECT \"version\" FROM \"flyway_schema_history\" WHERE \"success\" = TRUE AND \"version\" IS NOT NULL ORDER BY \"installed_rank\"",
                String.class);
        assertEquals(List.of("1", "2", "3"), versoes);
    }

    @Test
    void bolsaTemColunaSolicitacaoIdOpcional() {
        String nullable = jdbc.queryForObject(
                "SELECT IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS "
                        + "WHERE TABLE_NAME = 'BOLSA' AND COLUMN_NAME = 'SOLICITACAO_ID'",
                String.class);
        assertEquals("YES", nullable);
    }

    @Test
    void solicitacaoIdReferenciaSolicitacaoSangue() {
        Integer fks = jdbc.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS "
                        + "WHERE TABLE_NAME = 'BOLSA' AND CONSTRAINT_NAME = 'FK_BOLSA_SOLICITACAO' "
                        + "AND CONSTRAINT_TYPE = 'FOREIGN KEY'",
                Integer.class);
        assertTrue(fks != null && fks == 1);
    }
}
