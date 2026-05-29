package br.com.importaai.domain.port.in;

import br.com.importaai.domain.model.Moeda;
import br.com.importaai.domain.model.Pedido;

import java.math.BigDecimal;

public interface CriarPedidoUseCase {

    record Input(Long usuarioId, String codigoRastreamento, String descricao,
                 BigDecimal valorDeclarado, Moeda moeda) {}

    Pedido executar(Input input);
}
