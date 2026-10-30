package com.jackops.rotavital.dto;

import com.jackops.rotavital.model.enums.StatusSolicitacao;

public class RespostaAlocacaoBolsa {
    private Long bolsaId;
    private boolean alocada;
    private String mensagem;
    private long bolsasAlocadas;
    private StatusSolicitacao statusSolicitacao;

    public RespostaAlocacaoBolsa() {
    }

    public RespostaAlocacaoBolsa(Long bolsaId, boolean alocada, String mensagem,
                                 long bolsasAlocadas, StatusSolicitacao statusSolicitacao) {
        this.bolsaId = bolsaId;
        this.alocada = alocada;
        this.mensagem = mensagem;
        this.bolsasAlocadas = bolsasAlocadas;
        this.statusSolicitacao = statusSolicitacao;
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

    public long getBolsasAlocadas() {
        return bolsasAlocadas;
    }

    public void setBolsasAlocadas(long bolsasAlocadas) {
        this.bolsasAlocadas = bolsasAlocadas;
    }

    public StatusSolicitacao getStatusSolicitacao() {
        return statusSolicitacao;
    }

    public void setStatusSolicitacao(StatusSolicitacao statusSolicitacao) {
        this.statusSolicitacao = statusSolicitacao;
    }
}
