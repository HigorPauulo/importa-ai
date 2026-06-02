package br.com.importaai.application.usecase;

import br.com.importaai.domain.model.Cotacao;
import br.com.importaai.domain.port.in.ConsultarCotacaoUseCase;
import br.com.importaai.domain.port.out.CambioPort;
import br.com.importaai.domain.port.out.CotacaoRepository;

import java.time.Instant;
import java.util.Optional;

public class ConsultarCotacaoService implements ConsultarCotacaoUseCase {

    private final CotacaoRepository cotacaoRepository;
    private final CambioPort cambioPort;

    public ConsultarCotacaoService(CotacaoRepository cotacaoRepository, CambioPort cambioPort) {
        this.cotacaoRepository = cotacaoRepository;
        this.cambioPort = cambioPort;
    }

    @Override
    public Optional<Output> executar(Input input) {
        Instant agora = Instant.now();
        Optional<Cotacao> cacheada = cotacaoRepository.buscarPorPar(input.moedaOrigem(), input.moedaDestino());

        // Cotacao manual sempre prevalece e nunca e tratada como desatualizada (RF21 + RN07)
        if (cacheada.isPresent() && cacheada.get().isManual()) {
            return Optional.of(new Output(cacheada.get(), false));
        }

        // Cache automatico valido (< 24h): retorna direto (Cache-Aside hit)
        if (cacheada.isPresent() && !cacheada.get().estaDesatualizada(agora)) {
            return Optional.of(new Output(cacheada.get(), false));
        }

        // Cache ausente ou desatualizado: tenta a API (Cache-Aside miss)
        Optional<CambioPort.TaxaCambio> taxaApi = cambioPort.consultarTaxa(input.moedaOrigem(), input.moedaDestino());
        if (taxaApi.isPresent()) {
            CambioPort.TaxaCambio taxa = taxaApi.get();
            // cotadoEm vem da API; atualizadoEm = agora (controla o TTL do cache-aside)
            Cotacao fresca = cotacaoRepository.salvar(Cotacao.automatica(
                    input.moedaOrigem(), input.moedaDestino(), taxa.taxa(), taxa.cotadoEm(), agora));
            return Optional.of(new Output(fresca, false));
        }

        // API indisponivel: usa o cache marcando se esta desatualizado (RN07)
        return cacheada.map(c -> new Output(c, c.estaDesatualizada(agora)));
    }
}
