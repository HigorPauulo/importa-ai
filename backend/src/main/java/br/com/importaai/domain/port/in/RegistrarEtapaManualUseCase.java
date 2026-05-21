package br.com.importaai.domain.port.in;

import br.com.importaai.domain.model.Pedido;
import br.com.importaai.domain.model.TipoEtapa;

public interface RegistrarEtapaManualUseCase {

    record Input(Long pedidoId, TipoEtapa tipoEtapa, String localizacao, String descricao) {}

    Pedido executar(Input input);
}
