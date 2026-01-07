-- SOLUCIÓN DEFINITIVA: Contraseña en texto plano temporalmente
-- Solo para hacer funcionar el login inmediatamente

USE tahona;

-- Actualizar usuario admin con contraseña SIN cifrar temporalmente
-- Esto funciona porque CifradoService.verificarPassword acepta texto plano como fallback
UPDATE users
SET password = 'admin',
    bloqueado = 0,
    intentos_fallidos = 0,
    enabled = 1
WHERE username = 'admin';

-- Verificar
SELECT username, password, enabled, bloqueado, intentos_fallidos
FROM users
WHERE username = 'admin';

SELECT '✓ CONTRASEÑA ACTUALIZADA A TEXTO PLANO' AS RESULTADO;
SELECT 'Usuario: admin | Password: admin' AS CREDENCIALES;
SELECT 'IMPORTANTE: Cambiar a BCrypt después del primer login' AS NOTA;

