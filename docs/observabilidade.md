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
| R4.4 OK | Dashboard de aplicação (JVM/HTTP) com dados reais | E4 | |
| R4.5 OK | Dashboard de infraestrutura (host/contêineres) com dados reais | E4 | |
| R4.6 OK | JSON dos dashboards versionado em `grafana/dashboards/` | E4 | 0,75 |
| R5.1 OK | `/actuator/**` **não** exposto publicamente | E7 | |
| R5.2 OK | Senha do Grafana trocada, via `.env` não versionado | E7 | |
| R6.1 OK | Logs de todas as apps no Loki, filtráveis por aplicação | E5 | 0,75 |
| R6.2 OK | Traces no Tempo com múltiplos spans (controller -> service -> banco) | E6 | |
| R6.3 OK | `traceId` nos logs e navegação log -> trace no Grafana | E6 | 0,75 |
| R6.4 OK | Script de carga gerando logs e traces, **incluindo caso de erro** | E5/E6 | |
| R7.1 OK | Grafana acessível remotamente pelo professor | E7 | |
| R7.2 OK | Usuário com papel **Viewer** criado | E7 | |
| R7.3 OK | Gerador de carga rodando durante a janela de correção | E7 | 0,75 |
| R8.1 | README com pré-requisitos, comandos, portas/URLs | E8 | |
| R8.2 | PDF: capa, descrição, diagrama, evidências datadas, lições | E8 | 0,75 |
| A1 OK | >= 2 métricas de negócio com Micrometer, painel próprio | Adic. | +1,00 |

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

| `'dependencies.dependency.version' is missing` para `spring-boot-starter-aop` | No Boot 4 o starter foi renomeado; o nome antigo existe no Maven Central mas **não é gerenciado pelo BOM 4.x** | usar **`spring-boot-starter-aspectj`** |
| Métricas de negócio não apareciam no `/actuator/prometheus` | Banco vazio: `GROUP BY` sem linhas = MultiGauge sem séries. Não havia erro algum — simplesmente não havia o que publicar | gerador de carga passou a fazer **escrita** (POST) e a semear o banco |
| Nome exportado `rotavital_bolsas_estoque_bolsas` | `baseUnit("bolsas")` é anexado ao fim do nome pelo Micrometer | remover `baseUnit` quando o nome já carrega a unidade |
| `List<Row<capture of ?>> cannot be converted to List<Row<?>>` | Inferência de genéricos em `.map(...).toList()` encadeado | montar `List<MultiGauge.Row<?>>` com laço e `ArrayList` |

| Alloy: `permission denied while trying to connect to the docker API` | SELinux: mesmo como root, o processo roda com label `container_t`, sem acesso ao socket do daemon | `security_opt: [label:disable]` (mais cirúrgico que `privileged: true`) |
| `jdbc.includes=connection,query` não surtiu efeito | Os valores são o **enum em MAIÚSCULAS**; em minúsculas a propriedade é silenciosamente ignorada | `jdbc.includes=CONNECTION,QUERY` + `jdbc.resultset-operations.enabled=false` |
| Nenhum log de nível ERROR na aplicação | O Spring trata `ResponseStatusException` como erro esperado e não emite log ERROR | `log.error(...)` explícito antes de lançar |
| traceId não aparecia na resposta da API do Loki | O metadado existe, mas a serialização padrão não o inclui | header `X-Loki-Response-Encoding-Flags: categorize-labels` (o Grafana já envia) |

| Grafana rejeitava a senha nova (`401 Invalid username or password`) | **`GF_SECURITY_ADMIN_PASSWORD` só tem efeito no PRIMEIRO boot.** Depois a senha vive no volume `grafana_data` e a variável é ignorada | apagar o volume e recriar (tudo é provisionado), ou `grafana cli admin reset-admin-password` |
| `SyntaxError` no script do Viewer | Aspas duplas do Python dentro de string Bash com escaping | usar `%`-format e `aspas simples` no `python3 -c` |

**Regra de ouro do acoplamento:** o valor em `targets:` do `prometheus.yml` deve ser
o **nome do serviço** no `compose.yaml` (não o `container_name`). Divergência entre
os dois arquivos é a causa nº 1 de target DOWN.

### 3.8 PromQL: as cinco construções usadas nos dashboards

Toda consulta dos painéis é combinação destas cinco ideias.

