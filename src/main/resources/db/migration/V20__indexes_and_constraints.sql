-- V20: Índices de rendimiento y constraints de integridad
-- Cubre columnas FK sin índice y campos de negocio sin restricción de unicidad.

-- ===================== FACTURAS =====================

-- FK sin índice en V1 (facturas no tiene CONSTRAINT FOREIGN KEY declarado)
ALTER TABLE facturas ADD INDEX idx_facturas_cliente_id (cliente_id);
-- Columnas frecuentes en filtros y ordenación
ALTER TABLE facturas ADD INDEX idx_facturas_fecha (fecha);
ALTER TABLE facturas ADD INDEX idx_facturas_estado (estado);

-- ===================== FACTURA_LINEAS =====================

-- FKs sin índice ni CONSTRAINT en V1
ALTER TABLE factura_lineas ADD INDEX idx_factura_lineas_factura_id (factura_id);
ALTER TABLE factura_lineas ADD INDEX idx_factura_lineas_articulo_id (articulo_id);

-- ===================== ALBARANES_VENTA =====================

-- FKs sin CONSTRAINT ni índice en V10
ALTER TABLE albaranes_venta ADD INDEX idx_albaranes_venta_cliente_id (cliente_id);
ALTER TABLE albaranes_venta ADD INDEX idx_albaranes_venta_almacen_id (almacen_id);
-- Columnas frecuentes en filtros y ordenación
ALTER TABLE albaranes_venta ADD INDEX idx_albaranes_venta_fecha (fecha);
ALTER TABLE albaranes_venta ADD INDEX idx_albaranes_venta_estado (estado);

-- ===================== USERS =====================

-- El campo username ya tiene UNIQUE. Índice en rol_id tiene FK constraint (auto-creado por InnoDB).
-- Añadimos índice en último acceso para consultas de actividad
ALTER TABLE users ADD INDEX idx_users_ultimo_acceso (ultimo_acceso);

-- ===================== CLIENTES — UNIQUE CONSTRAINTS =====================

-- codigo es NOT NULL en V1, safe para UNIQUE
ALTER TABLE clientes ADD CONSTRAINT uq_clientes_codigo UNIQUE (codigo);

-- ===================== ARTICULOS — UNIQUE CONSTRAINTS =====================

-- codigo es NOT NULL en V1, safe para UNIQUE
ALTER TABLE articulos ADD CONSTRAINT uq_articulos_codigo UNIQUE (codigo);

-- ===================== AUDITORIA — ÍNDICE COMPUESTO =====================

-- Para consultas históricas por usuario + fecha (el índice individual idx_auditoria_usuario ya existe en V2)
ALTER TABLE auditoria_acciones ADD INDEX idx_auditoria_usuario_fecha (usuario_id, fecha);
