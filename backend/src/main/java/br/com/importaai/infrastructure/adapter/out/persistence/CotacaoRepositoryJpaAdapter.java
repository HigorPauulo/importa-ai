package br.com.importaai.infrastructure.adapter.out.persistence;

import br.com.importaai.domain.model.Cotacao;
import br.com.importaai.domain.model.Moeda;
import br.com.importaai.domain.port.out.CotacaoRepository;
import br.com.importaai.infrastructure.adapter.out.persistence.entity.CotacaoCacheEntity;
import br.com.importaai.infrastructure.adapter.out.persistence.mapper.CotacaoCacheEntityMapper;
import br.com.importaai.infrastructure.adapter.out.persistence.repository.CotacaoCacheJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class CotacaoRepositoryJpaAdapter implements CotacaoRepository {

    private final CotacaoCacheJpaRepository jpaRepository;

    public CotacaoRepositoryJpaAdapter(CotacaoCacheJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cotacao> buscarPorPar(Moeda moedaOrigem, Moeda moedaDestino) {
        return jpaRepository.findByMoedaOrigemAndMoedaDestino(moedaOrigem, moedaDestino)
                .map(CotacaoCacheEntityMapper::toDomain);
    }

    @Override
    @Transactional
    public Cotacao salvar(Cotacao cotacao) {
        // upsert por par: reaproveita a linha existente do par (UNIQUE uk_cotacao_par)
        CotacaoCacheEntity entity = jpaRepository
                .findByMoedaOrigemAndMoedaDestino(cotacao.moedaOrigem(), cotacao.moedaDestino())
                .orElseGet(CotacaoCacheEntity::new);

        CotacaoCacheEntityMapper.aplicar(entity, cotacao);
        return CotacaoCacheEntityMapper.toDomain(jpaRepository.save(entity));
    }
}
