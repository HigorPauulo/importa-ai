package br.com.importaai.infrastructure.adapter.in.rest.dto;

import br.com.importaai.domain.model.StatusPedido;
import br.com.importaai.domain.port.in.MonitorarDashboardUseCase;

import java.time.format.DateTimeFormatter;
import java.util.List;

public record DashboardResponse(
        long totalAtivos,
        long taxaPendente,
        long entreguesNoMes,
        long total,
        List<StatusKpi> porStatus,
        List<PontoEvolucao> evolucao
) {
    public record StatusKpi(StatusPedido status, long quantidade, double percentual) {}

    public record PontoEvolucao(String dia, long total) {}

    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("dd/MM");

    public static DashboardResponse from(MonitorarDashboardUseCase.Resumo r) {
        long total = r.total();
        List<StatusKpi> kpis = r.porStatus().entrySet().stream()
                .map(e -> new StatusKpi(
                        e.getKey(),
                        e.getValue(),
                        total == 0 ? 0.0 : Math.round((e.getValue() * 1000.0) / total) / 10.0
                ))
                .toList();
        List<PontoEvolucao> evolucao = r.evolucao().stream()
                .map(p -> new PontoEvolucao(p.dia().format(DIA), p.total()))
                .toList();
        return new DashboardResponse(r.totalAtivos(), r.taxaPendente(), r.entreguesNoMes(), total, kpis, evolucao);
    }
}
