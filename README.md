# Rota Vital — Sistema de Gestão de Hemocomponentes

Sistema acadêmico para gestão de estoque de hemocomponentes e atendimento de requisições hospitalares.

---

## 🚀 Desafio do PI — Container, TDD e Deploy Automatizados (2026.2)
**CESAR School — Infraestrutura de Software | ADS**

---

### 1. 👨‍💻 Integrantes

| Nome | E-mail | Função |
|---|---|---|
| Allan Max de Jesus Rodrigues de Lima | amjrl@cesar.school | Desenvolvimento |
| Boniek Araujo dos Santos Junior | basj@cesar.school | Desenvolvimento |
| Caio Cesar Leandro Amorim | ccla@cesar.school | Desenvolvimento |
| Luan Ventura Ferreira de Moura | lvfm2@cesar.school | Desenvolvimento |
| Miguel Victor Lussac Barboza | mvlb@cesar.school | Desenvolvimento / DevOps |
| Pedro Augusto Carvalho Araujo | paca@cesar.school | Desenvolvimento |
| Vinicius Pessoa de Albuquerque | vpa@cesar.school | Desenvolvimento |.
| Wesley Yuri da Silva | wys@cesar.school | Desenvolvimento |

---

### 2. 🐳 Como Rodar (Entrega E1)

O sistema sobe do zero com um único comando utilizando **Docker Compose**, orquestrando a API Spring Boot e o banco de dados PostgreSQL.

