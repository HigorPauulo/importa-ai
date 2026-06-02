package br.com.importaai.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotNull;

public record AlterarStatusRequest(@NotNull Boolean ativo) {}
