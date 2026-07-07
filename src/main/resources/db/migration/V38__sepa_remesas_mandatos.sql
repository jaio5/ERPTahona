-- V38: datos bancarios SEPA (cliente y empresa) y remesas de adeudos directos

ALTER TABLE clientes
    ADD COLUMN iban VARCHAR(34) NULL,
    ADD COLUMN mandato_sepa_referencia VARCHAR(35) NULL,
    ADD COLUMN mandato_sepa_fecha DATE NULL;

ALTER TABLE empresa_config
    ADD COLUMN iban VARCHAR(34) NULL,
    ADD COLUMN sepa_creditor_id VARCHAR(35) NULL;

CREATE TABLE IF NOT EXISTS remesas_sepa (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  fecha_creacion DATETIME(6),
  fecha_cobro DATE NOT NULL,
  concepto VARCHAR(140),
  estado VARCHAR(20) NOT NULL DEFAULT 'GENERADA',
  total DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  num_recibos INT NOT NULL DEFAULT 0,
  xml LONGTEXT,
  usuario_creacion VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS remesa_sepa_lineas (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  remesa_id BIGINT NOT NULL,
  factura_id BIGINT NOT NULL,
  importe DECIMAL(12,2) NOT NULL,
  iban_deudor VARCHAR(34),
  nombre_deudor VARCHAR(255),
  mandato_referencia VARCHAR(35),
  mandato_fecha DATE,
  KEY idx_remesa_lineas_remesa (remesa_id),
  KEY idx_remesa_lineas_factura (factura_id),
  CONSTRAINT fk_remesa_lineas_remesa FOREIGN KEY (remesa_id) REFERENCES remesas_sepa(id) ON DELETE CASCADE,
  CONSTRAINT fk_remesa_lineas_factura FOREIGN KEY (factura_id) REFERENCES facturas(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
