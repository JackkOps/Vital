package com.jackops.rotavital.dto;

import java.time.LocalDate;
import java.util.List;

public record VerificacaoValidadeResponse(
    LocalDate dataReferencia,
    List<BaixaBolsaResponse> baixasRealizadas,
    List<AlertaValidadeResponse> alertas) {
}
