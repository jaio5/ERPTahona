-- Los módulos 'produccion', 'reparto' y 'reportes' se usaban en @permisos.puede(...)
-- pero no existían en el JSON de permisos sembrado por V7: RolService.tienePermiso
-- devuelve false para módulos ausentes, así que solo los ADMIN podían usar esas
-- pantallas (y, desde la Fase 2, sus APIs). Esta migración los añade a los roles de
-- sistema (es_sistema=TRUE: no editables desde la UI, reescribir su JSON es seguro)
-- y crea el rol REPARTIDOR para la app móvil de reparto.

UPDATE roles
SET permisos = '{"dashboard":{"ver":true},"clientes":{"ver":true,"crear":true,"editar":true,"eliminar":true},"proveedores":{"ver":true,"crear":true,"editar":true,"eliminar":true},"articulos":{"ver":true,"crear":true,"editar":true,"eliminar":true},"ventas":{"ver":true,"crear":true,"editar":true,"eliminar":true},"compras":{"ver":true,"crear":true,"editar":true,"eliminar":true},"almacen":{"ver":true,"crear":true,"editar":true,"eliminar":true},"produccion":{"ver":true,"crear":true,"editar":true,"eliminar":true},"reparto":{"ver":true,"crear":true,"editar":true,"eliminar":true},"reportes":{"ver":true},"tesoreria":{"ver":true,"crear":true,"editar":true,"eliminar":true},"contabilidad":{"ver":true,"crear":true,"editar":true,"eliminar":true},"fiscal":{"ver":true,"crear":true,"editar":true,"eliminar":true},"usuarios":{"ver":true,"crear":true,"editar":true,"eliminar":true},"auditoria":{"ver":true,"exportar":true},"backup":{"ver":true,"crear":true,"restaurar":true},"configuracion":{"ver":true,"editar":true},"verifactu":{"ver":true,"enviar":true,"exportar":true}}'
WHERE nombre = 'ADMIN' AND es_sistema = TRUE;

UPDATE roles
SET permisos = '{"dashboard":{"ver":true},"clientes":{"ver":true,"crear":true,"editar":true,"eliminar":false},"proveedores":{"ver":true,"crear":true,"editar":true,"eliminar":false},"articulos":{"ver":true,"crear":true,"editar":true,"eliminar":false},"ventas":{"ver":true,"crear":true,"editar":true,"eliminar":false},"compras":{"ver":true,"crear":true,"editar":true,"eliminar":false},"almacen":{"ver":true,"crear":true,"editar":true,"eliminar":false},"produccion":{"ver":true,"crear":true,"editar":true,"eliminar":false},"reparto":{"ver":true,"crear":true,"editar":true,"eliminar":false},"reportes":{"ver":true},"tesoreria":{"ver":true,"crear":true,"editar":true,"eliminar":false},"contabilidad":{"ver":true,"crear":true,"editar":false,"eliminar":false},"fiscal":{"ver":true,"crear":true,"editar":false,"eliminar":false},"usuarios":{"ver":false},"auditoria":{"ver":false},"backup":{"ver":false},"configuracion":{"ver":false},"verifactu":{"ver":true,"enviar":false,"exportar":true}}'
WHERE nombre = 'GESTOR' AND es_sistema = TRUE;

UPDATE roles
SET permisos = '{"dashboard":{"ver":true},"clientes":{"ver":true,"crear":true,"editar":true,"eliminar":false},"articulos":{"ver":true},"ventas":{"ver":true,"crear":true,"editar":true,"eliminar":false},"almacen":{"ver":true},"produccion":{"ver":false},"reparto":{"ver":false},"reportes":{"ver":true},"proveedores":{"ver":false},"compras":{"ver":false},"tesoreria":{"ver":false},"contabilidad":{"ver":false},"fiscal":{"ver":false},"usuarios":{"ver":false},"auditoria":{"ver":false},"backup":{"ver":false},"configuracion":{"ver":false},"verifactu":{"ver":false}}'
WHERE nombre = 'VENDEDOR' AND es_sistema = TRUE;

UPDATE roles
SET permisos = '{"dashboard":{"ver":true},"clientes":{"ver":true},"proveedores":{"ver":true},"articulos":{"ver":true},"ventas":{"ver":true},"compras":{"ver":true,"crear":true,"editar":true,"eliminar":false},"tesoreria":{"ver":true,"crear":true,"editar":true,"eliminar":false},"contabilidad":{"ver":true,"crear":true,"editar":true,"eliminar":false},"fiscal":{"ver":true,"crear":true,"editar":true,"eliminar":false},"almacen":{"ver":true},"produccion":{"ver":false},"reparto":{"ver":false},"reportes":{"ver":true},"usuarios":{"ver":false},"auditoria":{"ver":true,"exportar":true},"backup":{"ver":false},"configuracion":{"ver":false},"verifactu":{"ver":true,"enviar":true,"exportar":true}}'
WHERE nombre = 'CONTABLE' AND es_sistema = TRUE;

UPDATE roles
SET permisos = '{"dashboard":{"ver":true},"clientes":{"ver":true},"articulos":{"ver":true},"ventas":{"ver":true},"almacen":{"ver":true},"produccion":{"ver":false},"reparto":{"ver":false},"reportes":{"ver":false},"proveedores":{"ver":false},"compras":{"ver":false},"tesoreria":{"ver":false},"contabilidad":{"ver":false},"fiscal":{"ver":false},"usuarios":{"ver":false},"auditoria":{"ver":false},"backup":{"ver":false},"configuracion":{"ver":false},"verifactu":{"ver":false}}'
WHERE nombre = 'USUARIO';

-- Rol para la app móvil del repartidor: ve su hoja de ruta y confirma entregas.
-- ventas:ver le permite consultar los albaranes que reparte.
INSERT INTO roles (nombre, descripcion, permisos, activo, es_sistema) VALUES
('REPARTIDOR', 'Repartidor: hoja de ruta del dia y confirmacion de entregas', '{"dashboard":{"ver":true},"reparto":{"ver":true,"crear":false,"editar":true,"eliminar":false},"ventas":{"ver":true},"clientes":{"ver":false},"articulos":{"ver":false},"almacen":{"ver":false},"produccion":{"ver":false},"reportes":{"ver":false},"proveedores":{"ver":false},"compras":{"ver":false},"tesoreria":{"ver":false},"contabilidad":{"ver":false},"fiscal":{"ver":false},"usuarios":{"ver":false},"auditoria":{"ver":false},"backup":{"ver":false},"configuracion":{"ver":false},"verifactu":{"ver":false}}', TRUE, TRUE)
ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion), permisos = VALUES(permisos), activo = VALUES(activo), es_sistema = VALUES(es_sistema);