**1. Seletor** — escolhe séries por nome e labels.

    http_server_requests_seconds_count{job="rotavital-api", status="200"}

`=` igual, `!=` diferente, `=~` casa regex, `!~` não casa regex.

**2. `rate(...[5m])`** — obrigatório para *counter*. Um counter só cresce, então seu
valor absoluto não diz nada ("1.432 requisições desde o boot"). `rate` devolve a
variação média por segundo dentro da janela, resistindo a reinícios da aplicação.

    sum(rate(http_server_requests_seconds_count{job="rotavital-api"}[5m]))

Regra: **counter sempre entra em `rate()`; gauge nunca.**

**3. `sum(...) by (label)`** — agrega várias séries, mantendo apenas os labels listados.
Sem `by`, viram um número só; com `by (uri)`, uma linha por rota.

**4. `histogram_quantile(0.95, ...)`** — calcula percentil a partir dos buckets.
Exige `by (le)` (*less or equal*, o label dos buckets) e só funciona se os buckets
existirem — é o que a propriedade `percentiles-histogram` habilitou.

    histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket{...}[5m])) by (le))

Por que p95 em vez de média: a média esconde picos. Se 95 requisições levam 10ms e
5 levam 2s, a média dá 109ms — número que ninguém experimentou. O p95 revela os 2s.

**5. Divisão para proporções** — numerador e denominador com a mesma agregação:

    sum(rate(http_server_requests_seconds_count{status=~"4..|5.."}[5m]))
    / sum(rate(http_server_requests_seconds_count[5m]))

**Truque do Node Exporter:** ele não expõe "uso de CPU", expõe tempo **ocioso**.
Daí `1 - avg(rate(node_cpu_seconds_total{mode="idle"}[5m]))`.

**Pegadinha de médias sem histograma:** quando não há buckets (GC, repositories),
a média sai de `rate(_sum) / rate(_count)` — nunca de `_sum` sozinho.

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

## 5. Métricas de negócio (adicional, +1,0 ponto)

Tabela no formato exigido pelo item 5 do PDF (nome, tipo, o que mede).

| Nome exportado | Tipo | O que mede | Tags | Origem |
|---|---|---|---|---|
| `rotavital_bolsas_estoque` | **Gauge** | Bolsas em estoque agora | `componente`, `status` | `GROUP BY` sobre `bolsa` |
| `rotavital_solicitacoes_quantidade` | **Gauge** | Solicitações por situação | `status`, `urgencia` | `GROUP BY` sobre `solicitacao_sangue` |
| `rotavital_remessas_quantidade` | **Gauge** | Remessas em transporte | `status`, `condicao` | `GROUP BY` sobre `remessa` |
| `rotavital_bolsas_alocadas_total` | **Counter** | Alocações realizadas | `result`, `exception`, `method` | `@Counted` em `AlocacaoBolsaService.alocarBolsaCompativel` |
| `rotavital_rotas_calculo_seconds` | **Timer** | Duração do cálculo de rota (Dijkstra) | `method`, `exception` | `@Timed` em `RotaService.calcular` |

### Por que cada tipo

- **Gauge** para estoque e filas: o valor **sobe e desce** (bolsa entra por doação, sai
  por alocação ou descarte). Um Counter responderia "bolsas já cadastradas na
  história", que não é a pergunta operacional — a pergunta é "quanto tenho agora".
- **Counter** para alocações: é um evento acumulado que nunca decresce. Nunca se lê o
  valor absoluto; lê-se `rate(...)`, que responde "alocações por minuto".
- **Timer** para o cálculo de rota: a pergunta é "quanto tempo levou". Com
  `percentiles-histogram` habilitado para esta métrica, dá p95 — e comparar p95 com a
  média mostra o quanto a média esconde os piores casos.

### Decisão de implementação: anotações em vez de injeção

Instrumentar via `MeterRegistry` injetado exigiria **um parâmetro novo no construtor**
de cada service. Os testes existentes instanciam os services diretamente
(`new AlocacaoBolsaService(...)`), então a mudança de assinatura quebraria a suíte
inteira por um motivo sem relação com regra de negócio.

Solução: `spring-boot-starter-aspectj` + beans `TimedAspect` e `CountedAspect`
(`config/MetricasAspectosConfig`). Instrumentar passou a ser **uma anotação no
método**; o código de negócio não conhece o Micrometer e os construtores ficaram
intactos. **Resultado: 184 testes seguem passando.**

