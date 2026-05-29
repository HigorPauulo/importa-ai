package br.com.importaai.domain.model;

import java.time.Instant;
import java.util.Objects;

public record Notificacao(
        Long id,
        Long usuarioId,
        Long pedidoId,
        String mensagem,
        boolean lida,
        Instant criadoEm
) {
    public Notificacao {
        Objects.requireNonNull(usuarioId, "usuarioId não pode ser nulo");
        Objects.requireNonNull(mensagem, "mensagem não pode ser nula");
        Objects.requireNonNull(criadoEm, "criadoEm não pode ser nulo");
    }

    public Notificacao(Long usuarioId, Long pedidoId, String mensagem, Instant criadoEm) {
        this(null, usuarioId, pedidoId, mensagem, false, criadoEm);
    }
}
