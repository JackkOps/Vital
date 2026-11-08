#include "grafo.h"
#include "heap_minimo.h"

#include <stdlib.h>

static int vertice_valido(const Grafo *grafo, int vertice) {
    return vertice >= 0 && vertice < grafo->quantidade_vertices;
}

int grafo_inicializar(Grafo *grafo, int quantidade_vertices) {
    grafo->quantidade_vertices = 0;
    grafo->adjacencias = NULL;
    if (quantidade_vertices < 0) {
        return 0;
    }
    if (quantidade_vertices > 0) {
        grafo->adjacencias = calloc((size_t) quantidade_vertices, sizeof(NoAresta *));
        if (grafo->adjacencias == NULL) {
            return 0;
        }
    }
    grafo->quantidade_vertices = quantidade_vertices;
    return 1;
}

int grafo_adicionar_aresta(Grafo *grafo, int origem, int destino, long tempo_minutos, double distancia_km) {
    if (!vertice_valido(grafo, origem) || !vertice_valido(grafo, destino) || tempo_minutos < 0) {
        return 0;
    }

    NoAresta *novo = malloc(sizeof(NoAresta));
    if (novo == NULL) {
        return 0;
    }
    novo->aresta.destino = destino;
    novo->aresta.tempo_minutos = tempo_minutos;
    novo->aresta.distancia_km = distancia_km;
    novo->proximo = grafo->adjacencias[origem];
    grafo->adjacencias[origem] = novo;
    return 1;
}

int grafo_adicionar_aresta_dupla(Grafo *grafo, int a, int b, long tempo_minutos, double distancia_km) {
    return grafo_adicionar_aresta(grafo, a, b, tempo_minutos, distancia_km)
        && grafo_adicionar_aresta(grafo, b, a, tempo_minutos, distancia_km);
}

int grafo_menores_caminhos(Grafo *grafo, int origem, CaminhosMinimos *saida) {
    saida->quantidade_vertices = 0;
    saida->origem = origem;
    saida->tempos = NULL;
    saida->distancias = NULL;
    saida->anteriores = NULL;
    if (!vertice_valido(grafo, origem)) {
        return 0;
    }

    int n = grafo->quantidade_vertices;
    saida->tempos = malloc(sizeof(long) * (size_t) n);
    saida->distancias = calloc((size_t) n, sizeof(double));
    saida->anteriores = malloc(sizeof(int) * (size_t) n);
    int *fechado = calloc((size_t) n, sizeof(int));
    HeapMinimo heap;
    int heap_ok = heap_inicializar(&heap, n);

    if (saida->tempos == NULL || saida->distancias == NULL || saida->anteriores == NULL
            || fechado == NULL || !heap_ok) {
        free(fechado);
        if (heap_ok) {
            heap_destruir(&heap);
        }
        caminhos_destruir(saida);
        return 0;
    }
    saida->quantidade_vertices = n;

    for (int i = 0; i < n; i++) {
        saida->tempos[i] = GRAFO_INALCANCAVEL;
        saida->anteriores[i] = -1;
    }
    saida->tempos[origem] = 0;

    int ok = heap_inserir(&heap, origem, 0);
    ItemHeap item;
    while (ok && heap_remover_minimo(&heap, &item)) {
        int atual = item.vertice;
        /* Entrada desatualizada: o vertice ja foi fechado com um tempo menor. */
        if (fechado[atual] || item.distancia > saida->tempos[atual]) {
            continue;
        }
        fechado[atual] = 1;

        for (NoAresta *no = grafo->adjacencias[atual]; no != NULL; no = no->proximo) {
            int destino = no->aresta.destino;
            long novo_tempo = saida->tempos[atual] + no->aresta.tempo_minutos;
            double nova_distancia = saida->distancias[atual] + no->aresta.distancia_km;
            int melhor = novo_tempo < saida->tempos[destino]
                || (novo_tempo == saida->tempos[destino] && nova_distancia < saida->distancias[destino]);
            if (!fechado[destino] && melhor) {
                saida->tempos[destino] = novo_tempo;
                saida->distancias[destino] = nova_distancia;
                saida->anteriores[destino] = atual;
                ok = heap_inserir(&heap, destino, novo_tempo);
                if (!ok) {
                    break;
                }
            }
        }
    }

    free(fechado);
    heap_destruir(&heap);
    if (!ok) {
        caminhos_destruir(saida);
        return 0;
    }
    return 1;
}

int caminhos_alcancavel(const CaminhosMinimos *caminhos, int destino) {
    return destino >= 0 && destino < caminhos->quantidade_vertices
        && caminhos->tempos[destino] != GRAFO_INALCANCAVEL;
}

int caminhos_montar(const CaminhosMinimos *caminhos, int destino, int *caminho, int capacidade) {
    if (!caminhos_alcancavel(caminhos, destino)) {
        return 0;
    }

    int passos = 0;
    for (int atual = destino; atual != -1; atual = caminhos->anteriores[atual]) {
        passos++;
    }
    if (passos > capacidade) {
        return 0;
    }

    int indice = passos - 1;
    for (int atual = destino; atual != -1; atual = caminhos->anteriores[atual]) {
        caminho[indice--] = atual;
    }
    return passos;
}

void caminhos_destruir(CaminhosMinimos *caminhos) {
    free(caminhos->tempos);
    free(caminhos->distancias);
    free(caminhos->anteriores);
    caminhos->tempos = NULL;
    caminhos->distancias = NULL;
    caminhos->anteriores = NULL;
    caminhos->quantidade_vertices = 0;
}

void grafo_destruir(Grafo *grafo) {
    for (int i = 0; i < grafo->quantidade_vertices; i++) {
        NoAresta *atual = grafo->adjacencias[i];
        while (atual != NULL) {
            NoAresta *proximo = atual->proximo;
            free(atual);
            atual = proximo;
        }
    }
    free(grafo->adjacencias);
    grafo->adjacencias = NULL;
    grafo->quantidade_vertices = 0;
}
