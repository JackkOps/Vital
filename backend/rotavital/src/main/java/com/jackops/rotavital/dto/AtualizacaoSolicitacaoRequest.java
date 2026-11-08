package com.jackops.rotavital.dto;

import com.jackops.rotavital.model.enums.TipoComponente;
import com.jackops.rotavital.model.enums.TipoSanguineo;

import com.jackops.rotavital.model.SolicitacaoSangue;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AtualizacaoSolicitacaoRequest(
        @NotBlank(message = "Campo obrigatório vazio: nomeHospital")
        String nomeHospital,

        @NotNull(message = "Campo obrigatório vazio: tipoSanguineo")
        TipoSanguineo tipoSanguineo,

        @NotNull(message = "Campo obrigatório vazio: tipoComponente")
        TipoComponente tipoComponente,

        @NotNull(message = "Campo obrigatório vazio: quantidade")
        @Positive(message = "Quantidade inválida")
        @Max(value = SolicitacaoSangue.QUANTIDADE_MAXIMA,
                message = "Quantidade acima do limite de " + SolicitacaoSangue.QUANTIDADE_MAXIMA + " bolsas por solicitação")
        Integer quantidade) {
}