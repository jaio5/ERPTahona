-- V12: Alérgenos alimentarios (UE 1169/2011) y corrección registro_sanitario
SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.columns
     WHERE table_schema = DATABASE() AND table_name = 'articulos' AND column_name = 'alergenos') = 0,
    'ALTER TABLE articulos ADD COLUMN alergenos VARCHAR(500)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.columns
     WHERE table_schema = DATABASE() AND table_name = 'recetas' AND column_name = 'alergenos') = 0,
    'ALTER TABLE recetas ADD COLUMN alergenos VARCHAR(500)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Asegurar que empresa_config tiene el campo registro_sanitario (por si acaso)
ALTER TABLE empresa_config MODIFY COLUMN registro_sanitario VARCHAR(100);
