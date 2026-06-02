-- =============================================================================
-- V8 — Coluna de status do usuário (ativar/desativar no painel admin)
-- =============================================================================

ALTER TABLE usuario
    ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT TRUE AFTER perfil;
