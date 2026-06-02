package br.com.importaai.domain.port.out;

import br.com.importaai.domain.model.ResultadoRastreio;

import java.time.Instant;

public interface RastreamentoCorreiosPort {

    ResultadoRastreio consultar(String codigoRastreamento, Instant pedidoCriadoEm);
}
