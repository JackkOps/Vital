package com.jackops.rotavital.dto;

import com.jackops.rotavital.model.enums.TipoComponente;
import com.jackops.rotavital.model.enums.TipoSanguineo;

public record DemandaPorHospitalResponse(
    String nomeHospital,
    TipoComponente tipoComponente,
    TipoSanguineo tipoSanguineo,
    Long quantidadeBolsasSolicitadasEmAberto) {
    }