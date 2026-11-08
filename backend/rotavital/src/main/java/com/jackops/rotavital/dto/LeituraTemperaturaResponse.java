package com.jackops.rotavital.dto;

import java.time.LocalDateTime;

public record LeituraTemperaturaResponse(
    Long id,
    String leituraId,
    Long sequencia,
    Double temperatura,
    LocalDateTime dataHora,
    LocalDateTime recebidaEm,
    boolean dentroDaFaixa) {
}
