package br.com.importaai.infrastructure.adapter.out.persistence;

import br.com.importaai.domain.model.PerfilUsuario;
import br.com.importaai.domain.model.Usuario;
import br.com.importaai.infrastructure.adapter.out.persistence.repository.UsuarioJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioRepositoryJpaAdapterTest extends PersistenceIntegrationTest {

    @Autowired UsuarioRepositoryJpaAdapter adapter;

    @Test
    @DisplayName("salvar deve persistir e retornar usuario com id gerado pelo banco")
    void salvar_devePersistir() {
        Usuario novo = new Usuario("Alice", "alice@x.com", "$2a$12$hash", PerfilUsuario.CLIENTE, Instant.now());

        Usuario salvo = adapter.salvar(novo);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getEmail()).isEqualTo("alice@x.com");
    }

    @Test
    @DisplayName("buscarPorId deve retornar Optional preenchido quando existe")
    void buscarPorId_deveRetornar() {
        Usuario salvo = adapter.salvar(
                new Usuario("Bob", "bob@x.com", "$2a$12$hash", PerfilUsuario.CLIENTE, Instant.now())
        );

        Optional<Usuario> achado = adapter.buscarPorId(salvo.getId());

        assertThat(achado).isPresent();
        assertThat(achado.get().getNome()).isEqualTo("Bob");
    }

    @Test
    @DisplayName("buscarPorEmail deve usar o metodo derivado e retornar o usuário")
    void buscarPorEmail_deveRetornar() {
        adapter.salvar(new Usuario("Higor", "higor@x.com", "$2a$12$hash", PerfilUsuario.ADMINISTRADOR, Instant.now()));

        Optional<Usuario> achado = adapter.buscarPorEmail("higor@x.com");

        assertThat(achado).isPresent();
        assertThat(achado.get().getPerfil()).isEqualTo(PerfilUsuario.ADMINISTRADOR);
    }

    @Test
    @DisplayName("buscarPorId deve retornar Optional.empty quando id não existe")
    void buscarPorId_inexistente_deveRetornarVazio() {
        assertThat(adapter.buscarPorId(99999L)).isEmpty();
    }
}
