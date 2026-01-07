-- ============================================================================
-- SCRIPT DE CORRECCIÓN: Establecer valores predeterminados para campos NULL
-- ============================================================================
-- Fecha: 2025-12-28
-- Propósito: Corregir valores NULL en campos booleanos que causan errores
-- ============================================================================

USE tahona;

-- ============================================================================
-- 1. Actualizar tabla clientes
-- ============================================================================

-- Verificar clientes con activo NULL
SELECT COUNT(*) as 'Clientes con activo NULL'
FROM clientes
WHERE activo IS NULL;

-- Establecer activo = true para todos los clientes con NULL
UPDATE clientes
SET activo = TRUE
WHERE activo IS NULL;

-- Modificar columna para que no permita NULL y tenga valor por defecto
ALTER TABLE clientes
MODIFY COLUMN activo TINYINT(1) NOT NULL DEFAULT 1;

-- ============================================================================
-- 2. Actualizar tabla proveedores
-- ============================================================================

-- Verificar proveedores con activo NULL
SELECT COUNT(*) as 'Proveedores con activo NULL'
FROM proveedores
WHERE activo IS NULL;

-- Establecer activo = true para todos los proveedores con NULL
UPDATE proveedores
SET activo = TRUE
WHERE activo IS NULL;

-- Modificar columna para que no permita NULL y tenga valor por defecto
ALTER TABLE proveedores
MODIFY COLUMN activo TINYINT(1) NOT NULL DEFAULT 1;

-- ============================================================================
-- 3. Actualizar tabla usuarios (verificar enabled)
-- ============================================================================

-- Verificar usuarios con enabled NULL
SELECT COUNT(*) as 'Usuarios con enabled NULL'
FROM users
WHERE enabled IS NULL;

-- Establecer enabled = true para todos los usuarios con NULL
UPDATE users
SET enabled = TRUE
WHERE enabled IS NULL;

-- Verificar usuarios con bloqueado NULL
SELECT COUNT(*) as 'Usuarios con bloqueado NULL'
FROM users
WHERE bloqueado IS NULL;

-- Establecer bloqueado = false para todos los usuarios con NULL
UPDATE users
SET bloqueado = FALSE
WHERE bloqueado IS NULL;

-- ============================================================================
-- 4. Actualizar tabla articulos (verificar activo NULL)
-- ============================================================================

-- Verificar artículos con activo NULL
SELECT COUNT(*) as 'Artículos con activo NULL'
FROM articulos
WHERE activo IS NULL;

-- Establecer activo = true para artículos con NULL
UPDATE articulos
SET activo = TRUE
WHERE activo IS NULL;

-- Modificar columna para que no permita NULL y tenga valor por defecto
ALTER TABLE articulos
MODIFY COLUMN activo TINYINT(1) NOT NULL DEFAULT 1;

-- ============================================================================
-- 5. Verificar resultados
-- ============================================================================

SELECT '============================================' as '';
SELECT 'VERIFICACIÓN DE CORRECCIONES' as '';
SELECT '============================================' as '';

SELECT
    (SELECT COUNT(*) FROM clientes WHERE activo IS NULL) as 'Clientes con activo NULL',
    (SELECT COUNT(*) FROM proveedores WHERE activo IS NULL) as 'Proveedores con activo NULL',
    (SELECT COUNT(*) FROM articulos WHERE activo IS NULL) as 'Artículos con activo NULL',
    (SELECT COUNT(*) FROM users WHERE enabled IS NULL) as 'Usuarios con enabled NULL',
    (SELECT COUNT(*) FROM users WHERE bloqueado IS NULL) as 'Usuarios con bloqueado NULL';

SELECT '============================================' as '';
SELECT 'TODOS LOS VALORES NULL CORREGIDOS' as '';
SELECT '============================================' as '';

-- ============================================================================
-- FIN DEL SCRIPT
-- ============================================================================

