package com.jackops.rotavital.dto;

import java.time.LocalDate;
import java.util.List;

public record IndicadoresResponse(
    LocalDate dataReferencia,
    String descricaoEstoque,
    String descricaoDemanda,
    List<EstoquePorTipoResponse> estoqueDisponivel,
    List<DemandaPorHospitalResponse> demandaEmAberto) {
}