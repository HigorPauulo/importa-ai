package br.com.importaai.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class CotacaoTest {

    private final Instant agora = Instant.now();

    @Test
    @DisplayName("cotacao com menos de 24h nao esta desatualizada")
    void recenteNaoDesatualizada() {
        Instant sync = agora.minus(Duration.ofHours(23));
        Cotacao c = Cotacao.automatica(Moeda.USD, Moeda.BRL, new BigDecimal("5.10"), sync, sync);
        assertThat(c.estaDesatualizada(agora)).isFalse();
    }

    @Test
    @DisplayName("cotacao com mais de 24h esta desatualizada (RN07)")
    void antigaDesatualizada() {
        Instant sync = agora.minus(Duration.ofHours(25));
        Cotacao c = Cotacao.automatica(Moeda.USD, Moeda.BRL, new BigDecimal("5.10"), sync, sync);
        assertThat(c.estaDesatualizada(agora)).isTrue();
    }

    @Test
    @DisplayName("taxa zero ou negativa e rejeitada")
    void taxaInvalida() {
        assertThatIllegalArgumentException().isThrownBy(() ->
                Cotacao.automatica(Moeda.USD, Moeda.BRL, BigDecimal.ZERO, agora, agora));
    }

    @Test
    @DisplayName("cotacao manual carrega fonte MANUAL e o usuario")
    void manualMarcaFonte() {
        Cotacao c = Cotacao.manual(Moeda.CNY, Moeda.BRL, new BigDecimal("0.72"), 7L, null, agora);
        assertThat(c.isManual()).isTrue();
        assertThat(c.manualPorUsuarioId()).isEqualTo(7L);
    }
}
