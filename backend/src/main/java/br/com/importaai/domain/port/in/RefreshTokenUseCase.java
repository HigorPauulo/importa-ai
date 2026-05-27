package br.com.importaai.domain.port.in;

public interface RefreshTokenUseCase {

    record Input(String refreshToken) {}

    record Output(String accessToken) {}

    Output executar(Input input);
}