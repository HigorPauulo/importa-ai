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

#### Trecho de código — Repository
`domain/port/out/PedidoRepository.java` (porta de saída, Java puro)
```java
public interface PedidoRepository {
    Pedido salvar(Pedido pedido);
    Optional<Pedido> buscarPorId(Long id);
    Optional<Pedido> buscarPorCodigoRastreamentoEUsuario(String codigoRastreamento, Long usuarioId);
    List<Pedido> listarPorUsuario(Long usuarioId);
    List<Pedido> listarNaoCancelados();
}
```

#### Trecho de código — Adapter (Hexagonal)
`infrastructure/adapter/out/external/Rastreamento17TrackAdapter.java` (um dos 4 adapters da porta, selecionado por `correios.adapter`)
```java
@Component
@ConditionalOnProperty(name = "correios.adapter", havingValue = "17track")
public class Rastreamento17TrackAdapter implements RastreamentoCorreiosPort {

    @Override
    @CircuitBreaker(name = CIRCUIT, fallbackMethod = "consultarDoCache")
    public ResultadoRastreio consultar(String codigoRastreamento, Instant pedidoCriadoEm) {
        TrackResponse resposta = consultarRastreio(codigoRastreamento);
        Objeto objeto = encontrar(resposta, codigoRastreamento);
        return interpretar(codigoRastreamento, objeto);
    }
}
```

### Patterns arquiteturais (event-driven)

| Pattern | Onde | Problema que resolve |
|---------|------|----------------------|
| **Publish-Subscribe (via RabbitMQ)** | Exchange `importaai.events` + filas por routing key | Desacopla o produtor (controllers) dos consumers (persistência, notificação). Permite adicionar novos consumers sem tocar quem publica. |
| **Saga Coreografada** | `PedidoCriadoConsumer` (criação) e `RastreamentoConsumer` (mudança de status, RF16) → `NotificacaoConsumer` | Cada consumer reage a um evento e publica o próximo, sem orquestrador central. Adequado quando há poucos passos e baixa complexidade transacional. |
| **Idempotent Consumer** | Tabela `evento_processado` com UNIQUE em `(exchange, routing_key, message_id)` | Garante que reprocessamento de mensagem (retry, redelivery) não gere efeitos duplicados. Implementação INSERT-first: tenta inserir; se UNIQUE dispara, descarta. |
| **Dead Letter Queue (DLQ)** | `*.dlq` para cada fila principal | Mensagem que falha (`basicNack(requeue=false)`) sai **imediatamente** do fluxo principal para a DLQ (sem retry automático nesta versão), permitindo investigação sem bloquear processamento. |
| **Cache-Aside** | Cotação de câmbio (`ConsultarCotacaoService`): lê cache → se miss/expired, chama API → atualiza cache | Reduz chamadas externas e provê fallback (RN07). |
| **Circuit Breaker + fallback** | resilience4j `@CircuitBreaker(name="correios")` em `CorreiosHttpAdapter` / `Rastreamento17TrackAdapter` | Após N falhas, abre o circuito e serve o último cache conhecido em vez de insistir na fonte externa fora do ar. |

#### Trecho de código — Publish-Subscribe
`infrastructure/adapter/out/messaging/RabbitMqEventPublisher.java`
```java
public void publicar(String routingKey, Object evento) {
    String messageId = UUID.randomUUID().toString();
    Envelope envelope = new Envelope(messageId, routingKey, SCHEMA_VERSION, Instant.now(), evento);
    template.convertAndSend(RabbitMQConfig.EXCHANGE_EVENTS, routingKey, envelope, msg -> {
        msg.getMessageProperties().setMessageId(messageId);
        msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        return msg;
    });
}
```

#### Trecho de código — Saga Coreografada
`infrastructure/adapter/in/messaging/PedidoCriadoConsumer.java` (reage a `pedido.criado` e publica o próximo evento)
```java
@RabbitListener(queues = RabbitMQConfig.Q_PEDIDO_CRIADO)
public void onPedidoCriado(Envelope envelope, String messageId, Channel channel, long deliveryTag) {
    eventoRepo.saveAndFlush(registro);
    PedidoCriadoEvento pedido = objectMapper.convertValue(envelope.data(), PedidoCriadoEvento.class);
    eventPublisher.publicar(
            RabbitMQConfig.RK_NOTIFICACAO_USUARIO,
            new NotificacaoEvento(pedido.usuarioId(), pedido.id(), MENSAGEM_PEDIDO_CRIADO));
    channel.basicAck(deliveryTag, false);
}
```

