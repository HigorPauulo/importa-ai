package br.com.importaai.infrastructure.adapter.out.external;

import br.com.importaai.domain.model.EtapaRastreamento;
import br.com.importaai.domain.port.out.RastreamentoCorreiosPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@ConditionalOnProperty(name = "correios.adapter", havingValue = "cache-only")
public class CorreiosCacheOnlyAdapter implements RastreamentoCorreiosPort {

    private final CorreiosCacheStore cache;

    public CorreiosCacheOnlyAdapter(CorreiosCacheStore cache) {
        this.cache = cache;
    }

    @Override
    public List<EtapaRastreamento> consultar(String codigoRastreamento, Instant pedidoCriadoEm) {
        return cache.recuperar(codigoRastreamento);
    }
}
