package br.com.importaai.infrastructure.adapter.out.persistence.mapper;

import br.com.importaai.domain.model.Notificacao;
import br.com.importaai.infrastructure.adapter.out.persistence.entity.NotificacaoEntity;

public final class NotificacaoEntityMapper {

    private NotificacaoEntityMapper() {}

    public static NotificacaoEntity toEntity(Notificacao domain) {
        NotificacaoEntity e = new NotificacaoEntity(
                domain.usuarioId(),
                domain.pedidoId(),
                domain.mensagem(),
                domain.lida(),
                domain.criadoEm()
        );
        e.setId(domain.id());
        return e;
    }

    public static Notificacao toDomain(NotificacaoEntity e) {
        return new Notificacao(
                e.getId(),
                e.getUsuarioId(),
                e.getPedidoId(),
                e.getMensagem(),
                e.isLida(),
                e.getCriadoEm()
        );
    }
}
