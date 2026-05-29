package br.com.importaai.infrastructure.adapter.in.rest.dto;

import br.com.importaai.domain.model.Moeda;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CriarPedidoRequest(
        // @NotBlank rejeita: null, "", "   ". @NotNull aceitaria "" — nao queremos.
        @NotBlank(message = "codigoRastreamento e obrigatorio")
        @Size(max = 50, message = "codigoRastreamento deve ter no maximo 50 caracteres")
        String codigoRastreamento,

        @NotBlank(message = "descriçao e obrigatoria")
        @Size(max = 255, message = "descriçao deve ter no maximo 255 caracteres")
        String descricao,

        @NotNull(message = "valorDeclarado e obrigatorio")
        @Positive(message = "valorDeclarado deve ser positivo")
        BigDecimal valorDeclarado,

        @NotNull(message = "moeda e obrigatoria")
        Moeda moeda

) {}
