package br.com.importaai.domain.port.in;

public interface LogoutUseCase {

    record Input(String refreshToken) {}

    void executar(Input input);
}