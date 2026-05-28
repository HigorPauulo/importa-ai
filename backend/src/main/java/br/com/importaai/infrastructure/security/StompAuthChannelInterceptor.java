package br.com.importaai.infrastructure.security;

import br.com.importaai.domain.exception.TokenInvalidoException;
import br.com.importaai.domain.model.TokenClaims;
import br.com.importaai.domain.port.out.TokenIssuer;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenIssuer tokenIssuer;

    public StompAuthChannelInterceptor(TokenIssuer tokenIssuer) {
        this.tokenIssuer = tokenIssuer;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        String header = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            throw new TokenInvalidoException("Authorization header ausente no CONNECT");
        }

        String token = header.substring(BEARER_PREFIX.length());
        TokenClaims claims = tokenIssuer.validarAccessToken(token);

        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + claims.perfil().name())
        );
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(claims.usuarioId(), null, authorities);

        accessor.setUser(auth);

        return message;
    }
}
