package br.com.importaai.domain.exception;

public class UsuarioNaoEncontradoException extends RuntimeException {
    public UsuarioNaoEncontradoException(Long id) {
        super("usuário " + id + " não encontrado");
    }
}
