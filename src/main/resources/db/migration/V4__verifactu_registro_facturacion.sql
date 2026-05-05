ALTER TABLE verifactu_evidence
ADD COLUMN tipo_registro VARCHAR(20) NULL;

ALTER TABLE verifactu_evidence
ADD COLUMN xml_generado LONGTEXT NULL;

ALTER TABLE verifactu_evidence
ADD COLUMN fecha_generacion_registro TIMESTAMP NULL;

ALTER TABLE verifactu_evidence
ADD COLUMN huella_registro VARCHAR(128) NULL;

ALTER TABLE verifactu_evidence
ADD COLUMN nif_emisor VARCHAR(20) NULL;

ALTER TABLE verifactu_evidence
ADD COLUMN fecha_expedicion_factura DATE NULL;

CREATE INDEX idx_verifactu_evidence_tipo_registro ON verifactu_evidence (tipo_registro);
CREATE INDEX idx_verifactu_evidence_fecha_generacion ON verifactu_evidence (fecha_generacion_registro);
