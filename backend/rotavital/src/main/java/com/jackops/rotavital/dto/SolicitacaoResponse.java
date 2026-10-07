package com.jackops.rotavital.dto;

import com.jackops.rotavital.model.enums.TipoComponente;
import com.jackops.rotavital.model.enums.TipoSanguineo;
import com.jackops.rotavital.model.enums.NivelUrgencia;
import com.jackops.rotavital.model.enums.StatusSolicitacao;

public record SolicitacaoResponse(Long id, String nomeHospital, TipoSanguineo tipoSanguineo,
        TipoComponente tipoComponente, Integer quantidade, NivelUrgencia nivelUrgencia, StatusSolicitacao status) {
}
