# Importa Aí — Frontend

Interface web do Importa Aí (gestão e rastreamento de encomendas internacionais). SPA em React 19 + TypeScript, consumindo a API REST e o canal WebSocket do backend.

## Stack

| Camada | Tecnologia |
|--------|-----------|
| UI | React 19 + TypeScript |
| Build/dev | Vite |
| Estilo | Tailwind CSS v4 (tokens em `src/index.css`) |
| Estado de servidor | TanStack Query v5 (`useQuery`/`useMutation`) |
| Formulários | React Hook Form v7 |
| Roteamento | React Router v7 |
| HTTP | Axios (interceptor JWT + refresh em `services/api.ts`) |
| Tempo real | STOMP/SockJS (`@stomp/stompjs`) no `NotificacaoContext` |

## Como rodar

Pré-requisitos: Node 20+ e o backend no ar (em dev, o proxy do Vite encaminha `/api` e `/ws` para `localhost:8080`).

```bash
npm install
npm run dev      # http://localhost:5173
```

Outros scripts: `npm run build` (typecheck `tsc -b` + build), `npm run preview`, `npm run lint`.

## Estrutura (`src/`)

```
components/   Componentes reutilizáveis (ui/, layout/)
context/      AuthContext, NotificacaoContext, ToastContext
features/     Módulos por feature (auth, pedidos, admin, perfil) — páginas + componentes + utils
hooks/        Custom hooks
lib/          Utilitários
services/     Chamadas à API (Axios) + cliente STOMP
types/        Interfaces TypeScript (pedidos, auth, etc.)
index.css     Tokens de design (Tailwind v4) — fonte das cores/typografia
```

## Convenções

- Estado de servidor sempre via TanStack Query (não `useState`/`useEffect` para chamadas de API).
- Formulários via React Hook Form com validação tipada.
- Cores e estilos saem dos tokens em `index.css` — fiéis ao Figma (não inventar UI).

O design system e os requisitos vivem em [`../artefatos/`](../artefatos/) (guia de estilos, ERS).
