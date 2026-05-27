package br.com.importaai.infrastructure.adapter.in.rest;

import br.com.importaai.domain.model.PerfilUsuario;
import br.com.importaai.domain.model.Usuario;
import br.com.importaai.domain.port.in.LoginUseCase;
import br.com.importaai.domain.port.in.LogoutUseCase;
import br.com.importaai.domain.port.in.RefreshTokenUseCase;
import br.com.importaai.domain.port.in.RegistrarUsuarioUseCase;
import br.com.importaai.infrastructure.adapter.in.rest.dto.LoginRequest;
import br.com.importaai.infrastructure.adapter.in.rest.dto.LoginResponse;
import br.com.importaai.infrastructure.adapter.in.rest.dto.RefreshResponse;
import br.com.importaai.infrastructure.adapter.in.rest.dto.RegisterRequest;
import br.com.importaai.infrastructure.adapter.in.rest.dto.TokenInputRequest;
import br.com.importaai.infrastructure.adapter.in.rest.dto.UsuarioResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;

    public AuthController(RegistrarUsuarioUseCase registrarUsuarioUseCase,
                          LoginUseCase loginUseCase,
                          RefreshTokenUseCase refreshTokenUseCase,
                          LogoutUseCase logoutUseCase) {
        this.registrarUsuarioUseCase = registrarUsuarioUseCase;
        this.loginUseCase = loginUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.logoutUseCase = logoutUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponse> register(@Valid @RequestBody RegisterRequest body) {
        Usuario u = registrarUsuarioUseCase.executar(new RegistrarUsuarioUseCase.Input(
                body.nome(),
                body.email(),
                body.senha(),
                PerfilUsuario.CLIENTE
        ));
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getPerfil()));
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest body) {
        LoginUseCase.Output out = loginUseCase.executar(
                new LoginUseCase.Input(body.email(), body.senha()));
        return new LoginResponse(out.accessToken(), out.refreshToken());
    }

    @PostMapping("/refresh")
    public RefreshResponse refresh(@Valid @RequestBody TokenInputRequest body) {
        RefreshTokenUseCase.Output out = refreshTokenUseCase.executar(
                new RefreshTokenUseCase.Input(body.refreshToken()));
        return new RefreshResponse(out.accessToken());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody TokenInputRequest body) {
        logoutUseCase.executar(new LogoutUseCase.Input(body.refreshToken()));
        return ResponseEntity.noContent().build();
    }
}