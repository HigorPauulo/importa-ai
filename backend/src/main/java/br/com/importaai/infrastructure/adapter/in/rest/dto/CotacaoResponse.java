package br.com.importaai.infrastructure.adapter.in.rest.dto;

import br.com.importaai.domain.model.Moeda;
import br.com.importaai.domain.port.in.ConsultarCotacaoUseCase;

import java.math.BigDecimal;
import java.time.Instant;

public record CotacaoResponse(
        Moeda moedaOrigem,
        Moeda moedaDestino,
        BigDecimal taxa,
        boolean manual,
        boolean desatualizada,
        Instant atualizadoEm,
        Instant cotadoEm
) {
    public static CotacaoResponse from(ConsultarCotacaoUseCase.Output out) {
        return new CotacaoResponse(
                out.cotacao().moedaOrigem(),
                out.cotacao().moedaDestino(),
                out.cotacao().taxa(),
                out.cotacao().isManual(),
                out.desatualizada(),
                out.cotacao().atualizadoEm(),
                out.cotacao().cotadoEm());
    }
}
