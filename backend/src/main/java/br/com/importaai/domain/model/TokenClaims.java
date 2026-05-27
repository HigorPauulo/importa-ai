package br.com.importaai.domain.model;

import java.time.Instant;

public record TokenClaims(
        Long usuarioId,
        String email,
        PerfilUsuario perfil,
        Instant expiraEm
) {}
