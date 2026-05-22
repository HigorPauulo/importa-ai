package br.com.importaai.infrastructure.adapter.in.rest.mapper;

import br.com.importaai.domain.model.EtapaRastreamento;
import br.com.importaai.domain.model.Pedido;
import br.com.importaai.infrastructure.adapter.in.rest.dto.EtapaResponse;
import br.com.importaai.infrastructure.adapter.in.rest.dto.PedidoResponse;

import java.util.List;

public final class PedidoResponseMapper {

    private PedidoResponseMapper() {}

    public static PedidoResponse toResponse(Pedido pedido) {
        List<EtapaResponse> etapas = pedido.getEtapas().stream()
                .map(PedidoResponseMapper::toEtapaResponse)
                .toList();

        return new PedidoResponse(
                pedido.getId(),
                pedido.getUsuarioId(),
                pedido.getCodigoRastreamento(),
                pedido.getDescricao(),
                pedido.getStatus(),
                pedido.isCancelado(),
                pedido.getCriadoEm(),
                etapas
        );
    }

    private static EtapaResponse toEtapaResponse(EtapaRastreamento e) {
        return new EtapaResponse(
                e.tipo(),
                e.criadoEm(),
                e.localizacao(),
                e.descricao()
        );
    }
}
