package br.com.importaai.infrastructure.adapter.in.rest.dto;

import java.time.Instant;
import java.util.List;

public record ErroResponse(
        int status,
        String codigo,
        String mensagem,
        List<String> detalhes,
        Instant timestamp
) {
    public ErroResponse(int status, String codigo, String mensagem) {
        this(status, codigo, mensagem, List.of(), Instant.now());
    }
}
