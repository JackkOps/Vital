package com.jackops.rotavital.dto;

import java.time.LocalDate;

import com.jackops.rotavital.model.enums.StatusBolsa;
import com.jackops.rotavital.model.enums.TipoComponente;
import com.jackops.rotavital.model.enums.TipoSanguineo;

public record BolsaResponse(Long id, String identificador, TipoSanguineo tipoSanguineo,
        TipoComponente tipoComponente, LocalDate dataColeta, LocalDate dataValidade,
        Integer volume, StatusBolsa status) {
}