Limitação conhecida: aspectos funcionam por proxy, logo só valem em chamadas que
atravessam o proxy do Spring (controller -> service). Chamada entre métodos da mesma
classe não é interceptada.

### Cardinalidade

As tags têm domínio pequeno e fechado: 4 status x 4 componentes = 16 séries no
máximo; 4 status x 3 urgências = 12. **Identificador de bolsa como tag criaria uma
série nova por bolsa, para sempre**, e degradaria o Prometheus. Nunca usar id como tag.

## 5.1 Achado de performance: problema N+1 em `listarEstoque()`

A instrumentação de traces **revelou um defeito real de desempenho** no projeto —
o melhor argumento possível a favor de observabilidade, e material direto para a
seção "lições aprendidas" do PDF.

Árvore de spans de uma requisição a `GET /api/bolsas`:

```
http get /api/bolsas                 60,06 ms
    connection                       59,47 ms
    listar-estoque-de-bolsas         57,82 ms
        query                         1,12 ms   <- a consulta principal
        query                         0,13 ms   <- e mais 525 consultas...
        query                         0,08 ms
        (... x526 no total)
```

**Diagnóstico:** `BolsaService.listarEstoque()` busca as bolsas e, ao montar cada
`BolsaResponse`, acessa o relacionamento `solicitacao`, que é *lazy*. Cada acesso
dispara uma consulta nova. Uma requisição = 1 consulta + N consultas (N = número de
bolsas). É o clássico **problema N+1**.

**Impacto medido:** 526 consultas e ~58 ms numa rota que deveria custar uma consulta
e poucos milissegundos. O custo cresce linearmente com o estoque.

**Correção sugerida (fora do escopo desta entrega, pois altera regra de negócio):**
usar `JOIN FETCH` na consulta do repositório, ou `@EntityGraph`, para trazer o
relacionamento numa só viagem ao banco.

**Observação metodológica:** nenhuma métrica teria apontado isso. O painel de latência
mostraria "p95 de 60 ms" sem explicar o porquê; só a árvore de spans mostra *onde* o
tempo foi gasto. É a diferença entre saber que está lento e saber por quê.

---

## 5.2 Segurança e acesso remoto (E7)

### O que estava exposto antes

Diagnóstico feito do próprio host, antes de qualquer ajuste:

| Porta | Serviço | Situação | Risco |
|---|---|---|---|
| 8080 | API + **`/actuator/*`** | HTTP 200 | `/actuator/env` entregaria a senha do banco |
| 9090 | Prometheus | HTTP 200 | leitura de todas as métricas e PromQL arbitrário |
| 5432 | **PostgreSQL** | porta aberta | o banco inteiro alcançável |
| 3000 | Grafana | HTTP 200 | correto, é o único que deve ficar |

Enquanto tudo isso valia apenas para `localhost`, o impacto era baixo. **A partir do
momento em que a máquina entra numa VPN ou ganha um túnel público, cada uma dessas
portas passa a ser alcançável.** Por isso o fechamento veio ANTES de expor.

### Solução: porta de gerenciamento separada

Em vez de fechar a porta da aplicação (o que tornaria a API inacessível), o Actuator
foi movido para uma porta própria:

    management.server.port=9091

A 8080 continua servindo a API de negócio; a **9091 não é publicada** no compose.
Resultado verificado:

| Verificação | De fora (host) | De dentro da rede Docker |
|---|---|---|
| `GET :8080/api/bolsas` | **200** | 200 |
| `GET :8080/actuator/prometheus` | **404** (saiu de lá) | 404 |
| `GET :9091/actuator/prometheus` | **sem resposta** | **200** |
| `GET :9090/-/healthy` (Prometheus) | **sem resposta** | 200 |
| Porta 5432 (Postgres) | **fechada** | alcançável por DNS `db:5432` |
| `GET :3000` (Grafana) | **200** | 200 |

E a coleta não quebrou: **4 de 4 targets UP**, agora em
`http://api:9091/actuator/prometheus`.

Isso é a demonstração prática de que `ports:` **não é firewall**: ele só cria o NAT
host→contêiner. Vizinhos da mesma rede alcançam qualquer porta que esteja escutando,
com ou sem `ports:`.

