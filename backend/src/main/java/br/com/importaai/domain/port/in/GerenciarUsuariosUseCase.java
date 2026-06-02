package br.com.importaai.domain.port.in;

import br.com.importaai.domain.model.PerfilUsuario;
import br.com.importaai.domain.model.Usuario;

import java.util.List;

public interface GerenciarUsuariosUseCase {

    List<Usuario> listar();

    Usuario alterarPerfil(Long id, PerfilUsuario perfil);

    Usuario definirStatus(Long id, boolean ativo);
}
