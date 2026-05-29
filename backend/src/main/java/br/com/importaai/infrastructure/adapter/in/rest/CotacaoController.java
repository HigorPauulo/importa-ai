package br.com.importaai.infrastructure.adapter.in.rest;

import br.com.importaai.domain.exception.CotacaoIndisponivelException;
import br.com.importaai.domain.model.Cotacao;
import br.com.importaai.domain.model.Moeda;
import br.com.importaai.domain.port.in.ConsultarCotacaoUseCase;
import br.com.importaai.domain.port.in.DefinirCotacaoManualUseCase;
import br.com.importaai.infrastructure.adapter.in.rest.dto.CotacaoManualRequest;
import br.com.importaai.infrastructure.adapter.in.rest.dto.CotacaoResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CotacaoController {

    private final ConsultarCotacaoUseCase consultarCotacao;
    private final DefinirCotacaoManualUseCase definirCotacaoManual;

    public CotacaoController(ConsultarCotacaoUseCase consultarCotacao,
                            DefinirCotacaoManualUseCase definirCotacaoManual) {
        this.consultarCotacao = consultarCotacao;
        this.definirCotacaoManual = definirCotacaoManual;
    }

    @GetMapping("/api/cotacoes/{moedaOrigem}")
    public CotacaoResponse consultar(@PathVariable Moeda moedaOrigem) {
        return consultarCotacao.executar(
                        new ConsultarCotacaoUseCase.Input(moedaOrigem, Moeda.BRL))
                .map(CotacaoResponse::from)
                .orElseThrow(() -> new CotacaoIndisponivelException(
                        "cotacao indisponivel para " + moedaOrigem + "-BRL"));
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping("/api/admin/cotacoes")
    @ResponseStatus(HttpStatus.CREATED)
    public CotacaoResponse definirManual(@Valid @RequestBody CotacaoManualRequest body,
                                         @AuthenticationPrincipal Long usuarioId) {
        Cotacao manual = definirCotacaoManual.executar(new DefinirCotacaoManualUseCase.Input(
                body.moedaOrigem(), body.moedaDestino(), body.taxa(), usuarioId, body.validoAte()));

        return new CotacaoResponse(
                manual.moedaOrigem(), manual.moedaDestino(), manual.taxa(),
                manual.isManual(), false, manual.atualizadoEm());
    }
}
