#ifndef HEAP_MINIMO_H
#define HEAP_MINIMO_H

/*
 * Item da fila de prioridade: um vertice e a distancia conhecida ate ele.
 */
typedef struct ItemHeap {
    int vertice;
    long distancia;
} ItemHeap;

/*
 * Heap binario de minimo guardado em vetor, usado pelo Dijkstra. Sai primeiro
 * o item de menor distancia. O vetor dobra de tamanho quando enche.
 */
typedef struct HeapMinimo {
    ItemHeap *itens;
    int tamanho;
    int capacidade;
} HeapMinimo;

/*
 * Inicializa um heap vazio. Retorna 1 em sucesso e 0 se nao houver memoria.
 */
int heap_inicializar(HeapMinimo *heap, int capacidade_inicial);

/*
 * Insere o item no fim do vetor e o sobe ate a posicao correta. Retorna 1 em
 * sucesso e 0 se nao houver memoria para crescer.
 */
int heap_inserir(HeapMinimo *heap, int vertice, long distancia);

/*
 * Remove o item de menor distancia. Retorna 1 se removeu e 0 se estava vazio.
 */
int heap_remover_minimo(HeapMinimo *heap, ItemHeap *saida);

/*
 * Retorna 1 se o heap estiver vazio e 0 caso contrario.
 */
int heap_vazio(HeapMinimo *heap);

/*
 * Libera o vetor do heap.
 */
void heap_destruir(HeapMinimo *heap);

#endif
