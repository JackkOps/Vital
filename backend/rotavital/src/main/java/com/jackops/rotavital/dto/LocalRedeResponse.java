package com.jackops.rotavital.dto;

import com.jackops.rotavital.model.enums.TipoLocal;

public record LocalRedeResponse(Long id, String nome, TipoLocal tipo) {
}
