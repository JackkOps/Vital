package com.jackops.rotavital.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Executa a verificação de validade do estoque uma vez por dia, à 1h.
 */
@Component
public class VerificacaoValidadeAgendada {
    private final ValidadeBolsaService validadeBolsaService;

    public VerificacaoValidadeAgendada(ValidadeBolsaService validadeBolsaService) {
        this.validadeBolsaService = validadeBolsaService;
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void executar() {
        validadeBolsaService.verificarValidade();
    }
}
