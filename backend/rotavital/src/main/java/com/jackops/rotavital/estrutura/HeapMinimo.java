package com.jackops.rotavital.estrutura;

/**
 * Fila de prioridade de mínimo (heap binário em vetor) usada pelo Dijkstra.
 * Cada item é um vértice com a distância conhecida até ele; sai primeiro o de
 * menor distância. Um vértice pode entrar mais de uma vez: quem usa o heap
 * ignora as entradas desatualizadas.
 */
public class HeapMinimo {
    private int[] vertices;
    private long[] distancias;
    private int tamanho;

    /**
     * Cria um heap vazio com a capacidade inicial informada.
     */
    public HeapMinimo(int capacidadeInicial) {
        int capacidade = Math.max(1, capacidadeInicial);
        this.vertices = new int[capacidade];
        this.distancias = new long[capacidade];
        this.tamanho = 0;
    }

    /**
     * Insere o vértice no fim do vetor e o sobe até a posição correta.
     */
    public void inserir(int vertice, long distancia) {
        if (tamanho == vertices.length) {
            crescer();
        }
        vertices[tamanho] = vertice;
        distancias[tamanho] = distancia;
        subir(tamanho);
        tamanho++;
    }

    /**
     * Distância do item no topo. Só pode ser chamado com o heap não vazio.
     */
    public long menorDistancia() {
        if (vazio()) {
            throw new IllegalStateException("Heap vazio");
        }
        return distancias[0];
    }

    /**
     * Remove o item de menor distância e retorna o vértice dele.
     * Só pode ser chamado com o heap não vazio.
     */
    public int removerMinimo() {
        if (vazio()) {
            throw new IllegalStateException("Heap vazio");
        }
        int vertice = vertices[0];
        tamanho--;
        vertices[0] = vertices[tamanho];
        distancias[0] = distancias[tamanho];
        descer(0);
        return vertice;
    }

    public boolean vazio() {
        return tamanho == 0;
    }

    public int tamanho() {
        return tamanho;
    }

    private void subir(int indice) {
        while (indice > 0) {
            int pai = (indice - 1) / 2;
            if (distancias[pai] <= distancias[indice]) {
                return;
            }
            trocar(indice, pai);
            indice = pai;
        }
    }

    private void descer(int indice) {
        while (true) {
            int esquerdo = 2 * indice + 1;
            int direito = esquerdo + 1;
            int menor = indice;
            if (esquerdo < tamanho && distancias[esquerdo] < distancias[menor]) {
                menor = esquerdo;
            }
            if (direito < tamanho && distancias[direito] < distancias[menor]) {
                menor = direito;
            }
            if (menor == indice) {
                return;
            }
            trocar(indice, menor);
            indice = menor;
        }
    }

    private void trocar(int a, int b) {
        int vertice = vertices[a];
        vertices[a] = vertices[b];
        vertices[b] = vertice;
        long distancia = distancias[a];
        distancias[a] = distancias[b];
        distancias[b] = distancia;
    }

    private void crescer() {
        int[] novosVertices = new int[vertices.length * 2];
        long[] novasDistancias = new long[distancias.length * 2];
        for (int i = 0; i < tamanho; i++) {
            novosVertices[i] = vertices[i];
            novasDistancias[i] = distancias[i];
        }
        vertices = novosVertices;
        distancias = novasDistancias;
    }
}
