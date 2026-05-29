package br.com.importaai.infrastructure.adapter.out.persistence;

import br.com.importaai.domain.model.Notificacao;
import br.com.importaai.domain.port.out.NotificacaoRepository;
import br.com.importaai.infrastructure.adapter.out.persistence.entity.NotificacaoEntity;
import br.com.importaai.infrastructure.adapter.out.persistence.mapper.NotificacaoEntityMapper;
import br.com.importaai.infrastructure.adapter.out.persistence.repository.NotificacaoJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class NotificacaoRepositoryJpaAdapter implements NotificacaoRepository {

    private final NotificacaoJpaRepository jpaRepository;

    public NotificacaoRepositoryJpaAdapter(NotificacaoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public Notificacao salvarComLimite(Notificacao notificacao, int limite) {
        jpaRepository.lockUsuario(notificacao.usuarioId());

        NotificacaoEntity salva = jpaRepository.save(NotificacaoEntityMapper.toEntity(notificacao));

        List<Long> idsManter = jpaRepository.findByUsuarioIdOrderByCriadoEmDesc(notificacao.usuarioId())
                .stream()
                .limit(limite)
                .map(NotificacaoEntity::getId)
                .toList();

        jpaRepository.deleteByUsuarioIdAndIdNotIn(notificacao.usuarioId(), idsManter);

        return NotificacaoEntityMapper.toDomain(salva);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notificacao> listarPorUsuario(Long usuarioId) {
        return jpaRepository.findByUsuarioIdOrderByCriadoEmDesc(usuarioId)
                .stream()
                .map(NotificacaoEntityMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public int marcarTodasComoLidas(Long usuarioId) {
        return jpaRepository.marcarTodasComoLidas(usuarioId);
    }
}

