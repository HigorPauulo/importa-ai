package br.com.importaai.infrastructure.adapter.in.rest.dto;

import br.com.importaai.domain.model.StatusPedido;

import java.time.Instant;
import java.util.List;

public record PedidoResponse(
        Long id,
        Long usuarioId,
        String codigoRastreamento,
        String descricao,
        StatusPedido status,
        boolean cancelado,
        Instant criadoEm,
        List<EtapaResponse> etapas
) {}
