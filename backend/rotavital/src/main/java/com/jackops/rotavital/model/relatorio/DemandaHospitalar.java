package com.jackops.rotavital.model.relatorio;

import com.jackops.rotavital.model.enums.TipoComponente;
import com.jackops.rotavital.model.enums.TipoSanguineo;

/** Projecao somente leitura de uma solicitacao para relatorios, incluindo a localizacao do hospital. */
public record DemandaHospitalar(String nomeHospital, TipoSanguineo tipoSanguineo, TipoComponente tipoComponente,
        int quantidade, double latitude, double longitude) {
}
