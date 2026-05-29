package br.com.importaai.infrastructure.adapter.out.messaging;

public record NotificacaoEvento(Long usuarioId, Long pedidoId, String mensagem) {}
