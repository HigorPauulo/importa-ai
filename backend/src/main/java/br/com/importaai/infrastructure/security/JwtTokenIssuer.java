package br.com.importaai.infrastructure.security;

import br.com.importaai.domain.exception.TokenInvalidoException;
import br.com.importaai.domain.model.PerfilUsuario;
import br.com.importaai.domain.model.TokenClaims;
import br.com.importaai.domain.port.out.TokenIssuer;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenIssuer implements TokenIssuer {

    private static final String CLAIM_TIPO = "tipo";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_PERFIL = "perfil";
    private static final String TIPO_ACCESS = "ACCESS";
    private static final String TIPO_REFRESH = "REFRESH";

    private final SecretKey key;
    private final Duration accessExpiration;
    private final Duration refreshExpiration;

    public JwtTokenIssuer(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration}") Duration accessExpiration,
            @Value("${jwt.refresh-expiration}") Duration refreshExpiration
    ) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("jwt.secret deve ter pelo menos 32 bytes para HS256");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    @Override
    public String emitirAccessToken(Long usuarioId, String email, PerfilUsuario perfil) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(usuarioId.toString())
                .claim(CLAIM_EMAIL, email)
                .claim(CLAIM_PERFIL, perfil.name())
                .claim(CLAIM_TIPO, TIPO_ACCESS)
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plus(accessExpiration)))
                .signWith(key)
                .compact();
    }

    @Override
    public String emitirRefreshToken(Long usuarioId) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(usuarioId.toString())
                .claim(CLAIM_TIPO, TIPO_REFRESH)
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plus(refreshExpiration)))
                .signWith(key)
                .compact();
    }

    @Override
    public TokenClaims validarAccessToken(String token) {
        Claims claims = parseAndValidate(token);
        exigirTipo(claims, TIPO_ACCESS);
        return new TokenClaims(
                Long.valueOf(claims.getSubject()),
                claims.get(CLAIM_EMAIL, String.class),
                PerfilUsuario.valueOf(claims.get(CLAIM_PERFIL, String.class)),
                claims.getExpiration().toInstant()
        );
    }

    @Override
    public TokenClaims validarRefreshToken(String token) {
        Claims claims = parseAndValidate(token);
        exigirTipo(claims, TIPO_REFRESH);
        return new TokenClaims(
                Long.valueOf(claims.getSubject()),
                null,
                null,
                claims.getExpiration().toInstant()
        );
    }

    private Claims parseAndValidate(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new TokenInvalidoException("token invalido: " + e.getClass().getSimpleName());
        }
    }

    private void exigirTipo(Claims claims, String tipoEsperado) {
        String tipo = claims.get(CLAIM_TIPO, String.class);
        if (!tipoEsperado.equals(tipo)) {
            throw new TokenInvalidoException("tipo de token incorreto (esperado " + tipoEsperado + ", veio " +
                    tipo + ")");
        }
    }
}
