package br.com.importaai.infrastructure.adapter.out.persistence.mapper;

import br.com.importaai.domain.model.Cotacao;
import br.com.importaai.infrastructure.adapter.out.persistence.entity.CotacaoCacheEntity;

public final class CotacaoCacheEntityMapper {

    private CotacaoCacheEntityMapper() {}

    public static void aplicar(CotacaoCacheEntity e, Cotacao domain) {
        e.setMoedaOrigem(domain.moedaOrigem());
        e.setMoedaDestino(domain.moedaDestino());
        e.setTaxa(domain.taxa());
        e.setFonte(domain.fonte());
        e.setManualPorUsuarioId(domain.manualPorUsuarioId());
        e.setValidoAte(domain.validoAte());
        e.setAtualizadoEm(domain.atualizadoEm());
        e.setCotadoEm(domain.cotadoEm());
    }

    public static Cotacao toDomain(CotacaoCacheEntity e) {
        return new Cotacao(
                e.getId(),
                e.getMoedaOrigem(),
                e.getMoedaDestino(),
                e.getTaxa(),
                e.getFonte(),
                e.getManualPorUsuarioId(),
                e.getValidoAte(),
                e.getAtualizadoEm(),
                e.getCotadoEm()
        );
    }
}
