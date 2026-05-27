package br.com.importaai.infrastructure.adapter.out.persistence;

import br.com.importaai.domain.model.TentativaLoginFalha;
import br.com.importaai.domain.port.out.TentativaLoginFalhaRepository;
import br.com.importaai.infrastructure.adapter.out.persistence.entity.TentativaLoginFalhaEntity;
import br.com.importaai.infrastructure.adapter.out.persistence.repository.TentativaLoginFalhaJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class TentativaLoginFalhaRepositoryJpaAdapter implements TentativaLoginFalhaRepository {

    private final TentativaLoginFalhaJpaRepository jpaRepository;

    public TentativaLoginFalhaRepositoryJpaAdapter(TentativaLoginFalhaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TentativaLoginFalha> buscarPorEmail(String email) {
        return jpaRepository.findById(email).map(this::toDomain);
    }

    @Override
    @Transactional
    public void salvar(TentativaLoginFalha tentativa) {
        TentativaLoginFalhaEntity entity = new TentativaLoginFalhaEntity(
                tentativa.getEmail(),
                tentativa.getContador(),
                tentativa.getBloqueadoAte());
        jpaRepository.save(entity);
    }

    private TentativaLoginFalha toDomain(TentativaLoginFalhaEntity e) {
        return new TentativaLoginFalha(e.getEmail(), e.getContador(), e.getBloqueadoAte());
    }
}
