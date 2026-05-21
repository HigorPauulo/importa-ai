package br.com.importaai.domain.model;

import br.com.importaai.domain.exception.EtapaRetroativaException;
import br.com.importaai.domain.exception.PedidoImutavelException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Pedido {

    private final String codigoRastreamento;
    private final String descricao;
    private final Instant criadoEm;

    private final List<EtapaRastreamento> etapas;

    private boolean cancelado;

    public Pedido(String codigoRastreamento, String descricao, Instant criadoEm) {
        this.codigoRastreamento = Objects.requireNonNull(codigoRastreamento, "codigoRastreamento nao pode ser nulo");
        this.descricao =  Objects.requireNonNull(descricao, "descricao nao pode ser nula");
        this.criadoEm = Objects.requireNonNull(criadoEm, "criadoEm nao pode ser nulo");
        this.etapas = new ArrayList<>();
        this.cancelado = false;
    }

    public String getCodigoRastreamento() {
        return codigoRastreamento;
    }

    public String getDescricao() {
        return descricao;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public List<EtapaRastreamento> getEtapas() {
        return Collections.unmodifiableList(etapas);
    }

    public boolean isCancelado() {
        return cancelado;
    }

    public StatusPedido getStatus() {
        Optional<TipoEtapa> ultima = etapas.isEmpty()
                ? Optional.empty()
                : Optional.of(etapas.get(etapas.size() - 1).tipo());
        return StatusPedido.derivar(ultima, cancelado);
    }

    public void adicionarEtapa(EtapaRastreamento etapa) {
        Objects.requireNonNull(etapa, "etapa nao pode ser nula");
        StatusPedido statusAtual = getStatus();
        if (statusAtual == StatusPedido.ENTREGUE || statusAtual == StatusPedido.CANCELADO) {
            throw new PedidoImutavelException(
                    "pedido com status " + statusAtual + " nao aceita novas etapas"
            );
        }

        if (!etapas.isEmpty()) {
            Instant ultimoTimeTamp = etapas.get(etapas.size() - 1).criadoEm();
            if (etapa.criadoEm().isBefore(ultimoTimeTamp)) {
                throw new EtapaRetroativaException(
                        "etapa em" + etapa.criadoEm() + " e anterior a ultima (" + ultimoTimeTamp + ")");
            }
        }

        etapas.add(etapa);
    }

    public void cancelar() {
        this.cancelado = true;
    }
}
