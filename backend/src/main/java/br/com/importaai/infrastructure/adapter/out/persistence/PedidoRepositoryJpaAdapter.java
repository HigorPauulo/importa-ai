package br.com.importaai.infrastructure.adapter.out.persistence;

import br.com.importaai.domain.exception.CodigoRastreamentoDuplicadoException;
import br.com.importaai.domain.model.Pedido;
import br.com.importaai.domain.port.out.PedidoRepository;
import br.com.importaai.infrastructure.adapter.out.persistence.entity.PedidoEntity;
import br.com.importaai.infrastructure.adapter.out.persistence.mapper.PedidoEntityMapper;
import br.com.importaai.infrastructure.adapter.out.persistence.repository.PedidoJpaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class PedidoRepositoryJpaAdapter implements PedidoRepository {

    private final PedidoJpaRepository jpaRepository;

    public PedidoRepositoryJpaAdapter(PedidoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public Pedido salvar(Pedido pedido) {
        try {
            PedidoEntity entity = PedidoEntityMapper.toEntity(pedido);
            // saveAndFlush força o INSERT agora, dentro do try — com save() puro a violação
            // da UNIQUE só estouraria no commit, fora do alcance deste catch.
            PedidoEntity salvo = jpaRepository.saveAndFlush(entity);
            return PedidoEntityMapper.toDomain(salvo);
        } catch (DataIntegrityViolationException e) {
            // Corrida na RN06: dois POSTs simultâneos passam pelo check do service e um bate
            // na constraint uk_pedido_usuario_codigo. Traduz para exceção de domínio (→ 422)
            // em vez de deixar vazar como 500.
            throw new CodigoRastreamentoDuplicadoException(
                    "usuario ja possui pedido com codigo " + pedido.getCodigoRastreamento());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Pedido> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(PedidoEntityMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Pedido> buscarPorCodigoRastreamentoEUsuario(String codigoRastreamento, Long usuarioId) {
        return jpaRepository
                .findByUsuarioIdAndCodigoRastreamento(usuarioId, codigoRastreamento)
                .map(PedidoEntityMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> listarPorUsuario(Long usuarioId) {
        return jpaRepository
                .findAllByUsuarioIdOrderByCriadoEmDesc(usuarioId)
                .stream()
                .map(PedidoEntityMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> listarNaoCancelados() {
        return jpaRepository.findByCanceladoFalse()
                .stream()
                .map(PedidoEntityMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> listarTodos() {
        return jpaRepository.findAll()
                .stream()
                .map(PedidoEntityMapper::toDomain)
                .toList();
    }
}
