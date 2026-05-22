package br.com.importaai.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedido")
@Getter
@Setter
@NoArgsConstructor
public class PedidoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "codigo_rastreamento", nullable = false, length = 50)
    private String codigoRastreamento;

    @Column(length = 500)
    private String descricao;

    @Column(nullable = false)
    private boolean cancelado;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @OneToMany(
            mappedBy = "pedido",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<EtapaRastreamentoEntity> etapas = new ArrayList<>();

    public void adicionarEtapa(EtapaRastreamentoEntity etapa) {
        etapas.add(etapa);
        etapa.setPedido(this);
    }
}
