package br.com.importaai.domain.port.out;

import br.com.importaai.domain.model.TentativaLoginFalha;

import java.util.Optional;

public interface TentativaLoginFalhaRepository {

    Optional<TentativaLoginFalha> buscarPorEmail(String email);

    void salvar(TentativaLoginFalha tentativa);
}
