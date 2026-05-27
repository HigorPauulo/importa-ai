-- =============================================================================
-- V3 — Tabelas de suporte à autenticação
-- =============================================================================

CREATE TABLE refresh_token_revogado (
    id           BIGINT     NOT NULL AUTO_INCREMENT,
    token_hash   CHAR(64)   NOT NULL,
    usuario_id   BIGINT     NOT NULL,
    revogado_em  TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expira_em    TIMESTAMP  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_refresh_token_revogado_hash (token_hash),
    CONSTRAINT fk_refresh_token_revogado_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,
    INDEX ix_refresh_token_revogado_expira (expira_em)
) ENGINE=InnoDB
      DEFAULT CHARACTER SET utf8mb4
      COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE tentativa_login_falha (
    email          VARCHAR(255) NOT NULL,
    contador       INT          NOT NULL DEFAULT 0,
    bloqueado_ate  TIMESTAMP    NULL,
    atualizado_em  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
       ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (email)
) ENGINE=InnoDB
      DEFAULT CHARACTER SET utf8mb4
      COLLATE utf8mb4_0900_ai_ci;
