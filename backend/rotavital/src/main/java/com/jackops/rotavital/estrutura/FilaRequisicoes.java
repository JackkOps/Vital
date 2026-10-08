package com.jackops.rotavital.estrutura;

import org.springframework.stereotype.Component;

import com.jackops.rotavital.model.SolicitacaoSangue;

/**
 * Fila FIFO usada para controlar requisicoes hospitalares pendentes.
 */
@Component
public class FilaRequisicoes {
    private No<SolicitacaoSangue> inicio;
    private No<SolicitacaoSangue> fim;

    /**
     * Cria uma fila vazia.
     */
    public FilaRequisicoes() {
        this.inicio = null;
        this.fim = null;
    }

    /**
     * Enfileira a requisicao no fim da fila.
     */
    public synchronized void enfileirar(SolicitacaoSangue solicitacao) {
        No<SolicitacaoSangue> novo = new No<>(solicitacao);

        if (fim == null) {
            inicio = novo;
            fim = novo;
            return;
        }

        fim.setProximo(novo);
        fim = novo;
    }

    /**
     * Remove e retorna a requisicao no inicio. Retorna null quando vazia.
     */
    public synchronized SolicitacaoSangue desenfileirar() {
        if (vazia()) {
            return null;
        }

        No<SolicitacaoSangue> removido = inicio;
        inicio = removido.getProximo();
        if (inicio == null) {
            fim = null;
        }
        removido.setProximo(null);
        return removido.getValor();
    }

    /**
     * Retorna a requisicao da frente sem remover, ou null quando vazia.
     */
    public synchronized SolicitacaoSangue frente() {
        if (vazia()) {
            return null;
        }
        return inicio.getValor();
    }

    /**
     * Retorna true se a fila estiver vazia.
     */
    public synchronized boolean vazia() {
        return inicio == null;
    }

    /**
     * Retorna as requisicoes em vetor, preservando a ordem FIFO.
     */
    public synchronized SolicitacaoSangue[] listar() {
        int tamanho = tamanho();
        SolicitacaoSangue[] solicitacoes = new SolicitacaoSangue[tamanho];
        No<SolicitacaoSangue> atual = inicio;
        int indice = 0;
        while (atual != null) {
            solicitacoes[indice] = atual.getValor();
            indice++;
            atual = atual.getProximo();
        }
        return solicitacoes;
    }

    /**
     * Remove todos os nos da fila.
     */
    public synchronized void limpar() {
        while (desenfileirar() != null) {
        }
    }

    private int tamanho() {
        int total = 0;
        No<SolicitacaoSangue> atual = inicio;
        while (atual != null) {
            total++;
            atual = atual.getProximo();
        }
        return total;
    }
}
