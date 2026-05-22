-- =============================================================================
-- V1 — Schema inicial: usuario, pedido, etapa_rastreamento
-- Baseado em artefatos/modelagem-de-dados/modelo-de-dados.md
-- =============================================================================

-- ---------- usuario ----------
CREATE TABLE usuario (
     id            BIGINT       NOT NULL AUTO_INCREMENT,
     nome          VARCHAR(200) NOT NULL,
     email         VARCHAR(255) NOT NULL,
     senha_hash    VARCHAR(60)  NOT NULL,
     perfil        ENUM('CLIENTE','ADMINISTRADOR') NOT NULL DEFAULT 'CLIENTE',
     criado_em     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
     PRIMARY KEY (id),
     UNIQUE KEY uk_usuario_email (email)
) ENGINE=InnoDB
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

-- ---------- pedido ----------
CREATE TABLE pedido (
    id                   BIGINT       NOT NULL AUTO_INCREMENT,
    usuario_id           BIGINT       NOT NULL,
    codigo_rastreamento  VARCHAR(50)  NOT NULL,
    descricao            VARCHAR(500) NULL,
    cancelado            BOOLEAN      NOT NULL DEFAULT FALSE,
    criado_em            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_pedido_usuario_codigo (usuario_id, codigo_rastreamento),
    CONSTRAINT fk_pedido_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE RESTRICT,
    INDEX ix_pedido_usuario_criado (usuario_id, criado_em DESC)
) ENGINE=InnoDB
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

-- ---------- etapa_rastreamento ----------
CREATE TABLE etapa_rastreamento (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    pedido_id    BIGINT       NOT NULL,
    tipo         ENUM(
                    'NA_CHINA','AEROPORTO_ORIGEM','EM_TRANSITO','AEROPORTO_DESTINO',
                    'NO_BRASIL','TAXA','CD_BRASIL','SAIDA_ENTREGA','ENTREGUE'
                 ) NOT NULL,
    localizacao  VARCHAR(255) NULL,
    descricao    VARCHAR(500) NULL,
    criado_em    TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT fk_etapa_pedido
        FOREIGN KEY (pedido_id) REFERENCES pedido(id) ON DELETE CASCADE,
    UNIQUE KEY uk_etapa_pedido_criado (pedido_id, criado_em),
    INDEX ix_etapa_pedido_criado_desc (pedido_id, criado_em DESC)
) ENGINE=InnoDB
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

