package br.com.importaai.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

public class MoedaTest {

    @Test
    @DisplayName("expõe as 4 moedas suportadas")
    void exposeAs4Moedas() {
        assertThat(Moeda.values()).containsExactly(
                Moeda.BRL, Moeda.USD, Moeda.EUR, Moeda.CNY
        );
    }

    @ParameterizedTest(name = "{0} -> simbolo {1}")
    @CsvSource({
            "BRL, R$",
            "USD, $",
            "EUR, €",
            "CNY, ¥"
    })
    @DisplayName("cada moeda expõe seu símbolo correto")
    void cadaMoedaExpoeSimboloCorreto(Moeda moeda, String simboloEsperado) {
        assertThat(moeda.getSimbolo()).isEqualTo(simboloEsperado);
    }
}