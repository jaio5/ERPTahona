-- Protege las actualizaciones concurrentes de stock y del catálogo.
ALTER TABLE articulos
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
