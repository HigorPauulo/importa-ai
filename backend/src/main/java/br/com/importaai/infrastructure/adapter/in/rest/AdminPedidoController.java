package br.com.importaai.infrastructure.adapter.in.rest;

import br.com.importaai.domain.exception.PedidoNaoEncontradoException;
import br.com.importaai.domain.port.out.PedidoRepository;
import br.com.importaai.infrastructure.adapter.in.rest.dto.PedidoResponse;
import br.com.importaai.infrastructure.adapter.in.rest.mapper.PedidoResponseMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/pedidos")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminPedidoController {

    private final PedidoRepository pedidoRepository;

    public AdminPedidoController(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @GetMapping
    public List<PedidoResponse> listar() {
        return pedidoRepository.listarTodos().stream()
                .map(PedidoResponseMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public PedidoResponse buscar(@PathVariable Long id) {
        return pedidoRepository.buscarPorId(id)
                .map(PedidoResponseMapper::toResponse)
                .orElseThrow(() -> new PedidoNaoEncontradoException(id));
    }
}