#### Trecho de código — Idempotent Consumer
INSERT-first em `evento_processado` (UNIQUE) antes do efeito de negócio
```java
try {
    eventoRepo.saveAndFlush(new EventoProcessadoEntity(
            RabbitMQConfig.EXCHANGE_EVENTS, routingKey, messageId, Instant.now()));
    // processa o efeito de negócio
    channel.basicAck(deliveryTag, false);
} catch (DataIntegrityViolationException e) {
    channel.basicAck(deliveryTag, false);
} catch (Exception e) {
    channel.basicNack(deliveryTag, false, false);
}
```

#### Trecho de código — Dead Letter Queue (DLQ)
`infrastructure/config/RabbitMQConfig.java`
```java
@Bean Queue qPedidoCriado() {
    return QueueBuilder.durable(Q_PEDIDO_CRIADO)
            .withArgument("x-dead-letter-exchange", EXCHANGE_EVENTS_DLQ)
            .withArgument("x-dead-letter-routing-key", RK_PEDIDO_CRIADO + ".dlq")
            .build();
}
```

#### Trecho de código — Cache-Aside
`application/usecase/ConsultarCotacaoService.java`
```java
Optional<Cotacao> cacheada = cotacaoRepository.buscarPorPar(input.moedaOrigem(), input.moedaDestino());
if (cacheada.isPresent() && !cacheada.get().estaDesatualizada(agora)) {
    return Optional.of(new Output(cacheada.get(), false));            // cache hit
}
Optional<CambioPort.TaxaCambio> taxaApi = cambioPort.consultarTaxa(input.moedaOrigem(), input.moedaDestino());
if (taxaApi.isPresent()) {                                            // miss: chama a API e atualiza o cache
    CambioPort.TaxaCambio taxa = taxaApi.get();
    Cotacao fresca = cotacaoRepository.salvar(Cotacao.automatica(
            input.moedaOrigem(), input.moedaDestino(), taxa.taxa(), taxa.cotadoEm(), agora));
    return Optional.of(new Output(fresca, false));
}
return cacheada.map(c -> new Output(c, c.estaDesatualizada(agora)));  // API fora: fallback (RN07)
```

#### Trecho de código — Circuit Breaker + fallback
`infrastructure/adapter/out/external/Rastreamento17TrackAdapter.java` (resilience4j)
```java
@CircuitBreaker(name = CIRCUIT, fallbackMethod = "consultarDoCache")
public ResultadoRastreio consultar(String codigoRastreamento, Instant pedidoCriadoEm) {
    // chamada à fonte externa (17track)
}

private ResultadoRastreio consultarDoCache(String codigo, Instant criadoEm, Throwable t) {
    return ResultadoRastreio.indisponivel(cache.recuperar(codigo));
}
```

### Padrões do domínio

| Pattern | Onde | Problema que resolve |
|---------|------|----------------------|
| **Value Object derivado** | `StatusPedido.derivar(ultimaEtapa, cancelado)` | Status do pedido não é atributo armazenado — é função pura das etapas. Garante uma única fonte de verdade (RN01) e elimina a possibilidade de "status desincronizado com a realidade". |
| **Aggregate Root** | `Pedido` é raiz que protege a coleção de `EtapaRastreamento` | Toda mutação de etapa passa pelo `Pedido` (`adicionarEtapa`), preservando invariantes (append-only por timestamp). |

#### Trecho de código — Value Object derivado
`domain/model/StatusPedido.java` (função pura, sem campo armazenado — RN01)
```java
public static StatusPedido derivar(Optional<TipoEtapa> ultimaEtapa, boolean cancelado) {
    if (cancelado) {
        return CANCELADO;
    }
    if (ultimaEtapa.isEmpty()) {
        return PROCESSANDO;
    }
    return switch (ultimaEtapa.get()) {
        case NA_CHINA -> PROCESSANDO;
        case ENTREGUE -> ENTREGUE;
        case DEVOLVIDO -> DEVOLVIDO;
        case AEROPORTO_ORIGEM, EM_TRANSITO, AEROPORTO_DESTINO,
             NO_BRASIL, TAXA, CD_BRASIL, SAIDA_ENTREGA -> ENVIADO;
    };
}
```

#### Trecho de código — Aggregate Root
`domain/model/Pedido.java` (toda mutação passa pela raiz, preservando invariantes)
```java
public void adicionarEtapa(EtapaRastreamento etapa) {
    StatusPedido statusAtual = getStatus();
    if (statusAtual == StatusPedido.ENTREGUE
            || statusAtual == StatusPedido.DEVOLVIDO
            || statusAtual == StatusPedido.CANCELADO) {
        throw new PedidoImutavelException("pedido com status " + statusAtual + " nao aceita novas etapas");
    }
    if (!etapas.isEmpty()) {
        Instant ultimo = etapas.get(etapas.size() - 1).criadoEm();
        if (etapa.criadoEm().isBefore(ultimo)) {
            throw new EtapaRetroativaException("etapa anterior a ultima");
        }
    }
    etapas.add(etapa);
}
```

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
