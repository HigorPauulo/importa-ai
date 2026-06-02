package br.com.importaai.infrastructure.adapter.in.rest;

import br.com.importaai.domain.exception.UsuarioNaoEncontradoException;
import br.com.importaai.domain.model.Usuario;
import br.com.importaai.domain.port.in.EditarPerfilUseCase;
import br.com.importaai.domain.port.out.UsuarioRepository;
import br.com.importaai.infrastructure.adapter.in.rest.dto.AtualizarPerfilRequest;
import br.com.importaai.infrastructure.adapter.in.rest.dto.UsuarioResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class PerfilController {

    private final UsuarioRepository usuarioRepository;
    private final EditarPerfilUseCase editarPerfil;

    public PerfilController(UsuarioRepository usuarioRepository, EditarPerfilUseCase editarPerfil) {
        this.usuarioRepository = usuarioRepository;
        this.editarPerfil = editarPerfil;
    }

    @GetMapping
    public UsuarioResponse meuPerfil(@AuthenticationPrincipal Long usuarioId) {
        Usuario u = usuarioRepository.buscarPorId(usuarioId)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(usuarioId));
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getPerfil());
    }

    @PatchMapping
    public UsuarioResponse atualizar(@AuthenticationPrincipal Long usuarioId,
                                     @Valid @RequestBody AtualizarPerfilRequest body) {
        Usuario u = editarPerfil.executar(new EditarPerfilUseCase.Input(
                usuarioId, body.nome(), body.email(), body.senha()));
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getPerfil());
    }
}
