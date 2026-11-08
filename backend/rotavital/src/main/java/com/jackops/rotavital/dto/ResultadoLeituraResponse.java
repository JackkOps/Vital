package com.jackops.rotavital.dto;

import java.util.List;

import com.jackops.rotavital.model.enums.CondicaoRemessa;

/**
 * Resultado do recebimento de uma leitura: se ela foi gravada ou descartada como
 * reenvio, a condição atual da remessa e os alertas gerados por esta leitura.
 */
public record ResultadoLeituraResponse(
    boolean duplicada,
    LeituraTemperaturaResponse leitura,
    CondicaoRemessa condicaoRemessa,
    List<AlertaRemessaResponse> alertasGerados) {
}
