# Observabilidade — Rota Vital

Documento vivo da entrega de Observabilidade (Infraestrutura de Comunicação, CESAR School).
Atualizado conforme o desenvolvimento avança.

- **Prazo:** sexta-feira
- **Valor:** 6,0 pontos em 8 etapas + 1,0 adicional (métricas de negócio) = 7,0 máximo
- **Aplicações Java a instrumentar:** 1 (`backend/rotavital`, Spring Boot 4.1.1 / Java 21)

---

## 1. Conceitos — o mapa das peças

Observabilidade se apoia em **três sinais**, e o trabalho exige os três:

| Sinal | O que é | Onde é armazenado |
|---|---|---|
| **Métricas** | Números agregados no tempo (req/s, heap, CPU) | Prometheus |
| **Logs** | Eventos textuais pontuais, com timestamp | Loki |
| **Traces** | O caminho de UMA requisição pelas camadas, dividido em *spans* | Tempo |

**Prometheus, Loki e Tempo não conversam entre si.** São três bancos independentes.
Quem reúne os três numa tela é o **Grafana**, que não armazena nada — só consulta.

### Push vs Pull

| Modelo | Quem toma a iniciativa | Quem usa |
|---|---|---|
| **Pull** | O servidor vai buscar (`GET` a cada 15s) | Prometheus |
| **Push** | A aplicação envia ativamente | Loki, Tempo |

Consequência prática: **quem faz push precisa do endereço configurado**.
A aplicação precisa saber onde estão Loki e Tempo. O Prometheus, ao contrário,
tem o endereço da aplicação no próprio `prometheus.yml` — a aplicação nem sabe
que está sendo observada.

### A cadeia de métricas da aplicação

```
Micrometer (API: Counter/Gauge/Timer)
  -> micrometer-registry-prometheus (traduz p/ o formato texto do Prometheus)
    -> Actuator (expõe em GET /actuator/prometheus)
      -> Prometheus (faz scrape de 15 em 15s)
        -> Grafana (consulta via PromQL e desenha)
```

- **Actuator**: módulo do Spring Boot que adiciona endpoints HTTP de gerenciamento
  (`health`, `info`, `metrics`, `prometheus`, `env`, `loggers`) sem escrever código.
- **Micrometer**: projeto independente, fachada de métricas. É o "SLF4J das métricas":
  o código usa a API do Micrometer e a implementação de destino é trocada por dependência.
- **Node Exporter / cAdvisor**: *exporters* — traduzem métricas que não falam HTTP
  (`/proc` do Linux, estatísticas do Docker) para o formato Prometheus.
  Node Exporter = o host inteiro. cAdvisor = contêiner por contêiner.
  Servem para separar "o código está lento" de "a máquina está no limite".

### Tipos de métrica (Micrometer)

| Tipo | Comportamento | Quando usar | Exemplo no Rota Vital |
|---|---|---|---|
| **Counter** | Só sobe, nunca desce | Totais acumulados | bolsas alocadas desde o start |
| **Gauge** | Sobe e desce; foto do agora | Valor instantâneo | bolsas disponíveis em estoque |
| **Timer** | Duração + contagem + histograma | "quanto tempo levou" | tempo de cálculo de rota |

Regra de bolso: *"isso pode diminuir?"* Não → Counter. Sim → Gauge. É duração → Timer.

---

## 2. Especificações (requisitos do enunciado como checklist verificável)

