package br.com.importaai.application.usecase;

import br.com.importaai.domain.exception.PedidoNaoEncontradoException;
import br.com.importaai.domain.model.Pedido;
import br.com.importaai.domain.port.in.CancelarPedidoUseCase;
import br.com.importaai.domain.port.out.EventPublisher;
import br.com.importaai.domain.port.out.PedidoRepository;

public class CancelarPedidoService implements CancelarPedidoUseCase {

    private final PedidoRepository pedidoRepository;
    private final EventPublisher eventPublisher;

    public CancelarPedidoService(PedidoRepository pedidoRepository, EventPublisher eventPublisher) {
        this.pedidoRepository = pedidoRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Pedido executar(Input input) {
        Pedido pedido = pedidoRepository.buscarPorId(input.pedidoId())
                .orElseThrow(() -> new PedidoNaoEncontradoException(input.pedidoId()));

        pedido.cancelar();

        Pedido salvo = pedidoRepository.salvar(pedido);
        eventPublisher.publicar("pedido.atualizado", salvo);
        return salvo;
    }
}
