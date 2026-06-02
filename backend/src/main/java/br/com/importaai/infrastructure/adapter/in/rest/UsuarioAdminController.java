package br.com.importaai.infrastructure.adapter.in.rest;

import br.com.importaai.domain.port.in.GerenciarUsuariosUseCase;
import br.com.importaai.infrastructure.adapter.in.rest.dto.AlterarPerfilRequest;
import br.com.importaai.infrastructure.adapter.in.rest.dto.AlterarStatusRequest;
import br.com.importaai.infrastructure.adapter.in.rest.dto.UsuarioAdminResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/usuarios")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class UsuarioAdminController {

    private final GerenciarUsuariosUseCase gerenciarUsuarios;

    public UsuarioAdminController(GerenciarUsuariosUseCase gerenciarUsuarios) {
        this.gerenciarUsuarios = gerenciarUsuarios;
    }

    @GetMapping
    public List<UsuarioAdminResponse> listar() {
        return gerenciarUsuarios.listar().stream()
                .map(UsuarioAdminResponse::from)
                .toList();
    }

    @PatchMapping("/{id}/perfil")
    public UsuarioAdminResponse alterarPerfil(@PathVariable Long id, @Valid @RequestBody AlterarPerfilRequest body) {
        return UsuarioAdminResponse.from(gerenciarUsuarios.alterarPerfil(id, body.perfil()));
    }

    @PatchMapping("/{id}/status")
    public UsuarioAdminResponse alterarStatus(@PathVariable Long id, @Valid @RequestBody AlterarStatusRequest body) {
        return UsuarioAdminResponse.from(gerenciarUsuarios.definirStatus(id, body.ativo()));
    }
}
