package com.jackops.rotavital.model.enums;

public enum TipoComponente {
    HEMACIAS(7),
    PLASMA(30),
    PLAQUETAS(2),
    SANGUE_TOTAL(7);

    /**
     * Quantidade de dias antes do vencimento em que uma bolsa deste componente
     * passa a ser sinalizada como próxima do vencimento.
     */
    private final int diasAlertaVencimento;

    TipoComponente(int diasAlertaVencimento) {
        this.diasAlertaVencimento = diasAlertaVencimento;
    }

    public int getDiasAlertaVencimento() {
        return diasAlertaVencimento;
    }
}
