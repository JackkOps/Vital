# Tradução comentada C para Java

## Por que lista, fila, pilha, heap e grafo

O estoque de bolsas foi modelado como lista encadeada porque a operação central da Unidade 1 é manter uma sequência de bolsas cadastradas, permitindo inserir novas bolsas, buscar por identificador de negócio, listar e remover um item específico sem usar coleções prontas. As requisições hospitalares foram modeladas como fila porque a estrutura é FIFO: a primeira requisição que entra é a primeira que sai. A prioridade por urgência da Entrega 04 não muda a fila; o serviço apenas a carrega com as pendentes já ordenadas por urgência e ordem de chegada. O histórico foi modelado como pilha porque a última operação registrada é a primeira consultada ou desfeita conceitualmente, seguindo LIFO. Na Entrega 04, a rede de distribuição foi modelada como grafo em lista de adjacência, porque cada hospital se liga a poucos vizinhos por trechos de estrada, e o cálculo de rota usa o Dijkstra com um heap de mínimo para escolher, a cada passo, o local ainda aberto de menor tempo acumulado.

## Lista encadeada de estoque

Na versão C, o nó da lista guarda uma `Bolsa` e um ponteiro para o próximo nó em `estruturas-c/bolsa.h:26` a `estruturas-c/bolsa.h:29`; na versão Java, a mesma ideia aparece em `backend/rotavital/src/main/java/com/jackops/rotavital/estrutura/No.java:7` e `backend/rotavital/src/main/java/com/jackops/rotavital/estrutura/No.java:8`, com `valor` e `proximo`. A inicialização da lista vazia usa `lista->inicio = NULL` em `estruturas-c/lista_estoque.c:7`, equivalente a `this.inicio = null` em `ListaEstoque.java:18`. A inserção rejeita duplicidade consultando antes de alocar em `estruturas-c/lista_estoque.c:11` e faz a mesma verificação em Java com `buscar(...) != null` em `ListaEstoque.java:26`; quando a bolsa é aceita, `malloc(sizeof(No))` em `estruturas-c/lista_estoque.c:15` corresponde a `new No<>(bolsa)` em `ListaEstoque.java:30`. O encadeamento também é espelhado: o C percorre por `atual->proximo` em `estruturas-c/lista_estoque.c:29` e liga `atual->proximo = novo` em `estruturas-c/lista_estoque.c:32`, enquanto o Java percorre por `atual.getProximo()` em `ListaEstoque.java:37` e liga com `atual.setProximo(novo)` em `ListaEstoque.java:40`. Na remoção, `free(atual)` em `estruturas-c/lista_estoque.c:55` vira `atual.setProximo(null)` em `ListaEstoque.java:65`: o Java desreferencia o nó removido e deixa o coletor de lixo liberar a memória. A busca que retorna `NULL` em `estruturas-c/lista_estoque.c:67` corresponde ao retorno `null` em `ListaEstoque.java:80`.

## Fila de requisições

Em C, a fila mantém ponteiros de início e fim inicializados com `NULL` em `estruturas-c/fila_requisicoes.c:6` e `estruturas-c/fila_requisicoes.c:7`; em Java, os campos equivalentes são `inicio` e `fim` em `FilaRequisicoes.java:12` e `FilaRequisicoes.java:13`, inicializados com `null` em `FilaRequisicoes.java:19` e `FilaRequisicoes.java:20`. O enfileiramento aloca um novo nó com `malloc(sizeof(NoRequisicao))` em `estruturas-c/fila_requisicoes.c:11`, enquanto a versão Java instancia `new No<>(solicitacao)` em `FilaRequisicoes.java:27`. Quando a fila está vazia, o C aponta `fila->inicio = novo` e `fila->fim = novo` em `estruturas-c/fila_requisicoes.c:20` e `estruturas-c/fila_requisicoes.c:21`; o Java faz o mesmo com `inicio = novo` e `fim = novo` em `FilaRequisicoes.java:30` e `FilaRequisicoes.java:31`. Para preservar FIFO, o C remove sempre `fila->inicio` em `estruturas-c/fila_requisicoes.c:35`, avança para `removido->proximo` em `estruturas-c/fila_requisicoes.c:40` e libera o nó com `free(removido)` em `estruturas-c/fila_requisicoes.c:45`; o Java remove `inicio` em `FilaRequisicoes.java:47`, avança com `removido.getProximo()` em `FilaRequisicoes.java:48` e desreferencia com `removido.setProximo(null)` em `FilaRequisicoes.java:52`. A frente vazia retorna `NULL` em `estruturas-c/fila_requisicoes.c:51`, correspondente ao `null` de `FilaRequisicoes.java:61`.

