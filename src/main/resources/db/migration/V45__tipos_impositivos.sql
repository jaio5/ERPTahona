-- V45: catálogo configurable de tipos de IVA (Ajustes). Las líneas de factura/albarán
-- siguen guardando el VALOR aplicado (histórico e inmutable); este catálogo solo alimenta
-- el desplegable al crear documentos, por lo que no lleva FK desde las líneas.

CREATE TABLE IF NOT EXISTS tipos_impositivos (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  porcentaje DECIMAL(5,2) NOT NULL,
  recargo_equivalencia DECIMAL(5,2) NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  orden INT NOT NULL DEFAULT 0,
  es_defecto BOOLEAN NOT NULL DEFAULT FALSE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Semilla con los tipos vigentes en España (con su recargo de equivalencia).
INSERT INTO tipos_impositivos (nombre, porcentaje, recargo_equivalencia, activo, orden, es_defecto) VALUES
  ('Exento / 0%',        0.00, 0.00, TRUE, 1, FALSE),
  ('Superreducido 4%',   4.00, 0.50, TRUE, 2, FALSE),
  ('Reducido 10%',      10.00, 1.40, TRUE, 3, FALSE),
  ('General 21%',       21.00, 5.20, TRUE, 4, TRUE);
