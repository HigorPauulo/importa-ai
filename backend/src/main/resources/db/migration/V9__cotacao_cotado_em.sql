-- =============================================================================
-- V9 — cotado_em: horario real da cotacao na fonte externa (campo timestamp da API)
--   Separa o "quando o mercado cotou" (exibido como "ha X min") do
--   "quando o backend sincronizou" (atualizado_em, que controla o TTL/RN07).
-- =============================================================================

ALTER TABLE cotacao_cache
    ADD COLUMN cotado_em TIMESTAMP NULL AFTER atualizado_em;

-- Linhas pre-existentes: assume que a cotacao ocorreu no instante do ultimo sync.
UPDATE cotacao_cache SET cotado_em = atualizado_em WHERE cotado_em IS NULL;

-- Obrigatoria a partir daqui. Sem ON UPDATE: nao deve se auto-atualizar a cada gravacao.
ALTER TABLE cotacao_cache
    MODIFY COLUMN cotado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
