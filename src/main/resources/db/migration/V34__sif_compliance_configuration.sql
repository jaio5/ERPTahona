ALTER TABLE empresa_config ADD COLUMN sif_modalidad VARCHAR(20) NOT NULL DEFAULT 'VERIFACTU';
ALTER TABLE empresa_config ADD COLUMN declaracion_responsable_emitida BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE empresa_config ADD COLUMN declaracion_responsable_fecha DATETIME NULL;
ALTER TABLE empresa_config ADD COLUMN declaracion_responsable_version VARCHAR(100) NULL;
ALTER TABLE empresa_config ADD COLUMN declaracion_responsable_hash VARCHAR(128) NULL;
ALTER TABLE empresa_config ADD COLUMN declaracion_responsable_ruta VARCHAR(500) NULL;
ALTER TABLE empresa_config ADD COLUMN productor_software VARCHAR(255) NULL;
ALTER TABLE empresa_config ADD COLUMN nif_productor_software VARCHAR(100) NULL;

ALTER TABLE facturacion_eventos ADD COLUMN version_normativa VARCHAR(50) NULL;
ALTER TABLE facturacion_eventos ADD COLUMN modalidad_sif VARCHAR(20) NULL;
ALTER TABLE facturacion_eventos ADD COLUMN origen_sistema VARCHAR(100) NULL;

CREATE INDEX idx_fact_eventos_modalidad ON facturacion_eventos (modalidad_sif);
