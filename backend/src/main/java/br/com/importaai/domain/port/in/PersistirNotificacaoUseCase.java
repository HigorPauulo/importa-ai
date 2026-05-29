package br.com.importaai.domain.port.in;

import br.com.importaai.domain.model.Notificacao;

public interface PersistirNotificacaoUseCase {

    record Input(Long usuarioId, Long pedidoId, String mensagem) {}

    Notificacao executar(Input input);
}
