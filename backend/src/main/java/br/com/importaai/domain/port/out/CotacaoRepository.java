package br.com.importaai.domain.port.out;

import br.com.importaai.domain.model.Cotacao;
import br.com.importaai.domain.model.Moeda;

import java.util.Optional;

public interface CotacaoRepository {

    Optional<Cotacao> buscarPorPar(Moeda moedaOrigem, Moeda moedaDestino);

    Cotacao salvar(Cotacao cotacao);
}
