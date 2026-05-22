package br.com.importaai.infrastructure.config;

import br.com.importaai.application.usecase.BuscarPedidoService;
import br.com.importaai.application.usecase.CancelarPedidoService;
import br.com.importaai.application.usecase.CriarPedidoService;
import br.com.importaai.application.usecase.RegistrarEtapaManualService;
import br.com.importaai.domain.port.in.BuscarPedidoUseCase;
import br.com.importaai.domain.port.in.CancelarPedidoUseCase;
import br.com.importaai.domain.port.in.CriarPedidoUseCase;
import br.com.importaai.domain.port.in.RegistrarEtapaManualUseCase;
import br.com.importaai.domain.port.out.EventPublisher;
import br.com.importaai.domain.port.out.PedidoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseBeanConfig {

    @Bean
    public CriarPedidoUseCase criarPedidoUseCase(
            PedidoRepository pedidoRepository,
            EventPublisher eventPublisher) {
        return new CriarPedidoService(pedidoRepository, eventPublisher);
    }

    @Bean
    public BuscarPedidoUseCase buscarPedidoUseCase(PedidoRepository pedidoRepository) {
        return new BuscarPedidoService(pedidoRepository);
    }

    @Bean
    public RegistrarEtapaManualUseCase registrarEtapaManualUseCase(
            PedidoRepository pedidoRepository,
            EventPublisher eventPublisher) {
        return new RegistrarEtapaManualService(pedidoRepository, eventPublisher);
    }

    @Bean
    public CancelarPedidoUseCase cancelarPedidoUseCase(
            PedidoRepository pedidoRepository,
            EventPublisher eventPublisher) {
        return new CancelarPedidoService(pedidoRepository, eventPublisher);
    }
}
