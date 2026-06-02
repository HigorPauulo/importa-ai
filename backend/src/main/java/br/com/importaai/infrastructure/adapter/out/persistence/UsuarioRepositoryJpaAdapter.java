package br.com.importaai.infrastructure.adapter.out.persistence;

import br.com.importaai.domain.model.Usuario;
import br.com.importaai.domain.port.out.UsuarioRepository;
import br.com.importaai.infrastructure.adapter.out.persistence.entity.UsuarioEntity;
import br.com.importaai.infrastructure.adapter.out.persistence.mapper.UsuarioEntityMapper;
import br.com.importaai.infrastructure.adapter.out.persistence.repository.UsuarioJpaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UsuarioRepositoryJpaAdapter implements UsuarioRepository {

    private final UsuarioJpaRepository jpaRepository;

    public UsuarioRepositoryJpaAdapter(UsuarioJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        UsuarioEntity entity = UsuarioEntityMapper.toEntity(usuario);
        UsuarioEntity salvo = jpaRepository.save(entity);
        return UsuarioEntityMapper.toDomain(salvo);
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(UsuarioEntityMapper::toDomain);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return jpaRepository.findByEmail(email).map(UsuarioEntityMapper::toDomain);
    }

    @Override
    public List<Usuario> listarTodos() {
        return jpaRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(UsuarioEntityMapper::toDomain)
                .toList();
    }
}
