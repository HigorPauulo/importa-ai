package br.com.importaai.infrastructure.adapter.in.rest;

import br.com.importaai.domain.exception.AcessoNegadoException;
import br.com.importaai.domain.exception.CodigoRastreamentoDuplicadoException;
import br.com.importaai.domain.exception.EtapaRetroativaException;
import br.com.importaai.domain.exception.PedidoNaoEncontradoException;
import br.com.importaai.domain.model.Pedido;
import br.com.importaai.domain.model.TipoEtapa;
import br.com.importaai.domain.port.in.BuscarPedidoUseCase;
import br.com.importaai.domain.port.in.CancelarPedidoUseCase;
import br.com.importaai.domain.port.in.CriarPedidoUseCase;
import br.com.importaai.domain.port.in.RegistrarEtapaManualUseCase;
import br.com.importaai.domain.port.out.PedidoRepository;
import br.com.importaai.infrastructure.config.SecurityConfig;
import br.com.importaai.infrastructure.security.JwtAuthFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PedidoController.class)
@Import(SecurityConfig.class)
class PedidoControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Autowired CriarPedidoUseCase criarPedido;
    @Autowired BuscarPedidoUseCase buscarPedido;
    @Autowired RegistrarEtapaManualUseCase registrarEtapa;
    @Autowired CancelarPedidoUseCase cancelarPedido;
    @Autowired PedidoRepository pedidoRepository;

    @TestConfiguration
    static class MocksConfig {
        @Bean CriarPedidoUseCase criarPedido() { return mock(CriarPedidoUseCase.class); }
        @Bean BuscarPedidoUseCase buscarPedido() { return mock(BuscarPedidoUseCase.class); }
        @Bean RegistrarEtapaManualUseCase registrarEtapa() { return mock(RegistrarEtapaManualUseCase.class); }
        @Bean CancelarPedidoUseCase cancelarPedido() { return mock(CancelarPedidoUseCase.class); }
        @Bean PedidoRepository pedidoRepository() { return mock(PedidoRepository.class); }
        @Bean br.com.importaai.domain.port.out.TokenIssuer tokenIssuer() {
            return mock(br.com.importaai.domain.port.out.TokenIssuer.class);
        }
    }

    private Pedido pedidoExemplo(Long id, Long usuarioId, String codigo) {
        return new Pedido(id, usuarioId, codigo, "produto teste",
                Instant.parse("2026-05-22T10:00:00Z"), List.of(), false);
    }

    private RequestPostProcessor cliente(Long usuarioId) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                usuarioId, null, List.of(new SimpleGrantedAuthority("ROLE_CLIENTE")));
        return authentication(auth);
    }

    private RequestPostProcessor admin(Long usuarioId) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                usuarioId, null, List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")));
        return authentication(auth);
    }

    @BeforeEach
    void resetMocks() {
        Mockito.reset(criarPedido, buscarPedido, registrarEtapa, cancelarPedido, pedidoRepository);
    }

    // ========== POST /api/pedidos ==========
    @Test
    @DisplayName("POST /api/pedidos autenticado retorna 202 + Location + body")
    void criar_ok() throws Exception {
        when(criarPedido.executar(any())).thenReturn(pedidoExemplo(42L, 1L, "BR-OK"));

        String body = json.writeValueAsString(Map.of("codigoRastreamento", "BR-OK", "descricao", "cabo USB-C", "valorDeclarado", new BigDecimal("100.00"), "moeda", "BRL"));

        mvc.perform(post("/api/pedidos")
                        .with(cliente(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isAccepted())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/pedidos/42")))
                .andExpect(jsonPath("$.id").value(42));
    }

    @Test
    @DisplayName("POST /api/pedidos sem autenticacao retorna 401")
    void criar_semAuth_naoAutorizado() throws Exception {
        String body = json.writeValueAsString(Map.of("codigoRastreamento", "BR-OK", "descricao", "cabo USB-C", "valorDeclarado", new BigDecimal("100.00"), "moeda", "BRL"));

        mvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/pedidos com codigo duplicado retorna 422")
    void criar_duplicado_422() throws Exception {
        when(criarPedido.executar(any()))
                .thenThrow(new CodigoRastreamentoDuplicadoException("usuario ja possui pedido com codigo BR-DUP"));
                        String body = json.writeValueAsString(Map.of("codigoRastreamento", "BR-DUP", "descricao", "fone", "valorDeclarado", new BigDecimal("100.00"), "moeda", "BRL"));

        mvc.perform(post("/api/pedidos")
                        .with(cliente(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("CODIGO_RASTREAMENTO_DUPLICADO"));
    }

    @Test
    @DisplayName("POST /api/pedidos com body invalido retorna 400")
    void criar_bodyInvalido_400() throws Exception {
        String body = json.writeValueAsString(Map.of("codigoRastreamento", "", "descricao", "cabo"));

        mvc.perform(post("/api/pedidos")
                        .with(cliente(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO_FALHOU"));
    }

    // ========== GET /api/pedidos/{id} ==========
    @Test
    @DisplayName("GET /api/pedidos/{id} existente retorna 200")
    void buscar_ok() throws Exception {
        when(buscarPedido.executar(any())).thenReturn(pedidoExemplo(7L, 1L, "BR-GET"));

        mvc.perform(get("/api/pedidos/7").with(cliente(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    @DisplayName("GET /api/pedidos/{id} inexistente retorna 404")
    void buscar_naoExiste_404() throws Exception {
        when(buscarPedido.executar(any())).thenThrow(new PedidoNaoEncontradoException(999L));

        mvc.perform(get("/api/pedidos/999").with(cliente(1L)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("PEDIDO_NAO_ENCONTRADO"));
    }

    @Test
    @DisplayName("GET /api/pedidos/{id} de outro usuario retorna 403")
    void buscar_naoDono_403() throws Exception {
        when(buscarPedido.executar(any()))
                .thenThrow(new AcessoNegadoException("usuario nao eh dono do pedido 5"));

        mvc.perform(get("/api/pedidos/5").with(cliente(2L)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACESSO_NEGADO"));
    }

    // ========== POST /api/pedidos/{id}/etapas (somente ADMIN) ==========
    @Test
    @DisplayName("POST /etapas como ADMIN com etapa retroativa retorna 422")
    void registrarEtapa_retroativa_422() throws Exception {
        when(registrarEtapa.executar(any()))
                .thenThrow(new EtapaRetroativaException("etapa anterior a ultima registrada"));

        String body = json.writeValueAsString(Map.of("tipoEtapa", TipoEtapa.EM_TRANSITO.name(),
                        "localizacao", "Sao Paulo", "descricao", "manual"));

        mvc.perform(post("/api/pedidos/1/etapas")
                        .with(admin(99L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("ETAPA_RETROATIVA"));
    }

    @Test
    @DisplayName("POST /etapas como CLIENTE retorna 403 ACESSO_NEGADO_PERFIL")
    void registrarEtapa_clienteSemPermissao_403() throws Exception {
        String body = json.writeValueAsString(Map.of("tipoEtapa", TipoEtapa.EM_TRANSITO.name(),
                        "localizacao", "X", "descricao", "y"));

        mvc.perform(post("/api/pedidos/1/etapas")
                        .with(cliente(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    // ========== DELETE /api/pedidos/{id} (somente ADMIN) ==========
    @Test
    @DisplayName("DELETE /api/pedidos/{id} como ADMIN cancela e retorna 202")
    void cancelar_ok() throws Exception {
        Pedido cancelado = new Pedido(9L, 1L, "BR-DEL", "produto teste",
                Instant.parse("2026-05-22T10:00:00Z"), List.of(), true);
        when(cancelarPedido.executar(any())).thenReturn(cancelado);

        mvc.perform(delete("/api/pedidos/9").with(admin(99L)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.cancelado").value(true));
    }

    @Test
    @DisplayName("DELETE /api/pedidos/{id} como CLIENTE retorna 403")
    void cancelar_clienteSemPermissao_403() throws Exception {
        mvc.perform(delete("/api/pedidos/9").with(cliente(1L)))
                .andExpect(status().isForbidden());
    }
}
