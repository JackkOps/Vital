package com.jackops.rotavital.dto;

/**
 * Parada de entrega da rota, com o minuto de chegada contado a partir da saída.
 */
public record EntregaRotaResponse(Long localId, String nome, int chegadaMinutos, boolean dentroDaJanela) {
}
