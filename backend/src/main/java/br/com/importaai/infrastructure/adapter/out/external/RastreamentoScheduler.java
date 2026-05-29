package br.com.importaai.infrastructure.adapter.out.external;

import br.com.importaai.domain.port.in.SincronizarRastreamentoUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RastreamentoScheduler {

    private static final Logger log = LoggerFactory.getLogger(RastreamentoScheduler.class);

    private final SincronizarRastreamentoUseCase sincronizarUseCase;

    public RastreamentoScheduler(SincronizarRastreamentoUseCase sincronizarUseCase) {
        this.sincronizarUseCase = sincronizarUseCase;
    }

    @Scheduled(fixedDelayString = "${correios.sync.interval}")
    public void sincronizar() {
        int atualizados = sincronizarUseCase.executar();
        if (atualizados > 0) {
            log.info("sincronizacao Correios: {} pedido(s) atualizado(s)", atualizados);
        }
    }
}
