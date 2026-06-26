-- Costes reales de producción por horneada
ALTER TABLE horneadas
    ADD COLUMN IF NOT EXISTS coste_mano_obra   DECIMAL(10,2) NULL,
    ADD COLUMN IF NOT EXISTS coste_energia      DECIMAL(10,2) NULL,
    ADD COLUMN IF NOT EXISTS coste_materiales   DECIMAL(10,2) NULL;
