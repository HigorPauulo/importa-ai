package br.com.importaai.infrastructure.adapter.in.messaging;

import br.com.importaai.domain.model.Notificacao;
import br.com.importaai.domain.port.in.PersistirNotificacaoUseCase;
import br.com.importaai.infrastructure.adapter.out.messaging.Envelope;
import br.com.importaai.infrastructure.adapter.out.messaging.NotificacaoEvento;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class NotificacaoConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoConsumer.class);
    private static final String DESTINO_STOMP = "/queue/notificacoes";

    private final EventoProcessadoJpaRepository eventoRepo;
    private final ObjectMapper objectMapper;
    private final PersistirNotificacaoUseCase persistirNotificacao;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificacaoConsumer(EventoProcessadoJpaRepository eventoRepo,
                               ObjectMapper objectMapper,
                               PersistirNotificacaoUseCase persistirNotificacao,
                               SimpMessagingTemplate messagingTemplate) {
        this.eventoRepo = eventoRepo;
        this.objectMapper = objectMapper;
        this.persistirNotificacao = persistirNotificacao;
        this.messagingTemplate = messagingTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.Q_NOTIFICACAO_USUARIO)
    public void onNotificacaoUsuario(
            Envelope envelope,
            @Header(AmqpHeaders.MESSAGE_ID) String messageId,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {

        try {
            EventoProcessadoEntity registro = new EventoProcessadoEntity(
                    RabbitMQConfig.EXCHANGE_EVENTS,
                    RabbitMQConfig.RK_NOTIFICACAO_USUARIO,
                    messageId,
                    Instant.now()
            );

            eventoRepo.saveAndFlush(registro);

            NotificacaoEvento evento = objectMapper.convertValue(envelope.data(), NotificacaoEvento.class);

            Notificacao salva = persistirNotificacao.executar(
                    new PersistirNotificacaoUseCase.Input(
                            evento.usuarioId(), evento.pedidoId(), evento.mensagem()));

            messagingTemplate.convertAndSendToUser(
                    String.valueOf(salva.usuarioId()), DESTINO_STOMP, salva);

            log.info("notificacao.usuario processado — messageId={} usuarioId={}", messageId, salva.usuarioId());

            channel.basicAck(deliveryTag, false);

        } catch (DataIntegrityViolationException e) {
            log.warn("notificacao.usuario duplicado, ignorando — messageId={}", messageId);
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("falha ao processar notificacao.usuario — messageId={}: {}", messageId, e.getMessage(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
