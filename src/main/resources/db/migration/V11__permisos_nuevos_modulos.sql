-- V11: Añadir permisos de nuevos módulos (producción, trazabilidad, reparto) a roles existentes
UPDATE roles
SET permisos = JSON_SET(permisos, '$.produccion', JSON_OBJECT('ver', true, 'crear', true, 'editar', true, 'eliminar', true),
                                   '$.trazabilidad', JSON_OBJECT('ver', true, 'crear', true, 'editar', true, 'eliminar', true),
                                   '$.reparto', JSON_OBJECT('ver', true, 'crear', true, 'editar', true, 'eliminar', true))
WHERE nombre = 'ADMIN';

UPDATE roles
SET permisos = JSON_SET(permisos, '$.produccion', JSON_OBJECT('ver', true, 'crear', true, 'editar', true, 'eliminar', false),
                                   '$.trazabilidad', JSON_OBJECT('ver', true, 'crear', true, 'editar', true, 'eliminar', false),
                                   '$.reparto', JSON_OBJECT('ver', true, 'crear', true, 'editar', true, 'eliminar', false))
WHERE nombre = 'GESTOR';

UPDATE roles
SET permisos = JSON_SET(permisos, '$.produccion', JSON_OBJECT('ver', true),
                                   '$.trazabilidad', JSON_OBJECT('ver', true),
                                   '$.reparto', JSON_OBJECT('ver', true))
WHERE nombre = 'CONTABLE';

UPDATE roles
SET permisos = JSON_SET(permisos, '$.produccion', JSON_OBJECT('ver', true),
                                   '$.trazabilidad', JSON_OBJECT('ver', true),
                                   '$.reparto', JSON_OBJECT('ver', true))
WHERE nombre = 'VENDEDOR';

UPDATE roles
SET permisos = JSON_SET(permisos, '$.produccion', JSON_OBJECT('ver', true),
                                   '$.trazabilidad', JSON_OBJECT('ver', true),
                                   '$.reparto', JSON_OBJECT('ver', true))
WHERE nombre = 'USUARIO';
