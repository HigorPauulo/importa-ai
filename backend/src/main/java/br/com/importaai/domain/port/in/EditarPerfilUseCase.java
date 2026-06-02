package br.com.importaai.domain.port.in;

import br.com.importaai.domain.model.Usuario;

public interface EditarPerfilUseCase {

    record Input(Long usuarioId, String nome, String email, String novaSenha) {}

    Usuario executar(Input input);
}
