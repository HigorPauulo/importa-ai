# Arquitetura de Mensageria — Importa Aí

**Versão:** 2.0
**Data:** 11 de Maio de 2026
**Autor:** Equipe Importa Aí

## Histórico de Revisão

| Data | Versão | Descrição |
|------|--------|-----------|
| 11/05/2026 | 1.0 | Versão completa (12 seções, 1.013 linhas) |
| 11/05/2026 | 2.0 | Consolidação de escopo e remoção de seções acessórias |

---

## Sumário

1. Visão geral
2. Escolha do broker: RabbitMQ
3. Topologia
4. Padrões de publicação
5. Padrões de consumo
6. Política de retry e Dead Letter Queue
7. Idempotência (RN04)
8. Fluxos detalhados
9. Tratamento de indisponibilidade do broker
10. Referências

---

## 1. Visão geral

O Importa Aí usa **RabbitMQ** para desacoplar operações de escrita do frontend da persistência no banco (RN03), oferecer garantias de durabilidade de eventos (RNF05) e permitir escala horizontal de consumidores (RNF11). Este documento descreve como a mensageria é configurada e operada.

---

## 2. Escolha do broker: RabbitMQ

Decisão completa em **[ADR-001](../design-de-software/adrs/001-broker-rabbitmq.md)**.

Em resumo: RabbitMQ foi escolhido pela maturidade do AMQP, suporte nativo a ACK manual, Publisher Confirms e DLQ, e painel Management. Kafka seria *over-engineering* para o volume e o padrão de fanout por usuário do projeto.

---

## 3. Topologia

### 3.1 Exchanges

| Nome | Tipo | Durable |
|------|------|---------|
| `importaai.events` | direct | true |
| `importaai.events.dlq` | direct | true |

`direct` foi escolhido porque o conjunto de rotas é fechado e exato (`pedido.criado`, `pedido.atualizado`, `rastreamento.atualizado`, `notificacao.usuario`).

### 3.2 Filas

| Nome | Recebe routing key | Consumer |
|------|--------------------|----------|
| `q.pedido.criado` | `pedido.criado` | `PedidoCriadoConsumer` |
| `q.pedido.atualizado` | `pedido.atualizado` | `PedidoAtualizadoConsumer` |
| `q.rastreamento.atualizado` | `rastreamento.atualizado` | `RastreamentoConsumer` |
| `q.notificacao.usuario` | `notificacao.usuario` | `NotificacaoConsumer` |

Cada fila principal tem uma DLQ correspondente (`*.dlq`), declarada via argumentos `x-dead-letter-exchange = importaai.events.dlq` e `x-dead-letter-routing-key = <routing-key-original>.dlq`.

**Propriedades comuns:** `durable=true`, `exclusive=false`, `auto-delete=false`, sem TTL. DLQ por fila (não centralizada) permite identificar o fluxo falho pelo próprio nome.

### 3.3 Bindings

Cada fila se liga à sua exchange com a routing key correspondente (binding direto e exato). A convenção `<routing-key>.dlq` mantém correspondência 1:1 entre fluxo principal e *dead letter*.

### 3.4 Propriedades das mensagens

| Propriedade | Valor | Função |
|-------------|-------|--------|
| `message-id` | UUID v4 | Chave de idempotência (§7) |
| `content-type` | `application/json` | Serialização padrão |
| `delivery-mode` | `2` (persistent) | Mensagem gravada em disco (RNF05) |
| `correlation-id` | propagado do request HTTP | Liga logs do producer ao consumer |

> `durable` é da fila; `persistent` é da mensagem. As duas precisam estar alinhadas para a garantia ser efetiva.

### 3.5 Diagrama da topologia

```
                publicação (producer)
                         │
                         ▼
              ┌──────────────────────────┐
              │ importaai.events (direct)│
              └──────────────────────────┘
                │     │     │     │
        (4 routing keys — uma por fluxo)
                │     │     │     │
                ▼     ▼     ▼     ▼
              [q1] [q2] [q3] [q4]         (filas principais)
                │     │     │     │
        ── falha 4× ──── ► importaai.events.dlq
                                  │
              ┌──────────┬────────┴───────┬──────────┐
              ▼          ▼                ▼          ▼
            [q1.dlq]  [q2.dlq]         [q3.dlq]   [q4.dlq]
```

---

## 4. Padrões de publicação

A publicação é mediada pela **porta de saída `EventPublisher`**, declarada no domínio. A implementação concreta vive na camada de infraestrutura — trocar de broker exige substituir apenas o adapter (ver [ADR-002](../design-de-software/adrs/002-arquitetura-hexagonal.md)).

