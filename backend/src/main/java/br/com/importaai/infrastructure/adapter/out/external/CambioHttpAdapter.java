package br.com.importaai.infrastructure.adapter.out.external;

import br.com.importaai.domain.model.Moeda;
import br.com.importaai.domain.port.out.CambioPort;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Component
public class CambioHttpAdapter implements CambioPort {

    private static final Logger log = LoggerFactory.getLogger(CambioHttpAdapter.class);

    private final RestClient restClient;

    public CambioHttpAdapter(@Value("${cambio.api.url}") String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }

    @Override
    public Optional<TaxaCambio> consultarTaxa(Moeda moedaOrigem, Moeda moedaDestino) {
        String par = moedaOrigem.name() + "-" + moedaDestino.name();
        String chave = moedaOrigem.name() + moedaDestino.name();
        try {
            JsonNode resposta = restClient.get()
                    .uri("/json/last/{par}", par)
                    .retrieve()
                    .body(JsonNode.class);

            if (resposta == null || !resposta.has(chave)) {
                return Optional.empty();
            }
            JsonNode cotacao = resposta.get(chave);
            BigDecimal taxa = new BigDecimal(cotacao.get("bid").asText());
            return Optional.of(new TaxaCambio(taxa, lerCotadoEm(cotacao)));

        } catch (Exception e) {
            log.warn("falha ao consultar cambio {} -> {}: {}", moedaOrigem, moedaDestino, e.getMessage());
            return Optional.empty();
        }
    }

    private Instant lerCotadoEm(JsonNode cotacao) {
        JsonNode timestamp = cotacao.get("timestamp");
        if (timestamp != null && timestamp.asText().matches("\\d+")) {
            return Instant.ofEpochSecond(Long.parseLong(timestamp.asText()));
        }
        return Instant.now();
    }
}
