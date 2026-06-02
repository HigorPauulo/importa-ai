package br.com.importaai.application.usecase;

import br.com.importaai.domain.exception.EtapaRetroativaException;
import br.com.importaai.domain.exception.PedidoImutavelException;
import br.com.importaai.domain.model.EtapaRastreamento;
import br.com.importaai.domain.model.Pedido;
import br.com.importaai.domain.model.ResultadoRastreio;
import br.com.importaai.domain.model.StatusPedido;
import br.com.importaai.domain.port.in.SincronizarRastreamentoUseCase;
import br.com.importaai.domain.port.out.EventPublisher;
import br.com.importaai.domain.port.out.PedidoRepository;
import br.com.importaai.domain.port.out.RastreamentoCorreiosPort;

import java.util.List;

public class SincronizarRastreamentoService implements SincronizarRastreamentoUseCase {

    private final PedidoRepository pedidoRepository;
    private final RastreamentoCorreiosPort correiosPort;
    private final EventPublisher eventPublisher;

    public SincronizarRastreamentoService(PedidoRepository pedidoRepository,
                                          RastreamentoCorreiosPort correiosPort,
                                          EventPublisher eventPublisher) {
        this.pedidoRepository = pedidoRepository;
        this.correiosPort = correiosPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public int executar() {
        int atualizados = 0;
        for (Pedido pedido : pedidoRepository.listarNaoCancelados()) {
            StatusPedido status = pedido.getStatus();
            if (status == StatusPedido.ENTREGUE || status == StatusPedido.DEVOLVIDO) {
                continue;
            }
            if (sincronizar(pedido)) {
                atualizados++;
            }
        }
        return atualizados;
    }

    private boolean sincronizar(Pedido pedido) {
        ResultadoRastreio resultado =
                correiosPort.consultar(pedido.getCodigoRastreamento(), pedido.getCriadoEm());

        boolean mudou = switch (resultado.situacao()) {
            case NAO_LOCALIZADO -> marcarNaoLocalizado(pedido);
            case OK -> aplicarEtapas(pedido, resultado.etapas());
            case FONTE_INDISPONIVEL -> false;
        };

        if (mudou) {
            Pedido salvo = pedidoRepository.salvar(pedido);
            eventPublisher.publicar("rastreamento.atualizado", salvo);
        }
        return mudou;
    }

    private boolean marcarNaoLocalizado(Pedido pedido) {
        if (pedido.isRastreioNaoLocalizado()) {
            return false;
        }
        pedido.marcarRastreioNaoLocalizado();
        return true;
    }

    private boolean aplicarEtapas(Pedido pedido, List<EtapaRastreamento> etapas) {
        boolean mudou = false;
        if (pedido.isRastreioNaoLocalizado()) {
            pedido.limparRastreioNaoLocalizado();
            mudou = true;
        }
        for (EtapaRastreamento etapa : etapas) {
            if (pedido.temEtapaDoTipo(etapa.tipo())) {
                continue;
            }
            try {
                pedido.adicionarEtapa(etapa);
                mudou = true;
            } catch (EtapaRetroativaException | PedidoImutavelException ignored) {
                // etapa fora de ordem ou pedido ja imutavel: ignora
            }
        }
        return mudou;
    }
}
