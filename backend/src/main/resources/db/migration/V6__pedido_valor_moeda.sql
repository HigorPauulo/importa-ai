-- =====================================================================
-- V6 - Adiciona valor declarado e moeda ao pedido
-- Colunas nullable: linhas legadas ficam sem valor; a API (Bean Validation)
-- garante que todo pedido novo chega com valorDeclarado + moeda.
-- =====================================================================

ALTER TABLE pedido
    ADD COLUMN valor_declarado DECIMAL(15,2) NULL AFTER descricao,
    ADD COLUMN moeda           VARCHAR(10)   NULL AFTER valor_declarado;
