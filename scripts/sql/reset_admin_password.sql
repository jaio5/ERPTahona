-- ========================================
-- RESET CONTRASEÑA ADMIN
-- Establece la contraseña "admin" con hash BCrypt
-- ========================================

USE tahona;

-- Hash BCrypt para "admin" generado con rounds=10
-- Password: admin
UPDATE users
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    enabled = 1,
    bloqueado = 0,
    intentos_fallidos = 0,
    fecha_bloqueo = NULL
WHERE username = 'admin';

-- Verificar
SELECT
    id,
    username,
    SUBSTRING(password, 1, 20) as password_hash,
    enabled,
    bloqueado,
    intentos_fallidos
FROM users
WHERE username = 'admin';

SELECT '✅ Contraseña de admin reseteada correctamente' as resultado;
SELECT 'Usuario: admin' as credenciales;
SELECT 'Contraseña: admin' as credenciales;

