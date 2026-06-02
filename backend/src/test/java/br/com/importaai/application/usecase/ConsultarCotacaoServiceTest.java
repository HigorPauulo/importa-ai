package br.com.importaai.application.usecase;

import br.com.importaai.domain.model.Cotacao;
import br.com.importaai.domain.model.Moeda;
import br.com.importaai.domain.port.in.ConsultarCotacaoUseCase;
import br.com.importaai.domain.port.out.CambioPort;
import br.com.importaai.domain.port.out.CotacaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarCotacaoServiceTest {

    @Mock CotacaoRepository cotacaoRepository;
    @Mock CambioPort cambioPort;
    @InjectMocks ConsultarCotacaoService service;

    private final Instant agora = Instant.now();

    @Test
    @DisplayName("TC25: cache ausente -> consulta API, grava e retorna fresca (nao desatualizada)")
    void tc25_consultaApiQuandoNaoHaCache() {
        when(cotacaoRepository.buscarPorPar(Moeda.USD, Moeda.BRL)).thenReturn(Optional.empty());
        when(cambioPort.consultarTaxa(Moeda.USD, Moeda.BRL))
                .thenReturn(Optional.of(new CambioPort.TaxaCambio(new BigDecimal("5.12"), agora)));
        when(cotacaoRepository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        var out = service.executar(new ConsultarCotacaoUseCase.Input(Moeda.USD, Moeda.BRL)).orElseThrow();

        assertThat(out.cotacao().taxa()).isEqualByComparingTo("5.12");
        assertThat(out.desatualizada()).isFalse();
        verify(cotacaoRepository).salvar(any());
    }

    @Test
    @DisplayName("TC26: API indisponivel + cache valido (<24h) -> retorna cache, desatualizada=false (RN07)")
    void tc26_apiForaCacheValido() {
        Instant sync = agora.minus(Duration.ofHours(2));
        Cotacao cache = Cotacao.automatica(Moeda.USD, Moeda.BRL, new BigDecimal("5.00"), sync, sync);
        when(cotacaoRepository.buscarPorPar(Moeda.USD, Moeda.BRL)).thenReturn(Optional.of(cache));
        // cache valido nao deve nem chamar a API
        var out = service.executar(new ConsultarCotacaoUseCase.Input(Moeda.USD, Moeda.BRL)).orElseThrow();

        assertThat(out.cotacao().taxa()).isEqualByComparingTo("5.00");
        assertThat(out.desatualizada()).isFalse();
        verify(cambioPort, never()).consultarTaxa(any(), any());
    }

    @Test
    @DisplayName("TC27: API indisponivel + cache > 24h -> retorna cache com desatualizada=true (RN07)")
    void tc27_apiForaCacheAntigo() {
        Instant sync = agora.minus(Duration.ofHours(30));
        Cotacao cache = Cotacao.automatica(Moeda.USD, Moeda.BRL, new BigDecimal("4.80"), sync, sync);
        when(cotacaoRepository.buscarPorPar(Moeda.USD, Moeda.BRL)).thenReturn(Optional.of(cache));
        when(cambioPort.consultarTaxa(Moeda.USD, Moeda.BRL)).thenReturn(Optional.empty());

        var out = service.executar(new ConsultarCotacaoUseCase.Input(Moeda.USD, Moeda.BRL)).orElseThrow();

        assertThat(out.cotacao().taxa()).isEqualByComparingTo("4.80");
        assertThat(out.desatualizada()).isTrue();
        verify(cotacaoRepository, never()).salvar(any());
    }

    @Test
    @DisplayName("cache > 24h mas API disponivel -> atualiza e retorna fresca")
    void cacheAntigoComApiDisponivelAtualiza() {
        Instant sync = agora.minus(Duration.ofHours(30));
        Cotacao cache = Cotacao.automatica(Moeda.USD, Moeda.BRL, new BigDecimal("4.80"), sync, sync);
        when(cotacaoRepository.buscarPorPar(Moeda.USD, Moeda.BRL)).thenReturn(Optional.of(cache));
        when(cambioPort.consultarTaxa(Moeda.USD, Moeda.BRL))
                .thenReturn(Optional.of(new CambioPort.TaxaCambio(new BigDecimal("5.20"), agora)));
        when(cotacaoRepository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        var out = service.executar(new ConsultarCotacaoUseCase.Input(Moeda.USD, Moeda.BRL)).orElseThrow();

        assertThat(out.cotacao().taxa()).isEqualByComparingTo("5.20");
        assertThat(out.desatualizada()).isFalse();
    }

    @Test
    @DisplayName("cotacao manual prevalece e nunca e desatualizada (RF21)")
    void manualPrevalece() {
        Cotacao manual = Cotacao.manual(Moeda.CNY, Moeda.BRL, new BigDecimal("0.72"),
                9L, null, agora.minus(Duration.ofHours(48)));
        when(cotacaoRepository.buscarPorPar(Moeda.CNY, Moeda.BRL)).thenReturn(Optional.of(manual));

        var out = service.executar(new ConsultarCotacaoUseCase.Input(Moeda.CNY, Moeda.BRL)).orElseThrow();

        assertThat(out.cotacao().isManual()).isTrue();
        assertThat(out.desatualizada()).isFalse();
        verify(cambioPort, never()).consultarTaxa(any(), any());
    }
}
