package com.jackops.rotavital.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Pedido de cálculo de rota. Os hospitais de destino vêm de {@code destinoIds}
 * (locais da rede) e/ou de {@code solicitacaoIds} (hospital de cada pedido).
 * Sem {@code janelaMinutos}, vale a janela padrão da cadeia fria.
 */
public record CalculoRotaRequest(
    @NotNull(message = "Campo obrigatório vazio: origemId") Long origemId,
    List<Long> destinoIds,
    List<Long> solicitacaoIds,
    @Positive(message = "Janela de tempo inválida") Integer janelaMinutos) {
}
