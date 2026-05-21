package br.com.importaai.domain.model;

import br.com.importaai.domain.exception.EtapaRetroativaException;
import br.com.importaai.domain.exception.PedidoImutavelException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class PedidoTest {

    private static final Instant T0 = Instant.parse("2026-05-20T10:00:00Z");
    private static final Instant T1 = Instant.parse("2026-05-20T12:00:00Z");
    private static final Instant T2 = Instant.parse("2026-05-20T14:00:00Z");

    @Test
    @DisplayName("cria pedido sem etapas com status PROCESSANDO")
    void criarPedido_statusProcessando_etapasVazias() {
        Pedido pedido = new Pedido("BR123456789", "Computador", T0);

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.PROCESSANDO);
        assertThat(pedido.getEtapas()).isEmpty();
        assertThat(pedido.isCancelado()).isFalse();
    }

    @Test
    @DisplayName("adicionar etapa AEROPORTO_ORIGEM muda status para ENVIADO")
    void adicionarEtapa_atualizaStatusDerivado() {
        Pedido pedido = new Pedido("BR123456789", "Computador", T0);
        EtapaRastreamento etapa = new EtapaRastreamento(
                TipoEtapa.AEROPORTO_ORIGEM, T1, "Shenzhen, CN", null);
        pedido.adicionarEtapa(etapa);

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.ENVIADO);
        assertThat(pedido.getEtapas()).containsExactly(etapa);
    }

    @Test
    @DisplayName("rejeita etapa com timestamp anterior a ultima (TC21)")
    void rejeitaEtapaRetroativa() {
        Pedido pedido = new Pedido("BR123456789", "Computador", T0);
        pedido.adicionarEtapa(new EtapaRastreamento(
                TipoEtapa.AEROPORTO_ORIGEM, T2, "Shenzhen, CN", null));
        EtapaRastreamento retroativa = new EtapaRastreamento(
                TipoEtapa.NA_CHINA, T1, "Shenzhen, CN", null);
        assertThatExceptionOfType(EtapaRetroativaException.class)
                .isThrownBy(() -> pedido.adicionarEtapa(retroativa));
    }

    @Test
    @DisplayName("cancelar marca o pedido como CANCELADO sobrescrevendo etapa")
    void cancelar_marcaComoCancelado() {
        Pedido pedido = new Pedido("BR123456789", "Computador", T0);
        pedido.adicionarEtapa(new EtapaRastreamento(
                TipoEtapa.AEROPORTO_ORIGEM, T1, "Shenzhen, CN", null));
        pedido.cancelar();

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CANCELADO);
        assertThat(pedido.isCancelado()).isTrue();
    }

    @Test
    @DisplayName("rejeita adicionar etapa apos ENTREGUE (TC04)")
    void rejeitaEtapaAposEntregue() {
        Pedido pedido = new Pedido("BR123456789", "Computador", T0);
        pedido.adicionarEtapa(new EtapaRastreamento(
                TipoEtapa.ENTREGUE, T1, "Goiânia, BR", null));

        EtapaRastreamento nova = new EtapaRastreamento(
                TipoEtapa.SAIDA_ENTREGA, T2, "Goiania, BR", null);

        assertThatExceptionOfType(PedidoImutavelException.class)
                .isThrownBy(() -> pedido.adicionarEtapa(nova));
    }

}
