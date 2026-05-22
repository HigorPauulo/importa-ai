package br.com.importaai.infrastructure.adapter.in.rest;

import br.com.importaai.domain.model.Pedido;
import br.com.importaai.domain.port.in.BuscarPedidoUseCase;
import br.com.importaai.domain.port.in.CancelarPedidoUseCase;
import br.com.importaai.domain.port.in.CriarPedidoUseCase;
import br.com.importaai.domain.port.in.RegistrarEtapaManualUseCase;
import br.com.importaai.domain.port.out.PedidoRepository;
import br.com.importaai.infrastructure.adapter.in.rest.dto.CriarPedidoRequest;
import br.com.importaai.infrastructure.adapter.in.rest.dto.PedidoResponse;
import br.com.importaai.infrastructure.adapter.in.rest.dto.RegistrarEtapaRequest;
import br.com.importaai.infrastructure.adapter.in.rest.mapper.PedidoResponseMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final CriarPedidoUseCase criarPedido;
    private final BuscarPedidoUseCase buscarPedido;
    private final RegistrarEtapaManualUseCase registrarEtapa;
    private final CancelarPedidoUseCase cancelarPedido;
    private final PedidoRepository pedidoRepository;

    public PedidoController(CriarPedidoUseCase criarPedido,
                            BuscarPedidoUseCase buscarPedido,
                            RegistrarEtapaManualUseCase registrarEtapa,
                            CancelarPedidoUseCase cancelarPedido,
                            PedidoRepository pedidoRepository) {
        this.criarPedido = criarPedido;
        this.buscarPedido = buscarPedido;
        this.registrarEtapa = registrarEtapa;
        this.cancelarPedido = cancelarPedido;
        this.pedidoRepository = pedidoRepository;
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> criar(
            @RequestHeader("X-User-Id") Long usuarioId,
            @Valid @RequestBody CriarPedidoRequest body) {

        Pedido pedido = criarPedido.executar(new CriarPedidoUseCase.Input(
                usuarioId,
                body.codigoRastreamento(),
                body.descricao()
        ));

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(pedido.getId())
                .toUri();

        return ResponseEntity
                .accepted()
                .location(location)
                .body(PedidoResponseMapper.toResponse(pedido));
    }

    @GetMapping
    public List<PedidoResponse> listar(@RequestHeader("X-User-Id") Long usuarioId) {
        return pedidoRepository.listarPorUsuario(usuarioId).stream()
                .map(PedidoResponseMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public PedidoResponse buscar(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long usuarioId) {

        Pedido pedido = buscarPedido.executar(
                new BuscarPedidoUseCase.Input(id, usuarioId)
        );
        return PedidoResponseMapper.toResponse(pedido);
    }

    @PostMapping("/{id}/etapas")
    public ResponseEntity<PedidoResponse> registrarEtapa(
            @PathVariable Long id,
            @Valid @RequestBody RegistrarEtapaRequest body) {

        Pedido atualizado = registrarEtapa.executar(new RegistrarEtapaManualUseCase.Input(
                id,
                body.tipoEtapa(),
                body.localizacao(),
                body.descricao()
        ));

        return ResponseEntity
                .accepted()
                .body(PedidoResponseMapper.toResponse(atualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<PedidoResponse> cancelar(@PathVariable Long id) {
        Pedido cancelado = cancelarPedido.executar(
                new CancelarPedidoUseCase.Input(id)
        );
        return ResponseEntity
                .accepted()
                .body(PedidoResponseMapper.toResponse(cancelado));
    }
}
