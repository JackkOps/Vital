# 🩸 Rota Vital

## Gestão e Distribuição de Hemocomponentes na Rede de Sangue

![Status](https://img.shields.io/badge/status-em%20desenvolvimento-orange)

## 📌 Sobre o Projeto

O **Rota Vital** é uma aplicação web desenvolvida para apoiar a gestão e distribuição de hemocomponentes dentro de uma rede de sangue.

A solução busca garantir que o componente sanguíneo correto esteja disponível para o hospital correto, no momento adequado e respeitando requisitos como compatibilidade sanguínea, validade dos componentes, rotas de distribuição e controle da cadeia fria.

O projeto é inspirado no fluxo da **Hemorrede/SUS**, considerando o processo entre centros de coleta e doação, hemocentros de processamento e controle, estoques e hospitais. Todos os dados utilizados no sistema são **sintéticos**, sem utilização de informações reais de pacientes ou doadores.
---

# 🎯 Objetivo

Desenvolver uma plataforma integrada capaz de:

- Gerenciar estoque de hemocomponentes;
- Controlar validade e disponibilidade das bolsas;
- Receber solicitações hospitalares;
- Realizar alocação de bolsas compatíveis;
- Priorizar componentes utilizando o conceito **FEFO (First Expire, First Out)**;
- Calcular rotas de distribuição considerando cadeia fria e janelas de tempo;
- Monitorar indicadores de estoque, demanda, temperatura e comunicação.


---

# 🚨 Problema

A rede de sangue precisa garantir:

> O componente certo, compatível e dentro da validade, no lugar certo, no tempo certo e na temperatura certa.

Falhas nesse processo podem causar:

- Desabastecimento;
- Descarte de bolsas por vencimento;
- Atrasos na distribuição;
- Riscos ao atendimento dos pacientes.

O projeto propõe uma solução integrada para conectar estoque, compatibilidade, roteirização e monitoramento da cadeia fria.

---

# 🏗️ Arquitetura da Solução

O sistema será composto por módulos responsáveis por diferentes áreas:

## 📦 Gestão de Estoque

Responsável pelo controle de:

- Hemocomponentes disponíveis;
- Tipos sanguíneos;
- Validade das bolsas;
- Entrada e saída de estoque;
- Solicitações hospitalares.

---

## 🧬 Compatibilidade Sanguínea

Implementação das regras de compatibilidade **ABO/Rh** para identificar quais bolsas podem atender determinada solicitação hospitalar.

O sistema utiliza regras didáticas de compatibilidade, não substituindo protocolos clínicos oficiais.

---

## 🚚 Roteirização

Responsável pelo cálculo das melhores rotas entre unidades da rede utilizando algoritmos de grafos.

Funcionalidades:

- Representação da malha de transporte;
- Cálculo de caminhos mínimos;
- Otimização da distribuição.

Algoritmo utilizado:

- Dijkstra (caminho mínimo).

---

## 📊 Indicadores e Análises

O sistema apresenta informações para apoiar decisões:

- Estoque por componente;
- Demanda por hospital;
- Tempo de atendimento;
- Probabilidade de desabastecimento;
- Taxa de descarte por vencimento.

Os indicadores utilizam análises estatísticas aplicadas ao domínio do projeto.

---

## 🌡️ Monitoramento da Cadeia Fria

Simulação de telemetria para acompanhamento de:

- Temperatura durante transporte;
- Comunicação entre unidades;
- Métricas de rede.

Os dados de telemetria são simulados para fins acadêmicos. 
---

# 🛠️ Tecnologias Utilizadas

## Backend

- Java
- Spring Boot
- API REST

## Banco de Dados

- *(Definir tecnologia utilizada pelo grupo)*

## Infraestrutura

- CI/CD
- Cloud
- Containers *(caso aplicável)*

## Algoritmos e Estruturas de Dados

- Grafos
- Dijkstra
- Hash
- Filas de prioridade
- Matching de compatibilidade

## Estatística

- Estatística descritiva
- Probabilidade
- Indicadores operacionais


---

# 📂 Estrutura do Projeto

```text
rota-vital/
│
├── backend/
│   ├── src/
│   └── pom.xml
│
├── frontend/
│   └── ...
│
├── docs/
│
├── README.md
└── docker-compose.yml
```
*(Estrutura pode variar conforme implementação final.)*

---

# 🚀 Funcionalidades

## Unidade 1

- [ ] Modelagem do domínio da rede de sangue
- [ ] CRUD das entidades principais
- [ ] Controle de validade dos componentes
- [ ] Grafo da malha de transporte
- [ ] Algoritmo de caminho mínimo
- [ ] Estrutura de estoque utilizando hash
- [ ] Priorização FEFO
- [ ] Primeiro deploy da aplicação

## Unidade 2

- [ ] Compatibilidade ABO/Rh
- [ ] Matching entre requisição e bolsa
- [ ] Integração entre rota, validade e compatibilidade
- [ ] Painéis estatísticos
- [ ] Monitoramento de telemetria
- [ ] Pipeline CI/CD completo

---

# 🔒 Segurança e Privacidade

O projeto segue as seguintes restrições:

- Utilização exclusiva de dados sintéticos;
- Nenhum dado real de pacientes ou doadores é armazenado;
- A compatibilidade sanguínea possui finalidade educacional;
- Não existe integração com sistemas oficiais da Hemorrede.


---

# 👥 Disciplinas Envolvidas

O projeto integra conhecimentos de:

| Disciplina | Contribuição |
|---|---|
| Programação Orientada a Objetos | Aplicação Java/Spring Boot e regras de negócio |
| Algoritmos e Estruturas de Dados | Rotas, compatibilidade e priorização |
| Estatística e Probabilidade | Indicadores e análises |
| Infraestrutura de Software | Cloud, CI/CD e concorrência |
| Infraestrutura de Comunicação | Redes, APIs e telemetria |
| Projeto Integrador | Organização e integração da equipe |

---

# 📦 Entrega 01

## 📖 Histórias de Usuário

Foram definidas **7 histórias de usuário**, documentadas com descrição da necessidade de negócio, critérios de discussão, cenários de validação em **BDD (Behavior-Driven Development)**, avaliação pelos critérios **INVEST** e diagramas de atividades.

1. **Cadastro de bolsa de hemocomponente**
2. **Alocação de bolsa compatível priorizando validade (FEFO)**
3. **Cálculo de rota de distribuição**
4. **Solicitação hospitalar de hemocomponentes**
5. **Alerta e baixa de bolsas próximas do vencimento**
6. **Monitoramento da cadeia fria durante o transporte**
7. **Painel de indicadores da rede de sangue**

🔗 **[Acessar Histórias de Usuário](./docs/rota-vital-historias-usuario.md)**

---

## 🎨 Protótipo Lo-Fi

O protótipo de baixa fidelidade da aplicação está disponível no Figma.

🔗 **[Acessar Protótipo Lo-Fi no Figma](https://www.figma.com/design/LGNH0CWyaNMfClKrTKIotp/Rota-Vital?node-id=0-1&p=f&t=X0556s3iPCew6liI-0)**

---

## 🎥 Screencast

O screencast apresenta o protótipo desenvolvido e as histórias de usuário contempladas nesta etapa do projeto.

🔗 **[Assistir ao Screencast no YouTube](https://youtu.be/ahV21_baUUQ)**

---

## 📎 Artefatos

| Artefato | Acesso |
|---|---|
| Histórias de Usuário | [Visualizar](./docs/rota-vital-historias-usuario.md) |
| Protótipo Lo-Fi | [Acessar Figma](https://www.figma.com/design/LGNH0CWyaNMfClKrTKIotp/Rota-Vital?node-id=0-1&p=f&t=X0556s3iPCew6liI-0) |
| Screencast | [Assistir no YouTube](https://youtu.be/ahV21_baUUQ) |

---

# 📦 Entrega 02

## Histórias implementadas

> **História 1 - Cadastro de bolsa de hemocomponente**
>
> Como operador do banco de sangue, quero registrar uma bolsa no estoque para que ela fique disponível para alocação e rastreamento.
> **Critérios entregues:** identificador único de negócio; campos obrigatórios e volume positivo; bloqueio de duplicidade; validade igual ou posterior à coleta; status inicial `DISPONIVEL`; cadastro e consulta de estoque pela API.

> **História 2 - Alocação de bolsa compatível priorizando validade (FEFO)**
>
> Como operador, quero que o sistema selecione a bolsa compatível que vence primeiro para reduzir descarte.
> **Critérios entregues:** compatibilidade ABO/Rh acadêmica; filtro de bolsas disponíveis e não vencidas; mesmo componente da solicitação; ordenação FEFO; alteração do status para `ALOCADA`; retorno claro quando não há estoque compatível.

## Como executar e demonstrar

No terminal, entre em `backend/rotavital` e execute `./mvnw.cmd spring-boot:run`. A API ficará disponível em `http://localhost:8080`.

Endpoints usados na demonstração:

| Ação | Método e URL |
|---|---|
| Cadastrar bolsa | `POST /api/bolsas` |
| Consultar estoque | `GET /api/bolsas` |
| Criar solicitação de apoio | `POST /api/solicitacoes` |
| Alocar por FEFO | `POST /api/alocacoes/solicitacoes/{solicitacaoId}/alocar` |

Os testes automatizados podem ser executados com `./mvnw.cmd test`.

## Issue / Bug Tracker

> **Espaço reservado para o print do GitHub Issues**
>
> Adicione aqui a captura de tela das issues usadas na Entrega 02 (cadastro de bolsa, validação de datas, compatibilidade ABO/Rh, FEFO e correções).

![Print do GitHub Issues](docs/images/github-issues-entrega-02.png)

## Screencasts

- Vídeo 1 - Uso do sistema: **[assistir no YouTube](https://youtu.be/awFqNrnNcWk)**
- Vídeo 2 - Explicação do código: **[assistir no YouTube](https://youtu.be/HQX0xPfTYAI)**
- Roteiros curtos: [docs/roteiros-videos-entrega-02.md](./docs/roteiros-videos-entrega-02.md)

---

# 📈 Roadmap

## Fase 1 — Modelagem e Base do Sistema

- Definição do domínio;
- Estrutura inicial do backend;
- Implementação das estruturas de dados;
- Configuração inicial de infraestrutura.

## Fase 2 — Integração

- Compatibilidade sanguínea;
- Roteirização;
- Painéis;
- Telemetria.

## Fase 3 — Finalização

- Testes;
- Deploy;
- Documentação;
- Apresentação final.

---

# 👨‍💻 Equipe

| Nome | Email | Função |
|---|---|---|
| Allan Max de Jesus Rodrigues de Lima | amjrl@cesar.school | Desenvolvimento |
| Boniek Araujo dos Santos Junior| basj@cesar.school | Desenvolvimento |
| Caio Cesar Leandro Amorim | ccla@cesar.school | Desenvolvimento |
| Luan Ventura Ferreira de Moura | lvfm2@cesar.school | Desenvolvimento |
| Miguel Victor Lussac Barboza | mvlb@cesar.school | Desenvolvimento |
| Pedro Augusto Carvalho Araujo | paca@cesar.school | Desenvolvimento |
| Vinicius Pessoa de Albuquerque | vpa@cesar.school | Desenvolvimento |
| Wesley Yuri da Silva | wys@cesar.school | Desenvolvimento |

---

# 📄 Licença

Projeto desenvolvido para fins acadêmicos no curso de **Análise e Desenvolvimento de Sistemas — CESAR School (2026.2)**.