| ID | Especificação | Etapa | Pts |
|---|---|---|---|
| R1.1 OK | `spring-boot-starter-actuator` no `pom.xml` | E1 | |
| R1.2 OK | Registry Prometheus no classpath | E1 | |
| R1.3 OK | Endpoints `health`, `info`, `metrics`, `prometheus` expostos | E1 | |
| R1.4 OK | Tag `application=rotavital` em **todas** as métricas | E1 | 0,75 |
| R2.1 OK | `http_server_requests_seconds` presente | E2 | |
| R2.2 OK | Métricas de JVM (heap, GC, threads), CPU do processo, uptime | E2 | |
| R2.3 OK | `hikaricp_connections_*` presentes (o projeto usa Postgres) | E2 | |
| R3.1 OK | Node Exporter rodando, target UP | E2 | |
| R3.2 OK | cAdvisor rodando, target UP | E2 | 0,75 |
| R4.1 OK | `docker compose up -d` sobe **tudo**, sem passo manual | E3 | 0,75 |
| R4.2 OK | Um job no Prometheus por app e por exporter; todos UP | E3 | |
| R4.3 OK | Datasources provisionados por arquivo em `grafana/provisioning` | E3 | |
| R4.4 | Dashboard de aplicação (JVM/HTTP) com dados reais | E4 | |
| R4.5 | Dashboard de infraestrutura (host/contêineres) com dados reais | E4 | |
| R4.6 | JSON dos dashboards versionado em `grafana/dashboards/` | E4 | 0,75 |
| R5.1 | `/actuator/**` **não** exposto publicamente | E7 | |
| R5.2 | Senha do Grafana trocada, via `.env` não versionado | E7 | |
| R6.1 | Logs de todas as apps no Loki, filtráveis por aplicação | E5 | 0,75 |
| R6.2 | Traces no Tempo com múltiplos spans (controller -> service -> banco) | E6 | |
| R6.3 | `traceId` nos logs e navegação log -> trace no Grafana | E6 | 0,75 |
| R6.4 | Script de carga gerando logs e traces, **incluindo caso de erro** | E5/E6 | |
| R7.1 | Grafana acessível remotamente pelo professor | E7 | |
| R7.2 | Usuário com papel **Viewer** criado | E7 | |
| R7.3 | Gerador de carga rodando durante a janela de correção | E7 | 0,75 |
| R8.1 | README com pré-requisitos, comandos, portas/URLs | E8 | |
| R8.2 | PDF: capa, descrição, diagrama, evidências datadas, lições | E8 | 0,75 |
| A1 | >= 2 métricas de negócio com Micrometer, painel próprio | Adic. | +1,00 |

### Adaptações justificadas

- **Estrutura de pastas:** o enunciado sugere `apps/app-1/`. O Rota Vital já tem o
  backend em `backend/rotavital` com `Dockerfile` na raiz e CI apontando pra lá.
  Mover quebraria o pipeline da E3 anterior. **Decisão: manter o backend onde está**
  e criar as pastas de observabilidade na raiz.
- **Nº de aplicações:** a equipe tem **uma** aplicação web Java. O frontend é React/Vite,
  não é aplicação Java, logo não entra em R1–R2.
- **Pasta `observability/`:** em vez de `prometheus/`, `grafana/`, `loki/` e `tempo/`
  soltas na raiz (como o enunciado sugere), as quatro ficam agrupadas em
  `observability/`. Justificativa: coesão — são um subsistema único, e quatro pastas
  soltas poluem a raiz, que já tem `backend/`, `frontend/`, `docs/`, `scripts/` e
  `estruturas-c/`. **Risco aceito:** o enunciado cita o caminho literal
  `grafana/dashboards/`; o caminho real é `observability/grafana/dashboards/`.
- **Versão do Spring Boot:** o enunciado foi escrito para Boot 3.3.5; o projeto usa 4.1.1.
  As dependências e propriedades diferem (ver seção 4).

---

## 3. Decisões de arquitetura

### 3.1 Um único `compose.yaml`

A especificação R4.1 exige que `docker compose up -d` suba tudo **sem passos manuais**.
Usar dois arquivos obrigaria `docker compose -f a.yaml -f b.yaml`, o que é um passo manual.
**Decisão: estender o `compose.yaml` existente** (hoje com `api` + `db`), adicionando
todos os serviços de observabilidade na mesma rede `rotavital_network`.

### 3.2 Topologia de rede e portas

```
                    HOST (Fedora)
   +--------------------------------------------------+
   |  rede bridge: rotavital_network                  |
   |                                                  |
   |   api:8080  <--------- scrape ---- prometheus    |
   |     |  |                             ^ :9090     |
   |     |  +--- push OTLP ---> tempo     |           |
   |     |                       ^        |           |
   |   (stdout)                  |        |           |
   |     |                       |        |           |
   |   alloy ---- push ----> loki |        |           |
   |                          ^   |        |           |
   |   node-exporter <--------+---+--------+ scrape    |
   |   cadvisor      <------------+--------+ scrape    |
   |                                                  |
   |   grafana :3000 --consulta--> prometheus/loki/tempo
   +--------------------------------------------------+
            |
       SÓ a porta 3000 (Grafana) sai para fora
```

Portas publicadas no host: **somente o Grafana (3000)**.
Prometheus, `/actuator`, Loki e Tempo ficam apenas na rede interna (R5.1).
Durante o desenvolvimento, portas extras podem ser publicadas temporariamente;
antes da entrega elas são fechadas.

