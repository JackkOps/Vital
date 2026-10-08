package com.jackops.rotavital.estrutura;

import org.springframework.stereotype.Component;

/**
 * Pilha LIFO para registrar historico simples de operacoes do estoque.
 */
@Component
public class PilhaHistorico {
    private No<String> topo;

    /**
     * Cria uma pilha vazia.
     */
    public PilhaHistorico() {
        this.topo = null;
    }

    /**
     * Empilha a descricao de uma operacao.
     */
    public synchronized void empilhar(String operacao) {
        No<String> novo = new No<>(operacao);
        novo.setProximo(topo);
        topo = novo;
    }

    /**
     * Remove e retorna a operacao do topo. Retorna null quando vazia.
     */
    public synchronized String desempilhar() {
        if (vazia()) {
            return null;
        }

        No<String> removido = topo;
        topo = removido.getProximo();
        removido.setProximo(null);
        return removido.getValor();
    }

    /**
     * Retorna a operacao do topo sem remover, ou null quando vazia.
     */
    public synchronized String topo() {
        if (vazia()) {
            return null;
        }
        return topo.getValor();
    }

    /**
     * Retorna true se a pilha estiver vazia.
     */
    public synchronized boolean vazia() {
        return topo == null;
    }

    /**
     * Remove todos os nos da pilha.
     */
    public synchronized void limpar() {
        while (desempilhar() != null) {
        }
    }
}
