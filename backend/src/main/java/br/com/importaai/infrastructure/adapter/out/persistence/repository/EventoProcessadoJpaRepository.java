package br.com.importaai.infrastructure.adapter.out.persistence.repository;

import br.com.importaai.infrastructure.adapter.out.persistence.entity.EventoProcessadoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoProcessadoJpaRepository extends JpaRepository<EventoProcessadoEntity, Long> {
}
