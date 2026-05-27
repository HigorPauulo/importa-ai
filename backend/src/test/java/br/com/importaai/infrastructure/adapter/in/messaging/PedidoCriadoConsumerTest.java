package br.com.importaai.infrastructure.adapter.in.messaging;

import br.com.importaai.infrastructure.adapter.out.messaging.Envelope;
import br.com.importaai.infrastructure.config.RabbitMQConfig;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.MessageDeliveryMode;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PedidoCriadoConsumerTest extends MessagingIntegrationTest {

    @Test
    @DisplayName("TC06: consumer processa pedido.criado e registra em evento_processado")
    void tc06_consumerProcessaEventoEhPersistido() {
        String messageId = UUID.randomUUID().toString();
        Envelope env = new Envelope(
                messageId,
                "pedido.criado",
                "1.0",
                Instant.now(),
                Map.of("id", 1, "codigoRastreamento", "BR-TC06")
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

        // Aguarda até 5s o consumer processar
        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> assertThat(eventoRepo.count()).isEqualTo(1L));

        // Confere o conteúdo da linha
        var registros = eventoRepo.findAll();
        assertThat(registros).hasSize(1);
        assertThat(registros.get(0).getMessageId()).isEqualTo(messageId);
        assertThat(registros.get(0).getRoutingKey()).isEqualTo("pedido.criado");
        assertThat(registros.get(0).getExchange()).isEqualTo("importaai.events");
    }

    @Test
    @DisplayName("TC07: reprocessamento com mesmo message_id resulta em apenas 1 linha (RN04)")
    void tc07_reprocessamentoIgnoraDuplicata() {
        String messageId = UUID.randomUUID().toString();
        Envelope env = new Envelope(
                messageId,
                "pedido.criado",
                "1.0",
                Instant.now(),
                Map.of("id", 2, "codigoRastreamento", "BR-TC07")
        );

        // Publica a primeira vez
        publicarComMessageId(env, messageId);

        // Aguarda a primeira ser processada
        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> assertThat(eventoRepo.count()).isEqualTo(1L));

        // Publica a SEGUNDA vez com o mesmo message_id
        publicarComMessageId(env, messageId);

        // Espera 2s pra dar tempo do consumer ver, tentar INSERT, UNIQUE estourar e ACK silencioso
        Awaitility.await()
                .pollDelay(Duration.ofSeconds(2))
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> assertThat(eventoRepo.count()).isEqualTo(1L)); // continua 1!
    }

    private void publicarComMessageId(Envelope env, String messageId) {
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
    }

}
