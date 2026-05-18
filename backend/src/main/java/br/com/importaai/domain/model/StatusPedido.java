package br.com.importaai.domain.model;

import java.util.Optional;

public enum StatusPedido {
    PROCESSANDO,
    ENVIADO,
    ENTREGUE,
    CANCELADO;

    public static StatusPedido derivar(Optional<TipoEtapa> ultimaEtapa, boolean cancelado) {
        if (cancelado) {
            return CANCELADO;
        }
        if (ultimaEtapa.isEmpty()) {
            return PROCESSANDO;
        }
        return switch (ultimaEtapa.get()) {
            case NA_CHINA -> PROCESSANDO;
            case ENTREGUE -> ENTREGUE;
            case AEROPORTO_ORIGEM, EM_TRANSITO, AEROPORTO_DESTINO,
                 NO_BRASIL, TAXA, CD_BRASIL, SAIDA_ENTREGA -> ENVIADO;
        };
    }
}
