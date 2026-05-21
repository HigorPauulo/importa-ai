package br.com.importaai.application.usecase;

import br.com.importaai.domain.exception.PedidoNaoEncontradoException;
import br.com.importaai.domain.model.EtapaRastreamento;
import br.com.importaai.domain.model.Pedido;
import br.com.importaai.domain.port.in.RegistrarEtapaManualUseCase;
import br.com.importaai.domain.port.out.EventPublisher;
import br.com.importaai.domain.port.out.PedidoRepository;

import java.time.Instant;

public class RegistrarEtapaManualService implements RegistrarEtapaManualUseCase {

    private final PedidoRepository pedidoRepository;
    private final EventPublisher eventPublisher;

    public RegistrarEtapaManualService(PedidoRepository pedidoRepository, EventPublisher eventPublisher) {
        this.pedidoRepository = pedidoRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Pedido executar(Input input) {
        Pedido pedido = pedidoRepository.buscarPorId(input.pedidoId())
                .orElseThrow(() -> new PedidoNaoEncontradoException(input.pedidoId()));

        EtapaRastreamento etapa = new EtapaRastreamento(
                input.tipoEtapa(), Instant.now(), input.localizacao(), input.descricao());

        pedido.adicionarEtapa(etapa);

        Pedido atualizado = pedidoRepository.salvar(pedido);
        eventPublisher.publicar("rastreamento.atualizado", atualizado);
        return atualizado;
    }
}
