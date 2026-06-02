package br.com.importaai.infrastructure.adapter.out.external;

import br.com.importaai.domain.model.EtapaRastreamento;
import br.com.importaai.domain.model.ResultadoRastreio;
import br.com.importaai.domain.model.TipoEtapa;
import br.com.importaai.domain.port.out.RastreamentoCorreiosPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(name = "correios.adapter", havingValue = "stub", matchIfMissing = true)
public class CorreiosStubAdapter implements RastreamentoCorreiosPort {

    private record Marco(long horasMinimas, TipoEtapa tipo, String localizacao, String descricao) {}

    private static final List<Marco> LINHA_DO_TEMPO = List.of(
            new Marco(0, TipoEtapa.NA_CHINA, "Shenzhen, CN", "Objeto postado pelo remetente"),
            new Marco(24, TipoEtapa.AEROPORTO_ORIGEM, "Guangzhou Airport, CN", "Objeto recebido no aeroporto de origem"),
            new Marco(48, TipoEtapa.EM_TRANSITO, "Em transito internacional", "Objeto em transito internacional"),
            new Marco(96, TipoEtapa.AEROPORTO_DESTINO, "Aeroporto de Guarulhos, BR", "Objeto chegou ao pais de destino"),
            new Marco(120, TipoEtapa.NO_BRASIL, "Centro de Triagem, BR", "Objeto recebido pelos Correios no Brasil"),
            new Marco(168, TipoEtapa.CD_BRASIL, "CD Regional, BR", "Objeto em transito para o centro de distribuicao"),
            new Marco(192, TipoEtapa.SAIDA_ENTREGA, "Saiu para entrega", "Objeto saiu para entrega ao destinatario"),
            new Marco(216, TipoEtapa.ENTREGUE, "Entregue ao destinatario", "Objeto entregue ao destinatario")
    );

    @Override
    public ResultadoRastreio consultar(String codigoRastreamento, Instant pedidoCriadoEm) {
        long horas = Duration.between(pedidoCriadoEm, Instant.now()).toHours();

        List<EtapaRastreamento> etapas = new ArrayList<>();
        for (Marco marco : LINHA_DO_TEMPO) {
            if (horas >= marco.horasMinimas()) {
                Instant momento = pedidoCriadoEm.plus(Duration.ofHours(marco.horasMinimas()));
                etapas.add(new EtapaRastreamento(
                        marco.tipo(), momento, marco.localizacao(), marco.descricao()));
            }
        }
        return ResultadoRastreio.ok(etapas);
    }
}
