package com.jackops.rotavital.model.enums;

public enum CondicaoRemessa {
    /** Todas as leituras recebidas ficaram dentro da faixa do componente. */
    CONFORME,
    /** Houve ao menos uma excursão térmica: a carga precisa ser avaliada. */
    SOB_RISCO
}
