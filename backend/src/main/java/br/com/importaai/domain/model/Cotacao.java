package br.com.importaai.domain.model;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public record Cotacao(
        Long id,
        Moeda moedaOrigem,
        Moeda moedaDestino,
        BigDecimal taxa,
        FonteCotacao fonte,
        Long manualPorUsuarioId,
        Instant validoAte,
        Instant atualizadoEm
) {
    private static final Duration VALIDADE_MAXIMA = Duration.ofHours(24);

    public Cotacao {
        Objects.requireNonNull(moedaOrigem, "moedaOrigem nao pode ser nula");
        Objects.requireNonNull(moedaDestino, "moedaDestino nao pode ser nula");
        Objects.requireNonNull(taxa, "taxa nao pode ser nula");
        Objects.requireNonNull(fonte, "fonte nao pode ser nula");
        Objects.requireNonNull(atualizadoEm, "atualizadoEm nao pode ser nulo");
        if (taxa.signum() <= 0) {
            throw new IllegalArgumentException("taxa deve ser positiva");
        }
    }

    public static Cotacao automatica(Moeda origem, Moeda destino, BigDecimal taxa, Instant agora) {
        return new Cotacao(null, origem, destino, taxa, FonteCotacao.AUTOMATICA, null, null, agora);
    }

    public static Cotacao manual(Moeda origem, Moeda destino, BigDecimal taxa,
                                 Long usuarioId, Instant validoAte, Instant agora) {
        return new Cotacao(null, origem, destino, taxa, FonteCotacao.MANUAL, usuarioId, validoAte, agora);
    }

    public boolean estaDesatualizada(Instant agora) {
        return Duration.between(atualizadoEm, agora).compareTo(VALIDADE_MAXIMA) > 0;
    }

    public boolean isManual() {
        return fonte == FonteCotacao.MANUAL;
    }
}