### 3.3 Stack escolhida

Stack **padrão** do enunciado: Prometheus + Grafana. Sem necessidade de justificar
troca no PDF.

| Papel | Escolha |
|---|---|
| Métricas (armazenamento) | Prometheus |
| Métricas de host | Node Exporter |
| Métricas de contêiner | cAdvisor |
| Logs | Loki |
| Coletor de logs | **Grafana Alloy** (ver 3.4) |
| Traces | Tempo (recebe via OTLP) |
| Visualização | Grafana |

### 3.4 Coletor de logs: Alloy, e não Promtail nem o driver Docker

Três caminhos possíveis para levar log ao Loki:

| Opção | Como funciona | Prós | Contras |
|---|---|---|---|
| **Promtail** | Agente lê arquivos de log | Muito material na internet | **EOL em 02/03/2026.** Descartado |
| **Driver Docker** | Plugin do Docker redireciona stdout | 4 linhas no compose, sem agente | Exige `docker plugin install` (passo manual, fere R4.1); bug conhecido de **deadlock do daemon** |
| **Grafana Alloy** | Contêiner que lê logs via API do Docker e empurra | Recomendado oficialmente; sem passo manual; pega logs de **todos** os contêineres (api + db); permite rotular por serviço | Um arquivo de config em sintaxe própria |

**Decisão (aprovada por Luan em 2026-11-25): Alloy.** O bug de deadlock do driver
no dia da entrega é um risco inaceitável, e o `docker plugin install` violaria R4.1.

Esclarecimento: Alloy **não** é desvio da stack padrão. A stack alternativa do
enunciado se refere a substituir o *Prometheus*. O Loki é apenas armazenamento —
ele não coleta nada, apenas recebe `POST` em `/loki/api/v1/push`. Portanto um
coletor é obrigatório em qualquer cenário; a escolha é só *qual*.

### 3.5 Tracing: OpenTelemetry via OTLP

Confirmado na documentação oficial do Spring Boot 4.1 (`/reference/actuator/tracing.html`):
tracing com OTel + OTLP exige **uma** dependência, `spring-boot-starter-opentelemetry`,
que já inclui `micrometer-tracing-bridge-otel` e `micrometer-registry-otlp`.

A ponte alternativa (Zipkin/Brave) **será removida no Spring Boot 4.2** — mais um motivo
para OTel.

Esclarecimento importante: a tabela de "stacks alternativas" do enunciado menciona
*OpenTelemetry Collector* como substituto do Prometheus na coleta de **métricas** —
esse uso foi descartado. Mas **OpenTelemetry entra no projeto de todo jeito**, como
protocolo e SDK dos **traces**, porque é assim que o Tempo recebe dados.

### 3.6 Correlação log -> trace sai de graça

A doc do Boot 4.1 confirma: com Micrometer Tracing no classpath, o Spring Boot
**já injeta `traceId` e `spanId` no MDC** e os inclui no padrão de log por padrão.
Isso atende R6.3 sem código. O que resta é configurar o Grafana para transformar
o `traceId` do Loki em link para o Tempo (campo derivado no datasource).

### 3.7 Armadilhas do ambiente Fedora (descobertas na prática)

Problemas reais encontrados que NÃO aparecem na maioria dos tutoriais (escritos
para Ubuntu/Debian). Material direto para a seção "dificuldades encontradas" do PDF.

| Problema | Causa | Solução |
|---|---|---|
| `docker compose` não existia | Fedora empacota o plugin separado do engine | `dnf install docker-compose` (v5.7.1) |
| `sudo` e `docker` falhavam no terminal do VS Code | VS Code é Flatpak; terminal roda em sandbox sem acesso ao host | `flatpak override --talk-name=org.freedesktop.Flatpak` + perfil `flatpak-spawn --host bash` |
| `permission denied` ao montar config | **SELinux Enforcing**: pasta com contexto `user_home_t`, ilegível por `container_t`. `chmod` não resolve | sufixo **`:z`** no volume (`:ro,z` em configs) |
| `unexpected promtool` | A imagem tem ENTRYPOINT fixo `/bin/prometheus`; o que vem após a imagem são argumentos | `--entrypoint promtool` |

**Regra de ouro do SELinux aprendida:** usar `:z` apenas em pastas do projeto.
Nunca em `/`, `/proc` ou `/sys` do host — reetiquetar o sistema de arquivos raiz
é destrutivo. Por isso os volumes do `node-exporter` e do `cadvisor` não o levam.

