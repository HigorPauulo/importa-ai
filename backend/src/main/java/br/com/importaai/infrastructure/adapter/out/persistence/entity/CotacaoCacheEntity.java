package br.com.importaai.infrastructure.adapter.out.persistence.entity;

import br.com.importaai.domain.model.FonteCotacao;
import br.com.importaai.domain.model.Moeda;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "cotacao_cache")
@Getter
@Setter
@NoArgsConstructor
public class CotacaoCacheEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "moeda_origem", nullable = false, length = 3)
    private Moeda moedaOrigem;

    @Enumerated(EnumType.STRING)
    @Column(name = "moeda_destino", nullable = false, length = 3)
    private Moeda moedaDestino;

    @Column(nullable = false, precision = 12, scale = 6)
    private BigDecimal taxa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FonteCotacao fonte;

    @Column(name = "manual_por_usuario_id")
    private Long manualPorUsuarioId;

    @Column(name = "valido_ate")
    private Instant validoAte;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;
}
