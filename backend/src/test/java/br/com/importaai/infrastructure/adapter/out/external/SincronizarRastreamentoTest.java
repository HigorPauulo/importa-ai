package br.com.importaai.infrastructure.adapter.out.external;

import br.com.importaai.domain.model.EtapaRastreamento;
import br.com.importaai.domain.model.Pedido;
import br.com.importaai.domain.model.PerfilUsuario;
import br.com.importaai.domain.model.TipoEtapa;
import br.com.importaai.domain.port.in.SincronizarRastreamentoUseCase;
import br.com.importaai.domain.port.out.PedidoRepository;
import br.com.importaai.domain.port.out.RastreamentoCorreiosPort;
import br.com.importaai.infrastructure.adapter.in.messaging.MessagingIntegrationTest;
import br.com.importaai.infrastructure.adapter.out.persistence.entity.UsuarioEntity;
import br.com.importaai.infrastructure.adapter.out.persistence.repository.PedidoJpaRepository;
import br.com.importaai.infrastructure.adapter.out.persistence.repository.UsuarioJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
class SincronizarRastreamentoTest extends MessagingIntegrationTest {

    @DynamicPropertySource
    static void desativaAdaptersReais(DynamicPropertyRegistry registry) {
        // nenhum @ConditionalOnProperty casa -> so o @MockitoBean implementa o port
        registry.add("correios.adapter", () -> "none");
    }

    @MockitoBean RastreamentoCorreiosPort correiosPort;

    @Autowired SincronizarRastreamentoUseCase sincronizar;
    @Autowired PedidoRepository pedidoRepository;
    @Autowired PedidoJpaRepository pedidoJpaRepository;
    @Autowired UsuarioJpaRepository usuarioJpaRepository;

    private Long usuarioId;
    private final Instant base = Instant.now().minus(Duration.ofDays(5));

    @BeforeEach
    void preparar() {
        pedidoJpaRepository.deleteAll();
        usuarioJpaRepository.deleteAll();

        UsuarioEntity u = new UsuarioEntity();
        u.setNome("Sync");
        u.setEmail("sync@x.com");
        u.setSenhaHash("hash");
        u.setPerfil(PerfilUsuario.CLIENTE);
        u.setCriadoEm(Instant.now());
        usuarioId = usuarioJpaRepository.save(u).getId();
    }

    @Test
    @DisplayName("TC31: sincroniza apenas pedidos em estado nacional e persiste novas etapas")
    void tc31_sincronizaApenasEstadoNacional() {
        Pedido nacional = new Pedido(null, usuarioId, "BR-NAC", "nacional", base, List.of(
                new EtapaRastreamento(TipoEtapa.NA_CHINA, base, "CN", null),
                new EtapaRastreamento(TipoEtapa.NO_BRASIL, base.plusSeconds(3600), "BR", null)), false);
        Long idNacional = pedidoRepository.salvar(nacional).getId();

        Pedido china = new Pedido(null, usuarioId, "BR-CHINA", "internacional", base, List.of(
                new EtapaRastreamento(TipoEtapa.NA_CHINA, base, "CN", null)), false);
        pedidoRepository.salvar(china);

        when(correiosPort.consultar(eq("BR-NAC"), any())).thenReturn(List.of(
                new EtapaRastreamento(TipoEtapa.CD_BRASIL, base.plusSeconds(7200), "CD Regional", "nova")));

        int atualizados = sincronizar.executar();

        assertThat(atualizados).isEqualTo(1);
        verify(correiosPort).consultar(eq("BR-NAC"), any());
        verify(correiosPort, never()).consultar(eq("BR-CHINA"), any());

        Pedido recarregado = pedidoRepository.buscarPorId(idNacional).orElseThrow();
        assertThat(recarregado.temEtapaDoTipo(TipoEtapa.CD_BRASIL)).isTrue();
        assertThat(recarregado.getEtapas()).hasSize(3);
    }
}
