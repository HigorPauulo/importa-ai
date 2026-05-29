-- ===========================
-- V4 — Notificacao
-- ===========================

CREATE TABLE notificacao (
     id           BIGINT       NOT NULL AUTO_INCREMENT,
     usuario_id   BIGINT       NOT NULL,
     pedido_id    BIGINT       NULL,
     mensagem     VARCHAR(500) NOT NULL,
     lida         BOOLEAN      NOT NULL DEFAULT FALSE,
     criado_em    TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
     PRIMARY KEY (id),
     CONSTRAINT fk_notificacao_usuario
     FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,
     CONSTRAINT fk_notificacao_pedido
     FOREIGN KEY (pedido_id) REFERENCES pedido(id) ON DELETE SET NULL,
     INDEX ix_notificacao_usuario_lida_criado (usuario_id, lida, criado_em DESC)
) ENGINE=InnoDB
      DEFAULT CHARACTER SET utf8mb4
      COLLATE utf8mb4_0900_ai_ci;
