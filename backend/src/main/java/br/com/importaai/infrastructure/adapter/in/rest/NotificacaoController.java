package br.com.importaai.infrastructure.adapter.in.rest;

import br.com.importaai.domain.port.out.NotificacaoRepository;
import br.com.importaai.infrastructure.adapter.in.rest.dto.NotificacaoResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {

    private final NotificacaoRepository notificacaoRepository;

    public NotificacaoController(NotificacaoRepository notificacaoRepository) {
        this.notificacaoRepository = notificacaoRepository;
    }

    @GetMapping
    public List<NotificacaoResponse> listar(@AuthenticationPrincipal Long usuarioId) {
        return notificacaoRepository.listarPorUsuario(usuarioId).stream()
                .map(NotificacaoResponse::from)
                .toList();
    }

    @PatchMapping("/lidas")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void marcarTodasComoLidas(@AuthenticationPrincipal Long usuarioId) {
        notificacaoRepository.marcarTodasComoLidas(usuarioId);
    }
}
