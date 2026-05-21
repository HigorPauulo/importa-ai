package br.com.importaai.application.usecase;

import br.com.importaai.domain.exception.CodigoRastreamentoDuplicadoException;
import br.com.importaai.domain.model.Pedido;
import br.com.importaai.domain.port.in.CriarPedidoUseCase;
import br.com.importaai.domain.port.out.EventPublisher;
import br.com.importaai.domain.port.out.PedidoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CriarPedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private CriarPedidoService service;

    @Test
    @DisplayName("cria pedido novo, salva e publica evento")
    void criaPedidoSalvarPublicarEvento() {
        CriarPedidoUseCase.Input input = new CriarPedidoUseCase.Input(1L, "BR123456789", "Tenis");

        when(pedidoRepository.buscarPorCodigoRastreamentoEUsuario("BR123456789", 1L))
                .thenReturn(Optional.empty());

        when(pedidoRepository.salvar(any(Pedido.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Pedido resultado = service.executar(input);

        assertThat(resultado.getUsuarioId()).isEqualTo(1L);
        assertThat(resultado.getCodigoRastreamento()).isEqualTo("BR123456789");

        verify(pedidoRepository).salvar(any(Pedido.class));
        verify(eventPublisher).publicar(eq("pedido.criado"), any(Pedido.class));
    }

    @Test
    @DisplayName("rejeita criação se codigo já existe para o usuario (RN06)")
    void rejeitaCodigoDuplicado() {
        CriarPedidoUseCase.Input input = new CriarPedidoUseCase.Input(1L, "BR123456789", "Tênis");
        Pedido existente = new Pedido(1L, "BR123456789", "outro", java.time.Instant.now());

        when(pedidoRepository.buscarPorCodigoRastreamentoEUsuario("BR123456789", 1L))
                .thenReturn(Optional.of(existente));

        assertThatExceptionOfType(CodigoRastreamentoDuplicadoException.class)
                .isThrownBy(() -> service.executar(input));

        verify(pedidoRepository, never()).salvar(any());
        verify(eventPublisher, never()).publicar(any(), any());
    }
}
