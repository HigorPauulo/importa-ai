# Plano de Testes — Importa Aí v1.4

## Objetivo
Garantir que o sistema atenda aos requisitos funcionais e não funcionais
definidos na ERS v1.6, com foco em:
- Confiabilidade do fluxo de mensageria (idempotência, DLQ, ordem).
- Integridade da derivação de status do pedido (RN01, Apêndice A).
- Resiliência das integrações externas (Correios, Câmbio).

## Histórico de Revisão
| Data | Versão | Descrição |
|------|--------|-----------|
| 12/03/2026 | 1.0 | Plano inicial alinhado à ERS v1.0 |
| 09/05/2026 | 1.1 | Alinhamento com ERS v1.2 (status derivado, INSERT-first, plano Correios), metas para adapters/infra, critério mensurável de latência WS, novos casos para RN07 e limite de pedidos ativos |
| 17/05/2026 | 1.2 | Alinhamento com ERS v1.4: adição de TC32 (etapa TAXA → status ENVIADO), remoção de marcadores de pendência |
| 31/05/2026 | 1.3 | Alinhamento com ERS v1.5: TC01/TC06 sem persistência no consumer (pedido persistido de forma síncrona); TC08 DLQ imediata (sem backoff nesta versão) |
| 02/06/2026 | 1.4 | Alinhamento com ERS v1.6: TC33 (etapa DEVOLVIDO → status DEVOLVIDO); RN09 aplicada na camada de aplicação (não trigger) em TC10/TC23; TC29 marcos do stub corrigidos; TC31 intervalo 20min e pedidos não terminais; nota sobre o adapter 17track de produção |

---

## Escopo
- Testes unitários do domínio (Java puro, sem Spring).
- Testes de integração dos endpoints REST (`@SpringBootTest`).
- Testes de integração do fluxo RabbitMQ (Testcontainers com RabbitMQ + MySQL reais).
- Testes de contrato da API (validação de request/response com schemas).

## Fora do escopo (nesta versão)
- Testes de carga/performance (mantidos como "roadmap v2").
- Testes E2E automatizados de frontend (validação manual via roteiro de aceitação).

---

## Metas de cobertura por camada

| Camada | Meta de cobertura | Ferramenta | Observação |
|--------|-------------------|------------|------------|
| `domain/` | **≥ 80%** linhas e branches | JaCoCo | Núcleo do sistema — onde a derivação de status, as validações de etapa e as regras de negócio vivem. |
| `application/usecase/` | **≥ 70%** linhas | JaCoCo | Casos de uso são finos, mas concentram a orquestração. |
| `infrastructure/adapter/in/rest/` | **≥ 60%** linhas | JaCoCo | Cobertura via `@SpringBootTest` + MockMvc. |
| `infrastructure/adapter/in/messaging/` | **≥ 60%** linhas | JaCoCo | Consumers RabbitMQ via Testcontainers. |
| `infrastructure/adapter/out/persistence/` | **≥ 50%** linhas | JaCoCo | Testes de repositório com Testcontainers MySQL. |
| `infrastructure/adapter/out/external/` | Smoke + contrato | Testcontainers / WireMock | Cobertura percentual menos importante; o que importa é validar contrato. |

**Outros critérios de qualidade:**
- Zero issues bloqueantes ou críticos no SonarQube.
- Complexidade ciclomática máxima por método: 10.
- Sem código morto (warnings de "unused" tratados como erro).

---

## Cenários de teste prioritários

### Módulo: Gestão de Pedidos

