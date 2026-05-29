package br.com.importaai.infrastructure.adapter.in.messaging;

import br.com.importaai.domain.model.PerfilUsuario;
import br.com.importaai.domain.port.out.TokenIssuer;
import br.com.importaai.infrastructure.adapter.out.messaging.Envelope;
import br.com.importaai.infrastructure.adapter.out.messaging.NotificacaoEvento;
import br.com.importaai.infrastructure.adapter.out.persistence.entity.UsuarioEntity;
import br.com.importaai.infrastructure.adapter.out.persistence.repository.NotificacaoJpaRepository;
import br.com.importaai.infrastructure.adapter.out.persistence.repository.UsuarioJpaRepository;
import br.com.importaai.infrastructure.config.RabbitMQConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
class NotificacaoWebSocketTest extends MessagingIntegrationTest {

    @LocalServerPort int port;

    @Autowired TokenIssuer tokenIssuer;
    @Autowired UsuarioJpaRepository usuarioJpaRepository;
    @Autowired NotificacaoJpaRepository notificacaoJpaRepository;

    private Long usuarioId;

    @BeforeEach
    void preparar() {
        notificacaoJpaRepository.deleteAll();
        usuarioJpaRepository.deleteAll();

        UsuarioEntity u = new UsuarioEntity();
        u.setNome("WS");
        u.setEmail("ws@x.com");
        u.setSenhaHash("hash");
        u.setPerfil(PerfilUsuario.CLIENTE);
        u.setCriadoEm(Instant.now());
        usuarioId = usuarioJpaRepository.save(u).getId();
    }

    @Test
    @DisplayName("TC09: cliente STOMP recebe notificacao em /user/queue/notificacoes em ate 2s")
    void tc09_clienteRecebeNotificacaoEmAte2s() throws Exception {
        String jwt = tokenIssuer.emitirAccessToken(usuarioId, "ws@x.com", PerfilUsuario.CLIENTE);

        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + jwt);

        StompSession session = stompClient
                .connectAsync("ws://localhost:" + port + "/ws/websocket",
                        new WebSocketHttpHeaders(),
                        connectHeaders,
                        new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);

        BlockingQueue<Map<String, Object>> recebidas = new LinkedBlockingDeque<>();
        session.subscribe("/user/queue/notificacoes", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Map.class;
            }

            @Override
            @SuppressWarnings("unchecked")
            public void handleFrame(StompHeaders headers, Object payload) {
                recebidas.add((Map<String, Object>) payload);
            }
        });

        // Garante que o SUBSCRIBE foi registrado no broker antes de publicar
        Thread.sleep(500);

        String messageId = UUID.randomUUID().toString();
        Envelope env = new Envelope(
                messageId,
                RabbitMQConfig.RK_NOTIFICACAO_USUARIO,
                "1.0",
                Instant.now(),
                new NotificacaoEvento(usuarioId, null, "Você tem uma nova notificação"));

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_EVENTS,
                RabbitMQConfig.RK_NOTIFICACAO_USUARIO,
                env,
                m -> {
                    m.getMessageProperties().setMessageId(messageId);
                    return m;
                });

        // TC09: latencia ponta-a-ponta <= 2s
        Map<String, Object> notificacao = recebidas.poll(2, TimeUnit.SECONDS);

        assertThat(notificacao).isNotNull();
        assertThat(notificacao.get("mensagem")).isEqualTo("Você tem uma nova notificação");
        assertThat(notificacao.get("lida")).isEqualTo(false);
        assertThat(((Number) notificacao.get("usuarioId")).longValue()).isEqualTo(usuarioId);

        session.disconnect();
        stompClient.stop();
    }
}
