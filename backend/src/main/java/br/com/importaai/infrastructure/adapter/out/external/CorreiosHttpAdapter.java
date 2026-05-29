package br.com.importaai.infrastructure.adapter.out.external;

import br.com.importaai.domain.model.EtapaRastreamento;
import br.com.importaai.domain.model.TipoEtapa;
import br.com.importaai.domain.port.out.RastreamentoCorreiosPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;

@Component
@ConditionalOnProperty(name = "correios.adapter", havingValue = "http")
public class CorreiosHttpAdapter implements RastreamentoCorreiosPort {

    private static final Logger log = LoggerFactory.getLogger(CorreiosHttpAdapter.class);
    private static final String CIRCUIT = "correios";

    private final RestClient restClient;
    private final CorreiosCacheStore cache;

    public CorreiosHttpAdapter(@Value("${correios.api.url}") String baseUrl, CorreiosCacheStore cache) {
        this.restClient = RestClient.create(baseUrl);
        this.cache = cache;
    }

    @Override
    @CircuitBreaker(name = CIRCUIT, fallbackMethod = "consultarDoCache")
    public List<EtapaRastreamento> consultar(String codigoRastreamento, Instant pedidoCriadoEm) {
        CorreiosResposta resposta = restClient.get()
                .uri("/rastreamento/{codigo}", codigoRastreamento)
                .retrieve()
                .body(CorreiosResposta.class);

        List<EtapaRastreamento> etapas = mapear(resposta);
        cache.guardar(codigoRastreamento, etapas);
        return etapas;
    }

    @SuppressWarnings("unused")
    private List<EtapaRastreamento> consultarDoCache(String codigoRastreamento, Instant pedidoCriadoEm, Throwable t) {
        log.warn("API Correios indisponivel ({}), usando cache para {}", t.getMessage(), codigoRastreamento);
        return cache.recuperar(codigoRastreamento);
    }

    private List<EtapaRastreamento> mapear(CorreiosResposta resposta) {
        if (resposta == null || resposta.eventos() == null) {
            return List.of();
        }
        return resposta.eventos().stream()
                .map(e -> new EtapaRastreamento(
                        TipoEtapa.valueOf(e.tipo()), e.data(), e.local(), e.descricao()))
                .toList();
    }
}
