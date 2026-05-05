UPDATE roles
SET permisos = '{"dashboard":{"ver":true},"clientes":{"ver":true,"crear":true,"editar":true,"eliminar":true},"proveedores":{"ver":true,"crear":true,"editar":true,"eliminar":true},"articulos":{"ver":true,"crear":true,"editar":true,"eliminar":true},"ventas":{"ver":true,"crear":true,"editar":true,"eliminar":true},"compras":{"ver":true,"crear":true,"editar":true,"eliminar":true},"almacen":{"ver":true,"crear":true,"editar":true,"eliminar":true},"tesoreria":{"ver":true,"crear":true,"editar":true,"eliminar":true},"contabilidad":{"ver":true,"crear":true,"editar":true,"eliminar":true},"fiscal":{"ver":true,"crear":true,"editar":true,"eliminar":true},"usuarios":{"ver":true,"crear":true,"editar":true,"eliminar":true},"auditoria":{"ver":true,"exportar":true},"backup":{"ver":true,"crear":true,"restaurar":true},"configuracion":{"ver":true,"editar":true},"verifactu":{"ver":true,"enviar":true,"exportar":true}}',
    descripcion = 'Administrador del sistema',
    activo = TRUE,
    es_sistema = TRUE
WHERE nombre = 'ADMIN';

INSERT INTO roles (nombre, descripcion, permisos, activo, es_sistema) VALUES
('GESTOR', 'Gestion operativa sin administracion de usuarios ni copias', '{"dashboard":{"ver":true},"clientes":{"ver":true,"crear":true,"editar":true,"eliminar":false},"proveedores":{"ver":true,"crear":true,"editar":true,"eliminar":false},"articulos":{"ver":true,"crear":true,"editar":true,"eliminar":false},"ventas":{"ver":true,"crear":true,"editar":true,"eliminar":false},"compras":{"ver":true,"crear":true,"editar":true,"eliminar":false},"almacen":{"ver":true,"crear":true,"editar":true,"eliminar":false},"tesoreria":{"ver":true,"crear":true,"editar":true,"eliminar":false},"contabilidad":{"ver":true,"crear":true,"editar":false,"eliminar":false},"fiscal":{"ver":true,"crear":true,"editar":false,"eliminar":false},"usuarios":{"ver":false},"auditoria":{"ver":false},"backup":{"ver":false},"configuracion":{"ver":false},"verifactu":{"ver":true,"enviar":false,"exportar":true}}', TRUE, TRUE),
('VENDEDOR', 'Ventas, clientes y catalogo en modo comercial', '{"dashboard":{"ver":true},"clientes":{"ver":true,"crear":true,"editar":true,"eliminar":false},"articulos":{"ver":true},"ventas":{"ver":true,"crear":true,"editar":true,"eliminar":false},"almacen":{"ver":true},"proveedores":{"ver":false},"compras":{"ver":false},"tesoreria":{"ver":false},"contabilidad":{"ver":false},"fiscal":{"ver":false},"usuarios":{"ver":false},"auditoria":{"ver":false},"backup":{"ver":false},"configuracion":{"ver":false},"verifactu":{"ver":false}}', TRUE, TRUE),
('CONTABLE', 'Contabilidad, fiscalidad, compras y tesoreria', '{"dashboard":{"ver":true},"clientes":{"ver":true},"proveedores":{"ver":true},"articulos":{"ver":true},"ventas":{"ver":true},"compras":{"ver":true,"crear":true,"editar":true,"eliminar":false},"tesoreria":{"ver":true,"crear":true,"editar":true,"eliminar":false},"contabilidad":{"ver":true,"crear":true,"editar":true,"eliminar":false},"fiscal":{"ver":true,"crear":true,"editar":true,"eliminar":false},"almacen":{"ver":true},"usuarios":{"ver":false},"auditoria":{"ver":true,"exportar":true},"backup":{"ver":false},"configuracion":{"ver":false},"verifactu":{"ver":true,"enviar":true,"exportar":true}}', TRUE, TRUE)
ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion), permisos = VALUES(permisos), activo = VALUES(activo), es_sistema = VALUES(es_sistema);

UPDATE roles
SET permisos = '{"dashboard":{"ver":true},"clientes":{"ver":true},"articulos":{"ver":true},"ventas":{"ver":true},"almacen":{"ver":true},"proveedores":{"ver":false},"compras":{"ver":false},"tesoreria":{"ver":false},"contabilidad":{"ver":false},"fiscal":{"ver":false},"usuarios":{"ver":false},"auditoria":{"ver":false},"backup":{"ver":false},"configuracion":{"ver":false},"verifactu":{"ver":false}}',
    descripcion = 'Usuario estandar con acceso de consulta operativo'
WHERE nombre = 'USUARIO';

UPDATE users
SET rol_id = (SELECT id FROM roles WHERE nombre = users.role)
WHERE role IS NOT NULL AND rol_id IS NULL;
