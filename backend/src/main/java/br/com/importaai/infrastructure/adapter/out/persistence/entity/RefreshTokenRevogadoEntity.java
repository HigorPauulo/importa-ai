package br.com.importaai.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "refresh_token_revogado")
@Getter
@Setter
@NoArgsConstructor
public class RefreshTokenRevogadoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "revogado_em", nullable = false, updatable = false)
    private Instant revogadoEm;

    @Column(name = "expira_em", nullable = false)
    private Instant expiraEm;

    public RefreshTokenRevogadoEntity(String tokenHash, Long usuarioId, Instant revogadoEm, Instant expiraEm) {
        this.tokenHash = tokenHash;
        this.usuarioId = usuarioId;
        this.revogadoEm = revogadoEm;
        this.expiraEm = expiraEm;
    }
}
