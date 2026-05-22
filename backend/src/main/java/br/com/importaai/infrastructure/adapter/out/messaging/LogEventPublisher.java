package br.com.importaai.infrastructure.adapter.out.messaging;

import br.com.importaai.domain.port.out.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Adapter TEMPORÁRIO do port EventPublisher.
 * Será substituído pelo RabbitMqEventPublisher na Fase 5.
 * Existe só pra Spring conseguir instanciar os use cases que dependem de EventPublisher.
 */
@Component
public class LogEventPublisher implements EventPublisher {

    // SLF4J padrão (sem Lombok @Slf4j) pra você ver o pattern Spring explícito.
    private static final Logger log = LoggerFactory.getLogger(LogEventPublisher.class);

    @Override
    public void publicar(String routingKey, Object evento) {
        // INFO porque é um sinal útil em dev. Em produção (Fase 5) vira DEBUG dentro do RabbitMQ publisher.
        log.info("[EVENTO] routingKey={} evento={}", routingKey, evento);
    }
}
