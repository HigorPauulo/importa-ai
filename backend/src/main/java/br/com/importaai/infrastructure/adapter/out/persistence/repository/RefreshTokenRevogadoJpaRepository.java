package br.com.importaai.infrastructure.adapter.out.persistence.repository;

import br.com.importaai.infrastructure.adapter.out.persistence.entity.RefreshTokenRevogadoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRevogadoJpaRepository
        extends JpaRepository<RefreshTokenRevogadoEntity, Long> {

    boolean existsByTokenHash(String tokenHash);
}
