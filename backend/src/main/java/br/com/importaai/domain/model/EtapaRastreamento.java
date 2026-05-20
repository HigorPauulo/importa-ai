package br.com.importaai.domain.model;

import java.time.Instant;
import java.util.Objects;

public record EtapaRastreamento (
        TipoEtapa tipo,
        Instant criadoEm,
        String localizacao,
        String descricao
) {
    public EtapaRastreamento {
        Objects.requireNonNull(tipo, "tipo não pode ser nulo");
        Objects.requireNonNull(criadoEm, "criadoEm não pode ser nulo");
    }
}
