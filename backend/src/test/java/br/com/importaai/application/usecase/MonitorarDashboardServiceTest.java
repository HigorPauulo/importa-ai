package br.com.importaai.application.usecase;

import br.com.importaai.domain.model.EtapaRastreamento;
import br.com.importaai.domain.model.Pedido;
import br.com.importaai.domain.model.StatusPedido;
import br.com.importaai.domain.model.TipoEtapa;
import br.com.importaai.domain.port.in.MonitorarDashboardUseCase.Resumo;
import br.com.importaai.domain.port.out.PedidoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonitorarDashboardServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    // "agora" fixo em junho/2026 pra tornar "entregues no mês" determinístico
    private final Clock clock = Clock.fixed(Instant.parse("2026-06-15T12:00:00Z"), ZoneOffset.UTC);

    private Pedido pedidoCom(String codigo, List<EtapaRastreamento> etapas, boolean cancelado) {
        return new Pedido(1L, 1L, codigo, "produto", null, null,
                Instant.parse("2026-06-01T10:00:00Z"), new ArrayList<>(etapas), cancelado);
    }

    @Test
    @DisplayName("TC32: agrega KPIs do dashboard (ativos, taxa pendente, entregues no mes, por status)")
    void agregaKpis() {
        Instant junho = Instant.parse("2026-06-10T10:00:00Z");
        Instant maio = Instant.parse("2026-05-10T10:00:00Z");

        List<Pedido> pedidos = List.of(
                // PROCESSANDO (sem etapa) — ativo
                pedidoCom("BR-1", List.of(), false),
                // ENVIADO (TAXA = taxa pendente) — ativo
                pedidoCom("BR-2", List.of(new EtapaRastreamento(TipoEtapa.TAXA, junho, "SP", "taxa")), false),
                // ENTREGUE em junho — entregue no mes
                pedidoCom("BR-3", List.of(new EtapaRastreamento(TipoEtapa.ENTREGUE, junho, "GO", "entregue")), false),
                // ENTREGUE em maio — NAO conta no mes
                pedidoCom("BR-4", List.of(new EtapaRastreamento(TipoEtapa.ENTREGUE, maio, "GO", "entregue")), false),
                // CANCELADO — nao ativo
                pedidoCom("BR-5", List.of(), true)
        );
        when(pedidoRepository.listarTodos()).thenReturn(pedidos);

        Resumo r = new MonitorarDashboardService(pedidoRepository, clock).executar();

        assertThat(r.total()).isEqualTo(5);
        assertThat(r.totalAtivos()).isEqualTo(2);       // BR-1, BR-2
        assertThat(r.taxaPendente()).isEqualTo(1);      // BR-2
        assertThat(r.entreguesNoMes()).isEqualTo(1);    // BR-3 (junho)
        assertThat(r.porStatus().get(StatusPedido.PROCESSANDO)).isEqualTo(1L);
        assertThat(r.porStatus().get(StatusPedido.ENVIADO)).isEqualTo(1L);
        assertThat(r.porStatus().get(StatusPedido.ENTREGUE)).isEqualTo(2L);
        assertThat(r.porStatus().get(StatusPedido.CANCELADO)).isEqualTo(1L);
    }

    @Test
    @DisplayName("dashboard vazio retorna tudo zerado sem quebrar")
    void dashboardVazio() {
        when(pedidoRepository.listarTodos()).thenReturn(List.of());

        Resumo r = new MonitorarDashboardService(pedidoRepository, clock).executar();

        assertThat(r.total()).isZero();
        assertThat(r.totalAtivos()).isZero();
        assertThat(r.taxaPendente()).isZero();
        assertThat(r.entreguesNoMes()).isZero();
        assertThat(r.porStatus()).isEmpty();
    }
}
