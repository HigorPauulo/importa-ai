package br.com.importaai.infrastructure.adapter.out.persistence.repository;

import br.com.importaai.infrastructure.adapter.out.persistence.entity.TentativaLoginFalhaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TentativaLoginFalhaJpaRepository
        extends JpaRepository<TentativaLoginFalhaEntity, String> {
}