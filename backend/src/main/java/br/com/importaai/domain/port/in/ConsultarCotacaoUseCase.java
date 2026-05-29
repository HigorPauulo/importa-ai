package br.com.importaai.domain.port.in;

import br.com.importaai.domain.model.Cotacao;
import br.com.importaai.domain.model.Moeda;

import java.util.Optional;

public interface ConsultarCotacaoUseCase {

    record Input(Moeda moedaOrigem, Moeda moedaDestino) {}

    record Output(Cotacao cotacao, boolean desatualizada) {}

    Optional<Output> executar(Input input);
}
