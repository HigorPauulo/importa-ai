package br.com.importaai.infrastructure.adapter.in.messaging;

import br.com.importaai.domain.port.out.EventPublisher;
import br.com.importaai.infrastructure.adapter.out.messaging.Envelope;
import br.com.importaai.infrastructure.adapter.out.messaging.NotificacaoEvento;
import br.com.importaai.infrastructure.adapter.out.persistence.entity.EventoProcessadoEntity;
import br.com.importaai.infrastructure.adapter.out.persistence.repository.EventoProcessadoJpaRepository;
import br.com.importaai.infrastructure.config.RabbitMQConfig;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class RastreamentoConsumer {

    private static final Logger log = LoggerFactory.getLogger(RastreamentoConsumer.class);

    private final EventoProcessadoJpaRepository eventoRepo;
    private final EventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public RastreamentoConsumer(EventoProcessadoJpaRepository eventoRepo,
                                EventPublisher eventPublisher,
                                ObjectMapper objectMapper) {
        this.eventoRepo = eventoRepo;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = {
            RabbitMQConfig.Q_RASTREAMENTO_ATUALIZADO,
            RabbitMQConfig.Q_PEDIDO_ATUALIZADO
    })
    public void onAtualizado(
            Envelope envelope,
            @Header(AmqpHeaders.MESSAGE_ID) String messageId,
            @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {

        try {
            EventoProcessadoEntity registro = new EventoProcessadoEntity(
                    RabbitMQConfig.EXCHANGE_EVENTS,
                    routingKey,
                    messageId,
                    Instant.now()
            );
            eventoRepo.saveAndFlush(registro);

            PedidoAtualizadoEvento pedido =
                    objectMapper.convertValue(envelope.data(), PedidoAtualizadoEvento.class);

            eventPublisher.publicar(
                    RabbitMQConfig.RK_NOTIFICACAO_USUARIO,
                    new NotificacaoEvento(
                            pedido.usuarioId(),
                            pedido.id(),
                            mensagemPara(pedido.status(), pedido.codigoRastreamento())));

            log.info("status atualizado processado — rk={} pedido={} status={}",
                    routingKey, pedido.id(), pedido.status());

            channel.basicAck(deliveryTag, false);

        } catch (DataIntegrityViolationException e) {
            log.warn("evento de atualizacao duplicado, ignorando — messageId={}", messageId);
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("falha ao processar atualizacao — messageId={}: {}", messageId, e.getMessage(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }

    private String mensagemPara(String status, String codigo) {
        return switch (status == null ? "" : status) {
            case "ENVIADO" -> "Seu pedido " + codigo + " está a caminho.";
            case "ENTREGUE" -> "Seu pedido " + codigo + " foi entregue.";
            case "DEVOLVIDO" -> "Seu pedido " + codigo + " foi devolvido pela autoridade aduaneira.";
            case "CANCELADO" -> "Seu pedido " + codigo + " foi cancelado.";
            default -> "Seu pedido " + codigo + " teve uma atualização de rastreio.";
        };
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record PedidoAtualizadoEvento(Long id, Long usuarioId, String codigoRastreamento, String status) {}
}
