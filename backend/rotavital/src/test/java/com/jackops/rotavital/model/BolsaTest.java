package com.jackops.rotavital.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.jackops.rotavital.model.enums.StatusBolsa;
import com.jackops.rotavital.model.enums.TipoComponente;
import com.jackops.rotavital.model.enums.TipoSanguineo;

class BolsaTest {
    private static final LocalDate HOJE = LocalDate.of(2026, 10, 20);

    @Test
    void deveEstarVencidaQuandoValidadeEhAnteriorAData() {
        assertTrue(bolsa(TipoComponente.HEMACIAS, HOJE.minusDays(1)).estaVencidaEm(HOJE));
    }

    @Test
    void naoDeveEstarVencidaNoDiaDaValidade() {
        assertFalse(bolsa(TipoComponente.HEMACIAS, HOJE).estaVencidaEm(HOJE));
    }

    @Test
    void deveRespeitarJanelaDeAlertaDoComponente() {
        int diasHemacias = TipoComponente.HEMACIAS.getDiasAlertaVencimento();
        assertTrue(bolsa(TipoComponente.HEMACIAS, HOJE.plusDays(diasHemacias)).estaProximaDoVencimentoEm(HOJE));
        assertFalse(bolsa(TipoComponente.HEMACIAS, HOJE.plusDays(diasHemacias + 1)).estaProximaDoVencimentoEm(HOJE));
    }

    @Test
    void plaquetasDevemTerJanelaDeAlertaPropria() {
        int diasPlaquetas = TipoComponente.PLAQUETAS.getDiasAlertaVencimento();
        assertTrue(bolsa(TipoComponente.PLAQUETAS, HOJE.plusDays(diasPlaquetas)).estaProximaDoVencimentoEm(HOJE));
        assertFalse(bolsa(TipoComponente.PLAQUETAS, HOJE.plusDays(diasPlaquetas + 1)).estaProximaDoVencimentoEm(HOJE));
    }

    @Test
    void bolsaVencidaNaoDeveSerSinalizadaComoProximaDoVencimento() {
        assertFalse(bolsa(TipoComponente.PLAQUETAS, HOJE.minusDays(1)).estaProximaDoVencimentoEm(HOJE));
    }

    private Bolsa bolsa(TipoComponente tipoComponente, LocalDate dataValidade) {
        return Bolsa.builder()
                .identificador("BOLSA-TESTE")
                .tipoSanguineo(TipoSanguineo.O_POSITIVO)
                .tipoComponente(tipoComponente)
                .dataColeta(dataValidade.minusDays(30))
                .dataValidade(dataValidade)
                .volume(450)
                .status(StatusBolsa.DISPONIVEL)
                .build();
    }
}
