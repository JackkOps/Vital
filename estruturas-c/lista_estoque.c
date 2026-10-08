#include "lista_estoque.h"

#include <stdlib.h>
#include <string.h>

void lista_inicializar(ListaEstoque *lista) {
    lista->inicio = NULL;
}

int lista_inserir(ListaEstoque *lista, Bolsa bolsa) {
    if (lista_buscar(lista, bolsa.identificador) != NULL) {
        return 0;
    }

    No *novo = malloc(sizeof(No));
    if (novo == NULL) {
        return 0;
    }

    novo->bolsa = bolsa;
    novo->proximo = NULL;

    if (lista->inicio == NULL) {
        lista->inicio = novo;
        return 1;
    }

    No *atual = lista->inicio;
    while (atual->proximo != NULL) {
        atual = atual->proximo;
    }
    atual->proximo = novo;
    return 1;
}

int lista_remover(ListaEstoque *lista, const char *identificador) {
    No *atual = lista->inicio;
    No *anterior = NULL;

    while (atual != NULL && strcmp(atual->bolsa.identificador, identificador) != 0) {
        anterior = atual;
        atual = atual->proximo;
    }

    if (atual == NULL) {
        return 0;
    }

    if (anterior == NULL) {
        lista->inicio = atual->proximo;
    } else {
        anterior->proximo = atual->proximo;
    }

    free(atual);
    return 1;
}

Bolsa *lista_buscar(ListaEstoque *lista, const char *identificador) {
    No *atual = lista->inicio;
    while (atual != NULL) {
        if (strcmp(atual->bolsa.identificador, identificador) == 0) {
            return &atual->bolsa;
        }
        atual = atual->proximo;
    }
    return NULL;
}

void lista_listar(ListaEstoque *lista, void (*visitante)(Bolsa *bolsa)) {
    No *atual = lista->inicio;
    while (atual != NULL) {
        visitante(&atual->bolsa);
        atual = atual->proximo;
    }
}

void lista_destruir(ListaEstoque *lista) {
    No *atual = lista->inicio;
    while (atual != NULL) {
        No *proximo = atual->proximo;
        free(atual);
        atual = proximo;
    }
    lista->inicio = NULL;
}
