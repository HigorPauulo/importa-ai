package br.com.importaai.application.usecase;

import br.com.importaai.domain.exception.CodigoRastreamentoDuplicadoException;
import br.com.importaai.domain.model.Pedido;
import br.com.importaai.domain.port.in.CriarPedidoUseCase;
import br.com.importaai.domain.port.out.EventPublisher;
import br.com.importaai.domain.port.out.PedidoRepository;

import java.time.Instant;

public class CriarPedidoService implements CriarPedidoUseCase {

    private final PedidoRepository pedidoRepository;
    private final EventPublisher eventPublisher;

    public CriarPedidoService(PedidoRepository pedidoRepository, EventPublisher eventPublisher) {
        this.pedidoRepository = pedidoRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Pedido executar(Input input) {

        pedidoRepository.buscarPorCodigoRastreamentoEUsuario(input.codigoRastreamento(), input.usuarioId())
                .ifPresent(p -> {
                    throw new CodigoRastreamentoDuplicadoException(
                            "usuario ja possui pedido com codigo " + input.codigoRastreamento());
                });

        Pedido pedido = new Pedido(input.usuarioId(), input.
                codigoRastreamento(), input.descricao(), Instant.now());
        Pedido salvo = pedidoRepository.salvar(pedido);

        eventPublisher.publicar("pedido.criado", salvo);

        return salvo;
    }
}
