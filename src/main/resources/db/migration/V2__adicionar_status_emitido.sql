-- ============================================================
-- Adicionar status EMITIDO ao ingresso
-- ============================================================

-- Remove a constraint antiga e cria uma nova com o status EMITIDO
ALTER TABLE eventgo.ingresso
    DROP CONSTRAINT IF EXISTS ingresso_status_check;

ALTER TABLE eventgo.ingresso
    ADD CONSTRAINT ingresso_status_check
    CHECK (status IN ('ATIVO', 'EMITIDO', 'UTILIZADO', 'CANCELADO'));
