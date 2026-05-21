package br.com.importaai.domain.exception;

public class PedidoNaoEncontradoException extends RuntimeException {
    public PedidoNaoEncontradoException(Long id) {
        super("pedido " + id + " não encontrado");
    }
}
