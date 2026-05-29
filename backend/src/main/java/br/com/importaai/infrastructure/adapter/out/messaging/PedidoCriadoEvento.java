package br.com.importaai.infrastructure.adapter.out.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PedidoCriadoEvento(Long id, Long usuarioId) {}
