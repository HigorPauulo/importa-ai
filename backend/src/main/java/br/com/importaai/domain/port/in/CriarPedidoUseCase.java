package br.com.importaai.domain.port.in;

import br.com.importaai.domain.model.Pedido;

public interface CriarPedidoUseCase {

    record Input(Long usuarioId, String codigoRastreamento, String descricao) {}

    Pedido executar(Input input);
}
