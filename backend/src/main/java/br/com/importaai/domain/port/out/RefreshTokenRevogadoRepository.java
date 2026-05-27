package br.com.importaai.domain.port.out;

import java.time.Instant;

public interface RefreshTokenRevogadoRepository {

    boolean estaRevogado(String tokenHash);

    void revogar(String tokenHash, Long usuarioId, Instant expiraEm);
}
