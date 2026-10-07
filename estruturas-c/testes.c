#include "fila_requisicoes.h"
#include "lista_estoque.h"
#include "pilha_historico.h"

#include <assert.h>
#include <string.h>

static Bolsa criar_bolsa(const char *identificador) {
    Bolsa bolsa = {"", "A_POS", "HEMACIAS", "2026-10-01", "2026-11-01", 450, "DISPONIVEL"};
    strcpy(bolsa.identificador, identificador);
    return bolsa;
}

static Requisicao criar_requisicao(const char *identificador) {
    Requisicao requisicao = {"", "Hospital Central", "A_POS", "HEMACIAS", 2};
    strcpy(requisicao.identificador, identificador);
    return requisicao;
}

static OperacaoHistorico criar_operacao(const char *descricao) {
    OperacaoHistorico operacao = {""};
    strcpy(operacao.descricao, descricao);
    return operacao;
}

static int total_listado = 0;

static void contar_bolsa(Bolsa *bolsa) {
    assert(bolsa != NULL);
    total_listado++;
}

static void testar_lista_estoque(void) {
    ListaEstoque lista;
    lista_inicializar(&lista);

    assert(lista_buscar(&lista, "B001") == NULL);
    assert(lista_remover(&lista, "B001") == 0);

    assert(lista_inserir(&lista, criar_bolsa("B001")) == 1);
    assert(lista_inserir(&lista, criar_bolsa("B002")) == 1);
    assert(lista_inserir(&lista, criar_bolsa("B003")) == 1);
    assert(lista_inserir(&lista, criar_bolsa("B002")) == 0);

    assert(lista_buscar(&lista, "B001") != NULL);
    assert(strcmp(lista_buscar(&lista, "B002")->identificador, "B002") == 0);

    total_listado = 0;
    lista_listar(&lista, contar_bolsa);
    assert(total_listado == 3);

    assert(lista_remover(&lista, "B001") == 1);
    assert(lista_buscar(&lista, "B001") == NULL);
    assert(lista_remover(&lista, "B003") == 1);
    assert(lista_buscar(&lista, "B003") == NULL);
    assert(lista_inserir(&lista, criar_bolsa("B004")) == 1);
    assert(lista_inserir(&lista, criar_bolsa("B005")) == 1);
    assert(lista_remover(&lista, "B004") == 1);
    assert(lista_buscar(&lista, "B002") != NULL);
    assert(lista_buscar(&lista, "B005") != NULL);
    assert(lista_remover(&lista, "NAO_EXISTE") == 0);

    lista_destruir(&lista);
    assert(lista.inicio == NULL);
}

static void testar_fila_requisicoes(void) {
    FilaRequisicoes fila;
    Requisicao removida;
    fila_inicializar(&fila);

    assert(fila_vazia(&fila) == 1);
    assert(fila_frente(&fila) == NULL);
    assert(fila_desenfileirar(&fila, &removida) == 0);

    assert(fila_enfileirar(&fila, criar_requisicao("R001")) == 1);
    assert(fila_enfileirar(&fila, criar_requisicao("R002")) == 1);
    assert(fila_enfileirar(&fila, criar_requisicao("R003")) == 1);
    assert(strcmp(fila_frente(&fila)->identificador, "R001") == 0);

    assert(fila_desenfileirar(&fila, &removida) == 1);
    assert(strcmp(removida.identificador, "R001") == 0);
    assert(fila_desenfileirar(&fila, &removida) == 1);
    assert(strcmp(removida.identificador, "R002") == 0);
    assert(fila_desenfileirar(&fila, &removida) == 1);
    assert(strcmp(removida.identificador, "R003") == 0);
    assert(fila_vazia(&fila) == 1);

    fila_destruir(&fila);
}

static void testar_pilha_historico(void) {
    PilhaHistorico pilha;
    OperacaoHistorico removida;
    pilha_inicializar(&pilha);

    assert(pilha_vazia(&pilha) == 1);
    assert(pilha_topo(&pilha) == NULL);
    assert(pilha_desempilhar(&pilha, &removida) == 0);

    assert(pilha_empilhar(&pilha, criar_operacao("cadastro B001")) == 1);
    assert(pilha_empilhar(&pilha, criar_operacao("cadastro B002")) == 1);
    assert(pilha_empilhar(&pilha, criar_operacao("remocao B001")) == 1);
    assert(strcmp(pilha_topo(&pilha)->descricao, "remocao B001") == 0);

    assert(pilha_desempilhar(&pilha, &removida) == 1);
    assert(strcmp(removida.descricao, "remocao B001") == 0);
    assert(pilha_desempilhar(&pilha, &removida) == 1);
    assert(strcmp(removida.descricao, "cadastro B002") == 0);
    assert(pilha_desempilhar(&pilha, &removida) == 1);
    assert(strcmp(removida.descricao, "cadastro B001") == 0);
    assert(pilha_vazia(&pilha) == 1);

    pilha_destruir(&pilha);
}

int main(void) {
    testar_lista_estoque();
    testar_fila_requisicoes();
    testar_pilha_historico();
    return 0;
}
