package br.com.importaai.domain.port.in;

import br.com.importaai.domain.model.StatusPedido;

import java.util.Map;

public interface MonitorarDashboardUseCase {

    Resumo executar();

    // KPIs do dashboard administrativo (RF22).
    record Resumo(
            long totalAtivos,
            long taxaPendente,
            long entreguesNoMes,
            Map<StatusPedido, Long> porStatus,
            long total
    ) {}
}
