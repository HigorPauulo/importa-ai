package br.com.importaai.infrastructure.adapter.in.rest.dto;

import br.com.importaai.domain.model.Moeda;
import br.com.importaai.domain.model.StatusPedido;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PedidoResponse(
        Long id,
        Long usuarioId,
        String codigoRastreamento,
        String descricao,
        BigDecimal valorDeclarado,
        Moeda moeda,
        StatusPedido status,
        boolean cancelado,
        boolean rastreioNaoLocalizado,
        Instant criadoEm,
        List<EtapaResponse> etapas
) {}
