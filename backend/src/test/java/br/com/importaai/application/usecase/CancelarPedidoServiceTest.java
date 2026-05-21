package br.com.importaai.application.usecase;

import br.com.importaai.domain.exception.PedidoNaoEncontradoException;
import br.com.importaai.domain.model.Pedido;
import br.com.importaai.domain.model.StatusPedido;
import br.com.importaai.domain.port.in.CancelarPedidoUseCase;
import br.com.importaai.domain.port.out.EventPublisher;
import br.com.importaai.domain.port.out.PedidoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelarPedidoServiceTest {

    @Mock private PedidoRepository pedidoRepository;
    @Mock private EventPublisher eventPublisher;
    @InjectMocks private CancelarPedidoService service;

    @Test
    @DisplayName("cancela pedido, salva e publica evento")
    void cancelaSalvaEPublica() {
        Pedido pedido = new Pedido(1L, "BR123", "Tênis", Instant.parse("2026-05-20T10:00:00Z"));
        when(pedidoRepository.buscarPorId(42L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.salvar(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido resultado = service.executar(new CancelarPedidoUseCase.Input(42L));

        assertThat(resultado.getStatus()).isEqualTo(StatusPedido.CANCELADO);
        assertThat(resultado.isCancelado()).isTrue();
        verify(pedidoRepository).salvar(pedido);
        verify(eventPublisher).publicar(eq("pedido.cancelado"), any(Pedido.class));
    }

    @Test
    @DisplayName("rejeita quando pedido nao existe")
    void rejeitaPedidoInexistente() {
        when(pedidoRepository.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(PedidoNaoEncontradoException.class)
                .isThrownBy(() -> service.executar(new CancelarPedidoUseCase.Input(99L)));

        verify(pedidoRepository, never()).salvar(any());
        verify(eventPublisher, never()).publicar(any(), any());
    }
}
