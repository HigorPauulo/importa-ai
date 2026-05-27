package br.com.importaai.domain.port.in;

import br.com.importaai.domain.model.PerfilUsuario;
import br.com.importaai.domain.model.Usuario;

public interface RegistrarUsuarioUseCase {

    record Input(String nome, String email, String senha, PerfilUsuario perfil) {}

    Usuario executar(Input input);
}