package com.jackops.rotavital.dto;

public record CoberturaPorTipo(String tipoSanguineo, long solicitacoes, long solicitacoesAtendiveis,
        long bolsasCompativeis, long bolsasVencendoEm7Dias) {
}
