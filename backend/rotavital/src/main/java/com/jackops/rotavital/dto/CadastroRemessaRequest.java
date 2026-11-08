package com.jackops.rotavital.dto;

import com.jackops.rotavital.model.enums.TipoComponente;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Despacho de uma remessa. {@code rotaId} é opcional, mas, se informado, a rota
 * precisa estar confirmada.
 */
public record CadastroRemessaRequest(
    Long rotaId,
    @NotNull(message = "Campo obrigatório vazio: tipoComponente") TipoComponente tipoComponente,
    @NotBlank(message = "Campo obrigatório vazio: sensorId") String sensorId) {
}
