package br.com.importaai.infrastructure.adapter.out.external;

import br.com.importaai.domain.model.EtapaRastreamento;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CorreiosCacheStore {

    private final Map<String, List<EtapaRastreamento>> cache = new ConcurrentHashMap<>();

    public void guardar(String codigoRastreamento, List<EtapaRastreamento> etapas) {
        cache.put(codigoRastreamento, List.copyOf(etapas));
    }

    public List<EtapaRastreamento> recuperar(String codigoRastreamento) {
        return cache.getOrDefault(codigoRastreamento, List.of());
    }
}
