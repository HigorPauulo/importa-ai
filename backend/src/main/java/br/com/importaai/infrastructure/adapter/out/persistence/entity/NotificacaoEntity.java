package br.com.importaai.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "notificacao")
@Getter
@Setter
@NoArgsConstructor
public class NotificacaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "pedido_id")
    private Long pedidoId;

    @Column(name = "mensagem", nullable = false, length = 500)
    private String mensagem;

    @Column(name = "lida", nullable = false)
    private boolean lida;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    public NotificacaoEntity(Long usuarioId, Long pedidoId, String mensagem, boolean lida, Instant criadoEm) {
        this.usuarioId = usuarioId;
        this.pedidoId = pedidoId;
        this.mensagem = mensagem;
        this.lida = lida;
        this.criadoEm = criadoEm;
    }
}

