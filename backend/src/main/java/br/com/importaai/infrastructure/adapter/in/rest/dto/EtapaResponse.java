package br.com.importaai.infrastructure.adapter.in.rest.dto;

import br.com.importaai.domain.model.TipoEtapa;

import java.time.Instant;

public record EtapaResponse(
        TipoEtapa tipo,
        Instant criadoEm,
        String localizacao,
        String descricao
) {}
