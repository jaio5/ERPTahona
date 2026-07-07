-- V14: Añadir tipo_impositivo a facturas (Art. 6.1.j RD 1619/2012)
SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.columns
     WHERE table_schema = DATABASE() AND table_name = 'facturas' AND column_name = 'tipo_impositivo') = 0,
    'ALTER TABLE facturas ADD COLUMN tipo_impositivo DECIMAL(5,2)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
