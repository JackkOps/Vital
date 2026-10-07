#ifndef BOLSA_H
#define BOLSA_H

typedef struct Bolsa {
    char identificador[64];
    char tipo_sanguineo[8];
    char componente[32];
    char data_coleta[11];
    char data_validade[11];
    int volume;
    char status[32];
} Bolsa;

typedef struct Requisicao {
    char identificador[64];
    char hospital[128];
    char tipo_sanguineo[8];
    char componente[32];
    int quantidade;
} Requisicao;

typedef struct OperacaoHistorico {
    char descricao[160];
} OperacaoHistorico;

typedef struct No {
    Bolsa bolsa;
    struct No *proximo;
} No;

#endif
