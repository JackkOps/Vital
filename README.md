# Rota Vital

Sistema acadêmico para gestão de estoque de hemocomponentes e requisições hospitalares.

Esta branch está organizada para a entrega do Projeto Integrador da Unidade 1. O foco da entrega é demonstrar estruturas de dados implementadas manualmente em C e reimplementadas em Java para consumo pela aplicação Spring Boot.

## Entrega do Projeto Integrador — Unidade 1

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
|-- scripts/
|   |-- verificar-citacoes.ps1
|   `-- citacoes-c-java.json
`-- README.md
```

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

## Verificar a Tradução Comentada

Na raiz do repositório:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/verificar-citacoes.ps1
```

## Histórico das Entregas

Os registros abaixo preservam o conteúdo das entregas anteriores. As funcionalidades de compatibilidade, FEFO e roteirização citadas nesses registros pertencem às etapas futuras na branch `unidade-2`; os endpoints históricos de alocação não estão disponíveis nesta entrega da Unidade 1.

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
