-- Script para agregar campos telefono y email a la tabla clientes
-- Fecha: 11 de enero de 2026

USE tahona;

-- Agregar columna telefono si no existe
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_SCHEMA = 'tahona'
               AND TABLE_NAME = 'clientes'
               AND COLUMN_NAME = 'telefono');

SET @sqlstmt := IF(@exist = 0,
    'ALTER TABLE clientes ADD COLUMN telefono VARCHAR(20) NULL AFTER cif',
    'SELECT ''La columna telefono ya existe'' AS mensaje');

PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Agregar columna email si no existe
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_SCHEMA = 'tahona'
               AND TABLE_NAME = 'clientes'
               AND COLUMN_NAME = 'email');

SET @sqlstmt := IF(@exist = 0,
    'ALTER TABLE clientes ADD COLUMN email VARCHAR(100) NULL AFTER telefono',
    'SELECT ''La columna email ya existe'' AS mensaje');

PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Mostrar estructura actualizada
DESC clientes;

SELECT '✅ Campos telefono y email agregados correctamente' AS resultado;

