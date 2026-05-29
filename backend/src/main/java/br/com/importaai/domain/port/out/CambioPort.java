package br.com.importaai.domain.port.out;

import br.com.importaai.domain.model.Moeda;

import java.math.BigDecimal;
import java.util.Optional;

public interface CambioPort {

    Optional<BigDecimal> consultarTaxa(Moeda moedaOrigem, Moeda moedaDestino);
}
