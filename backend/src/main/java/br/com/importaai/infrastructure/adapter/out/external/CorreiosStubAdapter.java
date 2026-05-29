package br.com.importaai.infrastructure.adapter.out.external;

import br.com.importaai.domain.model.EtapaRastreamento;
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

    private record Marco(long horasMinimas, TipoEtapa tipo, String localizacao) {}

    private static final List<Marco> LINHA_DO_TEMPO = List.of(
            new Marco(0, TipoEtapa.NA_CHINA, "Shenzhen, CN"),
            new Marco(24, TipoEtapa.AEROPORTO_ORIGEM, "Guangzhou Airport, CN"),
            new Marco(48, TipoEtapa.EM_TRANSITO, "Em transito internacional"),
            new Marco(96, TipoEtapa.AEROPORTO_DESTINO, "Aeroporto de Guarulhos, BR"),
            new Marco(120, TipoEtapa.NO_BRASIL, "Centro de Triagem, BR"),
            new Marco(168, TipoEtapa.CD_BRASIL, "CD Regional, BR"),
            new Marco(192, TipoEtapa.SAIDA_ENTREGA, "Saiu para entrega"),
            new Marco(216, TipoEtapa.ENTREGUE, "Entregue ao destinatario")
    );

    @Override
    public List<EtapaRastreamento> consultar(String codigoRastreamento, Instant pedidoCriadoEm) {
        long horas = Duration.between(pedidoCriadoEm, Instant.now()).toHours();

        List<EtapaRastreamento> etapas = new ArrayList<>();
        for (Marco marco : LINHA_DO_TEMPO) {
            if (horas >= marco.horasMinimas()) {
                Instant momento = pedidoCriadoEm.plus(Duration.ofHours(marco.horasMinimas()));
                etapas.add(new EtapaRastreamento(
                        marco.tipo(), momento, marco.localizacao(),
                        "Etapa sintetica (stub) para " + codigoRastreamento));
            }
        }
        return etapas;
    }
}