## Pilha de histórico

Na pilha C, o topo é inicializado com `pilha->topo = NULL` em `estruturas-c/pilha_historico.c:6`; no Java, `this.topo = null` em `PilhaHistorico.java:16` cumpre o mesmo papel. Empilhar em C cria o nó com `malloc(sizeof(NoHistorico))` em `estruturas-c/pilha_historico.c:10`, copia a operação em `estruturas-c/pilha_historico.c:15`, aponta o novo nó para o antigo topo em `estruturas-c/pilha_historico.c:16` e troca o topo em `estruturas-c/pilha_historico.c:17`. A versão Java repete a lógica com `new No<>(operacao)` em `PilhaHistorico.java:23`, `novo.setProximo(topo)` em `PilhaHistorico.java:24` e `topo = novo` em `PilhaHistorico.java:25`. Desempilhar remove sempre o último inserido: o C pega `pilha->topo` em `estruturas-c/pilha_historico.c:26`, avança para `removido->proximo` em `estruturas-c/pilha_historico.c:30` e chama `free(removido)` em `estruturas-c/pilha_historico.c:32`; o Java pega `topo` em `PilhaHistorico.java:36`, avança com `removido.getProximo()` em `PilhaHistorico.java:37` e desliga a referência com `removido.setProximo(null)` em `PilhaHistorico.java:38`. Quando não há item, o C usa `return NULL` em `estruturas-c/pilha_historico.c:38`, e o Java usa `return null` em `PilhaHistorico.java:47`.

## Heap de mínimo

Em C, o heap guarda um vetor de `ItemHeap` alocado com `malloc` em `estruturas-c/heap_minimo.c:44`; em Java, o mesmo par vértice e distância fica em dois vetores paralelos, criados em `HeapMinimo.java:19` e `HeapMinimo.java:20`. Inserir coloca o item no fim e o sobe enquanto o pai for maior: `subir(heap, heap->tamanho)` em `estruturas-c/heap_minimo.c:65` corresponde a `subir(tamanho)` em `HeapMinimo.java:33`. Quando o vetor enche, o C dobra a capacidade com `realloc` em `estruturas-c/heap_minimo.c:55`, e o Java copia os itens para vetores do dobro do tamanho, criados em `HeapMinimo.java:111` e `HeapMinimo.java:112`. Remover o mínimo leva o último item para a raiz e o desce: o C devolve o item pelo parâmetro de saída `*saida = heap->itens[0]` em `estruturas-c/heap_minimo.c:75`, reduz o tamanho em `estruturas-c/heap_minimo.c:77` e chama `descer(heap, 0)` em `estruturas-c/heap_minimo.c:79`; o Java reduz o tamanho em `HeapMinimo.java:56`, chama `descer(0)` em `HeapMinimo.java:59` e devolve o vértice como retorno. `heap_destruir` libera o vetor com `free(heap->itens)` em `estruturas-c/heap_minimo.c:88`; o Java não tem destruição explícita.

## Grafo e Dijkstra

