package br.com.importaai.domain.exception;

public class EmailJaCadastradoException extends RuntimeException {
    public EmailJaCadastradoException(String email) {
        super("email ja cadastrado: " + email);
    }
}
