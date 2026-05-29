package br.com.importaai.infrastructure.adapter.out.external;

import br.com.importaai.domain.model.EtapaRastreamento;
import br.com.importaai.domain.model.TipoEtapa;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CorreiosStubAdapterTest {

    private final CorreiosStubAdapter adapter = new CorreiosStubAdapter();

    private TipoEtapa ultimoTipo(Instant criadoEm) {
        List<EtapaRastreamento> etapas = adapter.consultar("BR123", criadoEm);
        return etapas.get(etapas.size() - 1).tipo();
    }

    @Test
    @DisplayName("TC29: pedido com 1h de idade retorna apenas NA_CHINA")
    void tc29_umaHoraRetornaNaChina() {
        Instant criadoEm = Instant.now().minus(Duration.ofHours(1));
        List<EtapaRastreamento> etapas = adapter.consultar("BR123", criadoEm);

        assertThat(etapas).hasSize(1);
        assertThat(etapas.get(0).tipo()).isEqualTo(TipoEtapa.NA_CHINA);
    }

    @Test
    @DisplayName("TC29: pedido com 48h de idade tem a etapa mais recente EM_TRANSITO")
    void tc29_quarentaEOitoHorasChegaEmTransito() {
        Instant criadoEm = Instant.now().minus(Duration.ofHours(48));
        assertThat(ultimoTipo(criadoEm)).isEqualTo(TipoEtapa.EM_TRANSITO);
    }

    @Test
    @DisplayName("TC29: etapas sao progressivas e acumulativas conforme a idade")
    void tc29_etapasAcumulamComOTempo() {
        Instant criadoEm = Instant.now().minus(Duration.ofHours(120));
        List<EtapaRastreamento> etapas = adapter.consultar("BR123", criadoEm);

        // 0h NA_CHINA, 24h AEROPORTO_ORIGEM, 48h EM_TRANSITO, 96h AEROPORTO_DESTINO, 120h NO_BRASIL
        assertThat(etapas).hasSize(5);
        assertThat(etapas.get(0).tipo()).isEqualTo(TipoEtapa.NA_CHINA);
        assertThat(etapas.get(etapas.size() - 1).tipo()).isEqualTo(TipoEtapa.NO_BRASIL);
    }

    @Test
    @DisplayName("TC29: pedido com 300h de idade chega ao ENTREGUE (ciclo completo)")
    void tc29_cicloCompletoChegaEntregue() {
        Instant criadoEm = Instant.now().minus(Duration.ofHours(300));
        List<EtapaRastreamento> etapas = adapter.consultar("BR123", criadoEm);

        assertThat(etapas).hasSize(8);
        assertThat(ultimoTipo(criadoEm)).isEqualTo(TipoEtapa.ENTREGUE);
    }
}
