-- =========================================================
-- SCRIPT PARA CORREGIR ARTÍCULOS SIN NOMBRE EN LA BD
-- =========================================================
-- Fecha: 12 de enero de 2026
-- Propósito: Actualizar artículos con nombre NULL o vacío

USE tahona;

-- 1. Ver cuántos artículos tienen problema
SELECT
    COUNT(*) as total_problematicos,
    SUM(CASE WHEN nombre IS NULL THEN 1 ELSE 0 END) as nombre_null,
    SUM(CASE WHEN nombre = '' THEN 1 ELSE 0 END) as nombre_vacio
FROM articulos;

-- 2. Ver los artículos problemáticos
SELECT id, codigo, nombre, descripcion, pvp, activo
FROM articulos
WHERE nombre IS NULL OR nombre = ''
LIMIT 10;

-- 3. OPCIÓN A: Actualizar con el código del artículo
-- (Recomendado si los códigos son descriptivos)
UPDATE articulos
SET nombre = codigo
WHERE nombre IS NULL OR nombre = '';

-- 4. OPCIÓN B: Actualizar con un nombre más descriptivo
-- (Descomentar si prefieres esta opción en lugar de la anterior)
/*
UPDATE articulos
SET nombre = CONCAT('Artículo ', codigo)
WHERE nombre IS NULL OR nombre = '';
*/

-- 5. OPCIÓN C: Actualizar solo los NULL (dejar vacíos como están)
-- (Descomentar si prefieres esta opción)
/*
UPDATE articulos
SET nombre = codigo
WHERE nombre IS NULL;
*/

-- 6. Verificar que se corrigieron
SELECT id, codigo, nombre, pvp
FROM articulos
WHERE activo = 1
ORDER BY codigo
LIMIT 10;

-- 7. Contar artículos corregidos
SELECT
    COUNT(*) as total_articulos,
    SUM(CASE WHEN nombre IS NULL OR nombre = '' THEN 1 ELSE 0 END) as con_problema,
    SUM(CASE WHEN nombre IS NOT NULL AND nombre != '' THEN 1 ELSE 0 END) as corregidos
FROM articulos;

-- =========================================================
-- FIN DEL SCRIPT
-- =========================================================

