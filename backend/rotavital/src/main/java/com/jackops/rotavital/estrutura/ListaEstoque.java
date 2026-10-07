package com.jackops.rotavital.estrutura;

import org.springframework.stereotype.Component;

import com.jackops.rotavital.model.Bolsa;

/**
 * Lista encadeada usada como estrutura de estoque de bolsas.
 */
@Component
public class ListaEstoque {
    private No<Bolsa> inicio;

    /**
     * Cria uma lista vazia.
     */
    public ListaEstoque() {
        this.inicio = null;
    }

    /**
     * Insere a bolsa no fim da lista. Retorna false quando o identificador ja
     * existe.
     */
    public synchronized boolean inserir(Bolsa bolsa) {
        if (buscar(bolsa.getIdentificador()) != null) {
            return false;
        }

        No<Bolsa> novo = new No<>(bolsa);
        if (inicio == null) {
            inicio = novo;
            return true;
        }

        No<Bolsa> atual = inicio;
        while (atual.getProximo() != null) {
            atual = atual.getProximo();
        }
        atual.setProximo(novo);
        return true;
    }

    /**
     * Remove a bolsa pelo identificador. Retorna true quando remove.
     */
    public synchronized boolean remover(String identificador) {
        No<Bolsa> atual = inicio;
        No<Bolsa> anterior = null;

        while (atual != null && !atual.getValor().getIdentificador().equals(identificador)) {
            anterior = atual;
            atual = atual.getProximo();
        }

        if (atual == null) {
            return false;
        }

        if (anterior == null) {
            inicio = atual.getProximo();
        } else {
            anterior.setProximo(atual.getProximo());
        }
        atual.setProximo(null);
        return true;
    }

    /**
     * Busca uma bolsa pelo identificador. Retorna null quando nao encontra.
     */
    public synchronized Bolsa buscar(String identificador) {
        No<Bolsa> atual = inicio;
        while (atual != null) {
            if (atual.getValor().getIdentificador().equals(identificador)) {
                return atual.getValor();
            }
            atual = atual.getProximo();
        }
        return null;
    }

    /**
     * Busca uma bolsa pelo id persistido. Retorna null quando nao encontra.
     */
    public synchronized Bolsa buscarPorId(Long id) {
        No<Bolsa> atual = inicio;
        while (atual != null) {
            if (atual.getValor().getId() != null && atual.getValor().getId().equals(id)) {
                return atual.getValor();
            }
            atual = atual.getProximo();
        }
        return null;
    }

    /**
     * Retorna os itens em um vetor, preservando a ordem dos nos.
     */
    public synchronized Bolsa[] listar() {
        int tamanho = tamanho();
        Bolsa[] bolsas = new Bolsa[tamanho];
        No<Bolsa> atual = inicio;
        int indice = 0;
        while (atual != null) {
            bolsas[indice] = atual.getValor();
            indice++;
            atual = atual.getProximo();
        }
        return bolsas;
    }

    /**
     * Remove todos os nos da lista.
     */
    public synchronized void limpar() {
        No<Bolsa> atual = inicio;
        while (atual != null) {
            No<Bolsa> proximo = atual.getProximo();
            atual.setProximo(null);
            atual = proximo;
        }
        inicio = null;
    }

    private int tamanho() {
        int total = 0;
        No<Bolsa> atual = inicio;
        while (atual != null) {
            total++;
            atual = atual.getProximo();
        }
        return total;
    }
}
