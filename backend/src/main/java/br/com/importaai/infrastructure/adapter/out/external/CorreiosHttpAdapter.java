package br.com.importaai.infrastructure.adapter.out.external;

import br.com.importaai.domain.model.EtapaRastreamento;
import br.com.importaai.domain.model.ResultadoRastreio;
import br.com.importaai.domain.model.TipoEtapa;
import br.com.importaai.domain.port.out.RastreamentoCorreiosPort;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.text.Normalizer;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Integracao real com o Correios CWS (ADR-004), autenticacao por CARTAO DE POSTAGEM:
 *   1) POST /token/v1/autentica/cartaopostagem  (Basic usuario:codigoAcesso + {numero: cartao}) -> Bearer
 *   2) GET  /srorastro/v1/objetos/{codigo}?resultado=T  (Authorization: Bearer)               -> eventos
 *
 * O token e cacheado em memoria; 401 dispara uma renovacao e um retry. Falha de infra (5xx/timeout)
 * cai no fallback do circuit breaker, que degrada pro cache marcando a fonte como indisponivel.
 */
@Component
@ConditionalOnProperty(name = "correios.adapter", havingValue = "http")
public class CorreiosHttpAdapter implements RastreamentoCorreiosPort {

    private static final Logger log = LoggerFactory.getLogger(CorreiosHttpAdapter.class);
    private static final String CIRCUIT = "correios";
    private static final ZoneId FUSO_CORREIOS = ZoneId.of("America/Sao_Paulo");

    private final RestClient restClient;
    private final CorreiosCacheStore cache;
    private final String usuario;
    private final String codigoAcesso;
    private final String cartaoPostagem;

    private volatile String token;
    private volatile Instant tokenExpiraEm = Instant.EPOCH;

    public CorreiosHttpAdapter(@Value("${correios.api.url}") String baseUrl,
                               @Value("${correios.cws.usuario:}") String usuario,
                               @Value("${correios.cws.codigo-acesso:}") String codigoAcesso,
                               @Value("${correios.cws.cartao-postagem:}") String cartaoPostagem,
                               CorreiosCacheStore cache) {
        // HTTP/1.1 explicito: evita o RST_STREAM da negociacao HTTP/2 do cliente JDK.
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(new JdkClientHttpRequestFactory(
                        HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build()))
                .build();
        this.usuario = usuario;
        this.codigoAcesso = codigoAcesso;
        this.cartaoPostagem = cartaoPostagem;
        this.cache = cache;
    }

