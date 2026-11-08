#include "heap_minimo.h"

#include <stdlib.h>

static void trocar(ItemHeap *a, ItemHeap *b) {
    ItemHeap temporario = *a;
    *a = *b;
    *b = temporario;
}

static void subir(HeapMinimo *heap, int indice) {
    while (indice > 0) {
        int pai = (indice - 1) / 2;
        if (heap->itens[pai].distancia <= heap->itens[indice].distancia) {
            return;
        }
        trocar(&heap->itens[pai], &heap->itens[indice]);
        indice = pai;
    }
}

static void descer(HeapMinimo *heap, int indice) {
    while (1) {
        int esquerdo = 2 * indice + 1;
        int direito = esquerdo + 1;
        int menor = indice;
        if (esquerdo < heap->tamanho && heap->itens[esquerdo].distancia < heap->itens[menor].distancia) {
            menor = esquerdo;
        }
        if (direito < heap->tamanho && heap->itens[direito].distancia < heap->itens[menor].distancia) {
            menor = direito;
        }
        if (menor == indice) {
            return;
        }
        trocar(&heap->itens[indice], &heap->itens[menor]);
        indice = menor;
    }
}

int heap_inicializar(HeapMinimo *heap, int capacidade_inicial) {
    heap->capacidade = capacidade_inicial > 0 ? capacidade_inicial : 1;
    heap->tamanho = 0;
    heap->itens = malloc(sizeof(ItemHeap) * (size_t) heap->capacidade);
    if (heap->itens == NULL) {
        heap->capacidade = 0;
        return 0;
    }
    return 1;
}

int heap_inserir(HeapMinimo *heap, int vertice, long distancia) {
    if (heap->tamanho == heap->capacidade) {
        int nova_capacidade = heap->capacidade * 2;
        ItemHeap *novos = realloc(heap->itens, sizeof(ItemHeap) * (size_t) nova_capacidade);
        if (novos == NULL) {
            return 0;
        }
        heap->itens = novos;
        heap->capacidade = nova_capacidade;
    }

    heap->itens[heap->tamanho].vertice = vertice;
    heap->itens[heap->tamanho].distancia = distancia;
    subir(heap, heap->tamanho);
    heap->tamanho++;
    return 1;
}

int heap_remover_minimo(HeapMinimo *heap, ItemHeap *saida) {
    if (heap_vazio(heap)) {
        return 0;
    }
    if (saida != NULL) {
        *saida = heap->itens[0];
    }
    heap->tamanho--;
    heap->itens[0] = heap->itens[heap->tamanho];
    descer(heap, 0);
    return 1;
}

int heap_vazio(HeapMinimo *heap) {
    return heap->tamanho == 0;
}

void heap_destruir(HeapMinimo *heap) {
    free(heap->itens);
    heap->itens = NULL;
    heap->tamanho = 0;
    heap->capacidade = 0;
}
