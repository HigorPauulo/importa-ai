package br.com.importaai.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AtualizarPerfilRequest(
        @NotBlank String nome,
        @NotBlank @Email String email,
        String senha
) {}
