package br.com.importaai.infrastructure.adapter.in.messaging;

import br.com.importaai.domain.model.Notificacao;
import br.com.importaai.domain.model.PerfilUsuario;
import br.com.importaai.domain.port.out.NotificacaoRepository;
import br.com.importaai.infrastructure.adapter.out.persistence.entity.UsuarioEntity;
import br.com.importaai.infrastructure.adapter.out.persistence.repository.NotificacaoJpaRepository;
import br.com.importaai.infrastructure.adapter.out.persistence.repository.UsuarioJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
class NotificacaoRn09Test extends MessagingIntegrationTest {

    private static final int LIMITE = 50;

    @Autowired NotificacaoRepository notificacaoRepository;
    @Autowired NotificacaoJpaRepository notificacaoJpaRepository;
    @Autowired UsuarioJpaRepository usuarioJpaRepository;

    private Long usuarioId;

    @BeforeEach
    void preparar() {
        notificacaoJpaRepository.deleteAll();
        usuarioJpaRepository.deleteAll();

        UsuarioEntity u = new UsuarioEntity();
        u.setNome("RN09");
        u.setEmail("rn09@x.com");
        u.setSenhaHash("hash");
        u.setPerfil(PerfilUsuario.CLIENTE);
        u.setCriadoEm(Instant.now());
        usuarioId = usuarioJpaRepository.save(u).getId();
    }

    @Test
    @DisplayName("TC10: ao inserir a 51a notificacao, FIFO mantem apenas as 50 mais recentes")
    void tc10_limiteFifoDe50() {
        Instant base = Instant.now();
        for (int i = 0; i < 55; i++) {
            Notificacao n = new Notificacao(
                    null, usuarioId, null, "msg-" + i, false, base.plusMillis(i));
            notificacaoRepository.salvarComLimite(n, LIMITE);
        }

        List<Notificacao> restantes = notificacaoRepository.listarPorUsuario(usuarioId);
        assertThat(restantes).hasSize(LIMITE);

        List<String> mensagens = restantes.stream().map(Notificacao::mensagem).toList();
        assertThat(mensagens).contains("msg-54", "msg-5");
        assertThat(mensagens).doesNotContain("msg-0", "msg-4");
    }

    @Test
    @DisplayName("TC23: 10 threads x 10 insercoes concorrentes nao ultrapassam o limite de 50")
    void tc23_concorrenciaRespeitaLimite() throws InterruptedException {
        int threads = 10;
        int insercoesPorThread = 10;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch largada = new CountDownLatch(1);
        CountDownLatch fim = new CountDownLatch(threads);
        Instant base = Instant.now();

        for (int t = 0; t < threads; t++) {
            final int threadId = t;
            pool.submit(() -> {
                try {
                    largada.await();
                    for (int i = 0; i < insercoesPorThread; i++) {
                        Notificacao n = new Notificacao(
                                null, usuarioId, null,
                                "t" + threadId + "-" + i,
                                false,
                                base.plusMillis(threadId * 100L + i));
                        notificacaoRepository.salvarComLimite(n, LIMITE);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    fim.countDown();
                }
            });
        }

        largada.countDown();
        assertThat(fim.await(60, TimeUnit.SECONDS)).isTrue();
        pool.shutdown();

        assertThat(notificacaoRepository.listarPorUsuario(usuarioId)).hasSize(LIMITE);
    }
}
