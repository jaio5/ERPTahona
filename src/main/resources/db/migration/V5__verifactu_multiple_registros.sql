ALTER TABLE verifactu_evidence
DROP INDEX uk_verifactu_evidence_factura_id;

CREATE INDEX idx_verifactu_evidence_factura_id ON verifactu_evidence (factura_id);
CREATE INDEX idx_verifactu_evidence_factura_tipo ON verifactu_evidence (factura_id, tipo_registro);
