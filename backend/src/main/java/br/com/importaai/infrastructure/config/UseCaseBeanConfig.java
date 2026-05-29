package br.com.importaai.infrastructure.config;

import br.com.importaai.application.usecase.BuscarPedidoService;
import br.com.importaai.application.usecase.CancelarPedidoService;
import br.com.importaai.application.usecase.CriarPedidoService;
import br.com.importaai.application.usecase.RegistrarEtapaManualService;
import br.com.importaai.domain.port.in.BuscarPedidoUseCase;
import br.com.importaai.domain.port.in.CancelarPedidoUseCase;
import br.com.importaai.domain.port.in.CriarPedidoUseCase;
import br.com.importaai.domain.port.in.RegistrarEtapaManualUseCase;
import br.com.importaai.domain.port.out.EventPublisher;
import br.com.importaai.domain.port.out.PedidoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.importaai.application.usecase.LoginService;
import br.com.importaai.application.usecase.LogoutService;
import br.com.importaai.application.usecase.RefreshTokenService;
import br.com.importaai.application.usecase.RegistrarUsuarioService;
import br.com.importaai.domain.port.in.LoginUseCase;
import br.com.importaai.domain.port.in.LogoutUseCase;
import br.com.importaai.domain.port.in.RefreshTokenUseCase;
import br.com.importaai.domain.port.in.RegistrarUsuarioUseCase;
import br.com.importaai.domain.port.out.PasswordHasher;
import br.com.importaai.domain.port.out.RefreshTokenRevogadoRepository;
import br.com.importaai.domain.port.out.TentativaLoginFalhaRepository;
import br.com.importaai.domain.port.out.TokenIssuer;
import br.com.importaai.domain.port.out.UsuarioRepository;

import br.com.importaai.application.usecase.PersistirNotificacaoService;
import br.com.importaai.domain.port.in.PersistirNotificacaoUseCase;
import br.com.importaai.domain.port.out.NotificacaoRepository;


@Configuration
public class UseCaseBeanConfig {

    @Bean
    public CriarPedidoUseCase criarPedidoUseCase(
            PedidoRepository pedidoRepository,
            EventPublisher eventPublisher) {
        return new CriarPedidoService(pedidoRepository, eventPublisher);
    }

    @Bean
    public BuscarPedidoUseCase buscarPedidoUseCase(PedidoRepository pedidoRepository) {
        return new BuscarPedidoService(pedidoRepository);
    }

    @Bean
    public RegistrarEtapaManualUseCase registrarEtapaManualUseCase(
            PedidoRepository pedidoRepository,
            EventPublisher eventPublisher) {
        return new RegistrarEtapaManualService(pedidoRepository, eventPublisher);
    }

    @Bean
    public CancelarPedidoUseCase cancelarPedidoUseCase(
            PedidoRepository pedidoRepository,
            EventPublisher eventPublisher) {
        return new CancelarPedidoService(pedidoRepository, eventPublisher);
    }

    @Bean
    public RegistrarUsuarioUseCase registrarUsuarioUseCase(
            UsuarioRepository usuarioRepository,
            PasswordHasher passwordHasher) {
        return new RegistrarUsuarioService(usuarioRepository, passwordHasher);
    }

    @Bean
    public LoginUseCase loginUseCase(
            UsuarioRepository usuarioRepository,
            TentativaLoginFalhaRepository tentativaRepository,
            PasswordHasher passwordHasher,
            TokenIssuer tokenIssuer) {
        return new LoginService(usuarioRepository, tentativaRepository, passwordHasher, tokenIssuer);
    }

    @Bean
    public RefreshTokenUseCase refreshTokenUseCase(
            UsuarioRepository usuarioRepository,
            RefreshTokenRevogadoRepository refreshRepository,
            TokenIssuer tokenIssuer) {
        return new RefreshTokenService(usuarioRepository, refreshRepository, tokenIssuer);
    }

    @Bean
    public LogoutUseCase logoutUseCase(
            RefreshTokenRevogadoRepository refreshRepository,
            TokenIssuer tokenIssuer) {
        return new LogoutService(refreshRepository, tokenIssuer);
    }

    @Bean
    public PersistirNotificacaoUseCase persistirNotificacaoUseCase(
            NotificacaoRepository notificacaoRepository) {
        return new PersistirNotificacaoService(notificacaoRepository);
    }
}
