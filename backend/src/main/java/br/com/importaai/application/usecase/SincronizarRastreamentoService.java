package br.com.importaai.application.usecase;

import br.com.importaai.domain.exception.EtapaRetroativaException;
import br.com.importaai.domain.exception.PedidoImutavelException;
import br.com.importaai.domain.model.EtapaRastreamento;
import br.com.importaai.domain.model.Pedido;
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
            if (!pedido.estaEmEstadoNacional()) {
                continue;
            }
            if (sincronizar(pedido)) {
                atualizados++;
            }
        }
        return atualizados;
    }

    private boolean sincronizar(Pedido pedido) {
        List<EtapaRastreamento> etapasApi =
                correiosPort.consultar(pedido.getCodigoRastreamento(), pedido.getCriadoEm());

        boolean mudou = false;
        for (EtapaRastreamento etapa : etapasApi) {
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

        if (mudou) {
            Pedido salvo = pedidoRepository.salvar(pedido);
            eventPublisher.publicar("rastreamento.atualizado", salvo);
        }
        return mudou;
    }
}
