package br.com.importaai.infrastructure.adapter.in.rest.dto;

import br.com.importaai.domain.model.TipoEtapa;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistrarEtapaRequest(

        @NotNull(message = "tipoEtapa e obrigatorio")
        TipoEtapa tipoEtapa,

        @Size(max = 255, message = "localizacao deve ter no maximo 255 caracteres")
        String localizacao,

        @Size(max = 255, message = "descricao deve ter no maximo 255 caracteres")
        String descricao

) {}