### Usuário de leitura (R7.2)

O Grafana provisiona datasources e dashboards por arquivo, mas **não usuários** —
a criação é pela API de administração. Daí o script
`scripts/criar_usuario_viewer.sh`, idempotente (rodar duas vezes não duplica nada).

Por que não entregar o admin ao professor: princípio do menor privilégio. Permissões
verificadas na prática, com o usuário `professor` real:

| Ação | Resultado | Esperado |
|---|---|---|
| Listar dashboards | **200** | pode ver |
| Listar datasources | 200 (sem nenhum campo sensível) | aceitável |
| Criar/editar dashboard | **403** | bloqueado |
| Criar datasource | **403** | bloqueado |
| Criar usuário | **403** | bloqueado |

Papéis conferidos: `admin` = Admin, `professor` = Viewer.

Observação: o Viewer **lista** os datasources, mas a resposta não traz
`secureJsonData`, `password` nem `basicAuthPassword` — e os três datasources deste
projeto não têm credencial alguma (são alcançados pela rede interna, sem autenticação).
Nada sensível é exposto.

**Armadilha importante para a entrega:** `GF_SECURITY_ADMIN_PASSWORD` só é aplicada
quando o Grafana cria o usuário admin, no primeiro boot. Trocar a senha no `.env`
depois **não surte efeito**, porque ela passa a viver no volume `grafana_data`. Para
aplicar uma senha nova: remover o volume e subir de novo (seguro, pois datasources e
dashboards são provisionados por arquivo) ou usar
`grafana cli admin reset-admin-password`.

Esse incidente serviu de **teste de reprodutibilidade** (R4.1): com o volume apagado,
um único `docker compose up -d` reconstruiu 3 datasources e 6 dashboards sem nenhum
passo manual — exatamente o que acontecerá na máquina de quem clonar o repositório.

### Acesso remoto: Tailscale

| Opção | Professor precisa | URL | HTTPS | Exposição |
|---|---|---|---|---|
| Tailscale puro (VPN) | instalar app + aceitar convite | IP `100.x.y.z:3000` | não | só a tailnet |
| **Tailscale Funnel** | **nada, só o link** | `https://<maquina>.<tailnet>.ts.net` | **sim** | internet |

**Decisão: Funnel.** Justificativa: o enunciado exige acesso durante a janela de
correção, e cada passo extra exigido do professor (instalar app, aceitar convite,
aprovar dispositivo) é risco de perder os 0,75 pontos por algo que não é técnico.

Contrapartida aceita e mitigada: com Funnel o Grafana fica na internet aberta, então
a senha passa a ser a única barreira. Mitigações aplicadas:
- senha do admin vinda do `.env` não versionado, com `${VAR:?}` no compose, que faz
  o `up` **falhar** se ela não existir (em vez de subir com `admin/admin`);
- `GF_USERS_ALLOW_SIGN_UP=false` (ninguém cria conta sozinho);
- professor recebe usuário **Viewer**, não admin;
- apenas a porta 3000 é publicada — Prometheus, Loki, Tempo e `/actuator` ficam na
  rede interna, fora do alcance do túnel.

### Configuração aplicada

```
sudo systemctl enable --now tailscaled     # daemon sobe junto com o sistema
sudo tailscale up                          # autentica a maquina na tailnet
sudo tailscale funnel --bg 3000            # publica SO a porta 3000, em background
```

O `--bg` grava a configuração no estado do `tailscaled`
(`/var/lib/tailscale/tailscaled.state`), então o túnel **sobrevive a reboot** e não
fica preso a um terminal aberto.

**URL de acesso:** `https://linhomachine.tail9034e2.ts.net`
**Usuário para o professor:** `professor` (papel Viewer), senha no `.env`.
**Certificado:** Let's Encrypt, emitido automaticamente, válido até 24/02/2027.

### Evidência de que o túnel expõe APENAS o Grafana

Testes feitos pelo caminho público (DNS + TLS + proxy da Tailscale):

| Requisição pela URL pública | Resultado | Leitura |
|---|---|---|
| `/api/health` | 200 | Grafana no ar |
| `/login` | 200 | tela de login |
| `/api/search` **sem credenciais** | **401** | exige autenticação |
| `/api/search` com usuário Viewer | **200** | professor vê os 6 dashboards |
| `/actuator/prometheus` | 302 (login) | rota não existe no Grafana |
| `/api/v1/query?query=up` | 401 | API do Prometheus inalcançável |
| `/loki/api/v1/labels` | 302 | API do Loki inalcançável |
| portas 9090, 3100, 3200, 8080, 9091, 5432 no domínio público | **nenhuma responde** | o Funnel publica só a 3000 |

