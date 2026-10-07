# Rota Vital

Sistema academico para gestao de estoque de hemocomponentes e requisicoes hospitalares.

Esta branch esta organizada para a entrega do Projeto Integrador da Unidade 1. O foco da entrega e demonstrar estruturas de dados implementadas manualmente em C e reimplementadas em Java para consumo pela aplicacao Spring Boot.

## Unidade 1

Funcionalidades contempladas nesta entrega:

- CRUD de bolsas de hemocomponentes.
- CRUD de solicitacoes hospitalares.
- Estoque implementado como lista encadeada.
- Requisicoes hospitalares implementadas como fila FIFO.
- Historico de operacoes implementado como pilha LIFO.
- Codigo C com ponteiros, `malloc` e `free`.
- Reimplementacao Java das mesmas estruturas, sem colecoes prontas dentro das estruturas.
- Documento de traducao comentada entre C e Java.

Funcionalidades de etapas futuras foram isoladas na branch `unidade-2`.

## Estrutura de Pastas

```text
.
|-- backend/
|   `-- rotavital/
|       |-- src/main/java/com/jackops/rotavital/
|       |   |-- controller/
|       |   |-- dto/
|       |   |-- estrutura/
|       |   |-- model/
|       |   |-- repository/
|       |   `-- service/
|       |-- src/test/java/com/jackops/rotavital/
|       `-- pom.xml
|-- docs/
|   `-- traducao-c-java.md
|-- estruturas-c/
|   |-- bolsa.h
|   |-- lista_estoque.h
|   |-- lista_estoque.c
|   |-- fila_requisicoes.h
|   |-- fila_requisicoes.c
|   |-- pilha_historico.h
|   |-- pilha_historico.c
|   |-- testes.c
|   `-- Makefile
|-- frontend/
|-- PLANO-PI.md
`-- README.md
```

## Estruturas de Dados

| Estrutura | Dominio | Arquivos C | Arquivos Java |
|---|---|---|---|
| Lista encadeada | Estoque de bolsas | `estruturas-c/lista_estoque.c` | `ListaEstoque.java` |
| Fila FIFO | Requisicoes hospitalares | `estruturas-c/fila_requisicoes.c` | `FilaRequisicoes.java` |
| Pilha LIFO | Historico de operacoes | `estruturas-c/pilha_historico.c` | `PilhaHistorico.java` |

A traducao comentada esta em [docs/traducao-c-java.md](docs/traducao-c-java.md).

## Como Compilar e Testar o C

Entre na pasta das estruturas:

```bash
cd estruturas-c
```

Compile:

```bash
make
```

Rode os testes:

```bash
make test
```

Verifique vazamentos de memoria:

```bash
make valgrind
```

O alvo `valgrind` requer o Valgrind instalado no ambiente.

## Como Rodar o Backend Java

Entre no backend:

```bash
cd backend/rotavital
```

No Linux/macOS:

```bash
./mvnw spring-boot:run
```

No Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

A API sobe em `http://localhost:8080`.

## Como Rodar os Testes Java

No Linux/macOS:

```bash
cd backend/rotavital
./mvnw test
```

No Windows:

```powershell
cd backend\rotavital
.\mvnw.cmd test
```

## Endpoints Principais

| Acao | Metodo e URL |
|---|---|
| Listar bolsas | `GET /api/bolsas` |
| Cadastrar bolsa | `POST /api/bolsas` |
| Buscar bolsa | `GET /api/bolsas/{id}` |
| Atualizar bolsa | `PUT /api/bolsas/{id}` |
| Excluir bolsa | `DELETE /api/bolsas/{id}` |
| Listar solicitacoes | `GET /api/solicitacoes` |
| Cadastrar solicitacao | `POST /api/solicitacoes` |
| Buscar solicitacao | `GET /api/solicitacoes/{id}` |
| Atualizar solicitacao | `PUT /api/solicitacoes/{id}` |
| Excluir solicitacao | `DELETE /api/solicitacoes/{id}` |

## Equipe

Projeto desenvolvido para fins academicos no curso de Analise e Desenvolvimento de Sistemas.
