package br.com.importaai.infrastructure.adapter.in.messaging;

import br.com.importaai.infrastructure.adapter.out.messaging.Envelope;
import br.com.importaai.infrastructure.adapter.out.persistence.entity.EventoProcessadoEntity;
import br.com.importaai.infrastructure.adapter.out.persistence.repository.EventoProcessadoJpaRepository;
import br.com.importaai.infrastructure.config.RabbitMQConfig;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
class DlqRoutingTest extends MessagingIntegrationTest {

    @MockitoBean
    private EventoProcessadoJpaRepository eventoRepoMock;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Test
    @DisplayName("TC08: exceção genérica no consumer roteia mensagem para a DLQ")
    void tc08_excecaoVaiParaDlq() {
        // Configura o mock pra jogar exceção genérica
        when(eventoRepoMock.saveAndFlush(any(EventoProcessadoEntity.class)))
                .thenThrow(new RuntimeException("falha simulada para testar DLQ"));

        String messageId = UUID.randomUUID().toString();
        Envelope env = new Envelope(
                messageId,
                "pedido.criado",
                "1.0",
                Instant.now(),
                Map.of("id", 99, "codigoRastreamento", "BR-TC08")
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_EVENTS,
                RabbitMQConfig.RK_PEDIDO_CRIADO,
                env,
                msg -> {
                    msg.getMessageProperties().setMessageId(messageId);
                    msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    return msg;
                }
        );

        // Aguarda a mensagem aparecer na DLQ
        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    Properties props = rabbitAdmin.getQueueProperties(RabbitMQConfig.Q_PEDIDO_CRIADO_DLQ);
                    assertThat(props).isNotNull();
                    Number count = (Number) props.get(RabbitAdmin.QUEUE_MESSAGE_COUNT);
                    assertThat(count).isNotNull();
                    assertThat(count.intValue()).isEqualTo(1);
                });
    }
}
