package com.jackops.rotavital.dto;

import com.jackops.rotavital.model.enums.TipoComponente;
import com.jackops.rotavital.model.enums.TipoSanguineo;

public record EstoquePorTipoResponse(
    TipoComponente tipoComponente,
    TipoSanguineo tipoSanguineo,
    Long quantidadeBolsasDisponiveis) {
    }