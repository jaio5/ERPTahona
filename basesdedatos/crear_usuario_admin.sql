-- =====================================================
-- Script para crear usuario admin en la base de datos
-- =====================================================

USE tahona;

-- Verificar si existe la tabla usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    nombre_completo VARCHAR(100) NOT NULL,
    telefono VARCHAR(20),
    rol_id BIGINT,
    activo BIT(1) NOT NULL DEFAULT b'1',
    bloqueado BIT(1) NOT NULL DEFAULT b'0',
    fecha_creacion DATETIME(6) NOT NULL,
    ultimo_login DATETIME(6),
    fecha_cambio_password DATETIME(6),
    intentos_fallidos INT DEFAULT 0,
    requiere_cambio_password BIT(1) NOT NULL DEFAULT b'0',
    token_recuperacion VARCHAR(255),
    fecha_expiracion_token DATETIME(6),
    observaciones TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Eliminar usuario admin si existe (para recrearlo limpio)
DELETE FROM usuarios WHERE username = 'admin';

-- Crear usuario admin con contraseña "admin" (BCrypt)
-- Password: admin
-- BCrypt hash: $2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG
INSERT INTO usuarios (
    username,
    password,
    email,
    nombre_completo,
    activo,
    bloqueado,
    fecha_creacion,
    intentos_fallidos,
    requiere_cambio_password
) VALUES (
    'admin',
    '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG',
    'admin@tahona.es',
    'Administrador del Sistema',
    b'1',
    b'0',
    NOW(),
    0,
    b'0'
);

-- Verificar que se creó correctamente
SELECT id, username, email, nombre_completo, activo, bloqueado, intentos_fallidos
FROM usuarios
WHERE username = 'admin';

-- Mostrar información
SELECT 'Usuario admin creado correctamente en tabla usuarios' AS mensaje;
SELECT 'Username: admin' AS credenciales;
SELECT 'Password: admin' AS credenciales;

