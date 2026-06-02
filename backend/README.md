# Importa Aí — Backend

API REST + consumidores de mensageria do Importa Aí, em **Java 21 / Spring Boot 3.5** com **Arquitetura Hexagonal**. Responsável por pedidos, rastreamento multiestágio, derivação de status, notificações em tempo real, cotação de câmbio e autenticação.

> Parte do monorepo Importa Aí — visão geral e quickstart no [README da raiz](../README.md).

## Sumário

- [Stack](#stack)
- [Arquitetura hexagonal](#arquitetura-hexagonal)
- [Como rodar](#como-rodar)
- [Variáveis de ambiente](#variáveis-de-ambiente)
- [Banco de dados (Flyway)](#banco-de-dados-flyway)
- [Mensageria](#mensageria)
- [Integrações externas](#integrações-externas)
- [Segurança](#segurança)
- [API REST](#api-rest)
- [Testes](#testes)
- [Convenções de código](#convenções-de-código)

## Stack

Java 21 · Spring Boot 3.5 (`web`, `security`, `data-jpa`, `amqp`, `websocket`, `validation`, `actuator`, `aop`) · MySQL 8 + Flyway · RabbitMQ 3.13 · Resilience4j (circuit breaker) · jjwt (JWT) + BCrypt · Lombok (só em `@Entity`) · JUnit 5 + Mockito + AssertJ + Testcontainers + WireMock.

## Arquitetura hexagonal

```
br.com.importaai/
├── domain/                 NÚCLEO — Java puro, ZERO Spring
│   ├── model/              entidades e value objects (Pedido, StatusPedido, EtapaRastreamento, Cotacao, ...)
│   ├── port/in/            interfaces de casos de uso (entrada)
│   ├── port/out/           interfaces de repositório/serviço (saída)
│   └── exception/          exceções de domínio
├── application/usecase/    implementações dos casos de uso (orquestração)
└── infrastructure/
    ├── adapter/in/rest/        controllers + DTOs + mappers
    ├── adapter/in/messaging/   consumers RabbitMQ
    ├── adapter/out/persistence/ JPA (entities, repositories, mappers)
    ├── adapter/out/messaging/  producer RabbitMQ (EventPublisher)
    ├── adapter/out/external/   APIs externas (rastreamento, câmbio) + schedulers
    ├── config/                 RabbitMQConfig, SecurityConfig, WebSocketConfig, UseCaseBeanConfig
    └── security/               JwtAuthFilter, JwtTokenIssuer
```

**Regra fundamental:** o pacote `domain/` **nunca** importa `org.springframework.*`. O domínio é testável sem subir banco, broker ou contexto Spring. Os casos de uso recebem as portas por construtor e são registrados como beans em `UseCaseBeanConfig` (sem anotações de framework no domínio/aplicação).

Patterns aplicados (com trechos de código): [Design Patterns](../artefatos/design-de-software/design-patterns.md). Decisão do estilo: [ADR-002](../artefatos/design-de-software/adrs/002-arquitetura-hexagonal.md).

## Como rodar

```bash
# infra (MySQL + RabbitMQ) na raiz do repo
cd ../infra && docker compose up -d

# backend
cd ../backend && ./mvnw spring-boot:run     # http://localhost:8080
```

`./mvnw clean package` gera o jar; `./mvnw test` roda a suíte. Healthcheck: `GET /actuator/health`. O Flyway aplica as migrations automaticamente no boot.

## Variáveis de ambiente

Defaults de desenvolvimento em `src/main/resources/application.properties`. Sobrescreva por ambiente em produção:

| Propriedade | Env / default | Descrição |
|-------------|---------------|-----------|
| `spring.datasource.*` | `IMPORTAAI_DB_USERNAME` / `IMPORTAAI_DB_PASSWORD` | Conexão MySQL |
| `spring.rabbitmq.*` | `IMPORTAAI_RABBITMQ_USERNAME` / `IMPORTAAI_RABBITMQ_PASSWORD` | Conexão RabbitMQ |
| `jwt.secret` | `IMPORTAAI_JWT_SECRET` | Assinatura HS256 (64+ chars) |
| `jwt.expiration` / `jwt.refresh-expiration` | `1h` / `7d` | Validade dos tokens |
| `correios.adapter` | `CORREIOS_ADAPTER` = `stub` (default), `http`, `cache-only`, `17track` | Fonte de rastreamento |
| `track17.token` | `IMPORTAAI_TRACK17_TOKEN` | Token 17track (quando `17track`) |
| `correios.cws.*` | `IMPORTAAI_CORREIOS_USUARIO` / `_CODIGO_ACESSO` / `_CARTAO_POSTAGEM` | Credenciais CWS (quando `http`) |
| `correios.sync.interval` | `1200000` (20 min) | Intervalo de sincronização de rastreio |
| `cambio.sync.interval` | `1800000` (30 min) | Intervalo de sincronização de câmbio |
| `importaai.cors.allowed-origins` | `http://localhost:5173` | Origens liberadas (REST + WS) |

## Banco de dados (Flyway)

Migrations versionadas em `src/main/resources/db/migration` (aplicadas em ordem no boot):

| Versão | Conteúdo |
|--------|----------|
| V1 | `usuario`, `pedido`, `etapa_rastreamento` |
| V2 | `evento_processado` (idempotência RN04) |
| V3 | `refresh_token_revogado`, `tentativa_login_falha` |
| V4 | `notificacao` |
| V5 | `cotacao_cache` |
| V6 | `pedido.valor_declarado` + `moeda` |
| V7 | seed do administrador |
| V8 | `usuario.ativo` (soft delete) |
| V9 | `cotacao_cache.cotado_em` |
| V10 | `pedido.rastreio_nao_localizado` |
| V11 | `DEVOLVIDO` no enum `etapa_rastreamento.tipo` |

Esquema detalhado e DER em [Modelo de Dados](../artefatos/modelagem-de-dados/modelo-de-dados.md). O `StatusPedido` **não** é coluna — é derivado da última etapa (RN01 / [ADR-003](../artefatos/design-de-software/adrs/003-status-derivado-da-etapa.md)).

## Mensageria

Escrita = persiste síncrono → publica evento → responde **HTTP 202**; efeitos colaterais nos consumers (RN03). Exchange `importaai.events` (direct, durable), 4 filas + 4 DLQs:

| Routing key | Consumer | Efeito |
|-------------|----------|--------|
| `pedido.criado` | `PedidoCriadoConsumer` | notificação de registro + dispara leitura inicial de rastreio |
| `rastreamento.atualizado` / `pedido.atualizado` | `RastreamentoConsumer` | notificação de mudança de status (RF16) |
| `notificacao.usuario` | `NotificacaoConsumer` | persiste (FIFO 50 — RN09) + envia STOMP |

- **Idempotência (RN04):** INSERT-first em `evento_processado` (UNIQUE `exchange, routing_key, message_id`); duplicata → ACK silencioso.
- **Durabilidade (RNF05):** filas/mensagens `durable`/`persistent`, Publisher Confirms, ACK manual.
- **DLQ:** falha de processamento → `basicNack(requeue=false)` → DLQ imediata (sem retry automático nesta versão).

Detalhes: [Arquitetura de Mensageria](../artefatos/mensageria-e-streams/arquitetura-mensageria.md) · [ADR-001](../artefatos/design-de-software/adrs/001-broker-rabbitmq.md).

## Integrações externas

- **Rastreamento** — porta `RastreamentoCorreiosPort` com 4 adapters (`stub`, `http` CWS, `cache-only`, `17track`) selecionados por `correios.adapter`. O `Rastreamento17TrackAdapter` (produção) registra o código e consulta o 17track, classificando os eventos pelo `sub_status` normalizado (incl. `Exception_Returning` → `DEVOLVIDO`), com **circuit breaker** + fallback para cache. Ver [ADR-004](../artefatos/design-de-software/adrs/004-adapters-correios.md).
- **Câmbio** — `ConsultarCotacaoService` usa **Cache-Aside**: lê o cache, em miss/expirado chama a API e atualiza; se a API cai, serve o cache marcando desatualizado (RN07). Cotação manual do admin sempre prevalece (RF21).

Schedulers (`@Scheduled`) disparam a sincronização periódica de rastreio (pedidos não terminais) e câmbio.

## Segurança

JWT stateless (`SecurityConfig` + `JwtAuthFilter`): access 1h, refresh 7d. Logout revoga o refresh (`refresh_token_revogado`). Bloqueio após 5 falhas de login (`tentativa_login_falha`, 15 min → HTTP 429). RBAC com `@PreAuthorize("hasRole('ADMINISTRADOR')")` nos endpoints administrativos; rotas de cliente validam posse do recurso. Não autenticado → **401**; sem permissão → **403** ([ADR-006](../artefatos/design-de-software/adrs/006-politica-cors.md) cobre o CORS unificado).

## API REST

Endpoints completos no [README da raiz](../README.md#api-rest). Resumo: `/api/auth/*`, `/api/pedidos`, `/api/pedidos/{id}/etapas` (admin), `/api/admin/{pedidos,dashboard,usuarios,cotacoes}`, `/api/notificacoes`, `/api/cotacoes/{moeda}`, `/api/me`.

## Testes

```bash
./mvnw test     # 114 testes; 1ª execução baixa containers (Testcontainers)
```

- **Unitários** (domínio + use cases) — JUnit 5 + Mockito + AssertJ, sem Spring.
- **Integração** — `@SpringBootTest` + Testcontainers (MySQL + RabbitMQ reais): REST (MockMvc), mensageria (idempotência, DLQ, STOMP ≤ 2 s), persistência JPA, fluxo de auth E2E.
- **WireMock** para os adapters HTTP externos.
- Cobertura do `domain/` via JaCoCo (`target/site/jacoco/index.html`), alvo ≥ 80%.

Plano de testes (TC01–TC33): [Plano de Testes](../artefatos/qualidade-de-software/plano-de-testes.md).

## Convenções de código

- Classes `PascalCase`, métodos/atributos `camelCase`; `record` para DTOs e value objects imutáveis; Lombok apenas em `@Entity`.
- `Optional<T>` para retornos potencialmente ausentes; exceções de domínio estendem `RuntimeException` em `domain/exception/`.
- Sem dependência de infraestrutura no domínio; toda query parametrizada; segredos só por variável de ambiente.
