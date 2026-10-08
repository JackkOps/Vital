#ifndef PILHA_HISTORICO_H
#define PILHA_HISTORICO_H

#include "bolsa.h"

typedef struct NoHistorico {
    OperacaoHistorico operacao;
    struct NoHistorico *proximo;
} NoHistorico;

typedef struct PilhaHistorico {
    NoHistorico *topo;
} PilhaHistorico;

/*
 * Inicializa uma pilha vazia.
 */
void pilha_inicializar(PilhaHistorico *pilha);

/*
 * Empilha uma operacao no topo. Retorna 1 em sucesso e 0 se nao houver
 * memoria.
 */
int pilha_empilhar(PilhaHistorico *pilha, OperacaoHistorico operacao);

/*
 * Remove a operacao do topo. Retorna 1 se removeu e 0 se vazia.
 */
int pilha_desempilhar(PilhaHistorico *pilha, OperacaoHistorico *saida);

/*
 * Retorna ponteiro para a operacao no topo ou NULL se a pilha estiver vazia.
 */
OperacaoHistorico *pilha_topo(PilhaHistorico *pilha);

/*
 * Retorna 1 se a pilha estiver vazia e 0 caso contrario.
 */
int pilha_vazia(PilhaHistorico *pilha);

/*
 * Libera todos os nos da pilha.
 */
void pilha_destruir(PilhaHistorico *pilha);

#endif
