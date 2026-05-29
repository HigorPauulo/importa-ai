package br.com.importaai.infrastructure.adapter.in.rest.dto;

import br.com.importaai.domain.model.Notificacao;

import java.time.Instant;

public record NotificacaoResponse(
        Long id,
        Long pedidoId,
        String mensagem,
        boolean lida,
        Instant criadoEm
) {
    public static NotificacaoResponse from(Notificacao n) {
        return new NotificacaoResponse(n.id(), n.pedidoId(), n.mensagem(), n.lida(), n.criadoEm());
    }
}
