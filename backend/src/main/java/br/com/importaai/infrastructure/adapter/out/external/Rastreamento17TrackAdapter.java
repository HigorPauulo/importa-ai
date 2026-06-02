package br.com.importaai.infrastructure.adapter.out.external;

import br.com.importaai.domain.model.EtapaRastreamento;
import br.com.importaai.domain.model.ResultadoRastreio;
import br.com.importaai.domain.model.TipoEtapa;
import br.com.importaai.domain.port.out.RastreamentoCorreiosPort;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@ConditionalOnProperty(name = "correios.adapter", havingValue = "17track")
public class Rastreamento17TrackAdapter implements RastreamentoCorreiosPort {

    private static final Logger log = LoggerFactory.getLogger(Rastreamento17TrackAdapter.class);
    private static final String CIRCUIT = "correios";
    private static final String HEADER_TOKEN = "17token";

    private final RestClient restClient;
    private final CorreiosCacheStore cache;
    private final String token;
    private final int carrier;

    public Rastreamento17TrackAdapter(@Value("${track17.api.url}") String baseUrl,
                                      @Value("${track17.token:}") String token,
                                      @Value("${track17.carrier:2151}") int carrier,
                                      CorreiosCacheStore cache) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(new JdkClientHttpRequestFactory(
                        HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build()))
                .build();
        this.token = token;
        this.carrier = carrier;
        this.cache = cache;
    }

    @Override
    @CircuitBreaker(name = CIRCUIT, fallbackMethod = "consultarDoCache")
    public ResultadoRastreio consultar(String codigoRastreamento, Instant pedidoCriadoEm) {
        TrackResponse resposta = consultarRastreio(codigoRastreamento);
        Objeto objeto = encontrar(resposta, codigoRastreamento);

        if (objeto == null && estaRejeitado(resposta, codigoRastreamento)) {
            registrar(codigoRastreamento);
            objeto = encontrar(consultarRastreio(codigoRastreamento), codigoRastreamento);
        }
        return interpretar(codigoRastreamento, objeto);
    }

    @SuppressWarnings("unused")
    private ResultadoRastreio consultarDoCache(String codigoRastreamento, Instant pedidoCriadoEm, Throwable t) {
        log.warn("17track indisponivel ({}), usando cache para {}", t.getMessage(), codigoRastreamento);
        return ResultadoRastreio.indisponivel(cache.recuperar(codigoRastreamento));
    }

    private ResultadoRastreio interpretar(String codigo, Objeto objeto) {
        if (objeto == null || objeto.trackInfo() == null) {
            return ResultadoRastreio.indisponivel(cache.recuperar(codigo));
        }

        List<EtapaRastreamento> etapas = mapearEtapas(objeto.trackInfo());
        if (etapas.isEmpty()) {
            if (ehNaoLocalizado(objeto)) {
                log.info("17track: codigo {} nao localizado", codigo);
                return ResultadoRastreio.naoLocalizado();
            }
            return ResultadoRastreio.indisponivel(cache.recuperar(codigo));
        }

        cache.guardar(codigo, etapas);
        return ResultadoRastreio.ok(etapas);
    }

    private List<EtapaRastreamento> mapearEtapas(TrackInfo trackInfo) {
        if (trackInfo.tracking() == null || trackInfo.tracking().providers() == null) {
            return List.of();
        }
        return trackInfo.tracking().providers().stream()
                .filter(p -> p.events() != null)
                .flatMap(p -> p.events().stream())
                .map(this::paraEtapa)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(EtapaRastreamento::criadoEm))
                .toList();
    }

    private EtapaRastreamento paraEtapa(Evento e) {
        TipoEtapa tipo = classificar(e.subStatus(), pais(e));
        if (tipo == null) {
            log.debug("evento 17track nao classificado: sub_status={} desc={}", e.subStatus(), e.description());
            return null;
        }
        return new EtapaRastreamento(tipo, parseData(e), localDe(e), e.description());
    }

    private TipoEtapa classificar(String subStatus, String pais) {
        if (subStatus == null) {
            return null;
        }
        if (subStatus.startsWith("Exception_Returning")
                || subStatus.startsWith("Exception_Returned")
                || subStatus.equals("Exception_Security")) {
            return TipoEtapa.DEVOLVIDO;
        }
        if (subStatus.startsWith("Delivered")) return TipoEtapa.ENTREGUE;
        if (subStatus.startsWith("OutForDelivery")) return TipoEtapa.SAIDA_ENTREGA;
        if (subStatus.startsWith("AvailableForPickup")) return TipoEtapa.CD_BRASIL;
        if (subStatus.equals("InTransit_CustomsProcessing")) return TipoEtapa.TAXA;
        if (subStatus.equals("InTransit_CustomsReleased")) return TipoEtapa.NO_BRASIL;
        if (subStatus.equals("InTransit_Arrival")) return TipoEtapa.AEROPORTO_DESTINO;
        if (subStatus.equals("InTransit_Departure")) return TipoEtapa.AEROPORTO_ORIGEM;
        if (subStatus.equals("InTransit_PickedUp")) return TipoEtapa.NA_CHINA;
        if (subStatus.equals("InfoReceived")) return TipoEtapa.NA_CHINA;
        if (subStatus.startsWith("InTransit")) {
            if ("CN".equalsIgnoreCase(pais)) return TipoEtapa.NA_CHINA;
            if ("BR".equalsIgnoreCase(pais)) return TipoEtapa.NO_BRASIL;
            return TipoEtapa.EM_TRANSITO;
        }
        return null;
    }

    private String pais(Evento e) {
        if (e.address() != null && e.address().country() != null) {
            return e.address().country();
        }
        if (e.location() != null && e.location().equalsIgnoreCase("CN")) {
            return "CN";
        }
        if (e.address() != null && e.address().city() != null) {
            return "BR";
        }
        return null;
    }

    private String localDe(Evento e) {
        String cidade = e.address() != null ? e.address().city() : null;
        String local = e.location();
        if (cidade != null && local != null) return cidade + " / " + local;
        if (cidade != null) return cidade;
        if ("CN".equalsIgnoreCase(local)) return "China";
        return local;
    }

    private Instant parseData(Evento e) {
        String iso = e.timeUtc() != null ? e.timeUtc() : e.timeIso();
        try {
            return OffsetDateTime.parse(iso).toInstant();
        } catch (Exception ex) {
            log.warn("data de evento 17track ilegivel '{}', usando agora", iso);
            return Instant.now();
        }
    }

    private boolean ehNaoLocalizado(Objeto objeto) {
        return objeto.trackInfo() != null
                && objeto.trackInfo().latestStatus() != null
                && "NotFound".equalsIgnoreCase(objeto.trackInfo().latestStatus().status());
    }

    private TrackResponse consultarRastreio(String codigo) {
        return restClient.post()
                .uri("/track/v2.2/gettrackinfo")
                .header(HEADER_TOKEN, token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(corpo(codigo))
                .retrieve()
                .body(TrackResponse.class);
    }

    private void registrar(String codigo) {
        try {
            restClient.post()
                    .uri("/track/v2.2/register")
                    .header(HEADER_TOKEN, token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(corpo(codigo))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("falha ao registrar {} no 17track: {}", codigo, e.getMessage());
        }
    }

    private List<Map<String, Object>> corpo(String codigo) {
        return List.of(Map.of("number", codigo, "carrier", carrier));
    }

    private Objeto encontrar(TrackResponse resposta, String codigo) {
        if (resposta == null || resposta.data() == null || resposta.data().accepted() == null) {
            return null;
        }
        return resposta.data().accepted().stream()
                .filter(o -> codigo.equals(o.number()))
                .findFirst()
                .orElse(null);
    }

    private boolean estaRejeitado(TrackResponse resposta, String codigo) {
        if (resposta == null || resposta.data() == null || resposta.data().rejected() == null) {
            return false;
        }
        return resposta.data().rejected().stream().anyMatch(r -> codigo.equals(r.number()));
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record TrackResponse(int code, Dados data) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Dados(List<Objeto> accepted, List<Rejeitado> rejected) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Rejeitado(String number) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Objeto(String number, @JsonProperty("track_info") TrackInfo trackInfo) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record TrackInfo(@JsonProperty("latest_status") LatestStatus latestStatus, Tracking tracking) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record LatestStatus(String status, @JsonProperty("sub_status") String subStatus) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Tracking(List<Provedor> providers) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Provedor(List<Evento> events) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Evento(@JsonProperty("time_iso") String timeIso,
                          @JsonProperty("time_utc") String timeUtc,
                          String description,
                          String location,
                          String stage,
                          @JsonProperty("sub_status") String subStatus,
                          Endereco address) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Endereco(String country, String state, String city) {}
}
