package br.com.importaai.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class StatusPedidoTest {
    @ParameterizedTest(name = "etapa={0}, cancelado={1} -> {2}")
    @CsvSource({
            ",                  false, PROCESSANDO",
            "NA_CHINA,           false, PROCESSANDO",
            "AEROPORTO_ORIGEM,   false, ENVIADO",
            "EM_TRANSITO,        false, ENVIADO",
            "AEROPORTO_DESTINO,  false, ENVIADO",
            "NO_BRASIL,          false, ENVIADO",
            "TAXA,               false, ENVIADO",
            "CD_BRASIL,          false, ENVIADO",
            "SAIDA_ENTREGA,      false, ENVIADO",
            "ENTREGUE,           false, ENTREGUE",
            "DEVOLVIDO,          false, DEVOLVIDO"
    })
    @DisplayName("derivar segue a tabela normativa do Apêndice A — caminho não cancelado")
    void derivar_segueTabelaApendiceA(TipoEtapa ultimaEtapa, boolean cancelado, StatusPedido esperado) {
        Optional<TipoEtapa> entrada = Optional.ofNullable(ultimaEtapa);

        StatusPedido resultado = StatusPedido.derivar(entrada, cancelado);

        assertThat(resultado).isEqualTo(esperado);
    }

    @ParameterizedTest(name = "etapa={0}, cancelado=true -> CANCELADO")
    @EnumSource(TipoEtapa.class)
    @DisplayName("cancelado=true sobrescreve qualquer etapa anterior")
    void derivar_canceladoSobrescreveQualquerEtapa(TipoEtapa etapa) {
        StatusPedido resultado = StatusPedido.derivar(Optional.of(etapa), true);

        assertThat(resultado).isEqualTo(StatusPedido.CANCELADO);
    }

    @Test
    @DisplayName("pedido sem etapas mas cancelado=true vira CANCELADO")
    void derivar_semEtapasComCanceladoTrue_retornaCancelado() {
        StatusPedido resultado = StatusPedido.derivar(Optional.empty(), true);

        assertThat(resultado).isEqualTo(StatusPedido.CANCELADO);
    }

}



