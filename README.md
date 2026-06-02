# Importa Aí

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F)
![React](https://img.shields.io/badge/React-19-61DAFB)
![TypeScript](https://img.shields.io/badge/TypeScript-6-3178C6)
![MySQL](https://img.shields.io/badge/MySQL-8-4479A1)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.13-FF6600)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED)

Sistema web full-stack de **gestão e rastreamento de encomendas internacionais**, com foco no corredor logístico **China–Brasil**. O usuário cadastra pedidos por código de rastreamento e acompanha cada etapa em tempo real — do despacho na China à entrega (ou devolução) no Brasil — com notificações via WebSocket e valores convertidos pela cotação de câmbio.

O backend segue **Arquitetura Hexagonal** com domínio em Java puro (sem framework), mensageria assíncrona (RabbitMQ) e fontes de rastreamento plugáveis por configuração. O frontend é uma SPA React que consome a API REST e um canal STOMP/WebSocket.

> **Status:** sistema completo e em execução — backend, frontend, mensageria, rastreamento real (17track), notificações em tempo real e dashboard administrativo, implantados em container.

---

## Sumário

- [Funcionalidades](#funcionalidades)
- [Stack tecnológica](#stack-tecnológica)
- [Arquitetura](#arquitetura)
- [Estrutura do repositório](#estrutura-do-repositório)
- [Como rodar localmente](#como-rodar-localmente)
- [Variáveis de ambiente](#variáveis-de-ambiente)
- [Testes](#testes)
- [API REST](#api-rest)
- [Mensageria](#mensageria)
- [Deploy](#deploy)
- [Documentação](#documentação)
- [Convenções de commit](#convenções-de-commit)
- [Equipe](#equipe)

---

## Funcionalidades

- **Gestão de pedidos** — cadastro por código de rastreamento, listagem com filtros (em trânsito, taxados, entregues, devolvidos), detalhe com linha do tempo e valor convertido.
- **Rastreamento multiestágio** — etapas internacionais (China → aeroportos → trânsito) e nacionais (Brasil → taxa → CD → saída → entrega), além do estado de exceção **devolvido** (barrado na alfândega).
- **Status derivado** — o status do pedido é função pura da última etapa (não há transição manual); ver [ADR-003](artefatos/design-de-software/adrs/003-status-derivado-da-etapa.md).
- **Notificações em tempo real** — push via STOMP/WebSocket na criação e a cada mudança de status, com histórico (FIFO de 50 por usuário — RN09).
- **Cotação de câmbio** — conversão CNY/USD/EUR → BRL com cache + fallback (RN07) e override manual pelo admin.
- **Dashboard administrativo** — KPIs (ativos, em trânsito, taxa pendente, entregues no mês), gráfico de evolução e gestão de usuários.
- **Autenticação e autorização** — JWT (access 1h + refresh 7d), bloqueio após 5 falhas, RBAC (Cliente / Administrador).

## Stack tecnológica

| Camada | Tecnologia |
|--------|-----------|
| **Backend** | Java 21 (LTS) · Spring Boot 3.5 (web, security, data-jpa, amqp, websocket, validation, actuator) |
| **Resiliência** | Resilience4j (circuit breaker) |
| **Auth** | JWT (jjwt) · BCrypt |
| **Banco** | MySQL 8 · Flyway (migrations versionadas) |
| **Mensageria** | RabbitMQ 3.13 (AMQP) |
| **Tempo real** | STOMP sobre WebSocket (SockJS fallback) |
| **Frontend** | React 19 · TypeScript 6 · Vite 8 · Tailwind CSS 4 |
| **Estado de servidor** | TanStack Query v5 |
| **Formulários / rotas / HTTP** | React Hook Form v7 · React Router v7 · Axios |
| **Testes** | JUnit 5 · Mockito · AssertJ · Testcontainers (MySQL + RabbitMQ) · WireMock |
| **Infra** | Docker + Docker Compose |

## Arquitetura

- **Hexagonal (Ports & Adapters)** no backend — núcleo de domínio em Java puro, isolado de qualquer framework. Ver [ADR-002](artefatos/design-de-software/adrs/002-arquitetura-hexagonal.md).
- **Mensageria assíncrona** — escrita persiste, publica evento e responde **HTTP 202**; efeitos colaterais rodam nos consumers (RN03). ACK manual, Publisher Confirms, idempotência por INSERT-first e DLQ. Ver [ADR-001](artefatos/design-de-software/adrs/001-broker-rabbitmq.md) e a [Arquitetura de Mensageria](artefatos/mensageria-e-streams/arquitetura-mensageria.md).
- **Status derivado** — `StatusPedido` é função pura da última etapa + flag `cancelado`, calculado *on-the-fly* (sem coluna de cache nesta versão). Ver [ADR-003](artefatos/design-de-software/adrs/003-status-derivado-da-etapa.md).
- **Rastreamento plugável** — quatro adapters intercambiáveis (`stub`, `http` CWS, `cache-only`, `17track`) para a porta `RastreamentoCorreiosPort`, selecionados por `correios.adapter`. Em produção, o agregador **17track** (cobre Correios + trecho chinês). Ver [ADR-004](artefatos/design-de-software/adrs/004-adapters-correios.md).
- **CORS único** para REST e WebSocket, configurável por ambiente. Ver [ADR-006](artefatos/design-de-software/adrs/006-politica-cors.md).
- **Criptografia de PII (planejada)** — AES-256-GCM + HMAC para busca; ainda não implementada (dívida v2). Ver [ADR-005](artefatos/design-de-software/adrs/005-criptografia-em-repouso.md).

Diagramas **C4** (Contexto, Container, Componentes) e DER em [`artefatos/design-de-software/diagramas-C4/`](artefatos/design-de-software/diagramas-C4/) e [`artefatos/modelagem-de-dados/`](artefatos/modelagem-de-dados/).

```
REST / WebSocket / Scheduler        (adapters de entrada)
            │
            ▼
   application/usecase   ──►   domain/  (Java puro: model, ports, exceptions)
            │
            ▼
 JPA · RabbitMQ · STOMP · APIs externas   (adapters de saída)
```

## Estrutura do repositório

```
importa-ai/
├── artefatos/            Documentação técnica (ERS, ADRs, C4, DER, mensageria, plano de testes, guia de estilos)
├── backend/              API + consumidores RabbitMQ (Java 21 / Spring Boot) — ver backend/README.md
├── frontend/             SPA React + TypeScript + Vite — ver frontend/README.md
└── infra/                Docker Compose de desenvolvimento (MySQL + RabbitMQ)
```

O backend é organizado em `domain/` (núcleo), `application/usecase/` (casos de uso) e `infrastructure/adapter/{in,out}` (REST, persistência JPA, mensageria, WebSocket, integrações).

## Como rodar localmente

**Pré-requisitos:** Docker + Docker Compose, Java 21 (o `mvnw` dispensa Maven do sistema), Node 20+.

```bash
# 1. Infraestrutura (MySQL + RabbitMQ)
cd infra && docker compose up -d

# 2. Backend  → http://localhost:8080
cd ../backend && ./mvnw spring-boot:run

# 3. Frontend → http://localhost:5173 (proxy /api e /ws → :8080)
cd ../frontend && npm install && npm run dev
```

**Acessos da infra local:** MySQL `localhost:3306` (`importaai`/`importaai123`/db `importaai`) · RabbitMQ Management `http://localhost:15672` (`importaai`/`importaai123`).

**Healthcheck:** `GET http://localhost:8080/actuator/health`.

**Login:** um administrador é semeado na primeira subida (migration `V7`). Use a credencial definida no seed e troque-a antes de qualquer uso real.

Detalhes por módulo: [`backend/README.md`](backend/README.md) · [`frontend/README.md`](frontend/README.md).

## Variáveis de ambiente

Em desenvolvimento, os defaults do `application.properties` cobrem tudo. Em produção, sobrescreva por ambiente (nunca commitar segredos):

| Variável | Função |
|----------|--------|
| `IMPORTAAI_DB_USERNAME` / `IMPORTAAI_DB_PASSWORD` | Credenciais MySQL |
| `IMPORTAAI_RABBITMQ_USERNAME` / `IMPORTAAI_RABBITMQ_PASSWORD` | Credenciais RabbitMQ |
| `IMPORTAAI_JWT_SECRET` | Segredo de assinatura do JWT (64+ chars) |
| `CORREIOS_ADAPTER` | Fonte de rastreamento: `stub` (default) · `http` · `cache-only` · `17track` |
| `IMPORTAAI_TRACK17_TOKEN` | Token da API 17track (quando `CORREIOS_ADAPTER=17track`) |
| `importaai.cors.allowed-origins` | Origens liberadas (REST + WebSocket) |

Intervalos de sincronização: rastreamento a cada **20 min** (`correios.sync.interval`), câmbio a cada **30 min** (`cambio.sync.interval`) — ambos configuráveis.

## Testes

```bash
cd backend && ./mvnw test      # 114 testes (unitários + integração com Testcontainers)
cd frontend && npx tsc --noEmit # type-check
```

A suíte cobre domínio (derivação de status RN01, invariantes do agregado), mensageria (idempotência, DLQ, WebSocket ≤ 2 s), autenticação, cotação e rastreamento. Cobertura do `domain/` via JaCoCo (alvo ≥ 80%). Plano completo em [Plano de Testes](artefatos/qualidade-de-software/plano-de-testes.md).

## API REST

| Método | Rota | Perfil | Descrição |
|--------|------|--------|-----------|
| POST | `/api/auth/register` · `/login` · `/refresh` · `/logout` | Público | Cadastro, login (JWT+refresh), renovação e revogação |
| GET / POST | `/api/pedidos` | Cliente | Listar / criar pedido (criar → **202**) |
| GET | `/api/pedidos/{id}` | Cliente (dono) | Detalhe + linha do tempo |
| POST | `/api/pedidos/{id}/etapas` | Admin | Inserir etapa manual (RF13) |
| DELETE | `/api/pedidos/{id}` | Admin | Cancelar pedido |
| GET | `/api/admin/pedidos` · `/{id}` | Admin | Todos os pedidos do sistema |
| GET | `/api/admin/dashboard` | Admin | KPIs |
| GET / PATCH | `/api/admin/usuarios` · `/{id}/perfil` · `/{id}/status` | Admin | Gestão de usuários |
| GET / PATCH | `/api/notificacoes` · `/lidas` | Cliente | Histórico e marcar como lidas |
| GET | `/api/cotacoes/{moeda}` | Autenticado | Cotação (cache + fallback) |
| POST | `/api/admin/cotacoes` | Admin | Cotação manual |
| GET / PATCH | `/api/me` | Autenticado | Perfil próprio |

## Mensageria

Exchange `importaai.events` (direct, durable) + 4 filas principais e 4 DLQs:

| Routing key | Fila | Consumer |
|-------------|------|----------|
| `pedido.criado` | `q.pedido.criado` | `PedidoCriadoConsumer` → notificação de registro + leitura inicial de rastreio |
| `rastreamento.atualizado` | `q.rastreamento.atualizado` | `RastreamentoConsumer` → notificação de mudança de status (RF16) |
| `pedido.atualizado` | `q.pedido.atualizado` | `RastreamentoConsumer` |
| `notificacao.usuario` | `q.notificacao.usuario` | `NotificacaoConsumer` → persiste (RN09) + STOMP |

Idempotência por INSERT-first em `evento_processado` (UNIQUE); falha de processamento → `basicNack(requeue=false)` → DLQ imediata. Detalhes na [Arquitetura de Mensageria](artefatos/mensageria-e-streams/arquitetura-mensageria.md).

## Deploy

Stack containerizada (`docker-compose.prod.yml`): MySQL + RabbitMQ + backend + frontend (nginx) + túnel. O frontend serve o SPA e faz proxy de `/api` e `/ws` no mesmo domínio (same-origin). Passo a passo em `DEPLOY.md` (não versionado).

## Documentação

| Documento | Propósito |
|-----------|-----------|
| [ERS](artefatos/design-de-software/ERS.md) | Requisitos (RF, RNF, RN, casos de uso) |
| [ADRs](artefatos/design-de-software/adrs/) | 6 registros de decisão arquitetural |
| [Design Patterns](artefatos/design-de-software/design-patterns.md) | Patterns adotados (com trechos de código) e descartados |
| [Arquitetura de Mensageria](artefatos/mensageria-e-streams/arquitetura-mensageria.md) | Topologia, fluxos, idempotência, DLQ |
| [Modelo de Dados](artefatos/modelagem-de-dados/modelo-de-dados.md) | Esquema relacional + DER |
| [Plano de Testes](artefatos/qualidade-de-software/plano-de-testes.md) | TC01–TC33 + metas de cobertura |
| [Guia de Estilos](artefatos/modelagem-de-interfaces/guia-de-estilos.md) | Tokens visuais e componentes |
| [Diagramas C4](artefatos/design-de-software/diagramas-C4/) | Contexto, Container, Componentes |

## Convenções de commit

[Conventional Commits](https://www.conventionalcommits.org/pt-br/v1.0.0/) em PT-BR, imperativo, primeira linha ≤ 72 caracteres, sem ponto final.

| Tipo | Uso |
|------|-----|
| `feat` / `fix` | Funcionalidade / correção |
| `docs` / `test` | Documentação / testes |
| `refactor` / `style` / `chore` | Refatoração / formatação / infra |

## Equipe

Projeto Integrador IV — ADS, PUC Goiás.

- **Higor Paulo Costa** — modelagem, frontend, backend, produto e arquitetura
- **Diogo Oliveira Almeida** — modelagem, frontend, produto e arquitetura
- **Alex Sander Aprigio Martins** — produto e arquitetura
- **Gustavo Veroneze Ribeiro** — produto e arquitetura
