package com.jackops.rotavital.dto;

import java.time.LocalDate;

import com.jackops.rotavital.model.enums.TipoComponente;
import com.jackops.rotavital.model.enums.TipoSanguineo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CadastroBolsaRequest(
        @NotBlank(message = "Campo obrigatório vazio: identificador") String identificador,
        @NotNull(message = "Campo obrigatório vazio: tipoSanguineo") TipoSanguineo tipoSanguineo,
        @NotNull(message = "Campo obrigatório vazio: tipoComponente") TipoComponente tipoComponente,
        @NotNull(message = "Campo obrigatório vazio: dataColeta") LocalDate dataColeta,
        @NotNull(message = "Campo obrigatório vazio: dataValidade") LocalDate dataValidade,
        @NotNull(message = "Campo obrigatório vazio: volume") @Positive(message = "Volume inválido") Integer volume) {
}
