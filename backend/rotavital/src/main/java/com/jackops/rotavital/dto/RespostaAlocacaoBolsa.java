package com.jackops.rotavital.dto;

public class RespostaAlocacaoBolsa {
    private Long bolsaId;
    private boolean alocada;
    private String mensagem;

    public RespostaAlocacaoBolsa() {
    }

    public RespostaAlocacaoBolsa(Long bolsaId, boolean alocada, String mensagem) {
        this.bolsaId = bolsaId;
        this.alocada = alocada;
        this.mensagem = mensagem;
    }

    public Long getBolsaId() {
        return bolsaId;
    }

    public void setBolsaId(Long bolsaId) {
        this.bolsaId = bolsaId;
    }

    public boolean isAlocada() {
        return alocada;
    }

    public void setAlocada(boolean alocada) {
        this.alocada = alocada;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
}
