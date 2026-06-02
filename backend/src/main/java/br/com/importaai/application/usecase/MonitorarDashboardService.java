package br.com.importaai.application.usecase;

import br.com.importaai.domain.model.Pedido;
import br.com.importaai.domain.model.StatusPedido;
import br.com.importaai.domain.model.TipoEtapa;
import br.com.importaai.domain.port.in.MonitorarDashboardUseCase;
import br.com.importaai.domain.port.out.PedidoRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MonitorarDashboardService implements MonitorarDashboardUseCase {

    private final PedidoRepository pedidoRepository;
    private final Clock clock; // injetado pra tornar "entregues no mês" testável

    public MonitorarDashboardService(PedidoRepository pedidoRepository, Clock clock) {
        this.pedidoRepository = pedidoRepository;
        this.clock = clock;
    }

    @Override
    public Resumo executar() {
        List<Pedido> todos = pedidoRepository.listarTodos();

        Map<StatusPedido, Long> porStatus = todos.stream()
                .collect(Collectors.groupingBy(Pedido::getStatus, Collectors.counting()));

        // Ativo = em andamento (nem entregue, nem cancelado).
        long totalAtivos = todos.stream()
                .map(Pedido::getStatus)
                .filter(s -> s == StatusPedido.PROCESSANDO || s == StatusPedido.ENVIADO)
                .count();

        // Taxa pendente (RF22): última etapa registrada é TAXA.
        long taxaPendente = todos.stream()
                .filter(p -> p.ultimaEtapa().map(e -> e.tipo() == TipoEtapa.TAXA).orElse(false))
                .count();

        // Entregues no mês corrente: status ENTREGUE com a etapa ENTREGUE neste mês.
        YearMonth mesAtual = YearMonth.now(clock);
        long entreguesNoMes = todos.stream()
                .filter(p -> p.getStatus() == StatusPedido.ENTREGUE)
                .filter(p -> p.ultimaEtapa()
                        .map(e -> YearMonth.from(e.criadoEm().atZone(clock.getZone())).equals(mesAtual))
                        .orElse(false))
                .count();

        // Série dos últimos 30 dias: pedidos criados por dia (zero nos dias sem pedido).
        LocalDate hoje = LocalDate.now(clock);
        Map<LocalDate, Long> criadosPorDia = todos.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getCriadoEm().atZone(clock.getZone()).toLocalDate(),
                        Collectors.counting()));
        List<PontoEvolucao> evolucao = IntStream.rangeClosed(0, 29)
                .mapToObj(i -> hoje.minusDays(29 - i))
                .map(dia -> new PontoEvolucao(dia, criadosPorDia.getOrDefault(dia, 0L)))
                .toList();

        return new Resumo(totalAtivos, taxaPendente, entreguesNoMes, porStatus, todos.size(), evolucao);
    }
}
