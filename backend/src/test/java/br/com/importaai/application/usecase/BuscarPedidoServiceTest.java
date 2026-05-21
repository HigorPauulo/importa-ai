package br.com.importaai.application.usecase;

import br.com.importaai.domain.exception.AcessoNegadoException;
import br.com.importaai.domain.exception.PedidoNaoEncontradoException;
import br.com.importaai.domain.model.Pedido;
import br.com.importaai.domain.port.in.BuscarPedidoUseCase;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuscarPedidoServiceTest {

    @Mock private PedidoRepository pedidoRepository;
    @InjectMocks private BuscarPedidoService service;

    @Test
    @DisplayName("retorna pedido quando solicitante é o dono")
    void retornaPedidoQuandoEhDono() {
        Pedido pedido = new Pedido(1L, "BR123", "Tênis", Instant.parse("2026-05-20T10:00:00Z"));
        when(pedidoRepository.buscarPorId(42L)).thenReturn(Optional.of(pedido));

        Pedido resultado = service.executar(new BuscarPedidoUseCase.Input(42L, 1L));

        assertThat(resultado).isSameAs(pedido);
    }

    @Test
    @DisplayName("rejeita quando solicitante não é o dono (RF13)")
    void rejeitaQuandoNaoEhDono() {
        Pedido pedido = new Pedido(1L, "BR123", "Tênis", Instant.parse("2026-05-20T10:00:00Z"));
        when(pedidoRepository.buscarPorId(42L)).thenReturn(Optional.of(pedido));

        assertThatExceptionOfType(AcessoNegadoException.class)
                .isThrownBy(() -> service.executar(new BuscarPedidoUseCase.Input(42L, 99L)));
    }

    @Test
    @DisplayName("rejeita quando pedido nao existe")
    void rejeitaPedidoInexistente() {
        when(pedidoRepository.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(PedidoNaoEncontradoException.class)
                .isThrownBy(() -> service.executar(new BuscarPedidoUseCase.Input(99L, 1L)));
    }
}