Garantias adotadas:

- **`delivery-mode=2`** em todas as mensagens (persistente em disco).
- **Publisher Confirms** habilitados em modo correlacionado — o producer recebe ACK explícito do broker antes de continuar. Atende RNF05.
- **Mandatory return** habilitado — mensagens com routing key sem fila bound são devolvidas ao producer e logadas como erro crítico.
- **Serialização JSON** com envelope padrão: `event_id` (igual ao `message-id`), `event_type`, `schema_version`, `occurred_at`, `data`.

Falha de publicação (NACK ou timeout) é propagada ao use case; o controller responde HTTP 503 (ver §9).

---

## 5. Padrões de consumo

Cada consumer vive em `infrastructure/adapter/in/messaging/`, recebe a mensagem, deserializa para um *record* de evento, delega ao use case e faz `ACK` ou `NACK` **manualmente** conforme o resultado.

| Configuração | Valor | Razão |
|--------------|-------|-------|
| `acknowledge-mode` | manual | Necessário para combinar com idempotência (§7) e retry granular (§6) |
| `prefetch` | 10 | Distribuição justa entre instâncias; volume baixo não exige throughput máximo |
| Threads concorrentes | 2 por consumer/instância | Combinado com 2-3 instâncias dá paralelismo efetivo de 4–6 |

Tratamento de exceção (combina §6 e §7):

```
TENTAR
    registrar (exchange, routing-key, message-id) em evento_processado
    processar o payload
    ACK manual

CAPTURAR violação de UNIQUE em evento_processado
    duplicata → ACK silencioso (§7)

CAPTURAR exceção transitória (timeout, rede, banco temporariamente indisponível)
    propagar para o mecanismo de retry (§6)

CAPTURAR qualquer outra exceção (validação, regra de negócio, schema)
    NACK sem requeue → DLQ (§6)
```

---

## 6. Política de retry e Dead Letter Queue

**Política adotada:** 1 entrega original + 3 retentativas com backoff exponencial.

| Tentativa | Espera antes |
|-----------|--------------|
| 1ª (entrega original) | 0 |
| 2ª | 1 s |
| 3ª | 2 s |
| 4ª | 4 s |
| → DLQ | após ≈ 7 s totais |

**Implementação:** retry **em memória** pelo framework AMQP. Alternativas (filas TTL, plugin *delayed messages*) descartadas por adicionar complexidade desproporcional ao volume.

**Roteamento para DLQ:** após esgotar retries, o consumer faz `basicNack(requeue=false)`. O broker consulta `x-dead-letter-exchange` e `x-dead-letter-routing-key` da fila e re-publica na DLX correspondente. A mensagem chega na DLQ com headers `x-death`, `x-first-death-queue` e `x-first-death-reason` injetados pelo broker — facilitam diagnóstico.

**Investigação:** DLQs **não têm consumer automático**. Operador acessa o painel Management, inspeciona a mensagem e decide: re-publicar (com **novo `message_id`** — ver §7), descartar, ou aguardar correção de bug.

---

## 7. Idempotência (RN04)

A garantia *at-least-once* do RabbitMQ implica que uma mesma mensagem pode chegar ao consumer mais de uma vez (redelivery após crash, retry do producer, reinjeção manual da DLQ). Sem proteção, o sistema duplicaria pedidos, etapas e notificações.

### 7.1 Tabela `evento_processado`

| Coluna | Tipo |
|--------|------|
| `id` | BIGINT AUTO_INCREMENT (PK) |
| `exchange` | VARCHAR(100) |
| `routing_key` | VARCHAR(100) |
| `message_id` | VARCHAR(36) |
| `processado_em` | TIMESTAMP DEFAULT CURRENT_TIMESTAMP |

**Constraint chave:** `UNIQUE (exchange, routing_key, message_id)`.

### 7.2 Estratégia INSERT-first

```
ABRIR transação
TENTAR INSERT INTO evento_processado VALUES (X, R, I)
    CASO sucesso:
        processar trabalho de negócio
        COMMIT + ACK manual
    CASO violação de UNIQUE:
        ROLLBACK
        ACK silencioso (mensagem já processada antes)
```

O registro de idempotência é feito **antes** do trabalho de negócio, dentro da mesma transação. Se o trabalho falha, a transação aborta e o registro some — preservando a possibilidade de retry futuro.

### 7.3 Por que INSERT-first e não SELECT-then-INSERT?