**Vazamento encontrado e corrigido:** `/metrics` do próprio Grafana respondia **200
sem autenticação**, expondo 4.432 séries — entre elas `grafana_build_info` com a
versão exata (13.0.14), que é o primeiro dado usado para cruzar com CVEs conhecidas,
além de contagem de usuários e organizações. Corrigido com `GF_METRICS_ENABLED=false`
(revalidado: passou a 302). Também desligadas as chamadas de telemetria e verificação
de atualização do Grafana.

### "O professor entrou e não apareceu dashboard nenhum"

Sintoma observado no primeiro acesso com o usuário Viewer: a tela inicial mostrava
apenas "Starred dashboards" e "Recently viewed dashboards", ambos vazios.

**Não era permissão.** A API consultada com as credenciais do próprio Viewer
devolvia **200 com os 6 dashboards**; se fosse falta de permissão, viria 403. O que
acontece é que a Home padrão do Grafana lista só favoritos e vistos recentemente —
e para um usuário recém-criado ambos estão necessariamente vazios. Os dashboards
estavam acessíveis o tempo todo pelo menu lateral **Dashboards**.

Como foi resolvido, para não depender de o avaliador procurar o menu:

    GF_DASHBOARDS_DEFAULT_HOME_DASHBOARD_PATH:
      /var/lib/grafana/dashboards/rotavital-aplicacao.json

Agora quem abre a URL cai direto no dashboard da aplicação, com 25 painéis e dados.

**Lição:** diante de "não aparece nada", consultar a API com as credenciais do
usuário afetado separa em um comando o problema de permissão (403) do problema de
apresentação (200 com a lista cheia).

### O professor precisa instalar Tailscale?

**Não** — e isso foi verificado, não suposto:

| Verificação | Resultado |
|---|---|
| `dig @8.8.8.8 linhomachine.tail9034e2.ts.net` | `209.177.145.97`, `209.177.145.192` |
| Esses IPs são da tailnet (`100.x`)? | **Não, são públicos** |
| `curl --resolve` forçando o IP público | **HTTP 200** |

O resolvedor público do Google conhece o nome e devolve **IPs públicos** dos
servidores da Tailscale. Forçando o acesso por esses IPs — isto é, ignorando o
MagicDNS da máquina local — o Grafana respondeu 200. Portanto o caminho não passa
pela tailnet.

Diferença entre as duas modalidades:

- **Tailscale puro:** endereço `100.88.126.98`, faixa privada de CGNAT que só existe
  dentro da tailnet → o app seria obrigatório.
- **Funnel:** a Tailscale recebe o tráfego HTTPS em servidores públicos dela e o
  encaminha pelo túnel até a máquina → **qualquer navegador acessa, sem app**, e o
  certificado é um Let's Encrypt legítimo.

**Risco residual:** a máquina precisa estar ligada e conectada durante a correção.
Sem alternativa num cenário caseiro; a opção robusta seria uma VM em nuvem.

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

- **2026-11-26 (F6 e F7 — E5 e E6 CONCLUÍDAS)** — Logs e traces no ar.

  **Logs (E5):** `loki` 3.7.10 (armazena) + `alloy` v1.22.0 (coleta). O Alloy
  descobre os contêineres pelo socket do Docker, lê o stdout de cada um, rotula com
  `service`/`container`/`project`/`level` e empurra para o Loki. Resultado: logs dos
  **9 contêineres**, filtráveis por aplicação (R6.1). Dashboard novo
  `rotavital-logs.json` com 6 painéis; 5 de 6 consultas LogQL com dados na criação.

  **Traces (E6):** `tempo` 2.10.10 recebendo via OTLP na 4318. Na aplicação,
  `spring-boot-starter-opentelemetry` (**uma** dependência — no Boot 3.x seriam
  três), amostragem em 100% e o endereço do Tempo via variável de ambiente no
  compose, não no `application.properties`, porque varia por ambiente.

  **Correlação (R6.3) comprovada ponta a ponta:**

  | Etapa | Evidência |
  |---|---|
  | Log no Loki | `{service="api", level="ERROR"}` com metadado `traceId=16e834f9...` |
  | Mesmo id no Tempo | `GET /api/traces/16e834f9...` devolveu o trace |
  | Hierarquia de spans | `http get /api/bolsas` -> `connection` -> `listar-estoque-de-bolsas` -> `query` |

  O `traceId` vai como **metadado estruturado**, não como label: cada requisição tem
  um traceId único, e usá-lo como label criaria uma série nova por requisição — o
  mesmo erro de cardinalidade que vale para métricas.

  Para gerar a hierarquia de spans foram necessárias duas peças além do HTTP:
  `ObservedAspect` + `@Observed` (camada de service) e a biblioteca
  `datasource-micrometer-spring-boot` 2.3.1 (spans de JDBC).

