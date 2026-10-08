#include "fila_requisicoes.h"

#include <stdlib.h>

void fila_inicializar(FilaRequisicoes *fila) {
    fila->inicio = NULL;
    fila->fim = NULL;
}

int fila_enfileirar(FilaRequisicoes *fila, Requisicao requisicao) {
    NoRequisicao *novo = malloc(sizeof(NoRequisicao));
    if (novo == NULL) {
        return 0;
    }

    novo->requisicao = requisicao;
    novo->proximo = NULL;

    if (fila->fim == NULL) {
        fila->inicio = novo;
        fila->fim = novo;
        return 1;
    }

    fila->fim->proximo = novo;
    fila->fim = novo;
    return 1;
}

int fila_desenfileirar(FilaRequisicoes *fila, Requisicao *saida) {
    if (fila_vazia(fila)) {
        return 0;
    }

    NoRequisicao *removido = fila->inicio;
    if (saida != NULL) {
        *saida = removido->requisicao;
    }

    fila->inicio = removido->proximo;
    if (fila->inicio == NULL) {
        fila->fim = NULL;
    }

    free(removido);
    return 1;
}

Requisicao *fila_frente(FilaRequisicoes *fila) {
    if (fila_vazia(fila)) {
        return NULL;
    }
    return &fila->inicio->requisicao;
}

int fila_vazia(FilaRequisicoes *fila) {
    return fila->inicio == NULL;
}

void fila_destruir(FilaRequisicoes *fila) {
    Requisicao ignorada;
    while (fila_desenfileirar(fila, &ignorada)) {
    }
}
