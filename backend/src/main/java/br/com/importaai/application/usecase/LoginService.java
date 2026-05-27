package br.com.importaai.application.usecase;

import br.com.importaai.domain.exception.CredenciaisInvalidasException;
import br.com.importaai.domain.exception.LoginBloqueadoException;
import br.com.importaai.domain.model.TentativaLoginFalha;
import br.com.importaai.domain.model.Usuario;
import br.com.importaai.domain.port.in.LoginUseCase;
import br.com.importaai.domain.port.out.PasswordHasher;
import br.com.importaai.domain.port.out.TentativaLoginFalhaRepository;
import br.com.importaai.domain.port.out.TokenIssuer;
import br.com.importaai.domain.port.out.UsuarioRepository;

import java.util.Optional;

public class LoginService implements LoginUseCase {

    private final UsuarioRepository usuarioRepository;
    private final TentativaLoginFalhaRepository tentativaRepository;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;

    public LoginService(UsuarioRepository usuarioRepository,
                        TentativaLoginFalhaRepository tentativaRepository,
                        PasswordHasher passwordHasher,
                        TokenIssuer tokenIssuer) {
        this.usuarioRepository = usuarioRepository;
        this.tentativaRepository = tentativaRepository;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
    }

    @Override
    public Output executar(Input input) {
        TentativaLoginFalha tentativa = tentativaRepository.buscarPorEmail(input.email())
                .orElse(new TentativaLoginFalha(input.email()));

        if (tentativa.estaBloqueado()) {
            throw new LoginBloqueadoException(tentativa.getBloqueadoAte());
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.buscarPorEmail(input.email());
        boolean senhaConfere = usuarioOpt
                .map(u -> passwordHasher.matches(input.senha(), u.getSenhaHash()))
                .orElse(false);

        if (!senhaConfere) {
            tentativa.registrarFalha();
            tentativaRepository.salvar(tentativa);
            throw new CredenciaisInvalidasException();
        }

        tentativa.zerar();
        tentativaRepository.salvar(tentativa);

        Usuario usuario = usuarioOpt.get();
        String accessToken = tokenIssuer.emitirAccessToken(
                usuario.getId(), usuario.getEmail(), usuario.getPerfil());
        String refreshToken = tokenIssuer.emitirRefreshToken(usuario.getId());

        return new Output(accessToken, refreshToken);
    }
}

