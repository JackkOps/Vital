# Arquitetura do Rota Vital · Parte 2 (planejamento da remodelagem)

Squad JackkOps · Infraestrutura de Software · ADS · CESAR School · 2026.2

Material de apoio ao documento da Parte 2 (DDD, três camadas, projeção com microsserviços e três cenários com orçamento).

| Pasta / arquivo | Conteúdo |
|---|---|
| [`diagramas/`](diagramas/) | Fontes Mermaid (`.mmd`) e imagens (`.svg`, `.png`) |
| [`orcamento/orcamento-cenarios.xlsx`](orcamento/orcamento-cenarios.xlsx) | Premissas, Lei de Little, orçamento por cenário e fontes de preço |
| [`adr/0001-decomposicao.md`](adr/0001-decomposicao.md) | Monolito modular por bounded context |
| [`adr/0002-comunicacao-por-eventos.md`](adr/0002-comunicacao-por-eventos.md) | Eventos de domínio entre contextos |
| [`bdd/alocacao-requisicao.feature`](bdd/alocacao-requisicao.feature) | Cenário Gherkin da história âncora (executado na Parte 3) |

## Diagramas

| Arquivo | Assunto |
|---|---|
| `01-mapa-de-contextos` | Bounded contexts e relações (cliente-fornecedor, OHS, ACL, shared kernel, conformista) |
| `02-agregado-requisicao` | Agregado `Requisicao` do contexto Atendimento |
| `03-tres-camadas` | Camadas lógicas e tiers físicos |
| `04-microsservicos-saga` | Projeção com microsserviços: serviços, comunicação e um banco por serviço |
| `05-saga-atendimento` | Saga coreografada do atendimento, com compensações |
| `06-cenario-baixo` · `07-cenario-moderado` · `08-cenario-alto` | Infraestrutura AWS de cada cenário |

Para gerar as imagens de novo: `npx @mermaid-js/mermaid-cli -i diagramas/<nome>.mmd -o diagramas/<nome>.svg`.

## Resumo dos cenários

Preços AWS on-demand em `us-east-1`, consultados em 26/11/2026 (fontes na aba `Precos` da planilha).

| Cenário | Pico λ | L = λ·W | Instâncias | Custo mensal | Custo por pedido |
|---|---|---|---|---|---|
| Baixo (piloto RMR) | 5 req/s | 1 | 1 | US$ 60,97 | US$ 0,0135 |
| Moderado (Pernambuco) | 50 req/s | 12,5 | 2 | US$ 180,28 | US$ 0,0030 |
| Alto (Nordeste) | 500 req/s | 150 | 13 | US$ 1.415,24 | US$ 0,0016 |
