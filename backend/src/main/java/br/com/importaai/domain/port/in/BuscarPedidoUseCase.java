package br.com.importaai.domain.port.in;

import br.com.importaai.domain.model.Pedido;

public interface BuscarPedidoUseCase {

    record Input(Long pedidoId, Long usuarioSolicitanteId) {}

    Pedido executar(Input input);
}
