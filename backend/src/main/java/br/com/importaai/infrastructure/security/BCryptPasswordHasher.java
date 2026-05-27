package br.com.importaai.infrastructure.security;

import br.com.importaai.domain.port.out.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordHasher implements PasswordHasher {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Override
    public String hash(String senhaEmClaro) {
        return encoder.encode(senhaEmClaro);
    }

    @Override
    public boolean matches(String senhaEmClaro, String senhaHash) {
        return encoder.matches(senhaEmClaro, senhaHash);
    }
}
