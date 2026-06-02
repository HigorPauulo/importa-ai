package br.com.importaai.infrastructure.adapter.in.rest.dto;

import br.com.importaai.domain.model.PerfilUsuario;
import br.com.importaai.domain.model.Usuario;

public record UsuarioAdminResponse(
        Long id,
        String nome,
        String email,
        PerfilUsuario perfil,
        boolean ativo
) {
    public static UsuarioAdminResponse from(Usuario u) {
        return new UsuarioAdminResponse(u.getId(), u.getNome(), u.getEmail(), u.getPerfil(), u.isAtivo());
    }
}
