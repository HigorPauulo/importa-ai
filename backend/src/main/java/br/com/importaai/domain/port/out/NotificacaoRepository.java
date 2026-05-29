package br.com.importaai.domain.port.out;

import br.com.importaai.domain.model.Notificacao;

import java.util.List;

public interface NotificacaoRepository {

    Notificacao salvarComLimite(Notificacao notificacao, int limite);

    List<Notificacao> listarPorUsuario(Long usuarioId);

    int marcarTodasComoLidas(Long usuarioId);
}