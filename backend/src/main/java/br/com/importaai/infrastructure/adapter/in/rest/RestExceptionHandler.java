package br.com.importaai.infrastructure.adapter.in.rest;

import br.com.importaai.domain.exception.AcessoNegadoException;
import br.com.importaai.domain.exception.CodigoRastreamentoDuplicadoException;
import br.com.importaai.domain.exception.EtapaRetroativaException;
import br.com.importaai.domain.exception.PedidoImutavelException;
import br.com.importaai.domain.exception.PedidoNaoEncontradoException;
import br.com.importaai.infrastructure.adapter.in.rest.dto.ErroResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.security.access.AccessDeniedException;

import br.com.importaai.domain.exception.CredenciaisInvalidasException;
import br.com.importaai.domain.exception.EmailJaCadastradoException;
import br.com.importaai.domain.exception.LoginBloqueadoException;
import br.com.importaai.domain.exception.TokenInvalidoException;

import java.time.Duration;
import java.time.Instant;

import java.util.List;

@RestControllerAdvice
public class RestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    // ===== 404 — recurso nao encontrado =====
    @ExceptionHandler(PedidoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> handlePedidoNaoEncontrado(PedidoNaoEncontradoException ex) {
        return build(HttpStatus.NOT_FOUND, "PEDIDO_NAO_ENCONTRADO", ex.getMessage());
    }

    // ===== 403 — autenticado mas sem permissao no recurso =====
    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<ErroResponse> handleAcessoNegado(AcessoNegadoException ex) {
        return build(HttpStatus.FORBIDDEN, "ACESSO_NEGADO", ex.getMessage());
    }

    // ===== 422 — regra de negocio violou (estado/dado valido sintaticamente mas inaceitavel) =====
    @ExceptionHandler(CodigoRastreamentoDuplicadoException.class)
    public ResponseEntity<ErroResponse> handleCodigoDuplicado(CodigoRastreamentoDuplicadoException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "CODIGO_RASTREAMENTO_DUPLICADO", ex.getMessage());
    }

    @ExceptionHandler(PedidoImutavelException.class)
    public ResponseEntity<ErroResponse> handlePedidoImutavel(PedidoImutavelException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "PEDIDO_IMUTAVEL", ex.getMessage());
    }

    @ExceptionHandler(EtapaRetroativaException.class)
    public ResponseEntity<ErroResponse> handleEtapaRetroativa(EtapaRetroativaException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "ETAPA_RETROATIVA", ex.getMessage());
    }

    // ===== 400 — payload mal formado ou validacao falhou =====
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();

        ErroResponse body = new ErroResponse(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDACAO_FALHOU",
                "um ou mais campos sao invalidos",
                detalhes,
                java.time.Instant.now()
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErroResponse> handleMissingHeader(MissingRequestHeaderException ex) {
        return build(HttpStatus.BAD_REQUEST, "HEADER_OBRIGATORIO_AUSENTE",
                "header obrigatorio ausente: " + ex.getHeaderName());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> handleJsonMalformado(HttpMessageNotReadableException ex) {
        // Causas comuns: JSON sintaticamente invalido OU enum com valor desconhecido.
        return build(HttpStatus.BAD_REQUEST, "PAYLOAD_INVALIDO",
                "corpo da requisicao invalido ou mal formado");
    }

    // ===== 422 — email ja cadastrado =====
    @ExceptionHandler(EmailJaCadastradoException.class)
    public ResponseEntity<ErroResponse> handleEmailJaCadastrado(EmailJaCadastradoException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "EMAIL_JA_CADASTRADO", ex.getMessage());
    }

    // ===== 401 — credenciais invalidas (senha errada ou email inexistente) =====
    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErroResponse> handleCredenciaisInvalidas(CredenciaisInvalidasException ex) {
        return build(HttpStatus.UNAUTHORIZED, "CREDENCIAIS_INVALIDAS", ex.getMessage());
    }

    // ===== 429 — login bloqueado por excesso de tentativas (RNF06) =====
    @ExceptionHandler(LoginBloqueadoException.class)
    public ResponseEntity<ErroResponse> handleLoginBloqueado(LoginBloqueadoException ex) {
        long segundosRestantes = Math.max(
                1L,
                Duration.between(Instant.now(), ex.getBloqueadoAte()).getSeconds()
        );
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(segundosRestantes))
                .body(new ErroResponse(429, "LOGIN_BLOQUEADO", ex.getMessage()));
    }

    // ===== 401 — JWT invalido (assinatura, expirado, revogado, tipo errado) =====
    @ExceptionHandler(TokenInvalidoException.class)
    public ResponseEntity<ErroResponse> handleTokenInvalido(TokenInvalidoException ex) {
        return build(HttpStatus.UNAUTHORIZED, "TOKEN_INVALIDO", ex.getMessage());
    }

    // ===== 403 — autenticado mas sem permissao no recurso (perfil errado) =====
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroResponse> handleAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "ACESSO_NEGADO_PERFIL", ex.getMessage());
    }

    // ===== 500 — catch-all para nao vazar stacktrace pro cliente =====
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> handleGenerico(Exception ex) {
        // Log com stacktrace completa pra debugging; resposta limpa pro cliente.
        log.error("erro nao tratado", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "ERRO_INTERNO",
                "ocorreu um erro inesperado");
    }

    private ResponseEntity<ErroResponse> build(HttpStatus status, String codigo, String mensagem) {
        return ResponseEntity.status(status).body(new ErroResponse(status.value(), codigo, mensagem));
    }
}