`SELECT-then-INSERT` tem race condition: duas instâncias do consumer (RNF11) podem ler "não existe", ambas processarem e duplicarem.

`INSERT-first` explora a atomicidade nativa do banco — o índice UNIQUE garante que apenas uma das instâncias consegue inserir; as outras recebem violação e descartam. Sem locks, sem mutex externo.

### 7.4 Reprocessamento intencional

Reinjetar mensagem da DLQ com o mesmo `message_id` resulta em descarte (idempotência protege). Para reprocessar deliberadamente, o operador deve **gerar novo `message_id`** antes de re-publicar.

### 7.5 Limpeza da tabela

Job diário: remover registros com `processado_em < NOW() - INTERVAL 30 DAY`. Após 30 dias, mensagens não podem mais ser redelivered em condições normais (TTL e DLQ já as teriam capturado).

---

## 8. Fluxos detalhados

### 8.1 Criar Pedido (UC01)

```
1. Controller recebe POST /api/pedidos.
2. CriarPedidoUseCase valida o payload.
3. UseCase publica "pedido.criado" via EventPublisher.
4. Broker confirma (publisher confirm ACK).
5. Controller retorna HTTP 202.
   ── FIM da jornada síncrona (RNF02: < 200 ms) ──

== ASSÍNCRONO A PARTIR DAQUI ==
6. PedidoCriadoConsumer consome de q.pedido.criado.
7. INSERT-first em evento_processado (§7).
8. Persiste o Pedido.
9. Publica "notificacao.usuario" (evento derivado).
10. ACK.
11. NotificacaoConsumer consome → persiste a notificação (trigger RN09 limita a 50) → envia STOMP → ACK.
```

### 8.2 Inserir Etapa Manual (UC07)

```
1. Controller recebe POST /api/pedidos/{id}/etapas.
2. RegistrarEtapaUseCase valida (timestamp ≥ última etapa, pedido não cancelado).
3. Publica "rastreamento.atualizado".
4. Controller retorna HTTP 202.

== ASSÍNCRONO ==
5. RastreamentoConsumer consome → INSERT-first → persiste etapa → recalcula status_cache.
6. Se o status derivado mudou: publica "pedido.atualizado" → cascata gera "notificacao.usuario".
```

### 8.3 Sincronização Correios (RF15)

Scheduler a cada 6 h consulta a API dos Correios para pedidos com status derivado `ENVIADO`. Para cada nova etapa detectada, publica `rastreamento.atualizado`. O fluxo segue idêntico a §8.2 a partir do passo 5.

### 8.4 Notificação ao usuário (UC03)

```
1. NotificacaoConsumer consome de q.notificacao.usuario.
2. INSERT-first em evento_processado.
3. Persiste a notificação (trigger SQL aplica RN09 — FIFO de 50).
4. Envia STOMP para /user/{usuario_id}/queue/notificacoes.
5. ACK.

Se a sessão WebSocket NÃO está ativa, a notificação fica no histórico (passo 3) — o usuário a vê no próximo acesso.
```

### 8.5 Coreografia

Cada consumer reage a um evento e publica o próximo (quando faz sentido), sem orquestrador central. Saga orquestrada seria *over-engineering* para o volume e a complexidade — coreografia basta.

---

## 9. Tratamento de indisponibilidade do broker

Se a publicação falha (timeout no publisher confirm, falha de conexão, NACK explícito), o backend retorna **HTTP 503 honesto** em vez de aceitar a operação:

```
HTTP/1.1 503 Service Unavailable
Retry-After: 30
```

**Por que não Outbox pattern?** Outbox (gravar local + worker republica) resolveria o conflito RN03 vs broker fora, mas adiciona complexidade significativa: tabela `outbox`, worker dedicado, atomicidade entre transação de negócio e INSERT na outbox. Registrado como dívida para v2; nesta versão, **503 honesto é melhor que 202 mentiroso**.

---

## 10. Referências

- [ERS](../design-de-software/ERS.md) — RN03, RN04, RN05, RNF02, RNF05, RNF11, Apêndice A
- [ADRs](../design-de-software/adrs/) — ADR-001 (broker), ADR-002 (hexagonal), ADR-003 (status derivado)
- [Design Patterns Escolhidos](../design-de-software/design-patterns-escolhidos.md)
- AMQP 0-9-1 Reference — https://www.rabbitmq.com/amqp-0-9-1-reference.html
- RabbitMQ Confirms and Returns — https://www.rabbitmq.com/confirms.html
- RabbitMQ Dead Letter Exchanges — https://www.rabbitmq.com/dlx.html