O grafo C é um vetor de ponteiros para listas encadeadas de arestas, criado zerado com `calloc` em `estruturas-c/grafo.c:17`. O Java usa um vetor de listas do mesmo `No` genérico da lista e da fila, criado em `Grafo.java:61`. Adicionar uma aresta insere o nó no início da lista da origem, em tempo constante: o C aloca com `malloc(sizeof(NoAresta))` em `estruturas-c/grafo.c:31`, aponta o novo nó para a lista antiga em `estruturas-c/grafo.c:38` e troca o início em `estruturas-c/grafo.c:39`; o Java instancia `new No<>(new Aresta(...))` em `Grafo.java:77`, liga com `novo.setProximo(adjacencias[origem])` em `Grafo.java:78` e troca o início em `Grafo.java:79`.

O Dijkstra começa com todos os tempos em "inalcançável", `GRAFO_INALCANCAVEL` em `estruturas-c/grafo.c:78` e `INALCANCAVEL` em `Grafo.java:110`, zera a origem em `estruturas-c/grafo.c:81` e `Grafo.java:115` e a coloca no heap em `estruturas-c/grafo.c:83` e `Grafo.java:116`. A cada volta sai do heap o local aberto de menor tempo, em `estruturas-c/grafo.c:85` e `Grafo.java:120`. Como as duas versões não atualizam a prioridade de um item que já está no heap, um local pode entrar mais de uma vez; as entradas desatualizadas são ignoradas em `estruturas-c/grafo.c:88` e `Grafo.java:121`. O relaxamento de cada aresta aceita um tempo menor ou, no empate, uma distância menor, em `estruturas-c/grafo.c:97` e `Grafo.java:130`; quando aceita, guarda o anterior em `estruturas-c/grafo.c:102` e `Grafo.java:135` e reinsere o destino no heap em `estruturas-c/grafo.c:103` e `Grafo.java:136`. O caminho é montado andando pelos anteriores a partir do destino e preenchendo o vetor de trás para frente, em `estruturas-c/grafo.c:140` e `Grafo.java:43`.

O resultado em C ocupa três vetores alocados no heap da memória, e quem chama libera com `caminhos_destruir` em `estruturas-c/grafo.c:145`; `grafo_destruir` libera cada nó de aresta em `estruturas-c/grafo.c:160` e o vetor de listas em `estruturas-c/grafo.c:164`. Em Java, o resultado é o registro `CaminhosMinimos` de `Grafo.java:22`, recolhido pelo coletor de lixo. Vértice inexistente ou tempo negativo fazem o C devolver 0, conferindo em `estruturas-c/grafo.c:6`, enquanto o Java lança exceção em `Grafo.java:145`. A ordem de visita a vários hospitais e a janela da cadeia fria ficam no serviço Java, em `RotaService.java:222`; a versão C cobre só a estrutura e o caminho mínimo.

## Tabela de equivalência por operação

