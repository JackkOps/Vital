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
| Vinicius Pessoa de Albuquerque | vpa@cesar.school | Desenvolvimento |
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

---

### 3. 🧪 Testes Automatizados e TDD (Entrega E2)

A aplicação conta com uma pirâmide completa de testes automatizados com JUnit 5, MockMvc e **Cucumber (BDD)**.

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
  2. Execução dos testes automatizados (`./mvnw test`). Se qualquer teste falhar, o pipeline quebra (vermelho) e o merge é barrado.
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
| **Imagem Base** | `eclipse-temurin:21-jre-alpine` (estágio 2) | Debian, Ubuntu, Node | Imagem mínima, reduzindo pacotes vulneráveis e superfície de ataque; o JDK completo fica isolado no 1º estágio. |
| **Dados** | PostgreSQL 16 com volume nomeado (`postgres_data`) | Bind mount, SQLite | Volume nomeado gerenciado pelo Docker garante que os dados do banco sobrevivem a `docker compose down`. |
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
* **E1 (Docker):** `docs/evidencias/e1/` *(Configurado localmente via `compose.yaml`; aguardando execução local do Docker Desktop para captura de tela do `docker compose ps` e API respondendo)*.
* **E2 (TDD):** [`docs/evidencias/e2/README.md`](docs/evidencias/e2/README.md) *(100% Concluído e versionado com hashes SHA-256 e logs `red-us01.txt`, `revalidacao.txt` e `revalidacao-junit.xml`)*.
* **E3 (Deploy):** `docs/evidencias/e3/` *(Workflow `.github/workflows/ci.yml` pronto; aguardando push para o GitHub para coleta de prints da execução vermelha que barra merge e verde que publica)*.

---

## Estrutura de Pastas

```text
.
|-- .github/workflows/
|   `-- ci.yml
|-- backend/
|   `-- rotavital/
|       |-- src/main/java/com/jackops/rotavital/
|       |-- src/test/java/com/jackops/rotavital/
|       `-- pom.xml
|-- docs/
|   |-- bdd/
|   |-- evidencias/
|   |-- story-map-e2.md
|   `-- traducao-c-java.md
|-- estruturas-c/
|-- .env.example
|-- .gitignore
|-- compose.yaml
|-- Dockerfile
`-- README.md
```
