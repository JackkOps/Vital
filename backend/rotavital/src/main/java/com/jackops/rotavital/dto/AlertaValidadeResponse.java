package com.jackops.rotavital.dto;

import java.time.LocalDate;

import com.jackops.rotavital.model.enums.TipoComponente;
import com.jackops.rotavital.model.enums.TipoSanguineo;

public record AlertaValidadeResponse(Long bolsaId, String identificador, TipoSanguineo tipoSanguineo,
        TipoComponente tipoComponente, LocalDate dataValidade, long diasParaVencer) {
}
