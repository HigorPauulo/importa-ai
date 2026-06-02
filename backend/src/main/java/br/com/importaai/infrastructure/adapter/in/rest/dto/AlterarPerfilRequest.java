package br.com.importaai.infrastructure.adapter.in.rest.dto;

import br.com.importaai.domain.model.PerfilUsuario;
import jakarta.validation.constraints.NotNull;

public record AlterarPerfilRequest(@NotNull PerfilUsuario perfil) {}
