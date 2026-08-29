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

Os indicadores utilizam análises estatísticas aplicadas ao domínio do projeto. :contentReference[oaicite:6]{index=6}

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

| Nome | Função |
|---|---|
| Allan Max de Jesus Rodrigues de Lima | Desenvolvimento |
| Boniek Araujo dos Santos Junior| Desenvolvimento |
| Caio Cesar Leandro Amorim | Desenvolvimento |
| Luan Ventura Ferreira de Moura | Desenvolvimento |
| Miguel Victor Lussac Barboza | Desenvolvimento |
| Pedro Augusto Carvalho Araujo | Desenvolvimento |
| Vinicius Pessoa de Albuquerque | Desenvolvimento |
| Wesley Yuri da Silva | Desenvolvimento |

---

# 📄 Licença

Projeto desenvolvido para fins acadêmicos no curso de **Análise e Desenvolvimento de Sistemas — CESAR School (2026.2)**.