**Regra de ouro do acoplamento:** o valor em `targets:` do `prometheus.yml` deve ser
o **nome do serviço** no `compose.yaml` (não o `container_name`). Divergência entre
os dois arquivos é a causa nº 1 de target DOWN.

---

## 4. Diferenças Spring Boot 4.1.1 vs. o enunciado (3.3.5)

| Assunto | Enunciado (Boot 3.3.5) | Nosso projeto (Boot 4.1.1) |
|---|---|---|
| Starter web | `spring-boot-starter-web` | `spring-boot-starter-webmvc` |
| Autoconfig de tracing | vinha no `starter-actuator` | movido para módulos próprios |
| Tracing OTLP | várias dependências avulsas | **`spring-boot-starter-opentelemetry`** |
| Propriedades de tracing | `management.otlp.tracing.*` | `management.opentelemetry.tracing.export.otlp.*` |
| Amostragem | `management.tracing.sampling.probability` | igual |

**Risco RESOLVIDO (2026-11-25):** a inspeção do POM real de
`spring-boot-starter-actuator:4.1.1` no Maven Central mostrou que ele já declara
`spring-boot-starter-micrometer-metrics` como dependência transitiva:

```
spring-boot-starter-actuator (4.1.1)
  +-- spring-boot-actuator-autoconfigure
  +-- spring-boot-health
  +-- spring-boot-starter-micrometer-metrics   <- autoconfig de metricas
  +-- micrometer-observation
  +-- micrometer-jakarta9
```

Confirmado empiricamente: `starter-actuator` + `micrometer-registry-prometheus`
bastaram para `/actuator/prometheus` responder 200. A modularização do Boot 4 só
exige dependência extra no **tracing** (F7), via `spring-boot-starter-opentelemetry`
(verificado: existe em 4.1.1 no Maven Central).

---

## 5. Métricas de negócio (candidatas, +1,0 ponto)

O enunciado pede no mínimo 2. Candidatas mapeadas a partir dos services existentes,
priorizando as que amarram com as estruturas de dados do PI:

| Nome proposto | Tipo | O que mede | Origem no código |
|---|---|---|---|
| `rotavital.bolsas.alocadas` | Counter | Bolsas alocadas a solicitações | `AlocacaoBolsaService` |
| `rotavital.solicitacoes.fila` | Gauge | Solicitações aguardando (FIFO) | `FilaRequisicoes` |
| `rotavital.estoque.disponivel` | Gauge | Bolsas disponíveis por tipo/componente | `ListaEstoque` / `BolsaRepository` |
| `rotavital.bolsas.descartadas` | Counter | Baixas, rotulado por `MotivoBaixa` | `ValidadeBolsaService` |
| `rotavital.rotas.calculo` | Timer | Duração do cálculo de rota (Dijkstra) | `RotaService` / `HeapMinimo` |

A definir: quais implementar e quais tags usar (atenção a **cardinalidade** — tag com
muitos valores distintos, como id de bolsa, explode o Prometheus).

---

## 6. Plano de execução

Ordem pensada para que cada fase produza evidência verificável e para que
nada dependa de algo ainda inexistente.

| Fase | O que | Etapa | Por que nessa posição |
|---|---|---|---|
| **F0** | Instalar plugin `compose`; subir o Vital atual e confirmar que funciona | — | Sem isso nada roda |
| **F1** | Actuator + registry Prometheus + tag `application` | E1 | Base de tudo |
| **F2** | Prometheus, Grafana, Node Exporter, cAdvisor no compose; datasource provisionado | E2+E3 | Precisa de F1 para ter o que raspar |
| **F3** | Gerador de carga (Python) com ~3% de erro | R6.4 | **Antes** dos dashboards: sem dados não se valida painel |
| **F4** | Dashboards de aplicação e infraestrutura; exportar JSON | E4 | Precisa de dados (F3) |
| **F5** | Métricas de negócio + painel próprio | Adic. | Painel reaproveita F4; +1,0 com baixo esforço |
| **F6** | Loki + Alloy; consulta LogQL filtrando por app | E5 | Independente das métricas |
| **F7** | Tempo + `spring-boot-starter-opentelemetry`; correlação log->trace | E6 | Maior risco técnico; precisa de F6 pronto para correlacionar |
| **F8** | Acesso remoto (Tailscale/Cloudflare Tunnel) + usuário Viewer + fechar portas | E7 | Depois que o ambiente está estável |
| **F9** | README + PDF com evidências datadas | E8 | Por último, consolidando prints |

