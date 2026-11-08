package com.jackops.rotavital.model.enums;

public enum TipoComponente {
    HEMACIAS(7, 1.0, 10.0),
    PLASMA(30, -40.0, -18.0),
    PLAQUETAS(2, 20.0, 24.0),
    SANGUE_TOTAL(7, 1.0, 10.0);

    /**
     * Quantidade de dias antes do vencimento em que uma bolsa deste componente
     * passa a ser sinalizada como próxima do vencimento.
     */
    private final int diasAlertaVencimento;

    /**
     * Faixa segura de temperatura durante o transporte, em °C (história 6).
     * Hemácias e sangue total viajam refrigerados, plasma congelado e plaquetas
     * em temperatura ambiente controlada.
     */
    private final double temperaturaMinima;
    private final double temperaturaMaxima;

    TipoComponente(int diasAlertaVencimento, double temperaturaMinima, double temperaturaMaxima) {
        this.diasAlertaVencimento = diasAlertaVencimento;
        this.temperaturaMinima = temperaturaMinima;
        this.temperaturaMaxima = temperaturaMaxima;
    }

    public int getDiasAlertaVencimento() {
        return diasAlertaVencimento;
    }

    public double getTemperaturaMinima() {
        return temperaturaMinima;
    }

    public double getTemperaturaMaxima() {
        return temperaturaMaxima;
    }

    public boolean temperaturaSegura(double temperatura) {
        return temperatura >= temperaturaMinima && temperatura <= temperaturaMaxima;
    }
}
