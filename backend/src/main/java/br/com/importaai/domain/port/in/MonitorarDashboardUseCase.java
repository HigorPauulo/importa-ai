package br.com.importaai.domain.port.in;

import br.com.importaai.domain.model.StatusPedido;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface MonitorarDashboardUseCase {

    Resumo executar();

    // KPIs do dashboard administrativo (RF22).
    record Resumo(
            long totalAtivos,
            long taxaPendente,
            long entreguesNoMes,
            Map<StatusPedido, Long> porStatus,
            long total,
            List<PontoEvolucao> evolucao
    ) {}

    // Pedidos criados por dia (série do gráfico de evolução — RF23).
    record PontoEvolucao(LocalDate dia, long total) {}
}
