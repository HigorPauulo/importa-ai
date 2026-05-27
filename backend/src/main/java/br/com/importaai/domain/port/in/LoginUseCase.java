package br.com.importaai.domain.port.in;

public interface LoginUseCase {

    record Input(String email, String senha) {}

    record Output(String accessToken, String refreshToken) {}

    Output executar(Input input);
}
