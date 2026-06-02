-- =============================================================================
-- V7 — Seed do usuário administrador (acesso ao painel admin)
-- =============================================================================

-- senha_hash = BCrypt(custo 12) da senha "Admin@2026"
INSERT IGNORE INTO usuario (nome, email, senha_hash, perfil)
VALUES (
    'Administrador Importa Aí',
    'admin@importaai.com.br',
    '$2a$12$xKTYmXlubgLqcJ7Z8hMVFOyZ31Bb7EeHXoKItFnTdnBMoA/s7jZgq',
    'ADMINISTRADOR'
);
