# Traducao comentada C para Java

## Por que lista, fila e pilha

O estoque de bolsas foi modelado como lista encadeada porque a operacao central da Unidade 1 e manter uma sequencia de bolsas cadastradas, permitindo inserir novas bolsas, buscar por identificador de negocio, listar e remover um item especifico sem usar colecoes prontas. As requisicoes hospitalares foram modeladas como fila porque a regra desta unidade e FIFO: a primeira requisicao que entra e a primeira que sai, sem prioridade por urgencia ou validade. O historico foi modelado como pilha porque a ultima operacao registrada e a primeira consultada ou desfeita conceitualmente, seguindo LIFO.

## Lista encadeada de estoque

Na versao C, o no da lista guarda uma `Bolsa` e um ponteiro para o proximo no em `estruturas-c/bolsa.h:26` a `estruturas-c/bolsa.h:29`; na versao Java, a mesma ideia aparece em `backend/rotavital/src/main/java/com/jackops/rotavital/estrutura/No.java:7` e `backend/rotavital/src/main/java/com/jackops/rotavital/estrutura/No.java:8`, com `valor` e `proximo`. A inicializacao da lista vazia usa `lista->inicio = NULL` em `estruturas-c/lista_estoque.c:7`, equivalente a `this.inicio = null` em `ListaEstoque.java:18`. A insercao rejeita duplicidade consultando antes de alocar em `estruturas-c/lista_estoque.c:11` e faz a mesma verificacao em Java com `buscar(...) != null` em `ListaEstoque.java:26`; quando a bolsa e aceita, `malloc(sizeof(No))` em `estruturas-c/lista_estoque.c:15` corresponde a `new No<>(bolsa)` em `ListaEstoque.java:30`. O encadeamento tambem e espelhado: o C percorre por `atual->proximo` em `estruturas-c/lista_estoque.c:29` e liga `atual->proximo = novo` em `estruturas-c/lista_estoque.c:32`, enquanto o Java percorre por `atual.getProximo()` em `ListaEstoque.java:37` e liga com `atual.setProximo(novo)` em `ListaEstoque.java:40`. Na remocao, `free(atual)` em `estruturas-c/lista_estoque.c:55` vira `atual.setProximo(null)` em `ListaEstoque.java:65`: o Java desreferencia o no removido e deixa o coletor de lixo liberar a memoria. A busca que retorna `NULL` em `estruturas-c/lista_estoque.c:67` corresponde ao retorno `null` em `ListaEstoque.java:80`.

## Fila de requisicoes

Em C, a fila mantem ponteiros de inicio e fim inicializados com `NULL` em `estruturas-c/fila_requisicoes.c:6` e `estruturas-c/fila_requisicoes.c:7`; em Java, os campos equivalentes sao `inicio` e `fim` em `FilaRequisicoes.java:12` e `FilaRequisicoes.java:13`, inicializados com `null` em `FilaRequisicoes.java:19` e `FilaRequisicoes.java:20`. O enfileiramento aloca um novo no com `malloc(sizeof(NoRequisicao))` em `estruturas-c/fila_requisicoes.c:11`, enquanto a versao Java instancia `new No<>(solicitacao)` em `FilaRequisicoes.java:27`. Quando a fila esta vazia, o C aponta `fila->inicio = novo` e `fila->fim = novo` em `estruturas-c/fila_requisicoes.c:20` e `estruturas-c/fila_requisicoes.c:21`; o Java faz o mesmo com `inicio = novo` e `fim = novo` em `FilaRequisicoes.java:30` e `FilaRequisicoes.java:31`. Para preservar FIFO, o C remove sempre `fila->inicio` em `estruturas-c/fila_requisicoes.c:35`, avanca para `removido->proximo` em `estruturas-c/fila_requisicoes.c:40` e libera o no com `free(removido)` em `estruturas-c/fila_requisicoes.c:45`; o Java remove `inicio` em `FilaRequisicoes.java:47`, avanca com `removido.getProximo()` em `FilaRequisicoes.java:48` e desreferencia com `removido.setProximo(null)` em `FilaRequisicoes.java:52`. A frente vazia retorna `NULL` em `estruturas-c/fila_requisicoes.c:51`, correspondente ao `null` de `FilaRequisicoes.java:61`.

## Pilha de historico

Na pilha C, o topo e inicializado com `pilha->topo = NULL` em `estruturas-c/pilha_historico.c:6`; no Java, `this.topo = null` em `PilhaHistorico.java:16` cumpre o mesmo papel. Empilhar em C cria o no com `malloc(sizeof(NoHistorico))` em `estruturas-c/pilha_historico.c:10`, copia a operacao em `estruturas-c/pilha_historico.c:15`, aponta o novo no para o antigo topo em `estruturas-c/pilha_historico.c:16` e troca o topo em `estruturas-c/pilha_historico.c:17`. A versao Java repete a logica com `new No<>(operacao)` em `PilhaHistorico.java:23`, `novo.setProximo(topo)` em `PilhaHistorico.java:24` e `topo = novo` em `PilhaHistorico.java:25`. Desempilhar remove sempre o ultimo inserido: o C pega `pilha->topo` em `estruturas-c/pilha_historico.c:26`, avanca para `removido->proximo` em `estruturas-c/pilha_historico.c:30` e chama `free(removido)` em `estruturas-c/pilha_historico.c:32`; o Java pega `topo` em `PilhaHistorico.java:36`, avanca com `removido.getProximo()` em `PilhaHistorico.java:37` e desliga a referencia com `removido.setProximo(null)` em `PilhaHistorico.java:38`. Quando nao ha item, o C usa `return NULL` em `estruturas-c/pilha_historico.c:38`, e o Java usa `return null` em `PilhaHistorico.java:47`.

## Tabela de equivalencia por operacao

| Estrutura | Operacao | C | Java |
|---|---|---|---|
| Lista | inicializar | `lista_inicializar` | construtor `ListaEstoque` |
| Lista | inserir | `lista_inserir` | `inserir` |
| Lista | remover | `lista_remover` | `remover` |
| Lista | buscar | `lista_buscar` | `buscar` |
| Lista | listar | `lista_listar` | `listar` |
| Lista | destruir/limpar | `lista_destruir` | `limpar` |
| Fila | inicializar | `fila_inicializar` | construtor `FilaRequisicoes` |
| Fila | enfileirar | `fila_enfileirar` | `enfileirar` |
| Fila | desenfileirar | `fila_desenfileirar` | `desenfileirar` |
| Fila | frente | `fila_frente` | `frente` |
| Fila | vazia | `fila_vazia` | `vazia` |
| Fila | destruir/limpar | `fila_destruir` | `limpar` |
| Pilha | inicializar | `pilha_inicializar` | construtor `PilhaHistorico` |
| Pilha | empilhar | `pilha_empilhar` | `empilhar` |
| Pilha | desempilhar | `pilha_desempilhar` | `desempilhar` |
| Pilha | topo | `pilha_topo` | `topo` |
| Pilha | vazia | `pilha_vazia` | `vazia` |
| Pilha | destruir/limpar | `pilha_destruir` | `limpar` |

## Ponteiros, null e memoria

Em C, `No *`, `NoRequisicao *` e `NoHistorico *` sao ponteiros explicitos e precisam ser alocados e liberados manualmente com `malloc` e `free`. Em Java, `No<T>` e uma referencia para objeto: `null` representa ausencia de no da mesma forma que `NULL` em C, mas a liberacao de memoria fica a cargo do garbage collector depois que a estrutura deixa de apontar para o no removido.