| Estrutura | Operação | C (assinatura) | Java (assinatura) |
|---|---|---|---|
| Lista | inicializar | `lista_inicializar` em `estruturas-c/lista_estoque.c:6` | `ListaEstoque` em `ListaEstoque.java:17` |
| Lista | inserir | `lista_inserir` em `estruturas-c/lista_estoque.c:10` | `inserir` em `ListaEstoque.java:25` |
| Lista | remover | `lista_remover` em `estruturas-c/lista_estoque.c:36` | `remover` em `ListaEstoque.java:47` |
| Lista | buscar | `lista_buscar` em `estruturas-c/lista_estoque.c:59` | `buscar` em `ListaEstoque.java:72` |
| Lista | listar | `lista_listar` em `estruturas-c/lista_estoque.c:70` | `listar` em `ListaEstoque.java:100` |
| Lista | destruir/limpar | `lista_destruir` em `estruturas-c/lista_estoque.c:78` | `limpar` em `ListaEstoque.java:116` |
| Fila | inicializar | `fila_inicializar` em `estruturas-c/fila_requisicoes.c:5` | `FilaRequisicoes` em `FilaRequisicoes.java:18` |
| Fila | enfileirar | `fila_enfileirar` em `estruturas-c/fila_requisicoes.c:10` | `enfileirar` em `FilaRequisicoes.java:26` |
| Fila | desenfileirar | `fila_desenfileirar` em `estruturas-c/fila_requisicoes.c:30` | `desenfileirar` em `FilaRequisicoes.java:42` |
| Fila | frente | `fila_frente` em `estruturas-c/fila_requisicoes.c:49` | `frente` em `FilaRequisicoes.java:59` |
| Fila | vazia | `fila_vazia` em `estruturas-c/fila_requisicoes.c:56` | `vazia` em `FilaRequisicoes.java:69` |
| Fila | destruir/limpar | `fila_destruir` em `estruturas-c/fila_requisicoes.c:60` | `limpar` em `FilaRequisicoes.java:92` |
| Pilha | inicializar | `pilha_inicializar` em `estruturas-c/pilha_historico.c:5` | `PilhaHistorico` em `PilhaHistorico.java:15` |
| Pilha | empilhar | `pilha_empilhar` em `estruturas-c/pilha_historico.c:9` | `empilhar` em `PilhaHistorico.java:22` |
| Pilha | desempilhar | `pilha_desempilhar` em `estruturas-c/pilha_historico.c:21` | `desempilhar` em `PilhaHistorico.java:31` |
| Pilha | topo | `pilha_topo` em `estruturas-c/pilha_historico.c:36` | `topo` em `PilhaHistorico.java:45` |
| Pilha | vazia | `pilha_vazia` em `estruturas-c/pilha_historico.c:43` | `vazia` em `PilhaHistorico.java:55` |
| Pilha | destruir/limpar | `pilha_destruir` em `estruturas-c/pilha_historico.c:47` | `limpar` em `PilhaHistorico.java:62` |
| Heap | inicializar | `heap_inicializar` em `estruturas-c/heap_minimo.c:41` | `HeapMinimo` em `HeapMinimo.java:17` |
| Heap | inserir | `heap_inserir` em `estruturas-c/heap_minimo.c:52` | `inserir` em `HeapMinimo.java:27` |
| Heap | remover mínimo | `heap_remover_minimo` em `estruturas-c/heap_minimo.c:70` | `removerMinimo` em `HeapMinimo.java:51` |
| Heap | vazio | `heap_vazio` em `estruturas-c/heap_minimo.c:83` | `vazio` em `HeapMinimo.java:63` |
| Heap | destruir | `heap_destruir` em `estruturas-c/heap_minimo.c:87` | sem equivalente (coletor de lixo) |
| Grafo | inicializar | `grafo_inicializar` em `estruturas-c/grafo.c:10` | `Grafo` em `Grafo.java:57` |
| Grafo | adicionar aresta | `grafo_adicionar_aresta` em `estruturas-c/grafo.c:26` | `adicionarAresta` em `Grafo.java:71` |
| Grafo | adicionar aresta de mão dupla | `grafo_adicionar_aresta_dupla` em `estruturas-c/grafo.c:43` | `adicionarArestaDupla` em `Grafo.java:85` |
| Grafo | menores caminhos (Dijkstra) | `grafo_menores_caminhos` em `estruturas-c/grafo.c:48` | `menoresCaminhos` em `Grafo.java:102` |
| Grafo | alcançável | `caminhos_alcancavel` em `estruturas-c/grafo.c:120` | `alcancavel` em `Grafo.java:24` |
| Grafo | montar caminho | `caminhos_montar` em `estruturas-c/grafo.c:125` | `caminhoAte` em `Grafo.java:32` |
| Grafo | destruir | `grafo_destruir` em `estruturas-c/grafo.c:155` | sem equivalente (coletor de lixo) |

## Ponteiros, null e memória

Em C, `No *`, `NoRequisicao *`, `NoHistorico *` e `NoAresta *` são ponteiros explícitos e precisam ser alocados e liberados manualmente com `malloc` e `free`. Em Java, `No<T>` é uma referência para objeto: `null` representa ausência de nó da mesma forma que `NULL` em C, mas a liberação de memória fica a cargo do garbage collector depois que a estrutura deixa de apontar para o nó removido.

## Diferenças deliberadas entre C e Java

