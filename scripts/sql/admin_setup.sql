-- Verificar y crear usuario admin en la base de datos tahona
USE tahona;

-- 1. Verificar si existe la tabla users
SHOW TABLES LIKE 'users';

-- 2. Ver estructura de la tabla users
DESCRIBE users;

-- 3. Ver usuarios actuales
SELECT id, username, LEFT(password, 20) as password_hash, enabled, role, bloqueado, intentos_fallidos FROM users;

-- 4. Eliminar usuario admin si existe
DELETE FROM users WHERE username = 'admin';

-- 5. Crear usuario admin con contraseña 'admin' cifrada con BCrypt
-- Password: admin
-- BCrypt hash: $2a$10$wv7IqmHPQXQQqCGQWVQXa.fCGKBQVt2FJXQvS/fHqZHUk/WqNdlF6
-- Este hash corresponde a la contraseña "admin"
INSERT INTO users (username, password, enabled, role, nombre, email, bloqueado, intentos_fallidos, fecha_creacion)
VALUES ('admin', '$2a$10$wv7IqmHPQXQQqCGQWVQXa.fCGKBQVt2FJXQvS/fHqZHUk/WqNdlF6', true, 'ROLE_ADMIN', 'Administrador', 'admin@tahona.es', false, 0, NOW());

-- 6. Verificar creación
SELECT id, username, LEFT(password, 20) as password_hash, enabled, role, bloqueado, intentos_fallidos, nombre, email FROM users WHERE username = 'admin';

-- 7. Contar registros en otras tablas
SELECT 'Clientes' AS tabla, COUNT(*) AS registros FROM clientes
UNION ALL
SELECT 'Proveedores', COUNT(*) FROM proveedores
UNION ALL
SELECT 'Articulos', COUNT(*) FROM articulos
UNION ALL
SELECT 'Almacenes', COUNT(*) FROM almacenes
UNION ALL
SELECT 'Facturas', COUNT(*) FROM facturas
UNION ALL
SELECT 'Albaranes', COUNT(*) FROM albaranes_venta
UNION ALL
SELECT 'Usuarios', COUNT(*) FROM users;

-- Resultado final
SELECT '✓ Usuario admin creado correctamente' AS RESULTADO;
SELECT 'Usuario: admin | Password: admin' AS CREDENCIALES;

