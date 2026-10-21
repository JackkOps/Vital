package com.jackops.rotavital.dto;

import java.time.LocalDate;

import com.jackops.rotavital.model.enums.MotivoBaixa;

public record BaixaBolsaResponse(Long id, Long bolsaId, String identificadorBolsa, MotivoBaixa motivo,
        LocalDate dataBaixa, boolean automatica) {
}
