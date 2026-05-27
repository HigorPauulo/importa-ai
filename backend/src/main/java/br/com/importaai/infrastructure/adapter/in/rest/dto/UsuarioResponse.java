package br.com.importaai.infrastructure.adapter.in.rest.dto;

import br.com.importaai.domain.model.PerfilUsuario;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        PerfilUsuario perfil
) {}
