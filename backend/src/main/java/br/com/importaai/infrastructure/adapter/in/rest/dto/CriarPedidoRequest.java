package br.com.importaai.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarPedidoRequest(
        // @NotBlank rejeita: null, "", "   ". @NotNull aceitaria "" — nao queremos.
        @NotBlank(message = "codigoRastreamento e obrigatorio")
        @Size(max = 50, message = "codigoRastreamento deve ter no maximo 50 caracteres")
        String codigoRastreamento,

        @NotBlank(message = "descriçao e obrigatoria")
        @Size(max = 255, message = "descriçao deve ter no maximo 255 caracteres")
        String descricao

) {}
