package br.com.importaai.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQConfig.class);

    public static final String EXCHANGE_EVENTS              = "importaai.events";
    public static final String EXCHANGE_EVENTS_DLQ          = "importaai.events.dlq";

    public static final String RK_PEDIDO_CRIADO             = "pedido.criado";
    public static final String RK_PEDIDO_ATUALIZADO         = "pedido.atualizado";
    public static final String RK_RASTREAMENTO_ATUALIZADO   = "rastreamento.atualizado";
    public static final String RK_NOTIFICACAO_USUARIO       = "notificacao.usuario";

    public static final String Q_PEDIDO_CRIADO              = "q.pedido.criado";
    public static final String Q_PEDIDO_ATUALIZADO          = "q.pedido.atualizado";
    public static final String Q_RASTREAMENTO_ATUALIZADO    = "q.rastreamento.atualizado";
    public static final String Q_NOTIFICACAO_USUARIO        = "q.notificacao.usuario";

    public static final String Q_PEDIDO_CRIADO_DLQ              = "q.pedido.criado.dlq";
    public static final String Q_PEDIDO_ATUALIZADO_DLQ          = "q.pedido.atualizado.dlq";
    public static final String Q_RASTREAMENTO_ATUALIZADO_DLQ    = "q.rastreamento.atualizado.dlq";
    public static final String Q_NOTIFICACAO_USUARIO_DLQ        = "q.notificacao.usuario.dlq";

    // ===== Exchanges =====
    @Bean
    DirectExchange eventsExchange() {
        return ExchangeBuilder.directExchange(EXCHANGE_EVENTS).durable(true).build();
    }

    @Bean
    DirectExchange eventsDlqExchange() {
        return ExchangeBuilder.directExchange(EXCHANGE_EVENTS_DLQ).durable(true).build();
    }

    // ===== Filas principais =====
    @Bean
    Queue qPedidoCriado() {
        return QueueBuilder.durable(Q_PEDIDO_CRIADO)
                .withArgument("x-dead-letter-exchange", EXCHANGE_EVENTS_DLQ)
                .withArgument("x-dead-letter-routing-key", RK_PEDIDO_CRIADO + ".dlq")
                .build();
    }

    @Bean
    Queue qPedidoAtualizado() {
        return QueueBuilder.durable(Q_PEDIDO_ATUALIZADO)
                .withArgument("x-dead-letter-exchange", EXCHANGE_EVENTS_DLQ)
                .withArgument("x-dead-letter-routing-key", RK_PEDIDO_ATUALIZADO + ".dlq")
                .build();
    }

    @Bean
    Queue qRastreamentoAtualizado() {
        return QueueBuilder.durable(Q_RASTREAMENTO_ATUALIZADO)
                .withArgument("x-dead-letter-exchange", EXCHANGE_EVENTS_DLQ)
                .withArgument("x-dead-letter-routing-key", RK_RASTREAMENTO_ATUALIZADO + ".dlq")
                .build();
    }

    @Bean
    Queue qNotificacaoUsuario() {
        return QueueBuilder.durable(Q_NOTIFICACAO_USUARIO)
                .withArgument("x-dead-letter-exchange", EXCHANGE_EVENTS_DLQ)
                .withArgument("x-dead-letter-routing-key", RK_NOTIFICACAO_USUARIO + ".dlq")
                .build();
    }

    // ===== DLQs =====
    @Bean Queue qPedidoCriadoDlq()           { return QueueBuilder.durable(Q_PEDIDO_CRIADO_DLQ).build(); }
    @Bean Queue qPedidoAtualizadoDlq()       { return QueueBuilder.durable(Q_PEDIDO_ATUALIZADO_DLQ).build(); }
    @Bean Queue qRastreamentoAtualizadoDlq() { return QueueBuilder.durable(Q_RASTREAMENTO_ATUALIZADO_DLQ).build(); }
    @Bean Queue qNotificacaoUsuarioDlq()     { return QueueBuilder.durable(Q_NOTIFICACAO_USUARIO_DLQ).build(); }

    // ===== Bindings (exchange - fila via routing key) =====
    @Bean Binding bPedidoCriado(Queue qPedidoCriado, DirectExchange eventsExchange) {
        return BindingBuilder.bind(qPedidoCriado).to(eventsExchange).with(RK_PEDIDO_CRIADO);
    }
    @Bean Binding bPedidoAtualizado(Queue qPedidoAtualizado, DirectExchange eventsExchange) {
        return BindingBuilder.bind(qPedidoAtualizado).to(eventsExchange).with(RK_PEDIDO_ATUALIZADO);
    }
    @Bean Binding bRastreamentoAtualizado(Queue qRastreamentoAtualizado, DirectExchange eventsExchange) {
        return BindingBuilder.bind(qRastreamentoAtualizado).to(eventsExchange).with(RK_RASTREAMENTO_ATUALIZADO);
    }
    @Bean Binding bNotificacaoUsuario(Queue qNotificacaoUsuario, DirectExchange eventsExchange) {
        return BindingBuilder.bind(qNotificacaoUsuario).to(eventsExchange).with(RK_NOTIFICACAO_USUARIO);
    }

    @Bean Binding bPedidoCriadoDlq(Queue qPedidoCriadoDlq, DirectExchange eventsDlqExchange) {
        return BindingBuilder.bind(qPedidoCriadoDlq).to(eventsDlqExchange).with(RK_PEDIDO_CRIADO + ".dlq");
    }
    @Bean Binding bPedidoAtualizadoDlq(Queue qPedidoAtualizadoDlq, DirectExchange eventsDlqExchange) {
        return BindingBuilder.bind(qPedidoAtualizadoDlq).to(eventsDlqExchange).with(RK_PEDIDO_ATUALIZADO + ".dlq");
    }
    @Bean Binding bRastreamentoAtualizadoDlq(Queue qRastreamentoAtualizadoDlq, DirectExchange eventsDlqExchange) {
        return BindingBuilder.bind(qRastreamentoAtualizadoDlq).to(eventsDlqExchange).with(RK_RASTREAMENTO_ATUALIZADO + ".dlq");
    }
    @Bean Binding bNotificacaoUsuarioDlq(Queue qNotificacaoUsuarioDlq, DirectExchange eventsDlqExchange) {
        return BindingBuilder.bind(qNotificacaoUsuarioDlq).to(eventsDlqExchange).with(RK_NOTIFICACAO_USUARIO + ".dlq");
    }

    // ===== Serializador JSON + RabbitTemplate customizado =====
    @Bean
    Jackson2JsonMessageConverter jacksonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory cf, Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(cf);
        template.setMessageConverter(converter);

        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.error("publisher confirm NACK — correlationData={} cause={}", correlationData, cause);
            }
        });

        template.setReturnsCallback(returned ->
                log.error("mensagem retornada (rota sem fila bound): exchange={} rk={} reply={}",
                        returned.getExchange(), returned.getRoutingKey(), returned.getReplyText())
        );

        return template;
    }
}
