package br.com.importaai.infrastructure.adapter.out.persistence.entity;

import br.com.importaai.domain.model.TipoEtapa;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "etapa_rastreamento")
@Getter
@Setter
@NoArgsConstructor
public class EtapaRastreamentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private PedidoEntity pedido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoEtapa tipo;

    @Column(length = 255)
    private String localizacao;

    @Column(length = 500)
    private String descricao;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;
}