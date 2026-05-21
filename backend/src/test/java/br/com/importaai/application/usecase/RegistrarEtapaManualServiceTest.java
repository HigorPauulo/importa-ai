package br.com.importaai.application.usecase;

import br.com.importaai.domain.exception.PedidoNaoEncontradoException;
import br.com.importaai.domain.model.Pedido;
import br.com.importaai.domain.model.StatusPedido;
import br.com.importaai.domain.model.TipoEtapa;
import br.com.importaai.domain.port.in.RegistrarEtapaManualUseCase;
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
class RegistrarEtapaManualServiceTest {

    @Mock private PedidoRepository pedidoRepository;
    @Mock private EventPublisher eventPublisher;
    @InjectMocks private RegistrarEtapaManualService service;

    @Test
    @DisplayName("registra etapa, atualiza status do pedido e publica evento")
    void registraEtapa_atualizaStatus_publicaEvento() {
        Pedido pedido = new Pedido(1L, "BR123456789", "Tênis", Instant.parse("2026-05-20T10:00:00Z"));

        when(pedidoRepository.buscarPorId(42L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.salvar(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido resultado = service.executar(new RegistrarEtapaManualUseCase.Input(
                42L, TipoEtapa.AEROPORTO_ORIGEM, "Shenzhen", "Embarque"));

        assertThat(resultado.getStatus()).isEqualTo(StatusPedido.ENVIADO);
        assertThat(resultado.getEtapas()).hasSize(1);
        verify(pedidoRepository).salvar(pedido);
        verify(eventPublisher).publicar(eq("rastreamento.atualizado"), any(Pedido.class));
    }

    @Test
    @DisplayName("rejeita quando pedido nao existe")
    void rejeitaPedidoInexistente() {
        when(pedidoRepository.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(PedidoNaoEncontradoException.class)
                .isThrownBy(() -> service.executar(new RegistrarEtapaManualUseCase.Input(
                        99L, TipoEtapa.AEROPORTO_ORIGEM, null, null)));

        verify(pedidoRepository, never()).salvar(any());
        verify(eventPublisher, never()).publicar(any(), any());
    }
}
