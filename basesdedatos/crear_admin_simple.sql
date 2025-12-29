USE tahona;

DELETE FROM usuarios WHERE username = 'admin';

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
    1,
    0,
    NOW(),
    0,
    0
);

SELECT id, username, email, nombre_completo, activo, bloqueado, intentos_fallidos, LENGTH(password) as password_length
FROM usuarios
WHERE username = 'admin';

