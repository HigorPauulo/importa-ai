package br.com.importaai.infrastructure.adapter.in.messaging;

import br.com.importaai.domain.model.PerfilUsuario;
import br.com.importaai.infrastructure.adapter.out.messaging.Envelope;
import br.com.importaai.infrastructure.adapter.out.persistence.entity.NotificacaoEntity;
import br.com.importaai.infrastructure.adapter.out.persistence.entity.UsuarioEntity;
import br.com.importaai.infrastructure.adapter.out.persistence.repository.NotificacaoJpaRepository;
import br.com.importaai.infrastructure.adapter.out.persistence.repository.UsuarioJpaRepository;
import br.com.importaai.infrastructure.config.RabbitMQConfig;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
class RastreamentoNotificacaoTest extends MessagingIntegrationTest {

    @Autowired UsuarioJpaRepository usuarioJpaRepository;
    @Autowired NotificacaoJpaRepository notificacaoJpaRepository;

    @Test
    @DisplayName("RF16: rastreamento.atualizado vira notificacao.usuario persistida")
    void rf16_mudancaDeStatusGeraNotificacao() {
        notificacaoJpaRepository.deleteAll();
        usuarioJpaRepository.deleteAll();

        UsuarioEntity u = new UsuarioEntity();
        u.setNome("Dono");
        u.setEmail("dono@x.com");
        u.setSenhaHash("hash");
        u.setPerfil(PerfilUsuario.CLIENTE);
        u.setCriadoEm(Instant.now());
        Long usuarioId = usuarioJpaRepository.save(u).getId();

        Map<String, Object> pedido = new HashMap<>();
        pedido.put("usuarioId", usuarioId);
        pedido.put("codigoRastreamento", "NN999000111BR");
        pedido.put("status", "ENTREGUE");

        String messageId = UUID.randomUUID().toString();
        Envelope env = new Envelope(
                messageId,
                RabbitMQConfig.RK_RASTREAMENTO_ATUALIZADO,
                "1.0",
                Instant.now(),
                pedido);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_EVENTS,
                RabbitMQConfig.RK_RASTREAMENTO_ATUALIZADO,
                env,
                m -> {
                    m.getMessageProperties().setMessageId(messageId);
                    return m;
                });

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    List<NotificacaoEntity> notificacoes =
                            notificacaoJpaRepository.findByUsuarioIdOrderByCriadoEmDesc(usuarioId);
                    assertThat(notificacoes).hasSize(1);
                    assertThat(notificacoes.get(0).getMensagem()).contains("entregue");
                });
    }
}