| ID | Cenário | Tipo | Critério de aceitação |
|----|---------|------|-----------------------|
| TC01 | Criar pedido com dados válidos | Integração | Retorna HTTP 202; pedido persistido de forma síncrona; evento `pedido.criado` publicado no RabbitMQ; consumer publica `notificacao.usuario` |
| TC02 | Criar pedido com código duplicado (mesmo usuário) | Integração | Retorna HTTP 422 com mensagem clara; pedido NÃO é criado (RN06) |
| TC03 | Criar pedido quando usuário já tem 200 pedidos ativos | Integração | Retorna HTTP 422; pré-condição da UC01 violada |
| TC04 | Editar pedido com status derivado `ENTREGUE` | Unitário | Domínio rejeita com `PedidoImutavelException` (RF09) |
| TC05 | Cancelar pedido em qualquer estado (perfil Admin) | Integração | Flag `cancelado` setada; status derivado vira `CANCELADO` |

### Módulo: Derivação de Status (RN01)

| ID | Cenário | Tipo | Critério de aceitação |
|----|---------|------|-----------------------|
| TC15 | Pedido sem etapas → status derivado | Unitário | `PROCESSANDO` |
| TC16 | Última etapa `NA_CHINA` → status derivado | Unitário | `PROCESSANDO` |
| TC17 | Última etapa `AEROPORTO_ORIGEM` → status derivado | Unitário | `ENVIADO` |
| TC18 | Última etapa `CD_BRASIL` → status derivado | Unitário | `ENVIADO` |
| TC19 | Última etapa `ENTREGUE` → status derivado | Unitário | `ENTREGUE` |
| TC20 | Pedido com `cancelado=true` (qualquer etapa) | Unitário | `CANCELADO` (sobrescreve etapa) |
| TC21 | Inserir etapa com timestamp anterior à última | Unitário | `EtapaRetroativaException` (HTTP 422) |
| TC32 | Última etapa `TAXA` → status derivado | Unitário | `ENVIADO` |
| TC33 | Última etapa `DEVOLVIDO` → status derivado | Unitário | `DEVOLVIDO` (terminal — bloqueia novas etapas, `PedidoImutavelException`) |

> **Cobertura:** TC15–TC20, TC32 e TC33 implementam as 12 linhas do Apêndice A da ERS — tabela parametrizada com `@ParameterizedTest`.

### Módulo: Mensageria

| ID | Cenário | Tipo | Critério de aceitação |
|----|---------|------|-----------------------|
| TC06 | Consumer processa evento `pedido.criado` | Integração | Tabela `evento_processado` recebe registro (idempotência); evento `notificacao.usuario` publicado |
| TC07 | Reprocessamento de evento já processado (mesmo `message_id`) | Integração | Nenhum efeito colateral; consumer ack sem reprocessar (UNIQUE constraint dispara, exceção tratada) |
| TC08 | Falha no consumer → DLQ imediata | Integração | `basicNack(requeue=false)`; mensagem aparece em `q.pedido.criado.dlq` com headers `x-death`/`x-first-death-reason` (sem retry automático nesta versão) |
| TC22 | Publisher confirms desativados acidentalmente | Unitário (config) | Falha o teste de configuração — guarda contra regressão de RNF05 |

### Módulo: Notificações

| ID | Cenário | Tipo | Critério de aceitação |
|----|---------|------|-----------------------|
| TC09 | Mudança de status dispara notificação WebSocket | Integração | Cliente STOMP de teste (`StompSession` em Testcontainers) recebe mensagem em **< 2.000ms p95** medido por `Awaitility.await().atMost(2, SECONDS)` em 30 execuções |
| TC10 | Limite de 50 notificações por usuário (RN09) | Integração | Após 60 inserts, tabela `notificacao` contém exatamente 50 registros do usuário; a **camada de aplicação** removeu os 10 mais antigos (FIFO) |
| TC23 | 50 notificações concorrentes (10 threads × 5 inserts) | Integração | Após execução, contagem final ≤ 50 (limite aplicado na **camada de aplicação** com lock pessimista por usuário — não há trigger SQL) |

### Módulo: Autenticação

