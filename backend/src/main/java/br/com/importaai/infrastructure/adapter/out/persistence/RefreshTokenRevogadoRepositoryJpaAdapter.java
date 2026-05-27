package br.com.importaai.infrastructure.adapter.out.persistence;

import br.com.importaai.domain.port.out.RefreshTokenRevogadoRepository;
import br.com.importaai.infrastructure.adapter.out.persistence.entity.RefreshTokenRevogadoEntity;
import br.com.importaai.infrastructure.adapter.out.persistence.repository.RefreshTokenRevogadoJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Repository
public class RefreshTokenRevogadoRepositoryJpaAdapter implements RefreshTokenRevogadoRepository {

    private final RefreshTokenRevogadoJpaRepository jpaRepository;

    public RefreshTokenRevogadoRepositoryJpaAdapter(RefreshTokenRevogadoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estaRevogado(String tokenHash) {
        return jpaRepository.existsByTokenHash(tokenHash);
    }

    @Override
    @Transactional
    public void revogar(String tokenHash, Long usuarioId, Instant expiraEm) {
        RefreshTokenRevogadoEntity entity = new RefreshTokenRevogadoEntity(
                tokenHash, usuarioId, Instant.now(), expiraEm);
        jpaRepository.save(entity);
    }
}
