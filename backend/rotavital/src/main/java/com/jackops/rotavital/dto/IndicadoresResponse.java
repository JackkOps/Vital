package com.jackops.rotavital.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Painel de indicadores. Quando {@code dadosSuficientes} é falso, os indicadores
 * não são calculados: listas vazias, valores nulos e {@code mensagem} explica o motivo.
 */
public record IndicadoresResponse(
    LocalDate dataReferencia,
    LocalDate periodoInicio,
    LocalDate periodoFim,
    boolean dadosSuficientes,
    String mensagem,
    String descricaoEstoque,
    String descricaoDemanda,
    List<EstoquePorTipoResponse> estoqueDisponivel,
    List<DemandaPorHospitalResponse> demandaEmAberto,
    Double taxaDescarteVencimento,
    Double tempoMedioAtendimentoHoras,
    int horizonteDesabastecimentoDias,
    List<SituacaoComponenteResponse> situacaoComponentes) {
}
