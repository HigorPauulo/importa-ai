package br.com.importaai.domain.port.in;

import br.com.importaai.domain.model.Pedido;

public interface CancelarPedidoUseCase {

    record Input(Long pedidoId) {}

    Pedido executar(Input input);
}
