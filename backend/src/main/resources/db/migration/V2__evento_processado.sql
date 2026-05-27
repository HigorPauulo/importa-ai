-- =============================================================================
-- V2 — Tabela de idempotência de eventos AMQP
-- =============================================================================

CREATE TABLE evento_processado (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    exchange      VARCHAR(100) NOT NULL,
    routing_key   VARCHAR(100) NOT NULL,
    message_id    VARCHAR(36)  NOT NULL,
    processado_em TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_evento_processado_chave (exchange, routing_key, message_id)
) ENGINE=InnoDB
      DEFAULT CHARACTER SET utf8mb4
      COLLATE utf8mb4_0900_ai_ci;
