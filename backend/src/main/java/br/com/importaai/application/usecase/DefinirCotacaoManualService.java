package br.com.importaai.application.usecase;

import br.com.importaai.domain.model.Cotacao;
import br.com.importaai.domain.port.in.DefinirCotacaoManualUseCase;
import br.com.importaai.domain.port.out.CotacaoRepository;

import java.time.Instant;

public class DefinirCotacaoManualService implements DefinirCotacaoManualUseCase {

    private final CotacaoRepository cotacaoRepository;

    public DefinirCotacaoManualService(CotacaoRepository cotacaoRepository) {
        this.cotacaoRepository = cotacaoRepository;
    }

    @Override
    public Cotacao executar(Input input) {
        // taxa positiva e validada no construtor de Cotacao; aqui montamos a cotacao MANUAL
        Cotacao manual = Cotacao.manual(
                input.moedaOrigem(),
                input.moedaDestino(),
                input.taxa(),
                input.usuarioId(),
                input.validoAte(),
                Instant.now());
        return cotacaoRepository.salvar(manual);
    }
}
