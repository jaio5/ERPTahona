ALTER TABLE empresa_config
  ADD COLUMN verifactu_fecha_inicio DATE;

ALTER TABLE empresa_config
  ADD COLUMN verifactu_fecha_renuncia DATE;

ALTER TABLE empresa_config
  ALTER COLUMN verifactu_habilitado SET DEFAULT FALSE;

UPDATE empresa_config
SET verifactu_habilitado = FALSE
WHERE verifactu_habilitado IS NULL;

UPDATE empresa_config
SET verifactu_habilitado = FALSE
WHERE nombre_empresa = 'ERP Tahona'
  AND cif = 'X0000000X'
  AND verifactu_fecha_inicio IS NULL;

UPDATE empresa_config
SET verifactu_fecha_inicio = CURRENT_DATE
WHERE verifactu_habilitado = TRUE
  AND verifactu_fecha_inicio IS NULL;
