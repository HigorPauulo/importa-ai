package br.com.importaai.domain.port.out;

public interface PasswordHasher {

    String hash(String senhaEmClaro);

    boolean matches(String senhaEmClaro, String senhaHash);
}
