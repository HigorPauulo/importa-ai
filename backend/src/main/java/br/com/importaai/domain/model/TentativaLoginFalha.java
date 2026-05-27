package br.com.importaai.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public class TentativaLoginFalha {

    public static final int MAX_TENTATIVAS = 5;
    public static final Duration DURACAO_BLOQUEIO = Duration.ofMinutes(15);

    private final String email;
    private int contador;
    private Instant bloqueadoAte;

    public TentativaLoginFalha(String email) {
        this(email, 0, null);
    }

    public TentativaLoginFalha(String email, int contador, Instant bloqueadoAte) {
        this.email = Objects.requireNonNull(email, "email nao pode ser nulo");
        this.contador = contador;
        this.bloqueadoAte = bloqueadoAte;
    }

    public String getEmail() { return email; }
    public int getContador() { return contador; }
    public Instant getBloqueadoAte() { return bloqueadoAte; }

    public boolean estaBloqueado() {
        return bloqueadoAte != null && bloqueadoAte.isAfter(Instant.now());
    }

    public void registrarFalha() {
        Instant agora = Instant.now();
        boolean bloqueioExpirou = bloqueadoAte != null && bloqueadoAte.isBefore(agora);
        if (bloqueioExpirou) {
            contador = 0;
            bloqueadoAte = null;
        }
        contador++;
        if (contador >= MAX_TENTATIVAS) {
            bloqueadoAte = agora.plus(DURACAO_BLOQUEIO);
        }
    }

    public void zerar() {
        contador = 0;
        bloqueadoAte = null;
    }
}