- **Concorrência:** os métodos Java usam `synchronized`, como `ListaEstoque.java:25`, `FilaRequisicoes.java:26` e `PilhaHistorico.java:22`, pois as estruturas são componentes compartilhados em uma aplicação Spring que pode atender requisições concorrentes. O C não possui sincronização em `estruturas-c/lista_estoque.c:10`, `estruturas-c/fila_requisicoes.c:10` e `estruturas-c/pilha_historico.c:9`. A proteção Java vale para cada chamada individual; uma sequência de chamadas no serviço não se torna atômica automaticamente.
- **Grafo e heap fora do Spring:** ao contrário da lista, da fila e da pilha, `Grafo` e `HeapMinimo` não são `@Component` nem usam `synchronized`. O serviço monta um grafo novo a cada cálculo, a partir das tabelas `local_rede` e `trecho`, em `RotaService.java:208`, e cada requisição trabalha com a própria instância.
- **Busca por ID:** `buscarPorId(Long id)`, em `ListaEstoque.java:86`, foi acrescentado para os endpoints que recebem o ID persistido. No C, `lista_buscar` em `estruturas-c/lista_estoque.c:59` recebe somente o identificador de negócio; não existe busca por `Long id`.
- **Listagem:** `lista_listar`, em `estruturas-c/lista_estoque.c:70`, recebe a função visitante `void (*visitante)(Bolsa *bolsa)` e a chama para cada bolsa. Em Java, `listar()` em `ListaEstoque.java:100` devolve `Bolsa[]`; a fila também oferece `SolicitacaoSangue[]` em `FilaRequisicoes.java:76`. Os vetores permitem converter o resultado em DTOs na borda sem coleções prontas dentro das estruturas.
- **Destruição e limpeza:** `lista_destruir` em `estruturas-c/lista_estoque.c:78` libera os nós com `free(atual)` em `estruturas-c/lista_estoque.c:82`. `limpar()` em `ListaEstoque.java:116` apenas desliga as referências. Fila e pilha seguem a mesma equivalência: `estruturas-c/fila_requisicoes.c:60` e `estruturas-c/pilha_historico.c:47` correspondem a `FilaRequisicoes.java:92` e `PilhaHistorico.java:62`. O GC pode recuperar os nós Java quando não restam referências a eles; as entidades ainda referenciadas em outros lugares continuam vivas.
- **Tipos e armazenamento:** as structs C usam campos `char[]`, como `char identificador[64]` em `estruturas-c/bolsa.h:5`, `char hospital[128]` em `estruturas-c/bolsa.h:16` e `char descricao[160]` em `estruturas-c/bolsa.h:23`. O Java usa entidades JPA, declaradas por `@Entity` em `Bolsa.java:20` e `SolicitacaoSangue.java:20`, com `String` em `Bolsa.java:29` e `SolicitacaoSangue.java:32`; as descrições são `String` em `PilhaHistorico.java:22`. A lista C guarda uma cópia de `Bolsa` em `estruturas-c/bolsa.h:27` e a fila uma cópia de `Requisicao` em `estruturas-c/fila_requisicoes.h:7`. Os nós Java guardam referências às entidades `Bolsa` e `SolicitacaoSangue`, recebidas em `ListaEstoque.java:25` e `FilaRequisicoes.java:26`, por meio do campo genérico em `No.java:7`.
- **Retornos:** a remoção C devolve `int` 0/1 em `estruturas-c/lista_estoque.c:36`, e a Java devolve `boolean` em `ListaEstoque.java:47`. O C retorna o item de fila por parâmetro de saída e sucesso por `int` em `estruturas-c/fila_requisicoes.c:30`; o Java retorna a entidade ou `null` em `FilaRequisicoes.java:42`. Na pilha, `estruturas-c/pilha_historico.c:21` e `PilhaHistorico.java:31` seguem o mesmo contraste. A falha de alocação C produz 0; o Java não transforma falta de memória em `false` ou `null`, pois a alocação pode lançar `OutOfMemoryError`.

## Integração com Spring Boot e JPA

