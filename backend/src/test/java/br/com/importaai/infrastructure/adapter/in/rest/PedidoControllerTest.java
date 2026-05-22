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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PedidoController.class)
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
    }

    private Pedido pedidoExemplo(Long id, Long usuarioId, String codigo) {
        return new Pedido(id, usuarioId, codigo, "produto teste",
                Instant.parse("2026-05-22T10:00:00Z"), java.util.List.of(), false);
    }

    @BeforeEach
    void resetMocks() {
        Mockito.reset(criarPedido, buscarPedido, registrarEtapa, cancelarPedido, pedidoRepository);
    }

    // ========== POST /api/pedidos ==========

    @Test
    @DisplayName("POST /api/pedidos com body valido retorna 202 + Location + body")
    void criar_ok() throws Exception {
        when(criarPedido.executar(any())).thenReturn(pedidoExemplo(42L, 1L, "BR-OK"));

        String body = json.writeValueAsString(
                Map.of("codigoRastreamento", "BR-OK", "descricao", "cabo USB-C"));

        mvc.perform(post("/api/pedidos")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isAccepted())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/pedidos/42")))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.codigoRastreamento").value("BR-OK"))
                .andExpect(jsonPath("$.status").value("PROCESSANDO"));
    }

    @Test
    @DisplayName("POST /api/pedidos sem header X-User-Id retorna 400 HEADER_OBRIGATORIO_AUSENTE")
    void criar_semHeader_400() throws Exception {
        String body = json.writeValueAsString(
                Map.of("codigoRastreamento", "BR-OK", "descricao", "cabo USB-C"));

        mvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("HEADER_OBRIGATORIO_AUSENTE"));
    }

    @Test
    @DisplayName("POST /api/pedidos com codigoRastreamento vazio retorna 400 VALIDACAO_FALHOU")
    void criar_bodyInvalido_400() throws Exception {
        String body = json.writeValueAsString(
                Map.of("codigoRastreamento", "", "descricao", "cabo USB-C"));

        mvc.perform(post("/api/pedidos")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO_FALHOU"))
                .andExpect(jsonPath("$.detalhes", org.hamcrest.Matchers.hasSize(1)));
    }

    @Test
    @DisplayName("POST /api/pedidos com codigo duplicado retorna 422")
    void criar_duplicado_422() throws Exception {
        when(criarPedido.executar(any()))
                .thenThrow(new CodigoRastreamentoDuplicadoException("usuario ja possui pedido com codigo BR-DUP"));

        String body = json.writeValueAsString(
                Map.of("codigoRastreamento", "BR-DUP", "descricao", "fone"));

        mvc.perform(post("/api/pedidos")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("CODIGO_RASTREAMENTO_DUPLICADO"));
    }

    // ========== GET /api/pedidos/{id} ==========

    @Test
    @DisplayName("GET /api/pedidos/{id} existente retorna 200")
    void buscar_ok() throws Exception {
        when(buscarPedido.executar(any())).thenReturn(pedidoExemplo(7L, 1L, "BR-GET"));

        mvc.perform(get("/api/pedidos/7").header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.codigoRastreamento").value("BR-GET"));
    }

    @Test
    @DisplayName("GET /api/pedidos/{id} inexistente retorna 404 PEDIDO_NAO_ENCONTRADO")
    void buscar_naoExiste_404() throws Exception {
        when(buscarPedido.executar(any())).thenThrow(new PedidoNaoEncontradoException(999L));

        mvc.perform(get("/api/pedidos/999").header("X-User-Id", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("PEDIDO_NAO_ENCONTRADO"));
    }

    @Test
    @DisplayName("GET /api/pedidos/{id} de outro usuario retorna 403 ACESSO_NEGADO")
    void buscar_naoDono_403() throws Exception {
        when(buscarPedido.executar(any()))
                .thenThrow(new AcessoNegadoException("usuario nao eh dono do pedido 5"));

        mvc.perform(get("/api/pedidos/5").header("X-User-Id", "2"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACESSO_NEGADO"));
    }

    // ========== POST /api/pedidos/{id}/etapas ==========

    @Test
    @DisplayName("POST /api/pedidos/{id}/etapas com etapa retroativa retorna 422 ETAPA_RETROATIVA")
    void registrarEtapa_retroativa_422() throws Exception {
        when(registrarEtapa.executar(any()))
                .thenThrow(new EtapaRetroativaException("etapa anterior a ultima registrada"));

        String body = json.writeValueAsString(
                Map.of("tipoEtapa", TipoEtapa.EM_TRANSITO.name(),
                        "localizacao", "Sao Paulo",
                        "descricao", "manual"));

        mvc.perform(post("/api/pedidos/1/etapas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("ETAPA_RETROATIVA"));
    }

    // ========== DELETE /api/pedidos/{id} ==========

    @Test
    @DisplayName("DELETE /api/pedidos/{id} cancela e retorna 202")
    void cancelar_ok() throws Exception {
        Pedido cancelado = new Pedido(9L, 1L, "BR-DEL", "produto teste",
                Instant.parse("2026-05-22T10:00:00Z"), java.util.List.of(), true);
        when(cancelarPedido.executar(any())).thenReturn(cancelado);

        mvc.perform(delete("/api/pedidos/9"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.cancelado").value(true))
                .andExpect(jsonPath("$.status").value("CANCELADO"));
    }
}
