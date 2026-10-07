#ifndef FILA_REQUISICOES_H
#define FILA_REQUISICOES_H

#include "bolsa.h"

typedef struct NoRequisicao {
    Requisicao requisicao;
    struct NoRequisicao *proximo;
} NoRequisicao;

typedef struct FilaRequisicoes {
    NoRequisicao *inicio;
    NoRequisicao *fim;
} FilaRequisicoes;

/*
 * Inicializa uma fila vazia.
 */
void fila_inicializar(FilaRequisicoes *fila);

/*
 * Enfileira uma requisicao no fim da fila. Retorna 1 em sucesso e 0 se nao
 * houver memoria.
 */
int fila_enfileirar(FilaRequisicoes *fila, Requisicao requisicao);

/*
 * Remove a requisicao do inicio da fila. Retorna 1 se removeu e 0 se vazia.
 */
int fila_desenfileirar(FilaRequisicoes *fila, Requisicao *saida);

/*
 * Retorna ponteiro para a requisicao da frente ou NULL se a fila estiver vazia.
 */
Requisicao *fila_frente(FilaRequisicoes *fila);

/*
 * Retorna 1 se a fila estiver vazia e 0 caso contrario.
 */
int fila_vazia(FilaRequisicoes *fila);

/*
 * Libera todos os nos da fila.
 */
void fila_destruir(FilaRequisicoes *fila);

#endif
