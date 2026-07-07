-- V28: Añade módulos produccion, reparto y reportes a la matriz de permisos
-- Estos módulos existían en la aplicación pero no estaban en la tabla roles

UPDATE roles SET permisos = JSON_SET(
    permisos,
    '$.produccion', JSON_OBJECT('ver', TRUE, 'crear', TRUE, 'editar', TRUE, 'eliminar', TRUE),
    '$.reparto',    JSON_OBJECT('ver', TRUE, 'crear', TRUE, 'editar', TRUE, 'eliminar', TRUE),
    '$.reportes',   JSON_OBJECT('ver', TRUE, 'exportar', TRUE)
) WHERE nombre = 'ADMIN';

UPDATE roles SET permisos = JSON_SET(
    permisos,
    '$.produccion', JSON_OBJECT('ver', TRUE, 'crear', TRUE, 'editar', TRUE, 'eliminar', FALSE),
    '$.reparto',    JSON_OBJECT('ver', TRUE, 'crear', TRUE, 'editar', TRUE, 'eliminar', FALSE),
    '$.reportes',   JSON_OBJECT('ver', TRUE, 'exportar', TRUE)
) WHERE nombre = 'GESTOR';

UPDATE roles SET permisos = JSON_SET(
    permisos,
    '$.produccion', JSON_OBJECT('ver', TRUE, 'crear', FALSE, 'editar', FALSE, 'eliminar', FALSE),
    '$.reparto',    JSON_OBJECT('ver', TRUE, 'crear', FALSE, 'editar', FALSE, 'eliminar', FALSE),
    '$.reportes',   JSON_OBJECT('ver', FALSE, 'exportar', FALSE)
) WHERE nombre = 'VENDEDOR';

UPDATE roles SET permisos = JSON_SET(
    permisos,
    '$.produccion', JSON_OBJECT('ver', TRUE, 'crear', FALSE, 'editar', FALSE, 'eliminar', FALSE),
    '$.reparto',    JSON_OBJECT('ver', TRUE, 'crear', FALSE, 'editar', FALSE, 'eliminar', FALSE),
    '$.reportes',   JSON_OBJECT('ver', TRUE, 'exportar', TRUE)
) WHERE nombre = 'CONTABLE';

UPDATE roles SET permisos = JSON_SET(
    permisos,
    '$.produccion', JSON_OBJECT('ver', FALSE, 'crear', FALSE, 'editar', FALSE, 'eliminar', FALSE),
    '$.reparto',    JSON_OBJECT('ver', FALSE, 'crear', FALSE, 'editar', FALSE, 'eliminar', FALSE),
    '$.reportes',   JSON_OBJECT('ver', FALSE, 'exportar', FALSE)
) WHERE nombre = 'USUARIO';
