package br.com.importaai.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "tentativa_login_falha")
@Getter
@Setter
@NoArgsConstructor
public class TentativaLoginFalhaEntity {

    @Id
    @Column(length = 255)
    private String email;

    @Column(nullable = false)
    private Integer contador;

    @Column(name = "bloqueado_ate")
    private Instant bloqueadoAte;

    @Column(name = "atualizado_em", insertable = false, updatable = false)
    private Instant atualizadoEm;

    public TentativaLoginFalhaEntity(String email, Integer contador, Instant bloqueadoAte) {
        this.email = email;
        this.contador = contador;
        this.bloqueadoAte = bloqueadoAte;
    }
}
