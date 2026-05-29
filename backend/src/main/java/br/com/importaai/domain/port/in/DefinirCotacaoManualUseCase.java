package br.com.importaai.domain.port.in;

import br.com.importaai.domain.model.Cotacao;
import br.com.importaai.domain.model.Moeda;

import java.math.BigDecimal;
import java.time.Instant;

public interface DefinirCotacaoManualUseCase {

    record Input(Moeda moedaOrigem, Moeda moedaDestino, BigDecimal taxa, Long usuarioId, Instant validoAte) {}

    Cotacao executar(Input input);
}
