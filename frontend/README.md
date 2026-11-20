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

- **Paleta temática de hemocomponentes**, definida em `src/index.css` (`@theme`): fundo frio clínico (`#EEF2F4`), vermelho de marca sóbrio (`#8F1B2E`), um vermelho mais quente reservado à emergência, verde "oxigenado" para disponível/compatível e âmbar para validade e cadeia fria. Modo escuro segue o sistema operacional.
- **Tipografia:** IBM Plex Sans para a interface; IBM Plex Mono só para identificadores reais (código da bolsa, sensor, tipo sanguíneo).
- **Status nunca só por cor:** todo selo de status tem ícone + texto.
- **Faixa "Disponível agora"** na barra lateral: estoque disponível por tipo sanguíneo, com destaque quando abaixo do nível crítico (5 bolsas, igual a `rotavital.indicadores.nivel-critico`).
- Separação por bordas de 1px; sombra só em sobreposições (diálogos e avisos).
