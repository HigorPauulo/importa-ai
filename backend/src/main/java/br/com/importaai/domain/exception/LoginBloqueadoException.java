package br.com.importaai.domain.exception;

import java.time.Instant;

public class LoginBloqueadoException extends RuntimeException {

    private final Instant bloqueadoAte;

    public LoginBloqueadoException(Instant bloqueadoAte) {
        super("login bloqueado ate " + bloqueadoAte);
        this.bloqueadoAte = bloqueadoAte;
    }

    public Instant getBloqueadoAte() {
        return bloqueadoAte;
    }
}
