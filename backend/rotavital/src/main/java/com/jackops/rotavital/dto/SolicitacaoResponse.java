package com.jackops.rotavital.dto;

import com.jackops.rotavital.model.enums.TipoComponente;
import com.jackops.rotavital.model.enums.TipoSanguineo;

public record SolicitacaoResponse(Long id, String nomeHospital, TipoSanguineo tipoSanguineo,
        TipoComponente tipoComponente, Integer quantidade) {
}
