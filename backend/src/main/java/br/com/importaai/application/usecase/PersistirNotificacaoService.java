package br.com.importaai.application.usecase;

import br.com.importaai.domain.model.Notificacao;
import br.com.importaai.domain.port.in.PersistirNotificacaoUseCase;
import br.com.importaai.domain.port.out.NotificacaoRepository;

import java.time.Instant;

public class PersistirNotificacaoService implements PersistirNotificacaoUseCase {

    private static final int LIMITE_NOTIFICACOES = 50;

    private final NotificacaoRepository notificacaoRepository;

    public PersistirNotificacaoService(NotificacaoRepository notificacaoRepository) {
        this.notificacaoRepository = notificacaoRepository;
    }

    @Override
    public Notificacao executar(Input input) {
        Notificacao notificacao = new Notificacao(
                input.usuarioId(),
                input.pedidoId(),
                input.mensagem(),
                Instant.now()
        );
        return notificacaoRepository.salvarComLimite(notificacao, LIMITE_NOTIFICACOES);
    }
}
