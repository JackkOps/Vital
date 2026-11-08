package com.jackops.rotavital.dto;

import com.jackops.rotavital.model.enums.TipoComponente;

/**
 * Situação de um componente no painel: estoque, demanda em aberto, se está abaixo
 * do nível crítico e a chance de faltar bolsa no horizonte configurado.
 */
public record SituacaoComponenteResponse(
    TipoComponente tipoComponente,
    long bolsasDisponiveis,
    long demandaEmAberto,
    int nivelCritico,
    boolean estoqueCritico,
    double probabilidadeDesabastecimento) {
}
