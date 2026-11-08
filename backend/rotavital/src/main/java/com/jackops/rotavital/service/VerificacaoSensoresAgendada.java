package com.jackops.rotavital.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Procura sensores sem sinal nas remessas em trânsito a cada minuto.
 */
@Component
public class VerificacaoSensoresAgendada {
    private final RemessaService remessaService;

    public VerificacaoSensoresAgendada(RemessaService remessaService) {
        this.remessaService = remessaService;
    }

    @Scheduled(fixedDelayString = "${rotavital.remessas.verificacao-ms:60000}", initialDelay = 60000)
    public void executar() {
        remessaService.verificarSensores();
    }
}
