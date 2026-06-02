-- =============================================================================
-- V10 — flag de rastreio nao localizado pela transportadora
--   Ligada quando a fonte (Correios) responde que nao conhece o codigo (404),
--   pra UI avisar o cliente em vez de ficar muda. Derivada da sincronizacao.
-- =============================================================================

ALTER TABLE pedido
    ADD COLUMN rastreio_nao_localizado BOOLEAN NOT NULL DEFAULT FALSE;
