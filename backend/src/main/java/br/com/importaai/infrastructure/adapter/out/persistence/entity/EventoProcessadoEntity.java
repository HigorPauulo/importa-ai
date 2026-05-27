package br.com.importaai.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "evento_processado")
@Getter
@Setter
@NoArgsConstructor
public class EventoProcessadoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String exchange;

    @Column(name = "routing_key", nullable = false, length = 100)
    private String routingKey;

    @Column(name = "message_id", nullable = false, length = 36)
    private String messageId;

    @Column(name = "processado_em", nullable = false, updatable = false)
    private Instant processadoEm;

    public EventoProcessadoEntity(String exchange, String routingKey, String messageId, Instant processadoEm) {
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.messageId = messageId;
        this.processadoEm = processadoEm;
    }
}
