package br.com.importaai.infrastructure.adapter.in.messaging;

import br.com.importaai.domain.model.PerfilUsuario;
import br.com.importaai.domain.port.out.TokenIssuer;
import br.com.importaai.infrastructure.adapter.out.persistence.entity.PedidoEntity;
import br.com.importaai.infrastructure.adapter.out.persistence.entity.UsuarioEntity;
import br.com.importaai.infrastructure.adapter.out.persistence.repository.PedidoJpaRepository;
import br.com.importaai.infrastructure.adapter.out.persistence.repository.RefreshTokenRevogadoJpaRepository;
import br.com.importaai.infrastructure.adapter.out.persistence.repository.TentativaLoginFalhaJpaRepository;
import br.com.importaai.infrastructure.adapter.out.persistence.repository.UsuarioJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
class AuthFlowTest extends MessagingIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired TokenIssuer tokenIssuer;
    @Autowired UsuarioJpaRepository usuarioJpaRepository;
    @Autowired PedidoJpaRepository pedidoJpaRepository;
    @Autowired RefreshTokenRevogadoJpaRepository refreshRevogadoRepository;
    @Autowired TentativaLoginFalhaJpaRepository tentativaRepository;

    @BeforeEach
    void limparAuth() {
        pedidoJpaRepository.deleteAll();
        refreshRevogadoRepository.deleteAll();
        tentativaRepository.deleteAll();
        usuarioJpaRepository.deleteAll();
    }

    // ===== Helpers =====
    private void registrar(String email, String senha) throws Exception {
        String body = String.format(
                "{\"nome\":\"User\",\"email\":\"%s\",\"senha\":\"%s\"}", email, senha);
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> login(String email, String senha) throws Exception {
        String body = String.format("{\"email\":\"%s\",\"senha\":\"%s\"}", email, senha);
        String response = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return (Map<String, String>) json.readValue(response, Map.class);
    }

    private UsuarioEntity criarUsuarioDireto(String email, PerfilUsuario perfil) {
        UsuarioEntity u = new UsuarioEntity();
        u.setNome("Test " + email);
        u.setEmail(email);
        u.setSenhaHash(new BCryptPasswordEncoder(4).encode("dummy"));
        u.setPerfil(perfil);
        u.setCriadoEm(Instant.now());
        return usuarioJpaRepository.save(u);
    }

    // ===== Tests =====
    @Test
    @DisplayName("TC11: login com credenciais validas retorna 200 + access/refresh tokens")
    void tc11_loginRetornaTokens() throws Exception {
        registrar("tc11@x.com", "senha12345");

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"tc11@x.com\",\"senha\":\"senha12345\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    @DisplayName("TC12: GET /api/pedidos sem JWT retorna 4xx")
    void tc12_endpointPrivadoSemJwt() throws Exception {
        mvc.perform(get("/api/pedidos"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("TC13: cliente acessando pedido de outro usuario retorna 403 ACESSO_NEGADO")
    void tc13_acessoAOutroUsuarioRetorna403() throws Exception {
        UsuarioEntity userA = criarUsuarioDireto("a@x.com", PerfilUsuario.CLIENTE);
        UsuarioEntity userB = criarUsuarioDireto("b@x.com", PerfilUsuario.CLIENTE);

        PedidoEntity pedido = new PedidoEntity();
        pedido.setUsuarioId(userA.getId());
        pedido.setCodigoRastreamento("BR-OWNED-BY-A");
        pedido.setDescricao("pedido do A");
        pedido.setCancelado(false);
        pedido.setCriadoEm(Instant.now());
        pedido = pedidoJpaRepository.save(pedido);

        String jwtB = tokenIssuer.emitirAccessToken(
                userB.getId(), userB.getEmail(), PerfilUsuario.CLIENTE);

        mvc.perform(get("/api/pedidos/" + pedido.getId())
                        .header("Authorization", "Bearer " + jwtB))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACESSO_NEGADO"));
    }

    @Test
    @DisplayName("TC14: 5 falhas consecutivas resultam em 429 com Retry-After na 6a tentativa")
    void tc14_bloqueioApos5Falhas() throws Exception {
        registrar("tc14@x.com", "senha12345");

        for (int i = 0; i < 5; i++) {
            mvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"tc14@x.com\",\"senha\":\"errada\"}"))
                    .andExpect(status().isUnauthorized());
        }

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"tc14@x.com\",\"senha\":\"errada\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.codigo").value("LOGIN_BLOQUEADO"));
    }

    @Test
    @DisplayName("TC24: logout revoga refresh; uso subsequente retorna 401 TOKEN_INVALIDO")
    void tc24_logoutRevogaRefreshToken() throws Exception {
        registrar("tc24@x.com", "senha12345");
        Map<String, String> tokens = login("tc24@x.com", "senha12345");
        String refresh = tokens.get("refreshToken");

        // Logout
        mvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refresh + "\"}"))
                .andExpect(status().isNoContent());

        // Refresh com token revogado -> 401
        mvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refresh + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("TOKEN_INVALIDO"));
    }
}
