-- Script de prueba para verificar datos de auditoría
-- Fecha: 11 de enero de 2026

USE tahona;

-- 1. Verificar si existe la tabla
SELECT 'Verificando tabla auditoria_acciones...' as paso;
SHOW TABLES LIKE 'auditoria_acciones';

-- 2. Ver estructura de la tabla
SELECT 'Estructura de la tabla:' as paso;
DESCRIBE auditoria_acciones;

-- 3. Contar registros
SELECT 'Total de registros:' as paso;
SELECT COUNT(*) as total_registros FROM auditoria_acciones;

-- 4. Ver últimos 5 registros
SELECT 'Últimos 5 registros:' as paso;
SELECT
    id,
    fecha,
    usuario_nombre,
    tipo_accion,
    modulo,
    entidad_tipo,
    descripcion,
    resultado
FROM auditoria_acciones
ORDER BY fecha DESC
LIMIT 5;

-- 5. Contar por tipo de acción
SELECT 'Registros por tipo de acción:' as paso;
SELECT
    tipo_accion,
    COUNT(*) as cantidad
FROM auditoria_acciones
GROUP BY tipo_accion
ORDER BY cantidad DESC;

-- 6. Contar por módulo
SELECT 'Registros por módulo:' as paso;
SELECT
    COALESCE(modulo, 'SIN MODULO') as modulo,
    COUNT(*) as cantidad
FROM auditoria_acciones
GROUP BY modulo
ORDER BY cantidad DESC;

-- 7. Contar por resultado
SELECT 'Registros por resultado:' as paso;
SELECT
    COALESCE(resultado, 'SIN RESULTADO') as resultado,
    COUNT(*) as cantidad
FROM auditoria_acciones
GROUP BY resultado;

-- 8. Verificar si hay registros en los últimos 30 días
SELECT 'Registros últimos 30 días:' as paso;
SELECT COUNT(*) as registros_recientes
FROM auditoria_acciones
WHERE fecha >= DATE_SUB(NOW(), INTERVAL 30 DAY);

-- 9. Si no hay datos, insertar registro de prueba
-- Descomenta las siguientes líneas para insertar datos de prueba:

/*
INSERT INTO auditoria_acciones
(usuario_nombre, tipo_accion, fecha, descripcion, modulo, resultado, entidad_tipo, entidad_id)
VALUES
('admin', 'LOGIN', NOW(), 'Login exitoso de prueba', 'AUTENTICACION', 'EXITO', 'Usuario', '1'),
('admin', 'CREAR', NOW(), 'Cliente creado de prueba', 'CLIENTES', 'EXITO', 'Cliente', '1'),
('admin', 'ACTUALIZAR', NOW(), 'Cliente actualizado de prueba', 'CLIENTES', 'EXITO', 'Cliente', '1'),
('admin', 'LEER', NOW(), 'Consulta de facturas', 'FACTURAS', 'EXITO', 'Factura', NULL),
('admin', 'ERROR', NOW(), 'Error de prueba', 'SISTEMA', 'ERROR', NULL, NULL);

SELECT 'Registros de prueba insertados!' as mensaje;
*/

SELECT '¡Verificación completa!' as final;

