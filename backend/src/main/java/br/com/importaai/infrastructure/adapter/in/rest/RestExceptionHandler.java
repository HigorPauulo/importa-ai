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