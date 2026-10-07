package com.jackops.rotavital.estrutura;

/**
 * Representa um no encadeado com valor e referencia para o proximo no.
 */
public class No<T> {
    private T valor;
    private No<T> proximo;

    /**
     * Cria um no apontando inicialmente para null.
     */
    public No(T valor) {
        this.valor = valor;
        this.proximo = null;
    }

    /**
     * Retorna o valor armazenado no no.
     */
    public T getValor() {
        return valor;
    }

    /**
     * Altera o valor armazenado no no.
     */
    public void setValor(T valor) {
        this.valor = valor;
    }

    /**
     * Retorna a referencia para o proximo no.
     */
    public No<T> getProximo() {
        return proximo;
    }

    /**
     * Altera a referencia para o proximo no.
     */
    public void setProximo(No<T> proximo) {
        this.proximo = proximo;
    }
}
