# Importa Aí

Sistema web full-stack de gestão e rastreamento de encomendas internacionais, com foco no corredor logístico China–Brasil. Permite cadastrar pedidos, acompanhar etapas em tempo real (do despacho na China à entrega no Brasil), receber notificações via WebSocket e visualizar valores convertidos pela cotação de câmbio.

> **Status:** sistema completo e em execução — backend, frontend, mensageria RabbitMQ, rastreamento real (17track), notificações e dashboard admin implantados em container. Projeto Integrador IV — ADS 4º Período — PUC Goiás 2026/1.

---

## Sumário

- [Stack tecnológica](#stack-tecnológica)
- [Estrutura do repositório](#estrutura-do-repositório)
- [Arquitetura](#arquitetura)
- [Princípios técnicos](#princípios-técnicos)
- [Como rodar localmente](#como-rodar-localmente)
- [Documentação](#documentação)
- [Roadmap acadêmico](#roadmap-acadêmico)
- [Disciplinas integradas](#disciplinas-integradas)
- [Convenções de commit](#convenções-de-commit)
- [Equipe](#equipe)

---

## Stack tecnológica

| Camada | Tecnologia |
|--------|-----------|
| **Backend** | Java 21 (LTS) + Spring Boot 3.5.x |
| **Frontend** | React 19 + TypeScript 6 + Vite 8 + Tailwind CSS 4.x |
| **Estado de servidor** | TanStack Query v5 (cache + invalidação) |
| **Formulários** | React Hook Form v7 |
| **Roteamento** | React Router v7 |
| **HTTP client** | Axios (com interceptor JWT) |
| **Mensageria** | RabbitMQ 3.13 (AMQP) |
| **Banco de dados** | MySQL 8.x |
| **Tempo real** | STOMP sobre WebSocket (SockJS como fallback) |
| **Autenticação** | JWT (access 1h + refresh 7d) |
| **Containerização** | Docker + Docker Compose |
| **Testes** | JUnit 5 + Mockito + Testcontainers (back) |
| **Cobertura** | JaCoCo — alvo: ≥ 80 % no `domain/` |

---

## Estrutura do repositório

```
importa-ai/
├── artefatos/                       Documentação técnica do projeto
│   ├── design-de-software/          ERS, design patterns, diagramas C4, 6 ADRs
│   ├── mensageria-e-streams/        Topologia AMQP, idempotência, DLQ
│   ├── modelagem-de-dados/          Esquema relacional + DER (drawio)
│   ├── modelagem-de-interfaces/     Guia de estilos e tokens visuais
│   └── qualidade-de-software/       Plano de testes (TC01–TC33)
├── backend/                         API + consumidores RabbitMQ (Java/Spring)
│   └── src/main/java/br/com/importaai/
│       ├── domain/                  Núcleo puro — entidades, ports, exceções
│       ├── application/             Casos de uso (orquestração)
│       └── infrastructure/          Adapters (REST, JPA, AMQP, WebSocket)
├── frontend/                        Interface React + TypeScript + Vite
└── infra/                           Docker Compose (MySQL + RabbitMQ)
```

---

## Arquitetura

- **Padrão:** Arquitetura Hexagonal (*Ports & Adapters*) no backend — núcleo de domínio em Java puro, isolado de qualquer framework de infraestrutura. Ver [ADR-002](artefatos/design-de-software/adrs/002-arquitetura-hexagonal.md).
- **Mensageria:** RabbitMQ via AMQP com ACK manual, Publisher Confirms, idempotência via INSERT-first e Dead Letter Queues. Ver [Arquitetura de Mensageria](artefatos/mensageria-e-streams/arquitetura-mensageria.md) e [ADR-001](artefatos/design-de-software/adrs/001-broker-rabbitmq.md).
- **Status do pedido:** derivado da última etapa registrada (RN01) — não há endpoint `PATCH /pedidos/{id}/status`. Ver [ADR-003](artefatos/design-de-software/adrs/003-status-derivado-da-etapa.md).
- **Rastreamento:** quatro adapters intercambiáveis (`stub`, `http` CWS, `cache-only`, `17track`) para a porta `RastreamentoCorreiosPort`, selecionados por configuração. Em produção usa-se o agregador **17track** (cobre Correios + trecho chinês). Ver [ADR-004](artefatos/design-de-software/adrs/004-adapters-correios.md).
- **Criptografia de PII (planejada):** decisão registrada (AES-256-GCM + HMAC-SHA256 para busca), **ainda não implementada nesta versão** — nome/e-mail em texto claro, dívida v2. Ver [ADR-005](artefatos/design-de-software/adrs/005-criptografia-em-repouso.md).
- **Notificações:** STOMP sobre WebSocket no canal privado `/user/{userId}/queue/notificacoes`, com histórico persistido (FIFO de 50 — RN09).
- **Diagramas C4:** Contexto, Container e Componentes do backend em [`artefatos/design-de-software/diagramas-C4/`](artefatos/design-de-software/diagramas-C4/).

---

## Princípios técnicos

Quatro invariantes guiam todo o backend:

1. **HTTP 202 nas escritas** — toda operação de escrita persiste o registro, publica o evento no broker e responde 202; os efeitos colaterais (notificações, integrações) são processados de forma assíncrona pelos consumers (RN03, RNF02).
2. **Status derivado, nunca armazenado como verdade** — `StatusPedido` é função pura da última etapa + flag `cancelado`, calculado on-the-fly (sem coluna `status_cache` nesta versão; o cache de query é dívida v2 — RN01 / ADR-003).
3. **Idempotência por design** — cada mensagem AMQP tem `message_id` único; o consumer tenta `INSERT` na tabela `evento_processado` antes do trabalho de negócio. UNIQUE constraint protege contra redelivery (RN04).
4. **Domínio sem Spring** — o pacote `domain/` é Java puro. Qualquer import de `org.springframework.*` ali significa que a arquitetura quebrou.

---

## Como rodar localmente

> As instruções abaixo sobem o ambiente de desenvolvimento local completo (infra + backend + frontend). Para o deploy em servidor, ver `DEPLOY.md`.

### Pré-requisitos

- Docker e Docker Compose
- Java 21 (o `mvnw` incluso dispensa Maven do sistema)
- Node 18+ e npm

### Subir infraestrutura

```bash
cd infra
docker compose up -d
```

Serviços que sobem:

- **MySQL** em `localhost:3306` — usuário `importaai`, senha `importaai123`, banco `importaai`
- **RabbitMQ** em `localhost:5672`; painel Management em `http://localhost:15672` — usuário `importaai`, senha `importaai123`

### Rodar o backend

```bash
cd backend
./mvnw spring-boot:run
```

API em `http://localhost:8080`.

### Rodar o frontend

```bash
cd frontend
npm install
npm run dev
```

UI em `http://localhost:5173`.

### Healthcheck

- Backend: `GET http://localhost:8080/actuator/health`
- RabbitMQ: painel Management
- MySQL: `docker compose exec mysql mysqladmin ping`

---

## Documentação

| Documento | Propósito |
|-----------|-----------|
| [ERS](artefatos/design-de-software/ERS.md) | Especificação completa de requisitos (RF, RNF, RN, casos de uso) |
| [Arquitetura de Mensageria](artefatos/mensageria-e-streams/arquitetura-mensageria.md) | Topologia, fluxos, DLQ, idempotência, observabilidade |
| [Design Patterns](artefatos/design-de-software/design-patterns.md) | Patterns adotados (com trechos de código) e descartados |
| [ADRs](artefatos/design-de-software/adrs/) | Registros de decisões arquiteturais (6 ADRs) |
| [Plano de Testes](artefatos/qualidade-de-software/plano-de-testes.md) | Casos de teste TC01–TC32 + metas de cobertura |
| [Guia de Estilos](artefatos/modelagem-de-interfaces/guia-de-estilos.md) | Tokens visuais, componentes-chave, acessibilidade |
| [Diagramas C4](artefatos/design-de-software/diagramas-C4/) | Contexto, Container, Componentes (drawio) |
| [Modelo de Dados](artefatos/modelagem-de-dados/modelo-de-dados.md) | Esquema relacional MySQL — entidades de negócio e tabelas de suporte |
| [DER](artefatos/modelagem-de-dados/der.drawio) | Diagrama Entidade-Relacionamento (Crow's Foot) das 5 entidades de negócio |

---

## Roadmap acadêmico

| Marco | Data | Status |
|-------|------|--------|
| N1 — Apresentação | 15/04/2026 | **Aprovada** (Figma + diagramas C4 + plano de testes) |
| **N2 — Entrega de código** | **22/05/2026** | **Concluída** — sistema completo + testes |
| Apresentação final | 03/06/2026 | Sistema deployado e em execução |

---

## Disciplinas integradas

- Design de Software
- Modelagem de Interfaces (UI)
- Desenvolvimento de Software Web
- Mensageria e Streams
- Qualidade de Software

---

## Convenções de commit

Este projeto segue [Conventional Commits](https://www.conventionalcommits.org/pt-br/v1.0.0/) em português:

| Tipo | Uso |
|------|-----|
| `feat` | Nova funcionalidade |
| `fix` | Correção de bug |
| `docs` | Documentação apenas |
| `test` | Adição ou correção de testes |
| `refactor` | Refatoração sem mudança de comportamento |
| `style` | Formatação / visual, sem mudança de lógica |
| `chore` | Configuração, dependências, infraestrutura |

A primeira linha não passa de 72 caracteres, em PT-BR, no imperativo, e sem ponto final.

---

## Equipe

- Higor Paulo Costa — modelagem, frontend, backend, produto e arquitetura
- Diogo Oliveira Almeida — modelagem, frontend, produto e arquitetura
- Alex Sander Aprigio Martins — produto e arquitetura
- Gustavo Veroneze Ribeiro — produto e arquitetura

