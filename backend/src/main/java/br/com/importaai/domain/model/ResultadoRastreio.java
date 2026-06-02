package br.com.importaai.domain.model;

import java.util.List;

// Resultado de uma consulta de rastreamento: distingue "achou", "transportadora nao
// conhece o codigo" e "fonte fora do ar" — cada um leva a uma reacao diferente no sync.
public record ResultadoRastreio(Situacao situacao, List<EtapaRastreamento> etapas) {

    public enum Situacao { OK, NAO_LOCALIZADO, FONTE_INDISPONIVEL }

    public ResultadoRastreio {
        etapas = etapas == null ? List.of() : List.copyOf(etapas);
    }

    public static ResultadoRastreio ok(List<EtapaRastreamento> etapas) {
        return new ResultadoRastreio(Situacao.OK, etapas);
    }

    public static ResultadoRastreio naoLocalizado() {
        return new ResultadoRastreio(Situacao.NAO_LOCALIZADO, List.of());
    }

    public static ResultadoRastreio indisponivel(List<EtapaRastreamento> cache) {
        return new ResultadoRastreio(Situacao.FONTE_INDISPONIVEL, cache);
    }
}
