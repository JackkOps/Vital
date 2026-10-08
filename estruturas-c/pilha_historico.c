#include "pilha_historico.h"

#include <stdlib.h>

void pilha_inicializar(PilhaHistorico *pilha) {
    pilha->topo = NULL;
}

int pilha_empilhar(PilhaHistorico *pilha, OperacaoHistorico operacao) {
    NoHistorico *novo = malloc(sizeof(NoHistorico));
    if (novo == NULL) {
        return 0;
    }

    novo->operacao = operacao;
    novo->proximo = pilha->topo;
    pilha->topo = novo;
    return 1;
}

int pilha_desempilhar(PilhaHistorico *pilha, OperacaoHistorico *saida) {
    if (pilha_vazia(pilha)) {
        return 0;
    }

    NoHistorico *removido = pilha->topo;
    if (saida != NULL) {
        *saida = removido->operacao;
    }
    pilha->topo = removido->proximo;

    free(removido);
    return 1;
}

OperacaoHistorico *pilha_topo(PilhaHistorico *pilha) {
    if (pilha_vazia(pilha)) {
        return NULL;
    }
    return &pilha->topo->operacao;
}

int pilha_vazia(PilhaHistorico *pilha) {
    return pilha->topo == NULL;
}

void pilha_destruir(PilhaHistorico *pilha) {
    OperacaoHistorico ignorada;
    while (pilha_desempilhar(pilha, &ignorada)) {
    }
}