### Riscos identificados

| Risco | Impacto | Mitigação |
|---|---|---|
| Boot 4 modularizou autoconfig; dependência faltando = endpoint ausente | F1 trava | Validar com `curl` real na F1, não assumir |
| `docker compose` ausente na máquina | Bloqueio total | F0 resolve |
| Máquina do aluno desligada na janela de correção (acesso remoto caseiro) | Perde 0,75 de E7 | Avaliar VM em nuvem na F8 |
| Tempo + OTLP é a parte com menos material para Boot 4 | Perde 0,75 de E6 | Deixar F7 com folga de tempo; não deixar para a última hora |
| Cardinalidade alta nas tags de métrica de negócio | Prometheus degrada | Nunca usar id de entidade como tag |

---

## 7. Decisões pendentes

- [ ] Confirmar Alloy como coletor de logs
- [ ] Escolher quais métricas de negócio implementar (mín. 2)
- [ ] Definir a forma de acesso remoto (F8)
- [ ] Definir se o gerador de carga roda como contêiner no compose ou script avulso

---

## 8. Histórico

- **2026-11-25 (F2 — E2 e E3 CONCLUÍDAS)** — `compose.yaml` estendido com
  Prometheus v3.9.0, Node Exporter v1.13.1, cAdvisor v0.55.1 e Grafana 13.0
  (versões fixadas, sem `latest`, para reprodutibilidade). Criados
  `observability/prometheus/prometheus.yml` (4 jobs) e o provisionamento do Grafana
  (`datasources/datasources.yml` + `dashboards/dashboards.yml`). Senha do Grafana
  movida para o `.env` com `:?` no compose, que faz o `up` falhar se ela faltar.

  | Verificação | Resultado |
  |---|---|
  | `promtool check config` | SUCCESS |
  | `docker compose config` | válido |
  | Targets UP | **4 de 4** (rotavital-api, node, cadvisor, prometheus) |
  | Datasource no Grafana | provisionado automaticamente, default |
  | Séries: `rotavital-api` | 545 |
  | Séries: `node` (host) | 1.254 |
  | Séries: `cadvisor` (contêineres) | 8.972 |
  | Séries: `prometheus` | 730 |
  | **Total coletado** | **11.501 séries** |

  Pendência de segurança: a porta 9090 do Prometheus está publicada
  temporariamente para desenvolvimento e deve ser fechada na F8 (R5.1), e a senha
  do Grafana no `.env` local ainda é placeholder.

- **2026-11-25 (F1 — E1 e E2 CONCLUÍDAS)** — Adicionadas as dependências
  `spring-boot-starter-actuator` e `micrometer-registry-prometheus` (escopo `runtime`)
  e as 4 propriedades de observabilidade em `application.properties`.
  Evidências colhidas após `docker compose up -d --build`:

  | Verificação | Resultado |
  |---|---|
  | `GET /actuator/prometheus` | HTTP 200 |
  | Séries expostas | 375 |
  | Linhas com `application="rotavital"` | 297 |
  | `http_server_requests_seconds` | 216 linhas |
  | `jvm_memory_used_bytes` / `jvm_gc_*` | 8 / 8 linhas |
  | `jvm_threads_live`, `process_cpu_usage`, `process_uptime_seconds` | presentes |
  | `hikaricp_connections_*` | 16 linhas |
  | `http_server_requests_seconds_bucket` (p95/p99) | 207 linhas |

- **2026-11-25 (F0)** — Ambiente desbloqueado: plugin `compose` ausente no Fedora,
  instalado via `dnf install docker-compose` (v5.7.1). VS Code é Flatpak e seu
  terminal roda em sandbox sem `sudo` nem acesso ao socket do Docker; resolvido com
  `flatpak override --talk-name=org.freedesktop.Flatpak` + perfil de terminal
  `flatpak-spawn --host bash`. Linha de base estabelecida: `api` e `db` healthy,
  `GET /api/bolsas` retornando `[]`.

- **2026-11-25** — Levantamento inicial. Mapeado o estado do projeto, confirmadas as
  diferenças do Spring Boot 4.1.1 contra o enunciado, identificada a ausência do plugin
  `compose` na máquina de desenvolvimento e escolhida a stack.
