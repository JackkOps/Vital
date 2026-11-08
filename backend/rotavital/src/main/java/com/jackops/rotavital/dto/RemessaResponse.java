package com.jackops.rotavital.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.jackops.rotavital.model.enums.CondicaoRemessa;
import com.jackops.rotavital.model.enums.StatusRemessa;
import com.jackops.rotavital.model.enums.TipoComponente;

public record RemessaResponse(
    Long id,
    Long rotaId,
    TipoComponente tipoComponente,
    double temperaturaMinima,
    double temperaturaMaxima,
    String sensorId,
    StatusRemessa status,
    CondicaoRemessa condicao,
    boolean sensorIndisponivel,
    LocalDateTime iniciadaEm,
    LocalDateTime ultimaLeituraEm,
    Double ultimaTemperatura,
    long totalLeituras,
    LocalDateTime entregueEm,
    List<AlertaRemessaResponse> alertas) {
}