- **2026-11-26 (F5 — ADICIONAL CONCLUÍDO, +1,0)** — Implementadas 5 métricas de
  negócio (ver seção 5): 3 Gauges via `MultiGauge`, 1 Counter e 1 Timer.
  Arquivos novos: `metrics/ConsultasMetricasNegocio.java`,
  `metrics/MetricasNegocio.java`, `config/MetricasAspectosConfig.java` e
  `controller/DiagnosticoController.java`.

  Criado `/api/diagnostico/erro` (HTTP 500) e `/api/diagnostico/indisponivel` (503),
  protegidos por `@ConditionalOnProperty`. Motivo: o painel de taxa de erro exigido
  pelo enunciado filtra `status=~"5.."`, e o gerador só produzia 404 (4xx, erro de
  *cliente*). Sem uma falha de servidor real aquele painel ficaria em zero.

  Gerador de carga ganhou operações de **escrita** (POST de bolsa, solicitação +
  alocação, cálculo de rota) e a opção `--semear`, porque com banco vazio as métricas
  de negócio não publicam série nenhuma.

  | Verificação | Resultado |
  |---|---|
  | Build com as mudanças | BUILD SUCCESS |
  | Testes | **184 de 184 passando** |
  | Séries `rotavital_*` expostas | 89 |
  | Painéis no dashboard de aplicação | 25 |
  | Queries validadas | **39 de 39 com dados** |

  Também habilitados `server.tomcat.mbeanregistry.enabled=true` (preenche
  `tomcat_threads_*`, antes vazio) e `--collector.processes` no Node Exporter
  (preenche painéis de PIDs do dashboard 1860).

- **2026-11-26 (F3 e F4 — E4 CONCLUÍDA)** — Criado `scripts/gerador_carga.py`
  (somente biblioteca padrão do Python: 8 workers, ~30 req/s, 3% de erro proposital
  em rotas inexistentes, relatório de p50/p95 a cada 10s). O script revelou que
  `/api/alocacoes` não possui `GET`: com ela na lista a taxa de erro media era
  12,8% em vez de 3%, o que invalidaria o painel de erros.

  Criados 2 dashboards versionados em `observability/grafana/dashboards/`:

  | Dashboard | Painéis | Cobre |
  |---|---|---|
  | Aplicação (JVM & HTTP) | 15 | R2.1, R2.2, R2.3, R4.4 |
  | Infraestrutura (Host & Contêineres) | 14 | R3.1, R3.2, R4.5 |

  **As 43 consultas PromQL foram validadas uma a uma contra o Prometheus: 43 de 43
  retornam dados, zero painel vazio.** Três precisaram correção após a validação:
  `jvm_gc_pause_seconds_bucket` e `spring_data_repository_invocations_seconds_bucket`
  não existem (histograma foi habilitado só para `http.server.requests`), então usam
  média `rate(_sum)/rate(_count)` e `_max`; e `container_fs_usage_bytes` não possui
  o label `name` (é por dispositivo do host), substituída pela divisão de CPU do
  contêiner em `user` x `system`.

  Armadilha nova encontrada: alterar o `uid` de um datasource já provisionado quebra
  o boot do Grafana (`Datasource provisioning error: data source not found`), porque o
  registro antigo persiste no volume `grafana_data`. Solução: bloco `deleteDatasources`
  no provisionamento + limpeza do volume. O `uid` ficou **fixo** (`prometheus`) porque
  os JSONs dos dashboards o referenciam; sem fixar, os painéis quebram em outra máquina.

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
