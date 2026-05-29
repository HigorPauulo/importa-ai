package br.com.importaai.infrastructure.adapter.in.rest.dto;

import br.com.importaai.domain.model.Moeda;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;

public record CotacaoManualRequest(
        @NotNull Moeda moedaOrigem,
        @NotNull Moeda moedaDestino,
        @NotNull @Positive BigDecimal taxa,
        Instant validoAte
) {}
