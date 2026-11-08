#include "fila_requisicoes.h"
#include "grafo.h"
#include "heap_minimo.h"
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

static void testar_heap_minimo(void) {
    HeapMinimo heap;
    ItemHeap item;
    long distancias[] = {40, 5, 27, 5, 100, 0, 13};
    long anterior = -1;

    assert(heap_inicializar(&heap, 2) == 1);
    assert(heap_vazio(&heap) == 1);
    assert(heap_remover_minimo(&heap, &item) == 0);

    /* Capacidade inicial 2: o vetor precisa crescer durante as insercoes. */
    for (int i = 0; i < 7; i++) {
        assert(heap_inserir(&heap, i, distancias[i]) == 1);
    }
    assert(heap.tamanho == 7);

    while (heap_remover_minimo(&heap, &item)) {
        assert(item.distancia >= anterior);
        anterior = item.distancia;
    }
    assert(anterior == 100);
    assert(heap_vazio(&heap) == 1);

    assert(heap_inserir(&heap, 7, 30) == 1);
    assert(heap_inserir(&heap, 3, 10) == 1);
    assert(heap_inserir(&heap, 9, 20) == 1);
    assert(heap_remover_minimo(&heap, &item) == 1 && item.vertice == 3);
    assert(heap_remover_minimo(&heap, &item) == 1 && item.vertice == 9);
    assert(heap_remover_minimo(&heap, &item) == 1 && item.vertice == 7);

    heap_destruir(&heap);
    assert(heap.itens == NULL);
}

static void testar_grafo_caminho_minimo(void) {
    Grafo grafo;
    CaminhosMinimos caminhos;
    int caminho[3];

    /* 0 -> 2 direto leva 50; passando por 1 leva 10 + 15 = 25. */
    assert(grafo_inicializar(&grafo, 3) == 1);
    assert(grafo_adicionar_aresta_dupla(&grafo, 0, 2, 50, 20.0) == 1);
    assert(grafo_adicionar_aresta_dupla(&grafo, 0, 1, 10, 8.0) == 1);
    assert(grafo_adicionar_aresta_dupla(&grafo, 1, 2, 15, 9.0) == 1);

    assert(grafo_menores_caminhos(&grafo, 0, &caminhos) == 1);
    assert(caminhos.tempos[2] == 25);
    assert(caminhos.distancias[2] == 17.0);
    assert(caminhos_montar(&caminhos, 2, caminho, 3) == 3);
    assert(caminho[0] == 0 && caminho[1] == 1 && caminho[2] == 2);
    assert(caminhos_montar(&caminhos, 2, caminho, 2) == 0);
    caminhos_destruir(&caminhos);

    grafo_destruir(&grafo);
    assert(grafo.adjacencias == NULL);
}

static void testar_grafo_desempate_e_inalcancavel(void) {
    Grafo grafo;
    CaminhosMinimos caminhos;
    int caminho[4];

    /* Dois caminhos de 20 min ate o 3: fica o de menor distancia (por 2). O 4 fica isolado. */
    assert(grafo_inicializar(&grafo, 5) == 1);
    assert(grafo_adicionar_aresta_dupla(&grafo, 0, 1, 10, 9.0) == 1);
    assert(grafo_adicionar_aresta_dupla(&grafo, 1, 3, 10, 9.0) == 1);
    assert(grafo_adicionar_aresta_dupla(&grafo, 0, 2, 10, 2.0) == 1);
    assert(grafo_adicionar_aresta_dupla(&grafo, 2, 3, 10, 2.0) == 1);

    assert(grafo_menores_caminhos(&grafo, 0, &caminhos) == 1);
    assert(caminhos.tempos[3] == 20);
    assert(caminhos_montar(&caminhos, 3, caminho, 4) == 3);
    assert(caminho[1] == 2);
    assert(caminhos_alcancavel(&caminhos, 4) == 0);
    assert(caminhos.tempos[4] == GRAFO_INALCANCAVEL);
    assert(caminhos_montar(&caminhos, 4, caminho, 4) == 0);
    assert(caminhos_montar(&caminhos, 0, caminho, 4) == 1 && caminho[0] == 0);
    caminhos_destruir(&caminhos);

    grafo_destruir(&grafo);
}

static void testar_grafo_entradas_invalidas(void) {
    Grafo grafo;
    CaminhosMinimos caminhos;

    assert(grafo_inicializar(&grafo, -1) == 0);
    assert(grafo_inicializar(&grafo, 2) == 1);
    assert(grafo_adicionar_aresta(&grafo, 0, 5, 1, 1.0) == 0);
    assert(grafo_adicionar_aresta(&grafo, 0, 1, -1, 1.0) == 0);
    assert(grafo_menores_caminhos(&grafo, 2, &caminhos) == 0);

    /* Aresta simples so vale em um sentido. */
    assert(grafo_adicionar_aresta(&grafo, 0, 1, 5, 1.0) == 1);
    assert(grafo_menores_caminhos(&grafo, 1, &caminhos) == 1);
    assert(caminhos_alcancavel(&caminhos, 0) == 0);
    caminhos_destruir(&caminhos);

    grafo_destruir(&grafo);
}

/*
 * Mesma rede da migration V4 do backend (ids - 1): HEMOPE ate o Hospital
 * Getulio Vargas leva 21 min passando pelo Agamenon Magalhaes.
 */
static void testar_grafo_rede_recife(void) {
    Grafo grafo;
    CaminhosMinimos caminhos;
    int caminho[10];
    int trechos[][2] = {{0, 1}, {0, 2}, {0, 3}, {1, 2}, {1, 4}, {3, 4}, {4, 5}, {5, 6}, {0, 6}, {6, 7}, {0, 7}, {6, 8}};
    long tempos[] = {8, 10, 12, 6, 14, 9, 11, 13, 25, 30, 40, 200};
    double distancias[] = {3.2, 4.5, 5.0, 2.1, 6.0, 3.8, 4.7, 5.9, 11.0, 24.0, 32.0, 245.0};

    assert(grafo_inicializar(&grafo, 10) == 1);
    for (int i = 0; i < 12; i++) {
        assert(grafo_adicionar_aresta_dupla(&grafo, trechos[i][0], trechos[i][1], tempos[i], distancias[i]) == 1);
    }

    assert(grafo_menores_caminhos(&grafo, 0, &caminhos) == 1);
    assert(caminhos.tempos[4] == 21);
    assert(caminhos_montar(&caminhos, 4, caminho, 10) == 3);
    assert(caminho[0] == 0 && caminho[1] == 3 && caminho[2] == 4);
    assert(caminhos.tempos[8] == 225);
    assert(caminhos_alcancavel(&caminhos, 9) == 0);
    caminhos_destruir(&caminhos);

    grafo_destruir(&grafo);
}

int main(void) {
    testar_lista_estoque();
    testar_fila_requisicoes();
    testar_pilha_historico();
    testar_heap_minimo();
    testar_grafo_caminho_minimo();
    testar_grafo_desempate_e_inalcancavel();
    testar_grafo_entradas_invalidas();
    testar_grafo_rede_recife();
    return 0;
}
