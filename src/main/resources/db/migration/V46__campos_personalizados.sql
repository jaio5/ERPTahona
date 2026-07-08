-- V46: campos personalizados para el formato de impresión. Permiten añadir información
-- extra (etiqueta: valor) a facturas/albaranes, de forma global (ámbito EMPRESA) o exclusiva
-- de un cliente (ámbito CLIENTE), en una zona del documento. Es la alternativa segura a
-- "editar la plantilla": datos configurables, sin HTML libre.

CREATE TABLE IF NOT EXISTS campos_personalizados (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  ambito VARCHAR(10) NOT NULL DEFAULT 'EMPRESA',      -- EMPRESA | CLIENTE
  cliente_id BIGINT NULL,
  etiqueta VARCHAR(100) NOT NULL,
  valor VARCHAR(500) NULL,
  ubicacion VARCHAR(20) NOT NULL DEFAULT 'PIE',       -- CABECERA | CLIENTE | OBSERVACIONES | PIE
  documento VARCHAR(10) NOT NULL DEFAULT 'AMBOS',     -- FACTURA | ALBARAN | AMBOS
  orden INT NOT NULL DEFAULT 0,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT fk_campo_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_campos_cliente ON campos_personalizados (cliente_id);
