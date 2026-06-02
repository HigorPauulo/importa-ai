package br.com.importaai.application.usecase;

import br.com.importaai.domain.exception.CodigoRastreamentoDuplicadoException;
import br.com.importaai.domain.exception.CodigoRastreamentoInvalidoException;
import br.com.importaai.domain.model.Pedido;
import br.com.importaai.domain.port.in.CriarPedidoUseCase;
import br.com.importaai.domain.port.out.EventPublisher;
import br.com.importaai.domain.port.out.PedidoRepository;

import java.time.Instant;
import java.util.regex.Pattern;

public class CriarPedidoService implements CriarPedidoUseCase {

    private static final Pattern CODIGO_VALIDO = Pattern.compile("[A-Z0-9]{8,40}");

    private final PedidoRepository pedidoRepository;
    private final EventPublisher eventPublisher;

    public CriarPedidoService(PedidoRepository pedidoRepository, EventPublisher eventPublisher) {
        this.pedidoRepository = pedidoRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Pedido executar(Input input) {
        String codigo = normalizarCodigo(input.codigoRastreamento());

        pedidoRepository.buscarPorCodigoRastreamentoEUsuario(codigo, input.usuarioId())
                .ifPresent(p -> {
                    throw new CodigoRastreamentoDuplicadoException(
                            "usuario ja possui pedido com codigo " + codigo);
                });

        Pedido pedido = new Pedido(input.usuarioId(), codigo, input.descricao(),
                input.valorDeclarado(), input.moeda(), Instant.now());
        Pedido salvo = pedidoRepository.salvar(pedido);

        eventPublisher.publicar("pedido.criado", salvo);

        return salvo;
    }

    private static String normalizarCodigo(String bruto) {
        String codigo = bruto == null ? "" : bruto.replaceAll("\\s+", "").toUpperCase();
        if (!CODIGO_VALIDO.matcher(codigo).matches()) {
            throw new CodigoRastreamentoInvalidoException(
                    "codigo de rastreamento invalido: use 8 a 40 letras ou numeros, sem simbolos");
        }
        return codigo;
    }
}
