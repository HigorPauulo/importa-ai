package br.com.importaai.infrastructure.adapter.out.persistence.mapper;

import br.com.importaai.domain.model.EtapaRastreamento;
import br.com.importaai.domain.model.Pedido;
import br.com.importaai.infrastructure.adapter.out.persistence.entity.PedidoEntity;

import java.util.List;

public final class PedidoEntityMapper {

    private PedidoEntityMapper() {}

    public static PedidoEntity toEntity(Pedido domain) {
        PedidoEntity e = new PedidoEntity();
        e.setId(domain.getId());
        e.setUsuarioId(domain.getUsuarioId());
        e.setCodigoRastreamento(domain.getCodigoRastreamento());
        e.setDescricao(domain.getDescricao());
        e.setValorDeclarado(domain.getValorDeclarado());
        e.setMoeda(domain.getMoeda());
        e.setCancelado(domain.isCancelado());
        e.setCriadoEm(domain.getCriadoEm());

        for (EtapaRastreamento et : domain.getEtapas()) {
            e.adicionarEtapa(EtapaRastreamentoEntityMapper.toEntity(et));
        }
        return e;
    }

    public static Pedido toDomain(PedidoEntity e) {
        List<EtapaRastreamento> etapas = e.getEtapas().stream()
                .map(EtapaRastreamentoEntityMapper::toDomain)
                .toList();

        return new Pedido(
                e.getId(),
                e.getUsuarioId(),
                e.getCodigoRastreamento(),
                e.getDescricao(),
                e.getValorDeclarado(),
                e.getMoeda(),
                e.getCriadoEm(),
                etapas,
                e.isCancelado()
        );
    }
}