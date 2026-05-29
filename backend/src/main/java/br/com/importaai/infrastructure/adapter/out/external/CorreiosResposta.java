package br.com.importaai.infrastructure.adapter.out.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CorreiosResposta(List<Evento> eventos) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Evento(String tipo, Instant data, String local, String descricao) {}
}
