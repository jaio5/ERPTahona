-- Script para agregar campos al módulo de artículos
-- Fecha: 11 de enero de 2026

USE tahona;

-- Agregar campo nombre si no existe
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_SCHEMA = 'tahona'
               AND TABLE_NAME = 'articulos'
               AND COLUMN_NAME = 'nombre');

SET @sqlstmt := IF(@exist = 0,
    'ALTER TABLE articulos ADD COLUMN nombre VARCHAR(255) NULL AFTER descripcion',
    'SELECT ''La columna nombre ya existe'' AS mensaje');

PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Agregar campo codigo_barras si no existe
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_SCHEMA = 'tahona'
               AND TABLE_NAME = 'articulos'
               AND COLUMN_NAME = 'codigo_barras');

SET @sqlstmt := IF(@exist = 0,
    'ALTER TABLE articulos ADD COLUMN codigo_barras VARCHAR(50) NULL AFTER nombre',
    'SELECT ''La columna codigo_barras ya existe'' AS mensaje');

PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Agregar campo categoria si no existe
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_SCHEMA = 'tahona'
               AND TABLE_NAME = 'articulos'
               AND COLUMN_NAME = 'categoria');

SET @sqlstmt := IF(@exist = 0,
    'ALTER TABLE articulos ADD COLUMN categoria VARCHAR(100) NULL AFTER codigo_barras',
    'SELECT ''La columna categoria ya existe'' AS mensaje');

PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Agregar campo stock si no existe
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_SCHEMA = 'tahona'
               AND TABLE_NAME = 'articulos'
               AND COLUMN_NAME = 'stock');

SET @sqlstmt := IF(@exist = 0,
    'ALTER TABLE articulos ADD COLUMN stock DECIMAL(10,2) DEFAULT 0.00 AFTER coste',
    'SELECT ''La columna stock ya existe'' AS mensaje');

PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Agregar campo stock_minimo si no existe
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_SCHEMA = 'tahona'
               AND TABLE_NAME = 'articulos'
               AND COLUMN_NAME = 'stock_minimo');

SET @sqlstmt := IF(@exist = 0,
    'ALTER TABLE articulos ADD COLUMN stock_minimo DECIMAL(10,2) DEFAULT 0.00 AFTER stock',
    'SELECT ''La columna stock_minimo ya existe'' AS mensaje');

PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Agregar campo stock_maximo si no existe
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_SCHEMA = 'tahona'
               AND TABLE_NAME = 'articulos'
               AND COLUMN_NAME = 'stock_maximo');

SET @sqlstmt := IF(@exist = 0,
    'ALTER TABLE articulos ADD COLUMN stock_maximo DECIMAL(10,2) DEFAULT 0.00 AFTER stock_minimo',
    'SELECT ''La columna stock_maximo ya existe'' AS mensaje');

PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Agregar campo control_stock si no existe
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_SCHEMA = 'tahona'
               AND TABLE_NAME = 'articulos'
               AND COLUMN_NAME = 'control_stock');

SET @sqlstmt := IF(@exist = 0,
    'ALTER TABLE articulos ADD COLUMN control_stock TINYINT(1) DEFAULT 1 AFTER stock_maximo',
    'SELECT ''La columna control_stock ya existe'' AS mensaje');

PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Mostrar estructura actualizada
DESC articulos;

SELECT '✅ Campos agregados correctamente a la tabla articulos' AS resultado;