    @Override
    @CircuitBreaker(name = CIRCUIT, fallbackMethod = "consultarDoCache")
    public ResultadoRastreio consultar(String codigoRastreamento, Instant pedidoCriadoEm) {
        RastroResponse resposta;
        try {
            resposta = rastrear(obterToken(false), codigoRastreamento);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 401) {
                // token expirado/invalido: renova e tenta de novo uma unica vez
                resposta = rastrear(obterToken(true), codigoRastreamento);
            } else {
                throw e;
            }
        }
        return interpretar(codigoRastreamento, resposta);
    }

    // Fallback do circuit breaker: 5xx, timeout, falha de auth ou circuito aberto.
    // Degrada pro cache marcando a fonte como indisponivel (transitorio -> o sync nao mexe no pedido).
    @SuppressWarnings("unused")
    private ResultadoRastreio consultarDoCache(String codigoRastreamento, Instant pedidoCriadoEm, Throwable t) {
        log.warn("API Correios indisponivel ({}), usando cache para {}", t.getMessage(), codigoRastreamento);
        return ResultadoRastreio.indisponivel(cache.recuperar(codigoRastreamento));
    }

    // ===== Autenticacao (cartao de postagem) =====
    private synchronized String obterToken(boolean forcarRenovacao) {
        if (!forcarRenovacao && token != null && Instant.now().isBefore(tokenExpiraEm)) {
            return token;
        }
        TokenResponse tk = restClient.post()
                .uri("/token/v1/autentica/cartaopostagem")
                .headers(h -> h.setBasicAuth(usuario, codigoAcesso))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("numero", cartaoPostagem))
                .retrieve()
                .body(TokenResponse.class);

        if (tk == null || tk.token() == null) {
            throw new IllegalStateException("CWS nao retornou token de autenticacao");
        }
        token = tk.token();
        // CWS expira em ~1h; renovamos com folga em vez de confiar no parsing do campo expiraEm.
        tokenExpiraEm = Instant.now().plus(Duration.ofMinutes(50));
        return token;
    }

    // ===== Rastreamento (SRO Rastro) =====
    private RastroResponse rastrear(String bearer, String codigo) {
        return restClient.get()
                .uri(uri -> uri.path("/srorastro/v1/objetos/{codigo}").queryParam("resultado", "T").build(codigo))
                .header("Authorization", "Bearer " + bearer)
                .retrieve()
                .body(RastroResponse.class);
    }

    private ResultadoRastreio interpretar(String codigo, RastroResponse resposta) {
        if (resposta == null || resposta.objetos() == null || resposta.objetos().isEmpty()) {
            return ResultadoRastreio.naoLocalizado();
        }
        Objeto objeto = resposta.objetos().get(0);
        if (objeto.eventos() == null || objeto.eventos().isEmpty()) {
            // CWS devolve "mensagem" (ex.: SRO-020 objeto nao encontrado) e sem eventos
            log.info("Correios: codigo {} nao localizado ({})", codigo, objeto.mensagem());
            return ResultadoRastreio.naoLocalizado();
        }
        List<EtapaRastreamento> etapas = objeto.eventos().stream()
                .map(this::paraEtapa)
                .filter(Objects::nonNull)
                // CWS lista do mais recente pro mais antigo; o dominio exige ordem cronologica.
                .sorted(Comparator.comparing(EtapaRastreamento::criadoEm))
                .toList();
        cache.guardar(codigo, etapas);
        return ResultadoRastreio.ok(etapas);
    }

    private EtapaRastreamento paraEtapa(EventoCws e) {
        TipoEtapa tipo = classificar(e);
        if (tipo == null) {
            log.debug("evento CWS nao classificado: codigo={} desc={}", e.codigo(), e.descricao());
            return null;
        }
        return new EtapaRastreamento(tipo, parseData(e.dtHrCriado()), localDe(e), e.descricao());
    }

    // Classificacao do evento CWS -> nossa TipoEtapa. Baseada na descricao (texto estavel e legivel),
    // com o codigo como reforco. Ajustar conforme os payloads reais do contrato.
    private TipoEtapa classificar(EventoCws e) {
        String d = semAcento(e.descricao());
        if (d.contains("entregue")) return TipoEtapa.ENTREGUE;
        if (d.contains("saiu para entrega")) return TipoEtapa.SAIDA_ENTREGA;
        if (d.contains("tribut") || d.contains("fiscaliza") || d.contains("aguardando pagamento")
                || d.contains("despacho aduaneiro")) return TipoEtapa.TAXA;
        if (d.contains("centro de distribuicao") || d.contains("unidade de distribuicao")) return TipoEtapa.CD_BRASIL;
        if (d.contains("aeroporto") && (d.contains("destino") || d.contains("brasil"))) return TipoEtapa.AEROPORTO_DESTINO;
        if (d.contains("pais de destino") || d.contains("no brasil") || d.contains("importacao")
                || d.contains("recebido pelos correios")) return TipoEtapa.NO_BRASIL;
        if (d.contains("aeroporto")) return TipoEtapa.AEROPORTO_ORIGEM;
        if (d.contains("transito") || d.contains("encaminhado") || d.contains("trafego")) return TipoEtapa.EM_TRANSITO;
        if (d.contains("postado") || d.contains("postagem")) return TipoEtapa.NA_CHINA;
        return null; // desconhecido: ignorado (nao derruba o lote nem abre o circuito)
    }

    private String localDe(EventoCws e) {
        if (e.unidade() == null || e.unidade().endereco() == null) {
            return null;
        }
        Endereco end = e.unidade().endereco();
        if (end.cidade() == null) {
            return end.uf();
        }
        return end.uf() == null ? end.cidade() : end.cidade() + " / " + end.uf();
    }

    private Instant parseData(String dtHrCriado) {
        try {
            return LocalDateTime.parse(dtHrCriado).atZone(FUSO_CORREIOS).toInstant();
        } catch (Exception ex) {
            log.warn("data de evento CWS ilegivel '{}', usando agora", dtHrCriado);
            return Instant.now();
        }
    }

    private static String semAcento(String texto) {
        if (texto == null) {
            return "";
        }
        String normalizado = Normalizer.normalize(texto, Normalizer.Form.NFD);
        return normalizado.replaceAll("\\p{M}", "").toLowerCase();
    }

    // ===== DTOs do contrato CWS (so o que consumimos) =====
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record TokenResponse(String token, String expiraEm) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record RastroResponse(List<Objeto> objetos) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Objeto(String codObjeto, String mensagem, List<EventoCws> eventos) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record EventoCws(String codigo, String tipo, String descricao, String dtHrCriado, Unidade unidade) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Unidade(Endereco endereco) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Endereco(String cidade, String uf) {}
}
