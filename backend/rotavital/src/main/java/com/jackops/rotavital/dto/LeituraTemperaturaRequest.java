package com.jackops.rotavital.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Leitura enviada pelo sensor (formato do tópico rotavital/remessas/{remessaId}/temperatura).
 * Temperaturas fora de -80 °C a 60 °C são fisicamente implausíveis e são rejeitadas.
 */
public record LeituraTemperaturaRequest(
    @NotBlank(message = "Campo obrigatório vazio: leituraId") String leituraId,
    @NotBlank(message = "Campo obrigatório vazio: sensorId") String sensorId,
    @NotNull(message = "Campo obrigatório vazio: sequencia")
    @PositiveOrZero(message = "Sequência inválida") Long sequencia,
    @NotNull(message = "Campo obrigatório vazio: temperatura")
    @DecimalMin(value = "-80.0", message = "Temperatura fora do intervalo físico do sensor")
    @DecimalMax(value = "60.0", message = "Temperatura fora do intervalo físico do sensor") Double temperatura,
    @NotNull(message = "Campo obrigatório vazio: dataHora") LocalDateTime dataHora) {
}
