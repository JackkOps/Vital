package com.jackops.rotavital.model.enums;

public enum NivelUrgencia {
    ELETIVA(3),
    URGENTE(2),
    EMERGENCIA(1);

    /**
     * Posição do nível na fila de atendimento: 1 é atendido primeiro.
     * Dentro do mesmo nível vale a ordem de chegada.
     */
    private final int prioridade;

    NivelUrgencia(int prioridade) {
        this.prioridade = prioridade;
    }

    public int getPrioridade() {
        return prioridade;
    }
}
