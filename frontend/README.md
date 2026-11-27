# Rota Vital — frontend

Interface web do Rota Vital em **React 19 + TypeScript + Vite + Tailwind 4**, com componentes no padrão shadcn/ui (Radix + `class-variance-authority`) e dados via **TanStack Query** consumindo a API REST do Spring Boot.

## Como rodar

Pré-requisito: Node 20+ e o backend rodando em `http://localhost:8080` (via `docker compose up -d --build` na raiz, ou `./mvnw spring-boot:run` em `backend/rotavital`).

```bash
cd frontend
npm install
npm run dev
```

Abra `http://localhost:5173`. Em desenvolvimento o Vite repassa `/api` para `localhost:8080` (ver `vite.config.ts`), então não é preciso configurar CORS no backend.

Outros comandos:

| Comando | O que faz |
|---|---|
| `npm run build` | Checagem de tipos (`tsc -b`) e build de produção em `dist/` |
| `npm run preview` | Serve o build de produção localmente |
| `npm run lint` | Lint com oxlint |

## Estrutura

```
src/
├── api/            cliente HTTP e tipos espelhando os DTOs/enums do backend
├── components/
│   ├── layout/     shell (sidebar, faixa de estoque ao vivo), cabeçalho de página, estado vazio
│   └── ui/         botão, selo, campos, diálogo, grupo de opções
├── features/       uma pasta por área: estoque, solicitacoes, validade, logistica, indicadores, historico
├── hooks/          hooks de consulta/mutação (TanStack Query) e toasts
└── lib/            formatação pt-BR, mapeamentos de status → cor/ícone
```

## Decisões de design

Sistema visual **"editorial cobalto"**, inspirado em pitch decks minimalistas: papel creme, tinta quase preta e um único azul cobalto como cor de marca. Tokens em `src/index.css` (`@theme`).

- **Paleta:** fundo creme (`#EFE9DF`), painéis em papel (`#F8F4ED`), tinta `#111`, cobalto `#1D4ED8` para marca e ação primária. O vermelho deixou de ser marca e virou só **sinal** (emergência, estoque crítico, descarte), para voltar a chamar atenção. Modo escuro segue o sistema operacional.
- **Tipografia em duas vozes:** Instrument Sans (grotesca) para interface e Instrument Serif itálica, em cobalto, como complemento dos títulos e legendas ("Estoque de / *bolsas.*"). IBM Plex Mono só para identificadores reais (bolsa, sensor, tipo sanguíneo, códigos).
- **Lâminas:** cada página abre com uma linha de metadados (marca · data · `01/06`) e um título em duas linhas. Painéis têm índice `(01)`, `(02)`… e fios de 1px no lugar de sombras; a lâmina azul invertida marca o elemento principal de cada tela (barra lateral, fila de atendimento, KPI de estoque).
- **Números grandes e finos** (`Stat`/`StatRow` em `components/ui/stat.tsx`) resumem cada área no topo da página.
- **Botões em pílula** com seta (`ButtonArrow`); selos também em pílula. Status nunca só por cor: todo selo tem ícone + texto.
- **Faixa "Disponível agora"** na barra lateral: estoque disponível por tipo sanguíneo, com destaque quando abaixo do nível crítico (5 bolsas, igual a `rotavital.indicadores.nivel-critico`).
- **UX:** busca e paginação nas tabelas, abas por status nas solicitações, contador de bolsas e seletor de urgência por cor no pedido, medidor de temperatura com faixa segura nas remessas, confirmação antes de remover registros do histórico, link "Pular para o conteúdo" e `prefers-reduced-motion` respeitado.
