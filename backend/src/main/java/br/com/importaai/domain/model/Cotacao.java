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
        Instant atualizadoEm,
        Instant cotadoEm
) {
    private static final Duration VALIDADE_MAXIMA = Duration.ofHours(24);

    public Cotacao {
        Objects.requireNonNull(moedaOrigem, "moedaOrigem nao pode ser nula");
        Objects.requireNonNull(moedaDestino, "moedaDestino nao pode ser nula");
        Objects.requireNonNull(taxa, "taxa nao pode ser nula");
        Objects.requireNonNull(fonte, "fonte nao pode ser nula");
        Objects.requireNonNull(atualizadoEm, "atualizadoEm nao pode ser nulo");
        Objects.requireNonNull(cotadoEm, "cotadoEm nao pode ser nulo");
        if (taxa.signum() <= 0) {
            throw new IllegalArgumentException("taxa deve ser positiva");
        }
    }

    // atualizadoEm = quando o backend sincronizou (controla o TTL/RN07);
    // cotadoEm = quando a fonte externa cotou de fato (o que o usuario ve como "ha X min").
    public static Cotacao automatica(Moeda origem, Moeda destino, BigDecimal taxa,
                                     Instant cotadoEm, Instant atualizadoEm) {
        return new Cotacao(null, origem, destino, taxa, FonteCotacao.AUTOMATICA, null, null, atualizadoEm, cotadoEm);
    }

    public static Cotacao manual(Moeda origem, Moeda destino, BigDecimal taxa,
                                 Long usuarioId, Instant validoAte, Instant agora) {
        // cotacao manual: o "momento da cotacao" e o instante em que o admin a definiu
        return new Cotacao(null, origem, destino, taxa, FonteCotacao.MANUAL, usuarioId, validoAte, agora, agora);
    }

    public boolean estaDesatualizada(Instant agora) {
        return Duration.between(atualizadoEm, agora).compareTo(VALIDADE_MAXIMA) > 0;
    }

    public boolean isManual() {
        return fonte == FonteCotacao.MANUAL;
    }
}
