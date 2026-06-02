package br.com.importaai.application.usecase;

import br.com.importaai.domain.exception.UsuarioNaoEncontradoException;
import br.com.importaai.domain.model.PerfilUsuario;
import br.com.importaai.domain.model.Usuario;
import br.com.importaai.domain.port.in.GerenciarUsuariosUseCase;
import br.com.importaai.domain.port.out.UsuarioRepository;

import java.util.List;

public class GerenciarUsuariosService implements GerenciarUsuariosUseCase {

    private final UsuarioRepository usuarioRepository;

    public GerenciarUsuariosService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<Usuario> listar() {
        return usuarioRepository.listarTodos();
    }

    @Override
    public Usuario alterarPerfil(Long id, PerfilUsuario perfil) {
        Usuario usuario = usuarioRepository.buscarPorId(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(id));
        return usuarioRepository.salvar(usuario.comPerfil(perfil));
    }

    @Override
    public Usuario definirStatus(Long id, boolean ativo) {
        Usuario usuario = usuarioRepository.buscarPorId(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(id));
        return usuarioRepository.salvar(usuario.comAtivo(ativo));
    }
}
