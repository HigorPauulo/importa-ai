package br.com.importaai.application.usecase;

import br.com.importaai.domain.exception.TokenInvalidoException;
import br.com.importaai.domain.model.TokenClaims;
import br.com.importaai.domain.model.Usuario;
import br.com.importaai.domain.port.in.RefreshTokenUseCase;
import br.com.importaai.domain.port.out.RefreshTokenRevogadoRepository;
import br.com.importaai.domain.port.out.TokenIssuer;
import br.com.importaai.domain.port.out.UsuarioRepository;

public class RefreshTokenService implements RefreshTokenUseCase {

    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRevogadoRepository refreshRepository;
    private final TokenIssuer tokenIssuer;

    public RefreshTokenService(UsuarioRepository usuarioRepository,
                               RefreshTokenRevogadoRepository refreshRepository,
                               TokenIssuer tokenIssuer) {
        this.usuarioRepository = usuarioRepository;
        this.refreshRepository = refreshRepository;
        this.tokenIssuer = tokenIssuer;
    }

    @Override
    public Output executar(Input input) {
        TokenClaims claims = tokenIssuer.validarRefreshToken(input.refreshToken());

        String hash = TokenHashUtil.sha256Hex(input.refreshToken());
        if (refreshRepository.estaRevogado(hash)) {
            throw new TokenInvalidoException("refresh token revogado");
        }

        Usuario usuario = usuarioRepository.buscarPorId(claims.usuarioId())
                .orElseThrow(() -> new TokenInvalidoException("usuario inexistente"));

        String accessToken = tokenIssuer.emitirAccessToken(
                usuario.getId(), usuario.getEmail(), usuario.getPerfil());

        return new Output(accessToken);
    }
}