As estruturas são registradas como `@Component` em `ListaEstoque.java:10`, `FilaRequisicoes.java:10` e `PilhaHistorico.java:8`. Os serviços recebem estruturas e repositórios por construtor em `BolsaService.java:29` e `SolicitacaoSangueService.java:41`. `@PostConstruct` inicia a reconstrução do estoque em `BolsaService.java:39` e das solicitações pendentes em `SolicitacaoSangueService.java:52`.

O repositório mantém o estado persistido; as operações de estoque percorrem a lista e a chamada da próxima solicitação usa a fila. O cadastro persiste e insere a bolsa em `BolsaService.java:64` e `BolsaService.java:65`, ou persiste a solicitação em `SolicitacaoSangueService.java:68` e recarrega a fila em `SolicitacaoSangueService.java:70`, para que uma emergência possa passar à frente das que já estavam esperando. A reconstrução foi mantida para refletir alterações na persistência, conforme a justificativa de `BolsaService.java:130`; as cargas estão em `BolsaService.java:137` e `SolicitacaoSangueService.java:207`. A fila é restaurada com as solicitações pendentes ordenadas por urgência e, dentro do mesmo nível, por ID crescente (ordem de chegada), pelo comparador de `SolicitacaoSangueService.java:32` aplicado em `SolicitacaoSangueService.java:208`; a estrutura em si continua sendo uma fila FIFO simples. A listagem geral de solicitações inclui também itens que já saíram da fila e consulta a persistência em `SolicitacaoSangueService.java:78`; a listagem de pendentes passa pela estrutura em `SolicitacaoSangueService.java:162`. O histórico permanece apenas em memória e não sobrevive à reinicialização da aplicação.

## Fluxo de dados pelas estruturas

| Endpoint | Entrada na aplicação | Operação da estrutura |
|---|---|---|
| `GET /api/solicitacoes/fila` | `SolicitacaoSangueController.java:47` → `SolicitacaoSangueService.java:160` | `listar()` em `FilaRequisicoes.java:76`; devolve as pendentes em ordem FIFO sem remover |
| `POST /api/solicitacoes/fila/proxima` | `SolicitacaoSangueController.java:52` → `SolicitacaoSangueService.java:168` | `desenfileirar()` em `FilaRequisicoes.java:42`; marca `EM_ATENDIMENTO` em `SolicitacaoSangueService.java:175` e persiste em `SolicitacaoSangueService.java:176` |
| `GET /api/historico` | `HistoricoController.java:22` → `HistoricoService.java:21` | `desempilhar()` em `PilhaHistorico.java:31` e `empilhar()` em `PilhaHistorico.java:22`; uma pilha auxiliar restaura a ordem em `HistoricoService.java:34` |
| `DELETE /api/historico/ultima` | `HistoricoController.java:27` → `HistoricoService.java:41` | `desempilhar()` em `PilhaHistorico.java:31`; remove e devolve a descrição da última operação |
| `POST /api/rotas` | `RotaController.java:47` → `RotaService.java:83` | `adicionarArestaDupla()` em `Grafo.java:85` para cada trecho e `menoresCaminhos()` em `Grafo.java:102` a partir da origem, em `RotaService.java:102`, e de cada hospital de destino |

Consultar o histórico não desfaz alterações no banco; remover sua última entrada também só remove o registro em memória. Fila e histórico vazios produzem listas vazias na consulta. A chamada da próxima solicitação vazia e a remoção da última operação inexistente produzem HTTP 404.

## Verificação das citações

A partir da raiz do repositório, execute:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/verificar-citacoes.ps1
```

O script lê todas as ocorrências de referências de arquivo e linha deste documento e compara cada linha real com o trecho esperado em `scripts/citacoes-c-java.json`. Esse manifesto fixa os trechos conferidos e não é regenerado durante a verificação: alterações de linha ou conteúdo, referências novas sem expectativa e entradas obsoletas fazem o comando falhar. Nomes Java abreviados são resolvidos apenas quando existe um arquivo correspondente único no código principal.
