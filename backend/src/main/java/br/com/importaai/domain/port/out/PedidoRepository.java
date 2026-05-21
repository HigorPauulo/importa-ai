package br.com.importaai.domain.port.out;

import br.com.importaai.domain.model.Pedido;

import java.util.Optional;

public interface PedidoRepository {

    Pedido salvar(Pedido pedido);

    Optional<Pedido> buscarPorId(Long id);

    Optional<Pedido>  buscarPorCodigoRastreamentoEUsuario(String codigoRastreamento, Long usuarioId);

}
