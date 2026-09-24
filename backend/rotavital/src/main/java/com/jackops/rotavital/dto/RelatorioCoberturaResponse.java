package com.jackops.rotavital.dto;

import java.util.List;

public record RelatorioCoberturaResponse(int bolsas, int solicitacoes, int threads, String modo,
        double tempoProcessamentoMs, long totalSolicitacoesAtendiveis, List<CoberturaPorTipo> porTipo) {
}
