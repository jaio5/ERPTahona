-- =========================================================
-- SCRIPT PARA CORREGIR NOMBRES Y DESCRIPCIONES DE ARTÍCULOS
-- =========================================================
-- Fecha: 12 de enero de 2026
-- Problema: nombre tiene el código, descripcion tiene el nombre real
-- Solución: Intercambiar nombre con descripcion

USE tahona;

-- 1. Ver el problema actual
SELECT
    id,
    codigo,
    nombre as nombre_actual_incorrecto,
    descripcion as descripcion_actual_tiene_nombre_real
FROM articulos
LIMIT 5;

-- 2. Crear columna temporal para el intercambio
ALTER TABLE articulos ADD COLUMN temp_nombre VARCHAR(255);

-- 3. Copiar descripcion (que tiene el nombre real) a temp
UPDATE articulos SET temp_nombre = descripcion;

-- 4. Poner codigo en descripcion y nombre real en nombre
UPDATE articulos
SET
    descripcion = nombre,  -- El código va a descripción
    nombre = temp_nombre;  -- El nombre real va a nombre

-- 5. Eliminar columna temporal
ALTER TABLE articulos DROP COLUMN temp_nombre;

-- 6. Verificar corrección
SELECT
    id,
    codigo,
    nombre as nombre_correcto,
    descripcion as descripcion_correcto
FROM articulos
LIMIT 10;

-- 7. Contar artículos corregidos
SELECT COUNT(*) as total_articulos_corregidos FROM articulos;

-- =========================================================
-- FIN DEL SCRIPT
-- =========================================================

