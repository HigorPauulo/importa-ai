package br.com.importaai.domain.model;

import br.com.importaai.domain.exception.EtapaRetroativaException;
import br.com.importaai.domain.exception.PedidoImutavelException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class Pedido {

    private static final Set<TipoEtapa> ETAPAS_NACIONAIS =
            EnumSet.of(TipoEtapa.NO_BRASIL, TipoEtapa.CD_BRASIL, TipoEtapa.SAIDA_ENTREGA);

    private final Long id;
    private final Long usuarioId;
    private final String codigoRastreamento;
    private final String descricao;
    private final BigDecimal valorDeclarado;
    private final Moeda moeda;
    private final Instant criadoEm;

    private final List<EtapaRastreamento> etapas;
    private boolean cancelado;
    private boolean rastreioNaoLocalizado;

    // Criação de pedido NOVO com valor declarado e moeda.
    public Pedido(Long usuarioId, String codigoRastreamento, String descricao,
                  BigDecimal valorDeclarado, Moeda moeda, Instant criadoEm) {
        this(null, usuarioId, codigoRastreamento, descricao, valorDeclarado, moeda,
                criadoEm, new ArrayList<>(), false);
    }

    // Compat: pedido sem valor/moeda (testes que nao exercitam dinheiro).
    public Pedido(Long usuarioId, String codigoRastreamento, String descricao, Instant criadoEm) {
        this(null, usuarioId, codigoRastreamento, descricao, null, null,
                criadoEm, new ArrayList<>(), false);
    }

    // Compat: reconstrucao sem valor/moeda (testes).
    public Pedido(Long id,
                  Long usuarioId,
                  String codigoRastreamento,
                  String descricao,
                  Instant criadoEm,
                  List<EtapaRastreamento> etapas,
                  boolean cancelado) {
        this(id, usuarioId, codigoRastreamento, descricao, null, null, criadoEm, etapas, cancelado);
    }

    // Construtor completo (reconstrucao a partir do banco).
    public Pedido(Long id,
                  Long usuarioId,
                  String codigoRastreamento,
                  String descricao,
                  BigDecimal valorDeclarado,
                  Moeda moeda,
                  Instant criadoEm,
                  List<EtapaRastreamento> etapas,
                  boolean cancelado) {
        this.id = id;
        this.usuarioId = Objects.requireNonNull(usuarioId, "usuarioId não pode ser nulo");
        this.codigoRastreamento = Objects.requireNonNull(codigoRastreamento, "codigoRastreamento não pode ser nulo");
        this.descricao = Objects.requireNonNull(descricao, "descricao não pode ser nula");
        this.valorDeclarado = valorDeclarado;
        this.moeda = moeda;
        this.criadoEm = Objects.requireNonNull(criadoEm, "criadoEm não pode ser nulo");
        this.etapas = new ArrayList<>(Objects.requireNonNull(etapas, "etapas não pode ser nula"));
        this.cancelado = cancelado;
    }
    public Long getId() { return id; }

    public String getCodigoRastreamento() {
        return codigoRastreamento;
    }

    public Long getUsuarioId() { return usuarioId; }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getValorDeclarado() {
        return valorDeclarado;
    }

    public Moeda getMoeda() {
        return moeda;
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

    public boolean isRastreioNaoLocalizado() {
        return rastreioNaoLocalizado;
    }

    public void marcarRastreioNaoLocalizado() {
        this.rastreioNaoLocalizado = true;
    }

    public void limparRastreioNaoLocalizado() {
        this.rastreioNaoLocalizado = false;
    }

    public Optional<EtapaRastreamento> ultimaEtapa() {
        return etapas.isEmpty() ? Optional.empty() : Optional.of(etapas.get(etapas.size() - 1));
    }

    public boolean estaEmEstadoNacional() {
        return !cancelado
                && ultimaEtapa().map(e -> ETAPAS_NACIONAIS.contains(e.tipo())).orElse(false);
    }

    public boolean temEtapaDoTipo(TipoEtapa tipo) {
        return etapas.stream().anyMatch(e -> e.tipo() == tipo);
    }
}
