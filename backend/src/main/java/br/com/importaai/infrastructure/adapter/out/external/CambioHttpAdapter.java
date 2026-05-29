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
import java.util.Optional;

@Component
public class CambioHttpAdapter implements CambioPort {

    private static final Logger log = LoggerFactory.getLogger(CambioHttpAdapter.class);

    private final RestClient restClient;

    public CambioHttpAdapter(@Value("${cambio.api.url}") String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }

    @Override
    public Optional<BigDecimal> consultarTaxa(Moeda moedaOrigem, Moeda moedaDestino) {
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
            String bid = resposta.get(chave).get("bid").asText();
            return Optional.of(new BigDecimal(bid));

        } catch (Exception e) {
            log.warn("falha ao consultar cambio {} -> {}: {}", moedaOrigem, moedaDestino, e.getMessage());
            return Optional.empty();
        }
    }
}
