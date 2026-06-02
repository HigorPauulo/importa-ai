package br.com.importaai.domain.port.in;

public interface SincronizarRastreamentoUseCase {

    int executar();

    void sincronizarPedido(Long pedidoId);
}
