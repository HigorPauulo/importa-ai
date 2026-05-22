package br.com.importaai.infrastructure.adapter.out.persistence.mapper;

import br.com.importaai.domain.model.Usuario;
import br.com.importaai.infrastructure.adapter.out.persistence.entity.UsuarioEntity;

public final class UsuarioEntityMapper {

    private UsuarioEntityMapper() {}

    public static UsuarioEntity toEntity(Usuario domain) {
        UsuarioEntity e = new UsuarioEntity();
        e.setId(domain.getId());
        e.setNome(domain.getNome());
        e.setEmail(domain.getEmail());
        e.setSenhaHash(domain.getSenhaHash());
        e.setPerfil(domain.getPerfil());
        e.setCriadoEm(domain.getCriadoEm());
        return e;
    }

    public static Usuario toDomain(UsuarioEntity e) {
        return new Usuario(
                e.getId(),
                e.getNome(),
                e.getEmail(),
                e.getSenhaHash(),
                e.getPerfil(),
                e.getCriadoEm()
        );
    }
}