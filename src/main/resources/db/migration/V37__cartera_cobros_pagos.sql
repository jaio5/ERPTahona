-- V37: cartera de cobros (clientes) y pagos (proveedores) con importes parciales

CREATE TABLE IF NOT EXISTS cobros_factura (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  factura_id BIGINT NOT NULL,
  fecha DATE NOT NULL,
  importe DECIMAL(12,2) NOT NULL,
  forma_pago VARCHAR(50),
  referencia VARCHAR(100),
  observaciones VARCHAR(500),
  usuario_creacion VARCHAR(255),
  fecha_creacion DATETIME(6),
  KEY idx_cobros_factura (factura_id),
  CONSTRAINT fk_cobros_factura FOREIGN KEY (factura_id) REFERENCES facturas(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS pagos_factura_compra (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  factura_compra_id BIGINT NOT NULL,
  fecha DATE NOT NULL,
  importe DECIMAL(12,2) NOT NULL,
  forma_pago VARCHAR(50),
  referencia VARCHAR(100),
  observaciones VARCHAR(500),
  usuario_creacion VARCHAR(255),
  fecha_creacion DATETIME(6),
  KEY idx_pagos_fc (factura_compra_id),
  CONSTRAINT fk_pagos_fc FOREIGN KEY (factura_compra_id) REFERENCES facturas_compra(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Importe acumulado pagado en facturas de compra (en facturas de venta ya existe `pagado`)
ALTER TABLE facturas_compra ADD COLUMN pagado DECIMAL(12,2) DEFAULT '0.00';
