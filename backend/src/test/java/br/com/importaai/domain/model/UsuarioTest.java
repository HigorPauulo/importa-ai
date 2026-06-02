package br.com.importaai.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

public class UsuarioTest {

    private static final Instant CRIADO_EM = Instant.parse("2026-05-20T10:00:00Z");

    @Test
    @DisplayName("cria usuario novo (sem id) e expoe os campos")
    void criaUsuarioNovo_idNullCamposExpostos() {
        Usuario usuario = new Usuario(
                "Higor Paulo",
                "higor@example.com",
                "hash-da-senha-aqui",
                PerfilUsuario.CLIENTE,
                CRIADO_EM
        );

        assertThat(usuario.getId()).isNull();
        assertThat(usuario.getNome()).isEqualTo("Higor Paulo");
        assertThat(usuario.getEmail()).isEqualTo("higor@example.com");
        assertThat(usuario.getSenhaHash()).isEqualTo("hash-da-senha-aqui");
        assertThat(usuario.getPerfil()).isEqualTo(PerfilUsuario.CLIENTE);
        assertThat(usuario.getCriadoEm()).isEqualTo(CRIADO_EM);
    }

    @Test
    @DisplayName("cria usuario reconstituido do banco (com id) e expoe o id")
    void criaUsuarioComId_idExposto() {
        Usuario usuario = new Usuario(
                42L,
                "Higor Paulo",
                "higor@example.com",
                "hash-da-senha-aqui",
                PerfilUsuario.ADMINISTRADOR,
                true,
                CRIADO_EM
        );

        assertThat(usuario.getId()).isEqualTo(42L);
        assertThat(usuario.getPerfil()).isEqualTo(PerfilUsuario.ADMINISTRADOR);
        assertThat(usuario.isAtivo()).isTrue();
    }

    @ParameterizedTest(name = "nome invalido: \"{0}\"")
    @NullAndEmptySource
    @DisplayName("rejeita nome nulo ou vazio")
    void rejeitaNomeInvalido(String nomeInvalido) {
        assertThatIllegalArgumentException().isThrownBy(() ->
                new Usuario(nomeInvalido, "higor@example.com", "hash", PerfilUsuario.CLIENTE, CRIADO_EM)
        );
    }

    @ParameterizedTest(name = "email inválido: \"{0}\"")
    @NullAndEmptySource
    @DisplayName("rejeita email nulo ou vazio")
    void rejeitaEmailInvalido(String emailInvalido) {
        assertThatIllegalArgumentException().isThrownBy(() ->
                new Usuario("Higor Paulo", emailInvalido, "hash", PerfilUsuario.CLIENTE, CRIADO_EM)
        );
    }

    @Test
    @DisplayName("rejeita senhaHash nulo com NullPointerException")
    void rejeitaSenhaHashNulo() {
        assertThatNullPointerException().isThrownBy(() ->
                new Usuario("Higor Paulo", "higor@example.com", null, PerfilUsuario.CLIENTE, CRIADO_EM)
        );
    }

    @Test
    @DisplayName("rejeita perfil nulo com NullPointerException")
    void rejeitaPerfilNulo() {
        assertThatNullPointerException().isThrownBy(() ->
                new Usuario("Higor Paulo", "higor@example.com", "hash", null, CRIADO_EM)
        );
    }

    @Test
    @DisplayName("rejeita criadoEm nulo com NullPointerException")
    void rejeitaCriadoEmNulo() {
        assertThatNullPointerException().isThrownBy(() ->
                new Usuario("Higor Paulo", "higor@example.com", "hash", PerfilUsuario.CLIENTE, null)
        );
    }
}
