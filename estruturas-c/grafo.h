#ifndef GRAFO_H
#define GRAFO_H

#include <limits.h>

/*
 * Tempo usado para vertice que a origem nao alcanca.
 */
#define GRAFO_INALCANCAVEL LONG_MAX

/*
 * Aresta saindo de um vertice: destino, tempo de viagem (peso do caminho
 * minimo) e distancia.
 */
typedef struct Aresta {
    int destino;
    long tempo_minutos;
    double distancia_km;
} Aresta;

typedef struct NoAresta {
    Aresta aresta;
    struct NoAresta *proximo;
} NoAresta;

/*
 * Grafo ponderado da rede de distribuicao: um vetor com uma lista encadeada de
 * arestas por vertice. Os vertices vao de 0 a quantidade_vertices - 1.
 */
typedef struct Grafo {
    int quantidade_vertices;
    NoAresta **adjacencias;
} Grafo;

/*
 * Resultado do Dijkstra a partir de uma origem: menor tempo ate cada vertice,
 * distancia percorrida nesse caminho e vertice anterior (-1 na origem e nos
 * inalcancaveis).
 */
typedef struct CaminhosMinimos {
    int quantidade_vertices;
    int origem;
    long *tempos;
    double *distancias;
    int *anteriores;
} CaminhosMinimos;

/*
 * Inicializa um grafo sem arestas. Retorna 1 em sucesso e 0 se a quantidade
 * for negativa ou nao houver memoria.
 */
int grafo_inicializar(Grafo *grafo, int quantidade_vertices);

/*
 * Adiciona a aresta origem -> destino no inicio da lista da origem. Retorna 1
 * em sucesso e 0 se algum vertice nao existir, o tempo for negativo ou nao
 * houver memoria.
 */
int grafo_adicionar_aresta(Grafo *grafo, int origem, int destino, long tempo_minutos, double distancia_km);

/*
 * Adiciona a aresta nos dois sentidos (estrada de mao dupla). Retorna 1 em
 * sucesso e 0 em erro.
 */
int grafo_adicionar_aresta_dupla(Grafo *grafo, int a, int b, long tempo_minutos, double distancia_km);

/*
 * Dijkstra com heap de minimo: menor tempo da origem ate todos os vertices.
 * Em empate de tempo fica o caminho de menor distancia. Retorna 1 em sucesso
 * e 0 se a origem nao existir ou nao houver memoria.
 */
int grafo_menores_caminhos(Grafo *grafo, int origem, CaminhosMinimos *saida);

/*
 * Retorna 1 se a origem alcanca o destino e 0 caso contrario.
 */
int caminhos_alcancavel(const CaminhosMinimos *caminhos, int destino);

/*
 * Escreve em caminho os vertices da origem ate o destino, na ordem. Retorna a
 * quantidade de vertices escritos, ou 0 se o destino for inalcancavel ou nao
 * couber na capacidade informada.
 */
int caminhos_montar(const CaminhosMinimos *caminhos, int destino, int *caminho, int capacidade);

/*
 * Libera os vetores do resultado do Dijkstra.
 */
void caminhos_destruir(CaminhosMinimos *caminhos);

/*
 * Libera todas as arestas e o vetor de listas do grafo.
 */
void grafo_destruir(Grafo *grafo);

#endif
