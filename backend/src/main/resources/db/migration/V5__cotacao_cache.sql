-- =============================================================================
-- V5 — Cache de cotacao de cambio (RF19, RF20, RF21, RN07)
-- =============================================================================

CREATE TABLE cotacao_cache (
    id                     BIGINT       NOT NULL AUTO_INCREMENT,
    moeda_origem           CHAR(3)      NOT NULL,
    moeda_destino          CHAR(3)      NOT NULL DEFAULT 'BRL',
    taxa                   DECIMAL(12,6) NOT NULL,
    fonte                  ENUM('AUTOMATICA','MANUAL') NOT NULL DEFAULT 'AUTOMATICA',
    manual_por_usuario_id  BIGINT       NULL,
    valido_ate             TIMESTAMP    NULL,
    atualizado_em          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cotacao_par (moeda_origem, moeda_destino),
    CONSTRAINT fk_cotacao_manual_usuario
        FOREIGN KEY (manual_por_usuario_id) REFERENCES usuario(id) ON DELETE SET NULL,
    INDEX ix_cotacao_par_atualizado (moeda_origem, moeda_destino, atualizado_em DESC)
) ENGINE=InnoDB
      DEFAULT CHARACTER SET utf8mb4
      COLLATE utf8mb4_0900_ai_ci;
