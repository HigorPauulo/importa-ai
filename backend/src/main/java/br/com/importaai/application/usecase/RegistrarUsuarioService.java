package br.com.importaai.application.usecase;

import br.com.importaai.domain.exception.EmailJaCadastradoException;
import br.com.importaai.domain.model.Usuario;
import br.com.importaai.domain.port.in.RegistrarUsuarioUseCase;
import br.com.importaai.domain.port.out.PasswordHasher;
import br.com.importaai.domain.port.out.UsuarioRepository;

import java.time.Instant;

public class RegistrarUsuarioService implements RegistrarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final PasswordHasher passwordHasher;

    public RegistrarUsuarioService(UsuarioRepository usuarioRepository, PasswordHasher passwordHasher) {
        this.usuarioRepository = usuarioRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public Usuario executar(Input input) {
        usuarioRepository.buscarPorEmail(input.email()).ifPresent(u -> {
            throw new EmailJaCadastradoException(input.email());
        });

        String senhaHash = passwordHasher.hash(input.senha());
        Usuario novo = new Usuario(
                input.nome(),
                input.email(),
                senhaHash,
                input.perfil(),
                Instant.now()
        );
        return usuarioRepository.salvar(novo);
    }
}
