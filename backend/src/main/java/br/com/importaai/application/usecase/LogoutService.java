package br.com.importaai.application.usecase;

import br.com.importaai.domain.model.TokenClaims;
import br.com.importaai.domain.port.in.LogoutUseCase;
import br.com.importaai.domain.port.out.RefreshTokenRevogadoRepository;
import br.com.importaai.domain.port.out.TokenIssuer;

public class LogoutService implements LogoutUseCase {

    private final RefreshTokenRevogadoRepository refreshRepository;
    private final TokenIssuer tokenIssuer;

    public LogoutService(RefreshTokenRevogadoRepository refreshRepository, TokenIssuer tokenIssuer) {
        this.refreshRepository = refreshRepository;
        this.tokenIssuer = tokenIssuer;
    }

    @Override
    public void executar(Input input) {
        TokenClaims claims = tokenIssuer.validarRefreshToken(input.refreshToken());

        String hash = TokenHashUtil.sha256Hex(input.refreshToken());
        if (refreshRepository.estaRevogado(hash)) {
            return;
        }
        refreshRepository.revogar(hash, claims.usuarioId(), claims.expiraEm());
    }
}
