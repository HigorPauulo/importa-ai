package br.com.importaai.domain.exception;

public class CredenciaisInvalidasException extends RuntimeException {
    public CredenciaisInvalidasException() {
        super("credenciais invalidas");
    }
}
