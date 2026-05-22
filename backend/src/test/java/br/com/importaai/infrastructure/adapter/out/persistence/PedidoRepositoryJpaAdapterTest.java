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
import java.util.List;

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

    @Test
    @DisplayName("listarPorUsuario retorna pedidos do dono em ordem desc e ignora pedidos de outros usuarios")
    void listarPorUsuario_ordenadoDescETenanted() {
        Usuario outro = usuarioAdapter.salvar(
                new Usuario("Outro", "outro@x.com", "$2a$12$hash", PerfilUsuario.CLIENTE, Instant.now())
        );

        Instant t1 = Instant.parse("2026-05-22T10:00:00Z");
        Instant t2 = Instant.parse("2026-05-22T11:00:00Z");
        Instant t3 = Instant.parse("2026-05-22T12:00:00Z");
        adapter.salvar(new Pedido(usuarioId, "BR-A", "primeiro", t1));
        adapter.salvar(new Pedido(usuarioId, "BR-B", "segundo", t2));
        adapter.salvar(new Pedido(outro.getId(), "BR-X", "de outro usuario", t3));

        List<Pedido> pedidos = adapter.listarPorUsuario(usuarioId);

        assertThat(pedidos).hasSize(2);
        assertThat(pedidos.get(0).getCodigoRastreamento()).isEqualTo("BR-B");
        assertThat(pedidos.get(1).getCodigoRastreamento()).isEqualTo("BR-A");
    }

    @Test
    @DisplayName("listarPorUsuario retorna lista vazia quando o usuario nao tem pedidos")
    void listarPorUsuario_vazio() {
        List<Pedido> pedidos = adapter.listarPorUsuario(usuarioId);

        assertThat(pedidos).isEmpty();
    }

}
