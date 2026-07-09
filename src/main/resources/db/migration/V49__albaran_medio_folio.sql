-- V49: opción de imprimir albaranes en media hoja (A5) para aprovechar el papel.
-- Preferencia global de la empresa; por defecto folio completo (A4), como hasta ahora.

ALTER TABLE empresa_config
  ADD COLUMN albaran_medio_folio BOOLEAN NOT NULL DEFAULT FALSE;
