package br.com.importaai.domain.port.out;

import br.com.importaai.domain.model.Moeda;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

public interface CambioPort {

    Optional<TaxaCambio> consultarTaxa(Moeda moedaOrigem, Moeda moedaDestino);

    // taxa + o instante em que a fonte externa cotou esse valor (campo timestamp da API)
    record TaxaCambio(BigDecimal taxa, Instant cotadoEm) {}
}
