package br.com.importaai.application.usecase;

import br.com.importaai.domain.exception.EmailJaCadastradoException;
import br.com.importaai.domain.exception.UsuarioNaoEncontradoException;
import br.com.importaai.domain.model.Usuario;
import br.com.importaai.domain.port.in.EditarPerfilUseCase;
import br.com.importaai.domain.port.out.PasswordHasher;
import br.com.importaai.domain.port.out.UsuarioRepository;

public class EditarPerfilService implements EditarPerfilUseCase {

    private final UsuarioRepository usuarioRepository;
    private final PasswordHasher passwordHasher;

    public EditarPerfilService(UsuarioRepository usuarioRepository, PasswordHasher passwordHasher) {
        this.usuarioRepository = usuarioRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public Usuario executar(Input input) {
        Usuario atual = usuarioRepository.buscarPorId(input.usuarioId())
                .orElseThrow(() -> new UsuarioNaoEncontradoException(input.usuarioId()));

        Usuario atualizado = atual;

        if (input.nome() != null && !input.nome().isBlank()) {
            atualizado = atualizado.comNome(input.nome());
        }

        if (input.email() != null && !input.email().isBlank()
                && !input.email().equalsIgnoreCase(atual.getEmail())) {
            usuarioRepository.buscarPorEmail(input.email()).ifPresent(u -> {
                throw new EmailJaCadastradoException(input.email());
            });
            atualizado = atualizado.comEmail(input.email());
        }

        if (input.novaSenha() != null && !input.novaSenha().isBlank()) {
            atualizado = atualizado.comSenhaHash(passwordHasher.hash(input.novaSenha()));
        }

        return usuarioRepository.salvar(atualizado);
    }
}
