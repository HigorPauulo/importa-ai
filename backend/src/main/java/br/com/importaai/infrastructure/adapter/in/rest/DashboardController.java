package br.com.importaai.infrastructure.adapter.in.rest;

import br.com.importaai.domain.port.in.MonitorarDashboardUseCase;
import br.com.importaai.infrastructure.adapter.in.rest.dto.DashboardResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    private final MonitorarDashboardUseCase monitorarDashboard;

    public DashboardController(MonitorarDashboardUseCase monitorarDashboard) {
        this.monitorarDashboard = monitorarDashboard;
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping
    public DashboardResponse obter() {
        return DashboardResponse.from(monitorarDashboard.executar());
    }
}
