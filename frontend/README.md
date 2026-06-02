# Importa Aí — Frontend

SPA do Importa Aí em **React 19 + TypeScript**, consumindo a API REST e o canal **STOMP/WebSocket** do backend. Cobre as áreas do cliente (pedidos, cotação, notificações) e do administrador (dashboard, pedidos, usuários, cotação manual, exportação).

> Parte do monorepo Importa Aí — visão geral e quickstart no [README da raiz](../README.md).

## Sumário

- [Stack](#stack)
- [Como rodar](#como-rodar)
- [Estrutura](#estrutura)
- [Padrões e decisões](#padrões-e-decisões)
- [Tempo real (STOMP/WebSocket)](#tempo-real-stompwebsocket)
- [Design system](#design-system)
- [Ambiente e proxy](#ambiente-e-proxy)
- [Build e deploy](#build-e-deploy)
- [Convenções](#convenções)

## Stack

| Camada | Tecnologia |
|--------|-----------|
| UI | React 19 + TypeScript 6 |
| Build/dev | Vite 8 |
| Estilo | Tailwind CSS 4 (tokens em `src/index.css`) |
| Estado de servidor | TanStack Query v5 (`useQuery` / `useMutation`) |
| Formulários | React Hook Form v7 |
| Roteamento | React Router v7 |
| HTTP | Axios (interceptor JWT + refresh em `services/api.ts`) |
| Tempo real | `@stomp/stompjs` + `sockjs-client` |

## Como rodar

Pré-requisitos: Node 20+ e o backend no ar (em dev, o proxy do Vite encaminha `/api` e `/ws` para `localhost:8080`).

```bash
npm install
npm run dev        # http://localhost:5173
```

| Script | Ação |
|--------|------|
| `npm run dev` | Servidor de desenvolvimento (HMR) |
| `npm run build` | Type-check (`tsc -b`) + build de produção (Vite) |
| `npm run preview` | Servir o build localmente |
| `npm run lint` | ESLint |

## Estrutura

```
src/
├── components/   reutilizáveis — ui/ (Button, Input, StatusBadge, ...) e layout/ (PageHeader, ...)
├── context/      AuthContext, NotificacaoContext (STOMP), ToastContext
├── features/     módulos por feature — auth, pedidos, admin, perfil
│                 cada um com pages/, components/ e utils/ (ex.: pedidos/utils/statusUtils.ts, filtros.ts)
├── hooks/        custom hooks
├── lib/          utilitários (ex.: usuario)
├── services/     api.ts (Axios), pedidos.ts, cotacao.ts, perfil.ts, admin.ts + cliente STOMP
├── types/        interfaces TypeScript (pedidos, auth, moeda, ...)
└── index.css     tokens de design (Tailwind v4 @theme) — fonte das cores/tipografia
```

## Padrões e decisões

- **Estado de servidor → TanStack Query.** Toda chamada à API passa por `useQuery`/`useMutation` (cache, dedup, `isLoading`/`isError`, invalidação). Não usar `useEffect + axios + useState` para dados de servidor.
- **HTTP centralizado.** `services/api.ts` é o único cliente Axios: injeta o JWT no header `Authorization` e faz **refresh automático em 401** (com guarda para não entrar em loop nas rotas `/auth/`). Os serviços (`pedidos.ts`, etc.) expõem funções tipadas e aplicam um **mapper anti-corrupção** que traduz o contrato do backend (`PedidoResponse`) para o modelo da UI.
- **Formulários → React Hook Form**, com validação tipada (sem controlar input por `useState`).
- **Roteamento → React Router v7.** `PrivateRoute` protege rotas autenticadas e tem prop `adminOnly`; o login redireciona admin → `/admin`.
- **Auth no `AuthContext`** — decodifica o claim `perfil` do JWT e expõe `isAdmin`.

## Tempo real (STOMP/WebSocket)

O `NotificacaoContext` abre uma conexão STOMP/SockJS sobre `/ws` (apenas quando autenticado, enviando o JWT no CONNECT) e assina o canal privado `/user/{userId}/queue/notificacoes`. Cada notificação recebida atualiza o contador do sino e dispara um toast (`ToastContext`). É por aqui que chegam, em tempo real, a notificação de criação e as de **mudança de status** (RF16).

## Design system

Cores, tipografia e tokens de status/etapa vivem em `src/index.css` (bloco `@theme` do Tailwind v4) e são a fonte única usada por `statusUtils.ts` (cor/label de `StatusPedido` e `TipoEtapa`, incluindo `DEVOLVIDO`). O layout é fiel ao Figma — **não inventar UI**. Detalhes em [Guia de Estilos](../artefatos/modelagem-de-interfaces/guia-de-estilos.md).

## Ambiente e proxy

Em desenvolvimento, o Vite faz proxy de `/api` e `/ws` para `http://localhost:8080`, então o navegador enxerga tudo como *same-origin* (sem CORS). Em produção, o frontend é servido por nginx que faz o mesmo proxy no domínio do deploy.

## Build e deploy

`npm run build` gera `dist/` (estático). No deploy, o `dist/` é servido por **nginx**, que também faz proxy de `/api` e `/ws` para o backend no mesmo domínio (same-origin → CORS não é acionado). Ver `DEPLOY.md` na raiz.

## Convenções

- Componentes funcionais com hooks (zero class components).
- Estado de servidor sempre via TanStack Query; estado global pequeno via Context (sem Redux/Zustand nesta versão).
- Estilos a partir dos tokens do `index.css` — fiéis ao Figma.
- Commits: Conventional Commits em PT-BR (ver [README da raiz](../README.md#convenções-de-commit)).
