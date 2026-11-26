# ADR 0001: Decomposição do Rota Vital

Status: proposto · Data: 26/11/2026 · Squad JackkOps

## Contexto

O Rota Vital é hoje uma API Spring Boot única, com pacotes por tipo técnico (`controller`, `service`, `model`, `repository`) e um banco PostgreSQL. A remodelagem com DDD identificou seis bounded contexts: Estoque, Atendimento, Logística, Cadeia Fria, Indicadores e Identidade e Acesso.

As premissas de carga (planilha `orcamento/orcamento-cenarios.xlsx`) dão 5 req/s no cenário baixo, 50 req/s no moderado e 500 req/s no alto. Pela Lei de Little, o baixo e o moderado cabem em 1 e 2 instâncias de um único serviço. O squad tem 8 integrantes, uma única esteira de CI/CD e o prazo da Parte 3 para materializar o plano. A alocação de bolsas precisa de consistência forte: a mesma bolsa não pode ir para duas requisições, e o código atual garante isso com uma transação e bloqueio no banco.

## Decisão

Adotar um **monolito modular**: um único deploy, organizado em um módulo por bounded context, cada módulo com as camadas apresentação, negócio e dados, e comunicação entre módulos só por interfaces de aplicação e eventos de domínio. A extração para microsserviços fica reservada ao cenário alto, começando pela Cadeia Fria (carga de telemetria independente) e por Indicadores (modelo de leitura).

## Consequências

- **Melhora:** os limites entre contextos ficam explícitos no código sem custo de operação distribuída; a alocação continua numa transação local; um deploy e um banco mantêm o custo do cenário moderado em cerca de US$ 180 por mês.
- **Piora:** todos os módulos escalam juntos e uma falha grave derruba o sistema inteiro; a disciplina de não acessar tabelas de outro módulo depende de revisão de código e de testes de arquitetura (ArchUnit), e não de uma fronteira de rede.
- **Alternativa descartada:** microsserviços desde já, um por contexto. Exigiria cinco bancos, barramento de eventos e saga para a alocação já no cenário baixo, multiplicando o custo e a complexidade sem uma carga que justifique.
