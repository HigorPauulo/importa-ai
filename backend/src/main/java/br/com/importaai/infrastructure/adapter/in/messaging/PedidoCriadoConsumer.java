package br.com.importaai.infrastructure.adapter.in.messaging;

import br.com.importaai.domain.port.out.EventPublisher;
import br.com.importaai.infrastructure.adapter.out.messaging.Envelope;
import br.com.importaai.infrastructure.adapter.out.messaging.NotificacaoEvento;
import br.com.importaai.infrastructure.adapter.out.messaging.PedidoCriadoEvento;
import br.com.importaai.infrastructure.adapter.out.persistence.entity.EventoProcessadoEntity;
import br.com.importaai.infrastructure.adapter.out.persistence.repository.EventoProcessadoJpaRepository;
import br.com.importaai.infrastructure.config.RabbitMQConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class PedidoCriadoConsumer {

    private static final Logger log = LoggerFactory.getLogger(PedidoCriadoConsumer.class);
    private static final String MENSAGEM_PEDIDO_CRIADO =
            "Seu pedido foi registrado e está em processamento.";

    private final EventoProcessadoJpaRepository eventoRepo;
    private final EventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public PedidoCriadoConsumer(EventoProcessadoJpaRepository eventoRepo,
                                EventPublisher eventPublisher,
                                ObjectMapper objectMapper) {
        this.eventoRepo = eventoRepo;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = RabbitMQConfig.Q_PEDIDO_CRIADO)
    public void onPedidoCriado(
            Envelope envelope,
            @Header(AmqpHeaders.MESSAGE_ID) String messageId,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {

        try {
            EventoProcessadoEntity registro = new EventoProcessadoEntity(
                    RabbitMQConfig.EXCHANGE_EVENTS,
                    RabbitMQConfig.RK_PEDIDO_CRIADO,
                    messageId,
                    Instant.now()
            );
            eventoRepo.saveAndFlush(registro);

            PedidoCriadoEvento pedido = objectMapper.convertValue(envelope.data(), PedidoCriadoEvento.class);

            eventPublisher.publicar(
                    RabbitMQConfig.RK_NOTIFICACAO_USUARIO,
                    new NotificacaoEvento(pedido.usuarioId(), pedido.id(), MENSAGEM_PEDIDO_CRIADO));

            log.info("pedido.criado processado — messageId={} eventId={}", messageId, envelope.eventId());

            channel.basicAck(deliveryTag, false);

        } catch (DataIntegrityViolationException e) {
            log.warn("pedido.criado duplicado, ignorando — messageId={}", messageId);
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("falha ao processar pedido.criado — messageId={}: {}", messageId, e.getMessage(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
