package br.com.importaai.infrastructure.adapter.out.external;

import br.com.importaai.domain.model.Cotacao;
import br.com.importaai.domain.model.Moeda;
import br.com.importaai.domain.port.out.CambioPort;
import br.com.importaai.domain.port.out.CotacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class CambioScheduler {

    private static final Logger log = LoggerFactory.getLogger(CambioScheduler.class);
    private static final List<Moeda> ORIGENS = List.of(Moeda.USD, Moeda.CNY, Moeda.EUR);

    private final CambioPort cambioPort;
    private final CotacaoRepository cotacaoRepository;

    public CambioScheduler(CambioPort cambioPort, CotacaoRepository cotacaoRepository) {
        this.cambioPort = cambioPort;
        this.cotacaoRepository = cotacaoRepository;
    }

    @Scheduled(fixedDelayString = "${cambio.sync.interval}")
    public void atualizarCotacoes() {
        for (Moeda origem : ORIGENS) {
            Optional<CambioPort.TaxaCambio> taxa = cambioPort.consultarTaxa(origem, Moeda.BRL);
            if (taxa.isEmpty()) {
                continue;
            }
            // nao sobrescreve cotacao MANUAL ativa (RF21 prevalece sobre a automatica)
            boolean manualAtiva = cotacaoRepository.buscarPorPar(origem, Moeda.BRL)
                    .map(Cotacao::isManual)
                    .orElse(false);
            if (manualAtiva) {
                continue;
            }
            CambioPort.TaxaCambio tx = taxa.get();
            cotacaoRepository.salvar(
                    Cotacao.automatica(origem, Moeda.BRL, tx.taxa(), tx.cotadoEm(), Instant.now()));
            log.debug("cotacao {} -> BRL atualizada: {}", origem, tx.taxa());
        }
    }
}
