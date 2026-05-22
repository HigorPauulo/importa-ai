package br.com.importaai.infrastructure.adapter.out.persistence;

import br.com.importaai.domain.model.*;
import br.com.importaai.infrastructure.adapter.out.persistence.repository.PedidoJpaRepository;
import br.com.importaai.infrastructure.adapter.out.persistence.repository.UsuarioJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PedidoRepositoryJpaAdapterTest extends PersistenceIntegrationTest {

    @Autowired PedidoRepositoryJpaAdapter adapter;
    @Autowired UsuarioRepositoryJpaAdapter usuarioAdapter;

    private Long usuarioId;

    @BeforeEach
    void prepararUsuario() {
        Usuario u = usuarioAdapter.salvar(
                new Usuario("Dono", "dono@x.com", "$2a$12$hash", PerfilUsuario.CLIENTE, Instant.now())
        );
        this.usuarioId = u.getId();
    }

    @Test
    @DisplayName("salvar deve persistir pedido sem etapas e devolver com id")
    void salvar_semEtapas() {
        Pedido pedido = new Pedido(usuarioId, "BR123", "Cabo USB-C", Instant.now());

        Pedido salvo = adapter.salvar(pedido);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getCodigoRastreamento()).isEqualTo("BR123");
        assertThat(salvo.getEtapas()).isEmpty();
    }

    @Test
    @DisplayName("salvar deve persistir etapas em cascade quando o pedido tem etapas")
    void salvar_comEtapas_cascade() {
        Pedido pedido = new Pedido(usuarioId, "BR456", "Fone", Instant.now());
        pedido.adicionarEtapa(new EtapaRastreamento(TipoEtapa.NA_CHINA, Instant.now(), "Shenzhen", null));
        pedido.adicionarEtapa(new EtapaRastreamento(TipoEtapa.EM_TRANSITO, Instant.now().plusSeconds(60), "Voo", null));

        Pedido salvo = adapter.salvar(pedido);

        Optional<Pedido> relido = adapter.buscarPorId(salvo.getId());
        assertThat(relido).isPresent();
        assertThat(relido.get().getEtapas()).hasSize(2);    // cascade salvou ambas
    }

    @Test
    @DisplayName("buscarPorCodigoRastreamentoEUsuario deve usar o índice composto")
    void buscarPorCodigoEUsuario() {
        adapter.salvar(new Pedido(usuarioId, "BR789", "Item", Instant.now()));

        Optional<Pedido> achado = adapter.buscarPorCodigoRastreamentoEUsuario("BR789", usuarioId);

        assertThat(achado).isPresent();
    }

    @Test
    @DisplayName("RN06: salvar 2 pedidos com mesmo código no mesmo usuário viola o UNIQUE")
    void rn06_unique() {
        adapter.salvar(new Pedido(usuarioId, "BR999", "Primeiro", Instant.now()));

        assertThatThrownBy(() -> adapter.salvar(new Pedido(usuarioId, "BR999", "Duplicado", Instant.now())))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
