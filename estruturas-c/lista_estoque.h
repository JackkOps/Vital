#ifndef LISTA_ESTOQUE_H
#define LISTA_ESTOQUE_H

#include "bolsa.h"

typedef struct ListaEstoque {
    No *inicio;
} ListaEstoque;

/*
 * Inicializa uma lista encadeada vazia.
 */
void lista_inicializar(ListaEstoque *lista);

/*
 * Insere uma bolsa no fim da lista. Retorna 1 em sucesso e 0 se o
 * identificador ja existir ou se nao houver memoria.
 */
int lista_inserir(ListaEstoque *lista, Bolsa bolsa);

/*
 * Remove a bolsa com o identificador informado. Retorna 1 se removeu e 0 se
 * o identificador nao foi encontrado.
 */
int lista_remover(ListaEstoque *lista, const char *identificador);

/*
 * Busca uma bolsa pelo identificador. Retorna ponteiro para a bolsa ou NULL.
 */
Bolsa *lista_buscar(ListaEstoque *lista, const char *identificador);

/*
 * Percorre a lista chamando a funcao visitante para cada bolsa.
 */
void lista_listar(ListaEstoque *lista, void (*visitante)(Bolsa *bolsa));

/*
 * Libera todos os nos da lista e deixa a lista vazia.
 */
void lista_destruir(ListaEstoque *lista);

#endif
