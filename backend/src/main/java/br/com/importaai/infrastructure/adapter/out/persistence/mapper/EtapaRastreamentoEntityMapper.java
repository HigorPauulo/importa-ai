package br.com.importaai.infrastructure.adapter.out.persistence.mapper;

import br.com.importaai.domain.model.EtapaRastreamento;
import br.com.importaai.infrastructure.adapter.out.persistence.entity.EtapaRastreamentoEntity;

public final class EtapaRastreamentoEntityMapper {

    private EtapaRastreamentoEntityMapper() {}

    public static EtapaRastreamentoEntity toEntity(EtapaRastreamento domain) {
        EtapaRastreamentoEntity e = new EtapaRastreamentoEntity();
        e.setTipo(domain.tipo());
        e.setCriadoEm(domain.criadoEm());
        e.setLocalizacao(domain.localizacao());
        e.setDescricao(domain.descricao());
        return e;
    }

    public static EtapaRastreamento toDomain(EtapaRastreamentoEntity e) {
        return new EtapaRastreamento(
                e.getTipo(),
                e.getCriadoEm(),
                e.getLocalizacao(),
                e.getDescricao()
        );
    }
}
