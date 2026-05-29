package br.com.importaai.domain.port.out;

import br.com.importaai.domain.model.EtapaRastreamento;

import java.time.Instant;
import java.util.List;

public interface RastreamentoCorreiosPort {

    List<EtapaRastreamento> consultar(String codigoRastreamento, Instant pedidoCriadoEm);
}
