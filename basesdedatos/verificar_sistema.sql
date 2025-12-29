-- =====================================================
-- Script de verificación completa del sistema
-- =====================================================

USE tahona;

-- 1. Verificar usuario admin
SELECT '=== USUARIO ADMIN ===' AS verificacion;
SELECT
    id,
    username,
    enabled,
    role,
    bloqueado,
    intentos_fallidos,
    nombre,
    email
FROM users
WHERE username = 'admin';

-- 2. Verificar tablas con datos
SELECT '=== CONTEO DE REGISTROS ===' AS verificacion;
SELECT 'Clientes' AS tabla, COUNT(*) AS registros FROM clientes
UNION ALL
SELECT 'Proveedores', COUNT(*) FROM proveedores
UNION ALL
SELECT 'Artículos', COUNT(*) FROM articulos
UNION ALL
SELECT 'Almacenes', COUNT(*) FROM almacenes
UNION ALL
SELECT 'Facturas', COUNT(*) FROM facturas
UNION ALL
SELECT 'Albaranes', COUNT(*) FROM albaranes_venta
UNION ALL
SELECT 'Usuarios', COUNT(*) FROM users;

-- 3. Verificar estructura de tabla users
SELECT '=== ESTRUCTURA TABLA USERS ===' AS verificacion;
DESCRIBE users;

-- 4. Verificar algunos clientes
SELECT '=== PRIMEROS 5 CLIENTES ===' AS verificacion;
SELECT id, codigo, nombre, cif, activo
FROM clientes
LIMIT 5;

-- 5. Verificar algunos artículos
SELECT '=== PRIMEROS 5 ARTÍCULOS ===' AS verificacion;
SELECT id, codigo, descripcion, pvp, activo
FROM articulos
LIMIT 5;

-- 6. Verificar almacenes
SELECT '=== ALMACENES ===' AS verificacion;
SELECT * FROM almacenes;

-- 7. Resumen final
SELECT '=== RESUMEN ===' AS verificacion;
SELECT
    'Sistema listo para usar' AS estado,
    'Usuario: admin' AS credencial1,
    'Password: admin' AS credencial2,
    'Base de datos: tahona' AS base_datos;

