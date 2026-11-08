package com.jackops.rotavital.dto;

import java.time.LocalDateTime;

import com.jackops.rotavital.model.enums.TipoAlertaRemessa;

public record AlertaRemessaResponse(Long id, Long remessaId, TipoAlertaRemessa tipo, String mensagem,
        LocalDateTime criadoEm) {
}
