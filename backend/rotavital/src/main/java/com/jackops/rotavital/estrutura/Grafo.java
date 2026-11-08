package com.jackops.rotavital.estrutura;

/**
 * Grafo ponderado da rede de distribuição, representado por listas de adjacência
 * encadeadas (uma lista de {@link No} por vértice). Os vértices são numerados de
 * 0 a quantidade - 1; cada aresta guarda o tempo de viagem (peso usado no
 * caminho mínimo) e a distância.
 */
public class Grafo {

    /**
     * Aresta saindo de um vértice.
     */
    public record Aresta(int destino, long tempoMinutos, double distanciaKm) {
    }

    /**
     * Resultado do Dijkstra a partir de uma origem: menor tempo até cada vértice
     * e o vértice anterior no caminho. Vértice inalcançável fica com tempo
     * {@link #INALCANCAVEL} e anterior -1.
     */
    public record CaminhosMinimos(int origem, long[] tempos, double[] distancias, int[] anteriores) {

        public boolean alcancavel(int destino) {
            return tempos[destino] != INALCANCAVEL;
        }

        /**
         * Vértices do caminho da origem até o destino, na ordem. Vazio se o
         * destino for inalcançável.
         */
        public int[] caminhoAte(int destino) {
            if (!alcancavel(destino)) {
                return new int[0];
            }
            int passos = 0;
            for (int atual = destino; atual != -1; atual = anteriores[atual]) {
                passos++;
            }
            int[] caminho = new int[passos];
            int indice = passos - 1;
            for (int atual = destino; atual != -1; atual = anteriores[atual]) {
                caminho[indice--] = atual;
            }
            return caminho;
        }
    }

    public static final long INALCANCAVEL = Long.MAX_VALUE;

    private final No<Aresta>[] adjacencias;

    /**
     * Cria um grafo sem arestas com a quantidade de vértices informada.
     */
    @SuppressWarnings("unchecked")
    public Grafo(int quantidadeVertices) {
        if (quantidadeVertices < 0) {
            throw new IllegalArgumentException("Quantidade de vértices negativa");
        }
        this.adjacencias = (No<Aresta>[]) new No[quantidadeVertices];
    }

    public int quantidadeVertices() {
        return adjacencias.length;
    }

    /**
     * Adiciona a aresta origem → destino no início da lista da origem.
     */
    public void adicionarAresta(int origem, int destino, long tempoMinutos, double distanciaKm) {
        validarVertice(origem);
        validarVertice(destino);
        if (tempoMinutos < 0) {
            throw new IllegalArgumentException("Tempo de viagem negativo");
        }
        No<Aresta> novo = new No<>(new Aresta(destino, tempoMinutos, distanciaKm));
        novo.setProximo(adjacencias[origem]);
        adjacencias[origem] = novo;
    }

    /**
     * Adiciona a aresta nos dois sentidos (estrada de mão dupla).
     */
    public void adicionarArestaDupla(int a, int b, long tempoMinutos, double distanciaKm) {
        adicionarAresta(a, b, tempoMinutos, distanciaKm);
        adicionarAresta(b, a, tempoMinutos, distanciaKm);
    }

    /**
     * Primeiro nó da lista de adjacência do vértice (null se não tiver arestas).
     */
    public No<Aresta> adjacentes(int vertice) {
        validarVertice(vertice);
        return adjacencias[vertice];
    }

    /**
     * Dijkstra com heap de mínimo: menor tempo da origem até todos os vértices.
     * Em empate de tempo, fica o caminho de menor distância.
     */
    public CaminhosMinimos menoresCaminhos(int origem) {
        validarVertice(origem);
        int n = adjacencias.length;
        long[] tempos = new long[n];
        double[] distancias = new double[n];
        int[] anteriores = new int[n];
        boolean[] fechado = new boolean[n];
        for (int i = 0; i < n; i++) {
            tempos[i] = INALCANCAVEL;
            anteriores[i] = -1;
        }

        HeapMinimo heap = new HeapMinimo(n);
        tempos[origem] = 0;
        heap.inserir(origem, 0);

        while (!heap.vazio()) {
            long tempoAtual = heap.menorDistancia();
            int atual = heap.removerMinimo();
            if (fechado[atual] || tempoAtual > tempos[atual]) {
                continue;
            }
            fechado[atual] = true;

            for (No<Aresta> no = adjacencias[atual]; no != null; no = no.getProximo()) {
                Aresta aresta = no.getValor();
                long novoTempo = tempos[atual] + aresta.tempoMinutos();
                double novaDistancia = distancias[atual] + aresta.distanciaKm();
                boolean melhor = novoTempo < tempos[aresta.destino()]
                        || (novoTempo == tempos[aresta.destino()] && novaDistancia < distancias[aresta.destino()]);
                if (!fechado[aresta.destino()] && melhor) {
                    tempos[aresta.destino()] = novoTempo;
                    distancias[aresta.destino()] = novaDistancia;
                    anteriores[aresta.destino()] = atual;
                    heap.inserir(aresta.destino(), novoTempo);
                }
            }
        }
        return new CaminhosMinimos(origem, tempos, distancias, anteriores);
    }

    private void validarVertice(int vertice) {
        if (vertice < 0 || vertice >= adjacencias.length) {
            throw new IllegalArgumentException("Vértice inexistente: " + vertice);
        }
    }
}
