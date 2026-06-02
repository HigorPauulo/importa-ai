package br.com.importaai.infrastructure.adapter.out.external;

import br.com.importaai.domain.model.EtapaRastreamento;
import br.com.importaai.domain.model.ResultadoRastreio;
import br.com.importaai.domain.model.TipoEtapa;
import br.com.importaai.domain.port.out.RastreamentoCorreiosPort;
import br.com.importaai.infrastructure.adapter.in.messaging.MessagingIntegrationTest;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.Instant;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
class CorreiosHttpAdapterTest extends MessagingIntegrationTest {

    static final WireMockServer WIREMOCK = new WireMockServer(options().dynamicPort());

    @BeforeAll
    static void startWireMock() {
        WIREMOCK.start();
    }

    @AfterAll
    static void stopWireMock() {
        WIREMOCK.stop();
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("correios.adapter", () -> "http");
        registry.add("correios.api.url", () -> "http://localhost:" + WIREMOCK.port());
        registry.add("correios.cws.usuario", () -> "u");
        registry.add("correios.cws.codigo-acesso", () -> "s");
        registry.add("correios.cws.cartao-postagem", () -> "1234567890");
    }

    @Autowired RastreamentoCorreiosPort port;
    @Autowired CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void reset() {
        WIREMOCK.resetAll();
        circuitBreakerRegistry.circuitBreaker("correios").reset();
    }

    private void stubToken() {
        WIREMOCK.stubFor(post(urlPathEqualTo("/token/v1/autentica/cartaopostagem"))
                .willReturn(okJson("""
                        {"token":"tk-123","expiraEm":"2099-12-31T23:59:59"}""")));
    }

    private void stubRastroSucesso() {
        WIREMOCK.stubFor(get(urlPathMatching("/srorastro/v1/objetos/.*"))
                .willReturn(okJson("""
                        {"objetos":[{"codObjeto":"BR123","eventos":[
                          {"codigo":"PO","descricao":"Objeto postado","dtHrCriado":"2026-05-01T10:00:00",
                           "unidade":{"endereco":{"cidade":"Shenzhen","uf":"CN"}}}]}]}""")));
    }

    private void stubRastroFalha() {
        WIREMOCK.stubFor(get(urlPathMatching("/srorastro/v1/objetos/.*"))
                .willReturn(aResponse().withStatus(500)));
    }

    private void stubRastroNaoEncontrado() {
        WIREMOCK.stubFor(get(urlPathMatching("/srorastro/v1/objetos/.*"))
                .willReturn(okJson("""
                        {"objetos":[{"codObjeto":"BR404",
                          "mensagem":"SRO-020: Objeto nao encontrado na base de dados dos Correios.",
                          "eventos":[]}]}""")));
    }

    private int rastreiosRecebidos() {
        return WIREMOCK.findAll(getRequestedFor(urlPathMatching("/srorastro/v1/objetos/.*"))).size();
    }

    @Test
    @DisplayName("TC30: apos 5 falhas o circuito abre e a proxima chamada nao toca a API (retorna cache)")
    void tc30_circuitBreakerAbreEUsaCache() {
        Instant criadoEm = Instant.now();
        stubToken();

        // 1 sucesso popula o cache (evento "Objeto postado" -> NA_CHINA)
        stubRastroSucesso();
        List<EtapaRastreamento> primeira = port.consultar("BR123", criadoEm).etapas();
        assertThat(primeira).hasSize(1);
        assertThat(primeira.get(0).tipo()).isEqualTo(TipoEtapa.NA_CHINA);

        // 5 falhas consecutivas -> circuito abre (fallback retorna cache)
        stubRastroFalha();
        for (int i = 0; i < 5; i++) {
            port.consultar("BR123", criadoEm);
        }

        assertThat(circuitBreakerRegistry.circuitBreaker("correios").getState())
                .isEqualTo(CircuitBreaker.State.OPEN);

        int antes = rastreiosRecebidos(); // 1 sucesso + 5 falhas = 6
        assertThat(antes).isEqualTo(6);

        // circuito aberto -> NAO toca a API, retorna do cache
        List<EtapaRastreamento> doCache = port.consultar("BR123", criadoEm).etapas();
        port.consultar("BR123", criadoEm);

        assertThat(rastreiosRecebidos()).isEqualTo(antes); // continua 6: curto-circuito
        assertThat(doCache).hasSize(1);
        assertThat(doCache.get(0).tipo()).isEqualTo(TipoEtapa.NA_CHINA);
    }

    @Test
    @DisplayName("codigo desconhecido (sem eventos / SRO-020) -> NAO_LOCALIZADO e NAO abre o circuito")
    void naoLocalizadoNaoAbreCircuito() {
        stubToken();
        stubRastroNaoEncontrado();

        for (int i = 0; i < 6; i++) {
            ResultadoRastreio resultado = port.consultar("BR404", Instant.now());
            assertThat(resultado.situacao()).isEqualTo(ResultadoRastreio.Situacao.NAO_LOCALIZADO);
            assertThat(resultado.etapas()).isEmpty();
        }

        // resposta valida (200 sem eventos), nao falha de infra: circuito permanece fechado
        assertThat(circuitBreakerRegistry.circuitBreaker("correios").getState())
                .isEqualTo(CircuitBreaker.State.CLOSED);
    }
}
