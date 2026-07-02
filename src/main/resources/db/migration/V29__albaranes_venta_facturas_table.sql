-- V29: Tabla de relación albarán-factura.
-- Esta tabla no tenía migración previa; Hibernate la auto-creaba con ddl-auto=update.
-- Paso 1: renombrar columna camelCase si existe (creada por Hibernate antes de esta migración).
-- Paso 2: crear la tabla si no existe.
-- Paso 3: añadir FK constraints si faltan (tabla pre-existente sin constraints).

-- Paso 1: renombrar columna camelCase si existe
SET @rename_sql = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME   = 'albaranes_venta_facturas'
       AND COLUMN_NAME  = 'albaranesVentas_id') > 0,
    'ALTER TABLE albaranes_venta_facturas RENAME COLUMN `albaranesVentas_id` TO albaranes_ventas_id',
    'SELECT 1'
);
PREPARE _stmt FROM @rename_sql;
EXECUTE _stmt;
DEALLOCATE PREPARE _stmt;

-- Paso 2: crear tabla si no existe
CREATE TABLE IF NOT EXISTS albaranes_venta_facturas (
    albaranes_ventas_id BIGINT NOT NULL,
    facturas_id         BIGINT NOT NULL,
    PRIMARY KEY (albaranes_ventas_id, facturas_id),
    CONSTRAINT fk_avf_albaran FOREIGN KEY (albaranes_ventas_id) REFERENCES albaranes_venta(id),
    CONSTRAINT fk_avf_factura FOREIGN KEY (facturas_id)         REFERENCES facturas(id)
);

-- Paso 3: añadir FK fk_avf_albaran si la tabla existía pero sin este constraint
SET @fk1_sql = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
     WHERE TABLE_SCHEMA    = DATABASE()
       AND TABLE_NAME      = 'albaranes_venta_facturas'
       AND CONSTRAINT_NAME = 'fk_avf_albaran'
       AND CONSTRAINT_TYPE = 'FOREIGN KEY') = 0,
    'ALTER TABLE albaranes_venta_facturas ADD CONSTRAINT fk_avf_albaran FOREIGN KEY (albaranes_ventas_id) REFERENCES albaranes_venta(id)',
    'SELECT 1'
);
PREPARE _stmt FROM @fk1_sql;
EXECUTE _stmt;
DEALLOCATE PREPARE _stmt;

-- Paso 3b: añadir FK fk_avf_factura si falta
SET @fk2_sql = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
     WHERE TABLE_SCHEMA    = DATABASE()
       AND TABLE_NAME      = 'albaranes_venta_facturas'
       AND CONSTRAINT_NAME = 'fk_avf_factura'
       AND CONSTRAINT_TYPE = 'FOREIGN KEY') = 0,
    'ALTER TABLE albaranes_venta_facturas ADD CONSTRAINT fk_avf_factura FOREIGN KEY (facturas_id) REFERENCES facturas(id)',
    'SELECT 1'
);
PREPARE _stmt FROM @fk2_sql;
EXECUTE _stmt;
DEALLOCATE PREPARE _stmt;
