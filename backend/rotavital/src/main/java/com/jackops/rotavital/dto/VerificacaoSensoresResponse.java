package com.jackops.rotavital.dto;

import java.time.LocalDateTime;
import java.util.List;

public record VerificacaoSensoresResponse(LocalDateTime verificadoEm, List<AlertaRemessaResponse> alertasGerados) {
}
