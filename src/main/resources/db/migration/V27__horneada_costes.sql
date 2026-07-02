-- Costes reales de producción por horneada
ALTER TABLE horneadas
    ADD COLUMN coste_mano_obra   DECIMAL(10,2) NULL,
    ADD COLUMN coste_energia    DECIMAL(10,2) NULL,
    ADD COLUMN coste_materiales DECIMAL(10,2) NULL;
