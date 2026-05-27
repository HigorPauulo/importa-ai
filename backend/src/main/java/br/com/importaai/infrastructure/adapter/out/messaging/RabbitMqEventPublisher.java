package br.com.importaai.infrastructure.adapter.out.messaging;

import br.com.importaai.domain.port.out.EventPublisher;
import br.com.importaai.infrastructure.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class RabbitMqEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RabbitMqEventPublisher.class);
    private static final String SCHEMA_VERSION = "1.0";

    private final RabbitTemplate template;

    public RabbitMqEventPublisher(RabbitTemplate template) {
        this.template = template;
    }

    @Override
    public void publicar(String routingKey, Object evento) {
        String messageId = UUID.randomUUID().toString();
        Envelope envelope = new Envelope(
                messageId,
                routingKey,
                SCHEMA_VERSION,
                Instant.now(),
                evento
        );

        template.convertAndSend(
                RabbitMQConfig.EXCHANGE_EVENTS,
                routingKey,
                envelope,
                msg -> {
                    msg.getMessageProperties().setMessageId(messageId);
                    msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    return msg;
                }
        );

        log.debug("evento publicado: routingKey={} messageId={}", routingKey, messageId);
    }
}
