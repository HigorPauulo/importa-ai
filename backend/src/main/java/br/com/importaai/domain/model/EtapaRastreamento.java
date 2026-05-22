package br.com.importaai.domain.model;

import java.time.Instant;
import java.util.Objects;

public record EtapaRastreamento(
        Long id,
        TipoEtapa tipo,
        Instant criadoEm,
        String localizacao,
        String descricao
) {
    public EtapaRastreamento {
        Objects.requireNonNull(tipo, "tipo não pode ser nulo");
        Objects.requireNonNull(criadoEm, "criadoEm não pode ser nulo");
    }

    public EtapaRastreamento(TipoEtapa tipo, Instant criadoEm, String localizacao, String descricao) {
        this(null, tipo, criadoEm, localizacao, descricao);
    }
}