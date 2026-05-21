package br.com.importaai.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

public class EtapaRastreamentoTest {

    private static final Instant QUANDO = Instant.parse("2026-05-20T10:00:00Z");

    @Test
    @DisplayName("cria etapa válida e expõe os campos pelos acessores")
    void criarEtapaValida_eExpoCampos() {
        EtapaRastreamento etapa = new EtapaRastreamento (
                TipoEtapa.NA_CHINA,
                QUANDO,
                "Shenzhen, CN",
                "Pacote recebido no centro de distribuição"
        );

        assertThat(etapa.tipo()).isEqualTo(TipoEtapa.NA_CHINA);
        assertThat(etapa.criadoEm()).isEqualTo(QUANDO);
        assertThat(etapa.localizacao()).isEqualTo("Shenzhen, CN");
        assertThat(etapa.descricao()).isEqualTo("Pacote recebido no centro de distribuição");
    }

    @Test
    @DisplayName("rejeita tipo nulo com NullPointerException")
    void rejeitarTipoNulo() {
        assertThatNullPointerException().isThrownBy(() ->
                new EtapaRastreamento(null, QUANDO, "Shenzhen, CN", "qualquer")
        );
    }

    @Test
    @DisplayName("rejeita criadoEm nulo com NullPointerException")
    void rejeitarCriadoEmNulo() {
        assertThatNullPointerException().isThrownBy(() ->
                new EtapaRastreamento(TipoEtapa.NA_CHINA, null, "Shenzhen, CN", "qualquer")
        );
    }

    @Test
    @DisplayName("aceita localizacao nula - campo opcional")
    void aceitaLocalizacaoNula() {
        EtapaRastreamento etapa = new EtapaRastreamento (
                TipoEtapa.NA_CHINA,
                QUANDO,
                null,
                "sem localização registrada"
        );

        assertThat(etapa.localizacao()).isNull();
    }

    @Test
    @DisplayName("aceita descricao nula - campo opcional")
    void aceitaDescricaoNula() {
        EtapaRastreamento etapa = new EtapaRastreamento(
                TipoEtapa.NA_CHINA,
                QUANDO,
                "Shenzhen, CN",
                null
        );

        assertThat(etapa.descricao()).isNull();
    }


}