| ID | Cenário | Tipo | Critério de aceitação |
|----|---------|------|-----------------------|
| TC11 | Login com credenciais válidas | Integração | Retorna 200 com JWT (exp 1h) + refresh token (exp 7d) |
| TC12 | Acesso a endpoint privado sem JWT | Integração | HTTP 401 |
| TC13 | Acesso a pedido de outro usuário (perfil Cliente) | Integração | HTTP 403 |
| TC14 | 5 tentativas de login falhas → bloqueio | Integração | 6ª tentativa retorna HTTP 429; `tentativa_login_falha.bloqueado_ate` futuro |
| TC24 | Logout invalida refresh token | Integração | Hash do token aparece em `refresh_token_revogado`; refresh subsequente retorna 401 (RF03) |

### Módulo: Cotação de Câmbio

| ID | Cenário | Tipo | Critério de aceitação |
|----|---------|------|-----------------------|
| TC25 | Cotação consultada da API com sucesso | Integração (WireMock) | Valor armazenado em cache; resposta de `/cotacao` reflete API |
| TC26 | API de câmbio indisponível, cache válido (< 24h) | Integração | Retorna valor do cache com header `X-Cotacao-Stale: false` (RN07) |
| TC27 | API indisponível e cache > 24h | Integração | Retorna valor do cache com flag `desatualizada=true` na resposta; UI deve sinalizar (RN07) |
| TC28 | Admin define cotação manual | Integração | Cotação manual sobrescreve API; flag `manual=true` retornada |

### Módulo: Integração com Correios

| ID | Cenário | Tipo | Critério de aceitação |
|----|---------|------|-----------------------|
| TC29 | `CorreiosStubAdapter` retorna etapas progressivas | Unitário | Pedido com 0h retorna `NA_CHINA`; 24h `AEROPORTO_ORIGEM`; 48h `EM_TRANSITO`; etc. (ver [ADR-004](../design-de-software/adrs/004-adapters-correios.md)) |
| TC30 | `CorreiosHttpAdapter` com circuit breaker aberto | Integração (WireMock) | Após 5 falhas, próxima chamada NÃO toca a API (curto-circuito); retorna do cache |
| TC31 | Sincronização automática | Integração | Scheduler (intervalo configurável, padrão 20min) consulta pedidos ativos (status não terminal); novas etapas persistidas |

> **Fonte de produção:** o adapter ativo em produção é o `Rastreamento17TrackAdapter` (agregador 17track — `register` + `gettrackinfo`, classifica por `sub_status`, incl. `Exception_Returning`→`DEVOLVIDO`), validado manualmente contra a API real. Teste de contrato automatizado (WireMock) do 17track é dívida v2.

---

## Métricas de aceitação da release

- [ ] **Domínio:** TC04, TC15–TC21 passando (derivação de status + invariantes)
- [ ] **Mensageria:** TC06–TC08, TC22 passando (idempotência, DLQ, durabilidade)
- [ ] **Notificações:** TC09, TC10, TC23 passando (RN09 atômico, latência WS)
- [ ] **Autenticação:** TC11–TC14, TC24 passando (JWT + revogação + bloqueio)
- [ ] **Cotação:** TC25–TC28 passando (RN07 e cotação manual)
- [ ] **Correios:** TC29 obrigatório (stub é o adapter default em demonstração)
- [ ] **Cobertura JaCoCo:** ≥ 80% no `domain/`, ≥ 70% no `application/`
- [ ] **SonarQube:** zero issues bloqueantes/críticos

---

## Ferramentas

| Categoria | Ferramenta |
|-----------|------------|
| Unit testing | JUnit 5 + Mockito + AssertJ |
| Parameterized tests | `@ParameterizedTest` (JUnit 5) — usado em TC15–TC20 |
| Integration testing | `@SpringBootTest` + Testcontainers (MySQL 8 + RabbitMQ 3.13) |
| HTTP mock | WireMock (para API Correios e API Câmbio nos testes) |
| WebSocket testing | Spring `WebSocketStompClient` + `Awaitility` |
| Cobertura | JaCoCo (relatório em `target/site/jacoco/index.html`) |
| Análise estática | SonarQube (gate de qualidade no CI) |
