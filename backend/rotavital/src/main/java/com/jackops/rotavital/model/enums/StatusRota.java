package com.jackops.rotavital.model.enums;

public enum StatusRota {
    /** Cumpre a janela de tempo e foi confirmada automaticamente. */
    CONFIRMADA,
    /** Passa da janela de tempo: o operador precisa assumir o risco de atraso. */
    AGUARDANDO_CONFIRMACAO
}
