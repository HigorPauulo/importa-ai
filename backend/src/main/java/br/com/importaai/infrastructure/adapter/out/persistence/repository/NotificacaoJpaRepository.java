package br.com.importaai.infrastructure.adapter.out.persistence.repository;

import br.com.importaai.infrastructure.adapter.out.persistence.entity.NotificacaoEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificacaoJpaRepository extends JpaRepository<NotificacaoEntity, Long> {

    List<NotificacaoEntity> findByUsuarioIdOrderByCriadoEmDesc(Long usuarioId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u.id FROM UsuarioEntity u WHERE u.id = :usuarioId")
    Optional<Long> lockUsuario(@Param("usuarioId") Long usuarioId);

    @Modifying
    @Query("DELETE FROM NotificacaoEntity n WHERE n.usuarioId = :usuarioId AND n.id NOT IN :idsManter")
    int deleteByUsuarioIdAndIdNotIn(@Param("usuarioId") Long usuarioId, @Param("idsManter") List<Long> idsManter);

    @Modifying
    @Query("UPDATE NotificacaoEntity n SET n.lida = true WHERE n.usuarioId = :usuarioId AND n.lida = false")
    int marcarTodasComoLidas(@Param("usuarioId") Long usuarioId);
}
