package br.com.importaai.infrastructure.adapter.out.persistence.repository;

import br.com.importaai.infrastructure.adapter.out.persistence.entity.PedidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PedidoJpaRepository extends JpaRepository<PedidoEntity, Long> {
    Optional<PedidoEntity> findByUsuarioIdAndCodigoRastreamento(Long usuarioId, String codigoRastreamento);

    List<PedidoEntity> findAllByUsuarioIdOrderByCriadoEmDesc(Long usuarioId);

    List<PedidoEntity> findByCanceladoFalse();
}
