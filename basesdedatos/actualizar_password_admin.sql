-- Actualizar contraseña del usuario admin
-- Password: admin
-- Hash BCrypt válido generado y verificado

USE tahona;

-- Actualizar contraseña
UPDATE users
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    bloqueado = false,
    intentos_fallidos = 0,
    enabled = true
WHERE username = 'admin';

-- Verificar actualización
SELECT
    username,
    LEFT(password, 30) as password_hash,
    enabled,
    role,
    bloqueado,
    intentos_fallidos
FROM users
WHERE username = 'admin';

-- Mensaje de confirmación
SELECT '✓ Contraseña actualizada correctamente' AS RESULTADO;
SELECT 'Usuario: admin | Password: admin' AS CREDENCIALES;

