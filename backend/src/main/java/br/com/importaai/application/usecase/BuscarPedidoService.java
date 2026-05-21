package br.com.importaai.application.usecase;

import br.com.importaai.domain.exception.AcessoNegadoException;
import br.com.importaai.domain.exception.PedidoNaoEncontradoException;
import br.com.importaai.domain.model.Pedido;
import br.com.importaai.domain.port.in.BuscarPedidoUseCase;
import br.com.importaai.domain.port.out.PedidoRepository;

public class BuscarPedidoService implements BuscarPedidoUseCase {

    private final PedidoRepository pedidoRepository;

    public BuscarPedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @Override
    public Pedido executar(Input input) {
        Pedido pedido = pedidoRepository.buscarPorId(input.pedidoId())
                .orElseThrow(() -> new PedidoNaoEncontradoException(input.pedidoId()));

        if (!pedido.getUsuarioId().equals(input.usuarioSolicitanteId())) {
            throw new AcessoNegadoException("usuario nao e dono do pedido " + input.pedidoId());
        }

        return pedido;
    }
}
