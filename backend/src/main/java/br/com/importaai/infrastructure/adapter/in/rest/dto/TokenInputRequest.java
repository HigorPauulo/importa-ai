package br.com.importaai.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenInputRequest(@NotBlank String refreshToken) {}
