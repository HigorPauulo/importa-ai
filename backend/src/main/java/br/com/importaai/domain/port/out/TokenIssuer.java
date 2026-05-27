package br.com.importaai.domain.port.out;

import br.com.importaai.domain.model.PerfilUsuario;
import br.com.importaai.domain.model.TokenClaims;

public interface TokenIssuer {

    String emitirAccessToken(Long usuarioId, String email, PerfilUsuario perfil);

    String emitirRefreshToken(Long usuarioId);

    /** @throws TokenInvalidoException se assinatura, expiração ou tipo divergirem */
    TokenClaims validarAccessToken(String token);

    /** @throws TokenInvalidoException se assinatura, expiração ou tipo divergirem */
    TokenClaims validarRefreshToken(String token);
}