#### Pré-requisitos
* [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado e em execução (backend WSL2 recomendado no Windows).

#### Passo a passo:
1. Clone o repositório:
   ```bash
   git clone https://github.com/JackkOps/Vital.git
   cd Vital
   ```
2. Crie seu arquivo de ambiente local a partir do modelo:
   ```bash
   cp .env.example .env
   # No Windows PowerShell:
   Copy-Item .env.example .env
   ```
3. Suba toda a infraestrutura com um único comando:
   ```bash
   docker compose up -d --build
   ```
4. Verifique os containers em execução e o healthcheck:
   ```bash
   docker compose ps
   ```
5. Teste a API respondendo:
   ```bash
   curl http://localhost:8080/api/bolsas
   ```

Para encerrar o ambiente preservando os dados persistidos:
```bash
docker compose down
```

#### Decisões do container (E1)
* **Imagem base:** o estágio final usa `eclipse-temurin:21-jre-alpine`, que traz só o runtime Java (sem compilador nem Maven) sobre o Alpine. Resultado: imagem final de **392 MB em disco / 124 MB de conteúdo**, com menos pacotes instalados e, portanto, menos superfície para vulnerabilidades. A aplicação roda como usuário sem privilégios (`appuser`), não como root.
* **Por que multi-stage:** o 1º estágio (`eclipse-temurin:21-jdk-alpine`) baixa as dependências, compila e **roda toda a suíte de testes** (`./mvnw clean package`); se um teste falhar, a imagem não é gerada. O 2º estágio copia apenas o `.jar`. JDK, Maven, código-fonte e cache de dependências ficam para trás e não vão para produção.
* **Onde ficam os dados:** no volume nomeado `rotavital_postgres_data`, montado em `/var/lib/postgresql/data`. Ele é gerenciado pelo Docker e sobrevive a `docker compose down` (só é apagado com `down -v`), como mostra a evidência [04-persistencia-volume](docs/evidencias/e1/04-persistencia-volume.png).
* **Como os serviços se encontram:** `api` e `db` estão na rede bridge `rotavital_network`, criada pelo Compose. Dentro dela o DNS do Docker resolve o nome do serviço, então a API acessa o banco por `jdbc:postgresql://db:5432/rotavital`, sem IP fixo.
* **Ordem de subida:** o `db` tem healthcheck com `pg_isready` e a `api` só inicia com `depends_on: condition: service_healthy`. A `api` também tem healthcheck próprio (`GET /api/bolsas`), visível no `docker compose ps`.
* **Configuração fora do código:** credenciais e portas vêm do `.env` (criado a partir do `.env.example` e ignorado pelo Git); o `compose.yaml` só tem valores padrão de desenvolvimento.

---

### 3. 🧪 Testes Automatizados e TDD (Entrega E2)

A aplicação conta com uma pirâmide completa de testes automatizados com JUnit 5, MockMvc e **Cucumber (BDD)**

#### User Story Map e Tarefas
O Story Map detalhado e a quebra de tarefas das 3 histórias selecionadas da Release 1 estão documentados em [docs/story-map-e2.md](docs/story-map-e2.md):
* **US-01:** Cadastro de Bolsa com Validações, Novo Hemocomponente (`SANGUE_TOTAL`) e Bloqueio de Duplicidade.
* **US-02:** Registro de Solicitação Hospitalar e Fila FIFO.
* **US-03:** Histórico de Operações em Ordem LIFO (Pilha de Auditoria).

#### Execução dos Testes Locais (via Maven Wrapper):
```powershell
cd backend/rotavital
.\mvnw.cmd clean test
```
* **Apenas cenários BDD (Gherkin/Cucumber):**
  ```powershell
  .\mvnw.cmd "-Dtest=RunCucumberTest" test
  ```
* **Apenas testes unitários e de integração:**
  ```powershell
  .\mvnw.cmd "-Dtest=BolsaValidationTest,SolicitacaoValidationTest,HistoricoServiceTest,BolsaRepositoryIntegrationTest" test
  ```

#### Histórico Red $\to$ Green (Git):
O ciclo estrito de TDD foi registrado através dos commits:
1. `6552fcb` — `test: evidencia RED da US-01 e automatiza BDD das tres historias` *(4 falhas esperadas)*
2. `dc2b990` — `feat: aceita sangue total e retorna 400 para validade anterior a coleta` *(GREEN: 61 testes passando)*
3. `4954c8d` — `docs: registra evidencias e reproducao do ciclo TDD da E2`
4. `95cc408` — `test: completa unitarios da E2 e explicita pop sem reversao de negocio` *(72 testes passando)*

---

### 4. 🔄 Pipeline de CI/CD e Deploy (Entrega E3)

A esteira de integração e entrega contínua está implementada via **GitHub Actions** em [.github/workflows/ci.yml](.github/workflows/ci.yml).

* **Trigger:** Executa a cada `push` e `pull request` em qualquer branch.
* **Gates de Qualidade:**
  1. Configuração do ambiente Java 21 Temurin.
  2. Execução dos testes automatizados (`./mvnw test`). Se qualquer teste falhar, o pipeline falha (vermelho), impedindo o avanço para as etapas de empacotamento e deploy (bloqueio de merge aplicado via branch protection no GitHub).
  3. Compilação do pacote JAR.
  4. Build e validação do `Dockerfile` multi-stage.
* **Deploy Contínuo (CD):** Disparado automaticamente após merge na branch `main` mediante aprovação de todos os gates.
* **URL Pública do Sistema no Ar:** `[Pendente de deploy remoto no Render/Railway via webhook DEPLOY_HOOK_URL]`.
* **Procedimento de Rollback:**
  Em caso de falha em produção:
  1. No painel do provedor de deploy (Render/Railway), selecione o deploy anterior íntegro e acione o rollback imediato.
  2. No Git (com branch `main` protegida por regras do repositório, onde pushes diretos são bloqueados):
     ```bash
     git checkout -b fix/rollback-release
     # Reverte o merge commit indicando a mainline (-m 1)
     git revert -m 1 <hash-do-merge>
     git push origin fix/rollback-release
     # Abrir Pull Request para a main; o CI executa os testes e o CD publica a versão restaurada
     ```

---

### 5. 📋 Tabela de Decisões Técnicas

| Decisão | O que escolhemos | Alternativas | Por quê? |
|---|---|---|---|
| **Imagem Base** | `eclipse-temurin:21-jre-alpine` (estágio 2) | `eclipse-temurin:21-jdk`, `21-jre` (Ubuntu), `openjdk` | Só o JRE sobre Alpine: imagem final de 392 MB (124 MB de conteúdo), menos pacotes vulneráveis e execução como usuário não-root; o JDK fica isolado no 1º estágio. |
| **Build da imagem** | Dockerfile multi-stage (build + testes → runtime) | Estágio único com JDK; copiar um `.jar` gerado fora do Docker | Os testes viram gate do build (teste vermelho = sem imagem) e a imagem final não carrega JDK, Maven nem código-fonte. |
| **Dados** | PostgreSQL 16 com volume nomeado (`rotavital_postgres_data`) | Bind mount, H2 em memória | Volume nomeado gerenciado pelo Docker garante que os dados do banco sobrevivem a `docker compose down`, sem depender de caminhos da máquina do aluno. |
| **Rede** | Rede bridge do Compose (`rotavital_network`) | `network_mode: host`, IP fixo | A API encontra o banco pelo nome do serviço (`db`) via DNS interno do Docker; funciona igual em qualquer máquina. |
| **Saúde e ordem de subida** | Healthcheck em `db` (`pg_isready`) e `api` + `depends_on: service_healthy` | `depends_on` simples, scripts de espera | A API só sobe quando o banco aceita conexões, evitando falha de conexão na inicialização. |
| **Testes** | Priorização da US-01 (`SANGUE_TOTAL` e datas) e compatibilidade | Testar cadastro cosmético | Uma bolsa com data de validade inconsistente ou tipo não reconhecido gera risco iminente de descarte ou erro transfusional. |
| **Fluxo de Branch** | GitHub Flow (feature branches $\to$ PR $\to$ `main` protegida) | Git Flow clássico | Equipe ágil, entrega contínua com uma única versão em produção e deploy automático a cada merge aprovado na `main`. |
| **CI/CD e Deploy** | GitHub Actions + Render / Railway | GitLab CI, Jenkins, AWS | Actions possui integração nativa com o repositório GitHub sem custo; Render oferece deploy automatizado via Webhook/Docker. |
| **Segredos** | `.env` local (no `.gitignore`) + GitHub Secrets no CI/CD | Commit de variáveis, Hardcoded | Garante que credenciais de produção e banco de dados nunca vazem no histórico do repositório público. |

---

### 6. 🤖 Declaração de Uso de IA

O uso de Inteligência Artificial Generativa neste projeto foi ativo, supervisionado e validado pela equipe acadêmica.

* **Ferramentas utilizadas:** Google Antigravity e OpenAI Codex via canvas colaborativo Maestri.
* **Divisão de papéis:**
  * **Antigravity:** Engenharia de prompt, estruturação do User Story Map, cenários Gherkin BDD, Dockerfile multi-stage, Compose e Pipeline CI/CD.
  * **OpenAI Codex:** Automação dos testes unitários, classes step definition do Cucumber e implementação dos ciclos RED $\to$ GREEN no Spring Boot.
  * **Codex #2:** Validação independente de conformidade técnica e integridade acadêmica.
  * **Equipe de Alunos:** Validação, revisão das decisões arquiteturais e assinatura final da entrega.

#### Principais Prompts Utilizados:
1. *"Elabore o Story Map com Backbone e Release 1 quebrando 3 histórias de gestão de bolsas de sangue em tarefas com critérios BDD em Gherkin."*
2. *"Execute o ciclo estrito de TDD para o enum TipoComponente (suporte a SANGUE_TOTAL) e validação de validade temporal, gerando commits RED antes do GREEN."*
3. *"Crie um Dockerfile multi-stage onde o estágio 1 compila e roda os testes Maven, e o estágio 2 carrega apenas o JRE enxuto e o artefato JAR sob usuário não-root."*
4. *"Configure o pipeline do GitHub Actions para rodar a cada push e pull request, bloqueando deploy em caso de teste vermelho."*

---

### 7. 📁 Evidências

Todos os registros e logs comprobatórios são organizados na pasta [`docs/evidencias/`](docs/evidencias/):
* **E1 (Docker):** [`docs/evidencias/e1/`](docs/evidencias/e1/)
  1. [`docker compose ps` com os dois serviços healthy](docs/evidencias/e1/01-compose-ps.png)
  2. [API respondendo `HTTP 200`](docs/evidencias/e1/02-api-respondendo.png)
  3. [Subida do zero (`down -v` + `up -d --build`)](docs/evidencias/e1/03-sobe-do-zero.png)
  4. [Dados persistem após `docker compose down`](docs/evidencias/e1/04-persistencia-volume.png)
  5. [Volume nomeado e rede do Compose](docs/evidencias/e1/05-volume-rede.png)
  6. [Tamanho da imagem e usuário não-root](docs/evidencias/e1/06-imagem-usuario.png)
  7. [Testes rodando dentro do build multi-stage (`--no-cache`: 72 testes, `BUILD SUCCESS`)](docs/evidencias/e1/07-testes-no-build.png)
* **E2 (TDD):** [`docs/evidencias/e2/README.md`](docs/evidencias/e2/README.md) *(Suíte local implementada com 72 testes verdes, logs duráveis e hashes SHA-256 versionados; execução na esteira remota do GitHub Actions aguarda push)*.
* **E3 (Deploy):** `docs/evidencias/e3/` *(Workflow `.github/workflows/ci.yml` pronto; aguardando push para o GitHub para coleta de prints da execução vermelha que barra merge e verde que publica)*.

---

## Estrutura de Pastas

```text
.
|-- .github/
|   |-- CODEOWNERS
|   `-- workflows/
|       `-- ci.yml
|-- backend/
|   `-- rotavital/
|       |-- src/main/java/com/jackops/rotavital/
|       |   |-- controller/
|       |   |-- dto/
|       |   |-- estrutura/
|       |   |-- exception/
|       |   |-- model/
|       |   |-- repository/
|       |   `-- service/
|       |-- src/main/resources/static/
|       |-- src/test/java/com/jackops/rotavital/
|       |-- E2-TDD.md
|       `-- pom.xml
|-- docs/
|   |-- bdd/
|   |-- evidencias/
|   |-- rota-vital-historias-usuario.md
|   |-- rota-vital-topologia.md
|   |-- story-map-e2.md
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
|-- scripts/
|   |-- verificar-citacoes.ps1
|   `-- citacoes-c-java.json
|-- .env.example
|-- .gitignore
|-- compose.yaml
|-- Dockerfile
`-- README.md
```

---

## Entrega do Projeto Integrador — Unidade 1

Esta branch está organizada para a entrega do Projeto Integrador da Unidade 1. O foco da entrega é demonstrar estruturas de dados implementadas manualmente em C e reimplementadas em Java para consumo pela aplicação Spring Boot.

Funcionalidades contempladas nesta entrega:

- CRUD de bolsas de hemocomponentes.
- CRUD de solicitações hospitalares.
- Estoque implementado como lista encadeada.
- Requisições hospitalares implementadas como fila FIFO.
- Histórico de operações implementado como pilha LIFO.
- Código C com ponteiros, `malloc` e `free`.
- Reimplementação Java das mesmas estruturas, sem coleções prontas dentro das estruturas.
- Documento de tradução comentada entre C e Java.

Funcionalidades de etapas futuras foram isoladas na branch [`unidade-2`](https://github.com/Vini-palb/Vital/tree/unidade-2), publicada no remoto `origin` em 07/10/2026.

## Tecnologias Usadas

| Área | Tecnologias |
|---|---|
| Backend | Java 21, Spring Boot 4.1.1 (Web MVC, Data JPA, Validation, DevTools) |
| Banco de dados | H2 em memória (desenvolvimento) e PostgreSQL 16 (Docker Compose) |
| Interface web | HTML, CSS e JavaScript, servidos pelo próprio Spring Boot e consumindo a API REST |
| Estruturas de dados | C (C11) com GCC, Make e Valgrind |
| Testes | JUnit, Spring Boot Test, MockMvc e Cucumber (BDD) |
| Build | Maven (Maven Wrapper) |
| Containers e CI/CD | Docker (Dockerfile multi-stage), Docker Compose e GitHub Actions |
| Versionamento e gestão | Git, GitHub (código e issues) e Jira |
| Documentação | Markdown, Mermaid e script PowerShell de verificação da tradução C → Java |

## Estruturas de Dados

| Estrutura | Domínio | Arquivos C | Arquivos Java |
|---|---|---|---|
| Lista encadeada | Estoque de bolsas | `estruturas-c/lista_estoque.c` | `ListaEstoque.java` |
| Fila FIFO | Requisições hospitalares | `estruturas-c/fila_requisicoes.c` | `FilaRequisicoes.java` |
| Pilha LIFO | Histórico de operações | `estruturas-c/pilha_historico.c` | `PilhaHistorico.java` |

A tradução comentada está em [docs/traducao-c-java.md](docs/traducao-c-java.md).

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

Verifique vazamentos de memória:

```bash
make valgrind
```

O alvo `valgrind` requer o Valgrind instalado em Linux ou WSL. No Windows com MinGW, substitua `make` por `mingw32-make` para compilar e testar; o Valgrind precisa de um ambiente Linux.

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

Requisitos: Java 21. O backend usa Spring Boot, JPA e H2.

A API sobe em `http://localhost:8080`.

A interface web fica disponível no navegador em `http://localhost:8080/`.

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

| Ação | Método e URL |
|---|---|
| Listar bolsas | `GET /api/bolsas` |
| Cadastrar bolsa | `POST /api/bolsas` |
| Buscar bolsa | `GET /api/bolsas/{id}` |
| Atualizar bolsa | `PUT /api/bolsas/{id}` |
| Excluir bolsa | `DELETE /api/bolsas/{id}` |
| Listar solicitações | `GET /api/solicitacoes` |
| Cadastrar solicitação | `POST /api/solicitacoes` |
| Buscar solicitação | `GET /api/solicitacoes/{id}` |
| Atualizar solicitação | `PUT /api/solicitacoes/{id}` |
| Excluir solicitação | `DELETE /api/solicitacoes/{id}` |
| Consultar fila FIFO | `GET /api/solicitacoes/fila` |
| Chamar próxima solicitação | `POST /api/solicitacoes/fila/proxima` |
| Consultar histórico (topo primeiro) | `GET /api/historico` |
| Remover última operação do histórico | `DELETE /api/historico/ultima` |
| Consultar indicadores estatísticos | `GET /api/indicadores` |
| Alocar bolsa compatível (ABO/Rh + FEFO) | `POST /api/alocacoes/solicitacoes/{solicitacaoId}/alocar` |
| Listar bolsas alocadas para uma solicitação | `GET /api/alocacoes/solicitacoes/{solicitacaoId}` |
| Listar alertas de vencimento | `GET /api/validade/alertas` |
| Verificar validade e dar baixa nas vencidas | `POST /api/validade/verificar` |
| Dar baixa manual por vencimento | `POST /api/validade/bolsas/{bolsaId}/baixa` |
| Listar baixas registradas | `GET /api/validade/baixas` |

## Verificar a Tradução Comentada

Na raiz do repositório:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/verificar-citacoes.ps1
```

## Histórico das Entregas

Os registros abaixo preservam o conteúdo das entregas anteriores. As funcionalidades de compatibilidade, FEFO e roteirização citadas nesses registros pertencem às etapas futuras na branch `unidade-2`; os endpoints históricos de alocação não estão disponíveis nesta entrega da Unidade 1.

Na Entrega 03 de POO, a alocação compatível ABO/Rh com FEFO (Entrega 02) foi reintegrada ao `main`.

## 📦 Entrega 01

### 📖 Histórias de Usuário

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

### 🎨 Protótipo Lo-Fi

O protótipo de baixa fidelidade da aplicação está disponível no Figma.

🔗 **[Acessar Protótipo Lo-Fi no Figma](https://www.figma.com/design/LGNH0CWyaNMfClKrTKIotp/Rota-Vital?node-id=0-1&p=f&t=X0556s3iPCew6liI-0)**

---

### 🎥 Screencast

O screencast apresenta o protótipo desenvolvido e as histórias de usuário contempladas nesta etapa do projeto.

🔗 **[Assistir ao Screencast no YouTube](https://youtu.be/ahV21_baUUQ)**

---

### 📎 Artefatos

| Artefato | Acesso |
|---|---|
| Histórias de Usuário | [Visualizar](./docs/rota-vital-historias-usuario.md) |
| Protótipo Lo-Fi | [Acessar Figma](https://www.figma.com/design/LGNH0CWyaNMfClKrTKIotp/Rota-Vital?node-id=0-1&p=f&t=X0556s3iPCew6liI-0) |
| Screencast | [Assistir no YouTube](https://youtu.be/ahV21_baUUQ) |

---

## 📦 Entrega 02

### Histórias implementadas

> **História 1 - Cadastro de bolsa de hemocomponente**
>
> Como operador do banco de sangue, quero registrar uma bolsa no estoque para que ela fique disponível para alocação e rastreamento.
> **Critérios entregues:** identificador único de negócio; campos obrigatórios e volume positivo; bloqueio de duplicidade; validade igual ou posterior à coleta; status inicial `DISPONIVEL`; cadastro e consulta de estoque pela API.

> **História 2 - Alocação de bolsa compatível priorizando validade (FEFO)**
>
> Como operador, quero que o sistema selecione a bolsa compatível que vence primeiro para reduzir descarte.
> **Critérios entregues:** compatibilidade ABO/Rh acadêmica; filtro de bolsas disponíveis e não vencidas; mesmo componente da solicitação; ordenação FEFO; alteração do status para `ALOCADA`; retorno claro quando não há estoque compatível.

### Como executar e demonstrar

No terminal, entre em `backend/rotavital` e execute `./mvnw.cmd spring-boot:run`. A API ficará disponível em `http://localhost:8080`.

Endpoints usados na demonstração:

| Ação | Método e URL |
|---|---|
| Cadastrar bolsa | `POST /api/bolsas` |
| Consultar estoque | `GET /api/bolsas` |
| Criar solicitação de apoio | `POST /api/solicitacoes` |
| Alocar por FEFO | `POST /api/alocacoes/solicitacoes/{solicitacaoId}/alocar` |

Os testes automatizados podem ser executados com `./mvnw.cmd test`.

### Issue / Bug Tracker

> **Espaço reservado para o print do GitHub Issues**
>
> Adicione aqui a captura de tela das issues usadas na Entrega 02 (cadastro de bolsa, validação de datas, compatibilidade ABO/Rh, FEFO e correções).

Referência histórica do print: `docs/images/github-issues-entrega-02.png`. A imagem não consta no histórico consultado e permanece pendente.

### Screencasts

- Vídeo 1 - Uso do sistema: **[assistir no YouTube](https://youtu.be/awFqNrnNcWk)**
- Vídeo 2 - Explicação do código: **[assistir no YouTube](https://youtu.be/HQX0xPfTYAI)**
- Roteiros curtos: `docs/roteiros-videos-entrega-02.md` (referência histórica; arquivo não encontrado no histórico consultado).

---

## 📦 Entrega 03

### Histórias implementadas

> **História 4 - Solicitação hospitalar de hemocomponentes**
>
> Como responsável pelo estoque de um hospital, quero registrar uma solicitação de hemocomponentes informando tipo, quantidade e urgência, para que o hemocentro receba e atenda meu pedido.
> **Critérios entregues:** hospital, tipo sanguíneo, componente, quantidade e nível de urgência obrigatórios; quantidade maior que zero ("Quantidade inválida"); status inicial `PENDENTE`; entrada na fila de atendimento; chamada da próxima solicitação mudando o status para `EM_ATENDIMENTO`; consulta, atualização e exclusão pela API, com remoção da fila ao excluir uma solicitação pendente.

> **História 5 - Alerta e baixa de bolsas próximas do vencimento**
>
> Como gestor do hemocentro, quero ser alertado sobre bolsas próximas do vencimento e dar baixa nas vencidas, para reduzir o descarte e impedir que uma bolsa inválida seja distribuída.
> **Critérios entregues:** janela de alerta por tipo de componente (plaquetas 2 dias, hemácias 7 dias, plasma 30 dias); alerta das bolsas disponíveis dentro da janela; verificação diária automática e sob demanda; bolsa vencida muda para `DESCARTADA` e deixa de ser considerada na alocação; bloqueio da baixa de bolsa `ALOCADA` ("Cancele a alocação antes de dar baixa"); registro persistido de cada baixa com motivo, data e origem (automática ou manual).

A alocação compatível ABO/Rh com FEFO, entregue na Entrega 02, foi reintegrada ao `main` e as bolsas descartadas por vencimento não são mais alocadas.

### Como executar e demonstrar

No terminal, entre em `backend/rotavital` e execute `./mvnw.cmd spring-boot:run` (Windows) ou `./mvnw spring-boot:run` (Linux/macOS). A API ficará disponível em `http://localhost:8080`.

Endpoints usados na demonstração:

| Ação | Método e URL |
|---|---|
| Cadastrar solicitação | `POST /api/solicitacoes` |
| Consultar fila de atendimento | `GET /api/solicitacoes/fila` |
| Chamar próxima solicitação | `POST /api/solicitacoes/fila/proxima` |
| Cadastrar bolsa | `POST /api/bolsas` |
| Listar alertas de vencimento | `GET /api/validade/alertas` |
| Verificar validade e dar baixa nas vencidas | `POST /api/validade/verificar` |
| Dar baixa manual por vencimento | `POST /api/validade/bolsas/{bolsaId}/baixa` |
| Listar baixas registradas | `GET /api/validade/baixas` |

Todas essas ações também podem ser feitas pela interface web em `http://localhost:8080/`, nas áreas Estoque, Solicitações e Validade.

Os testes automatizados podem ser executados com `./mvnw.cmd test`.

### Issue / Bug Tracker

> **Espaço reservado para o print do GitHub Issues da Entrega 03**

### Screencasts

- Vídeo 1 - Uso do sistema: **[assistir no YouTube](https://youtu.be/rVwKBNQx7dY)**
- Vídeo 2 - Explicação do código: **[assistir no YouTube](https://youtu.be/1NgdL95KPeM)**

---

## 📈 Roadmap

### Fase 1 — Modelagem e Base do Sistema

- Definição do domínio;
- Estrutura inicial do backend;
- Implementação das estruturas de dados;
- Configuração inicial de infraestrutura.

### Fase 2 — Integração

- Compatibilidade sanguínea;
- Roteirização;
- Painéis;
- Telemetria.

### Fase 3 — Finalização

- Testes;
- Deploy;
- Documentação;
- Apresentação final.

---

## 👨‍💻 Equipe

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

## 📄 Licença

Projeto desenvolvido para fins acadêmicos no curso de **Análise e Desenvolvimento de Sistemas — CESAR School (2026.2)**.
