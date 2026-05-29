package br.com.importaai.infrastructure.adapter.out.persistence.repository;

import br.com.importaai.domain.model.Moeda;
import br.com.importaai.infrastructure.adapter.out.persistence.entity.CotacaoCacheEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CotacaoCacheJpaRepository extends JpaRepository<CotacaoCacheEntity, Long> {

    Optional<CotacaoCacheEntity> findByMoedaOrigemAndMoedaDestino(Moeda moedaOrigem, Moeda moedaDestino);
}
