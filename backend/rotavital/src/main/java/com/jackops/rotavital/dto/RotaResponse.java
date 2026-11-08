package com.jackops.rotavital.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.jackops.rotavital.model.enums.StatusRota;

public record RotaResponse(
    Long id,
    LocalRedeResponse origem,
    List<LocalRedeResponse> caminho,
    List<EntregaRotaResponse> entregas,
    int tempoTotalMinutos,
    double distanciaTotalKm,
    int janelaMinutos,
    boolean cumpreJanela,
    StatusRota status,
    String alerta,
    LocalDateTime criadaEm) {
}
