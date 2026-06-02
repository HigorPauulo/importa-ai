# Arquitetura e Design Patterns — Importa Aí

## Arquitetura: Hexagonal (Ports & Adapters)

### Justificativa
A Arquitetura Hexagonal foi escolhida porque:
1. **Testabilidade**: o domínio não depende de frameworks — pode ser testado em isolamento com JUnit puro.
2. **Independência de mensageria**: RabbitMQ e API REST são adaptadores intercambiáveis. Trocar de broker ou de canal de entrada não toca o domínio.
3. **Manutenibilidade**: limites claros entre domínio e infraestrutura reduzem acoplamento acidental e facilitam evolução incremental.
4. **Adequação ao problema**: o sistema possui múltiplos adaptadores de entrada (REST, RabbitMQ consumer, scheduler) e saída (MySQL, RabbitMQ producer, WebSocket, API Correios, API Câmbio). Essa multiplicidade é exatamente o caso de uso natural do estilo Hexagonal.
5. **Defesa concreta contra falhas externas**: o `RastreamentoCorreiosPort` tem 4 adapters (stub, http CWS, cache-only, 17track). Trocar de comportamento em produção é mudar configuração — não código de domínio.

---

## Design Patterns aplicados

> **Princípio:** cada pattern listado abaixo resolve um problema concreto do sistema. Patterns que apareciam em versões anteriores apenas para "demonstrar conhecimento" foram removidos para manter a documentação honesta com a implementação.

### Patterns estruturais e de criação

| Pattern | Onde | Problema que resolve |
|---------|------|----------------------|
| **Repository** | `PedidoRepository`, `UsuarioRepository`, `NotificacaoRepository` (portas de saída do domínio) | Abstrai a persistência; o domínio expressa "salvar pedido" sem saber que existe MySQL ou JPA. |
| **Adapter (Hexagonal)** | `CorreiosStubAdapter`, `CorreiosHttpAdapter`, `CorreiosCacheOnlyAdapter`, `Rastreamento17TrackAdapter` para a porta `RastreamentoCorreiosPort` | Permite trocar a fonte de rastreamento (stub, CWS, cache, 17track) por configuração, sem alterar o domínio. |

### Patterns arquiteturais (event-driven)

| Pattern | Onde | Problema que resolve |
|---------|------|----------------------|
| **Publish-Subscribe (via RabbitMQ)** | Exchange `importaai.events` + filas por routing key | Desacopla o produtor (controllers) dos consumers (persistência, notificação). Permite adicionar novos consumers sem tocar quem publica. |
| **Saga Coreografada** | `PedidoCriadoConsumer` (criação) e `RastreamentoConsumer` (mudança de status, RF16) → `NotificacaoConsumer` | Cada consumer reage a um evento e publica o próximo, sem orquestrador central. Adequado quando há poucos passos e baixa complexidade transacional. |
| **Idempotent Consumer** | Tabela `evento_processado` com UNIQUE em `(exchange, routing_key, message_id)` | Garante que reprocessamento de mensagem (retry, redelivery) não gere efeitos duplicados. Implementação INSERT-first: tenta inserir; se UNIQUE dispara, descarta. |
| **Dead Letter Queue (DLQ)** | `*.dlq` para cada fila principal | Mensagem que falha (`basicNack(requeue=false)`) sai **imediatamente** do fluxo principal para a DLQ (sem retry automático nesta versão), permitindo investigação sem bloquear processamento. |
| **Cache-Aside** | Cotação de câmbio (`ConsultarCotacaoService`): lê cache → se miss/expired, chama API → atualiza cache | Reduz chamadas externas e provê fallback (RN07). |
| **Circuit Breaker + fallback** | resilience4j `@CircuitBreaker(name="correios")` em `CorreiosHttpAdapter` / `Rastreamento17TrackAdapter` | Após N falhas, abre o circuito e serve o último cache conhecido em vez de insistir na fonte externa fora do ar. |

### Padrões do domínio

| Pattern | Onde | Problema que resolve |
|---------|------|----------------------|
| **Value Object derivado** | `StatusPedido.derivar(ultimaEtapa, cancelado)` | Status do pedido não é atributo armazenado — é função pura das etapas. Garante uma única fonte de verdade (RN01) e elimina a possibilidade de "status desincronizado com a realidade". |
| **Aggregate Root** | `Pedido` é raiz que protege a coleção de `EtapaRastreamento` | Toda mutação de etapa passa pelo `Pedido` (`adicionarEtapa`), preservando invariantes (append-only por timestamp). |

---

## Patterns considerados e descartados

Documentar o que foi rejeitado é tão importante quanto o que foi adotado.

| Pattern | Motivo da rejeição |
|---------|-------------------|
| **Chain of Responsibility** (validação de transição de estado) | A "transição" deixou de existir após a derivação de status (RN01). Mesmo se existisse, a regra é uma matriz pequena — um `switch` resolve. CoR seria over-engineering. |
| **Strategy** (cotação de câmbio) | Strategy se justifica quando o caller escolhe a fonte em runtime. No nosso caso, há uma única fonte real e um fallback — **Cache-Aside** é a leitura correta (ver tabela acima). |
| **Facade** (`PedidoService`) | Em Spring, qualquer `@Service` orquestra múltiplos colaboradores e funciona como facade. Citar isso como pattern explícito é trivial e diluiria a lista. |
| **Singleton** | Beans Spring são singletons por padrão. Nada a documentar como "decisão". |
| **Outbox Pattern** | Resolveria a janela "registro persistido, evento não publicado" quando o broker está fora (UC01 FA02), mas adiciona complexidade significativa (tabela outbox, worker dedicado, polling/CDC). Ficou para v2. Nesta versão, a escrita persiste de forma síncrona (o dado não se perde) e a publicação do evento é *best-effort* — ver `arquitetura-mensageria.md §9`. |
