-- ================================================================
-- SCRIPT COMPLETO: Módulos Faltantes ERP - Tablas
-- Fecha: 2025-12-29
-- Descripción: Creación de todas las tablas para módulos faltantes
-- ================================================================

USE tahona;

-- ================================================================
-- 1. PROVEEDORES (ya creada anteriormente)
-- ================================================================

-- ================================================================
-- 2. PEDIDOS DE COMPRA
-- ================================================================

CREATE TABLE IF NOT EXISTS pedidos_compra (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero VARCHAR(100) NOT NULL UNIQUE,
    fecha DATE NOT NULL,
    fecha_esperada_entrega DATE,
    proveedor_id BIGINT NOT NULL,
    total DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    estado VARCHAR(20) DEFAULT 'BORRADOR',
    observaciones TEXT,
    fecha_creacion DATETIME,
    fecha_modificacion DATETIME,

    FOREIGN KEY (proveedor_id) REFERENCES proveedores(id),
    INDEX idx_numero (numero),
    INDEX idx_proveedor (proveedor_id),
    INDEX idx_estado (estado),
    INDEX idx_fecha (fecha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pedidos_compra_lineas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_compra_id BIGINT NOT NULL,
    articulo_id BIGINT,
    descripcion VARCHAR(500),
    cantidad DECIMAL(10,2) NOT NULL,
    cantidad_recibida DECIMAL(10,2) DEFAULT 0.00,
    precio_unitario DECIMAL(10,4) NOT NULL,
    descuento DECIMAL(5,2),
    tipo_iva DECIMAL(5,2),
    importe DECIMAL(12,2) NOT NULL,
    orden INT,

    FOREIGN KEY (pedido_compra_id) REFERENCES pedidos_compra(id) ON DELETE CASCADE,
    FOREIGN KEY (articulo_id) REFERENCES articulos(id),
    INDEX idx_pedido (pedido_compra_id),
    INDEX idx_articulo (articulo_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- 3. FACTURAS DE COMPRA
-- ================================================================

CREATE TABLE IF NOT EXISTS facturas_compra (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero VARCHAR(100) NOT NULL UNIQUE,
    numero_serie VARCHAR(100),
    fecha DATE NOT NULL,
    proveedor_id BIGINT NOT NULL,
    pedido_compra_id BIGINT,

    -- Importes
    base_imponible DECIMAL(12,2) NOT NULL,
    tipo_iva DECIMAL(5,2),
    importe_iva DECIMAL(12,2) NOT NULL,
    tipo_recargo DECIMAL(5,2),
    importe_recargo DECIMAL(12,2) DEFAULT 0.00,
    tipo_retencion DECIMAL(5,2),
    importe_retencion DECIMAL(12,2) DEFAULT 0.00,
    total DECIMAL(12,2) NOT NULL,

    -- Estado y pago
    estado VARCHAR(20) DEFAULT 'PENDIENTE',
    pagada BOOLEAN DEFAULT FALSE,
    fecha_pago DATE,
    fecha_vencimiento DATE,
    forma_pago VARCHAR(50),

    -- Control
    contabilizada BOOLEAN DEFAULT FALSE,
    fecha_contabilizacion DATETIME,
    observaciones TEXT,
    notas_internas TEXT,
    fecha_creacion DATETIME,
    fecha_modificacion DATETIME,
    usuario_creacion VARCHAR(100),

    FOREIGN KEY (proveedor_id) REFERENCES proveedores(id),
    FOREIGN KEY (pedido_compra_id) REFERENCES pedidos_compra(id),
    INDEX idx_numero (numero),
    INDEX idx_proveedor (proveedor_id),
    INDEX idx_fecha (fecha),
    INDEX idx_estado (estado),
    INDEX idx_pagada (pagada)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS facturas_compra_lineas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    factura_compra_id BIGINT NOT NULL,
    articulo_id BIGINT,
    descripcion VARCHAR(500),
    cantidad DECIMAL(10,2) NOT NULL,
    precio_unitario DECIMAL(10,4) NOT NULL,
    descuento DECIMAL(5,2),
    tipo_iva DECIMAL(5,2),
    importe DECIMAL(12,2) NOT NULL,
    orden INT,

    FOREIGN KEY (factura_compra_id) REFERENCES facturas_compra(id) ON DELETE CASCADE,
    FOREIGN KEY (articulo_id) REFERENCES articulos(id),
    INDEX idx_factura (factura_compra_id),
    INDEX idx_articulo (articulo_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- 4. TESORERÍA - BANCOS
-- ================================================================

CREATE TABLE IF NOT EXISTS bancos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(100) NOT NULL UNIQUE,
    nombre VARCHAR(255) NOT NULL,
    iban VARCHAR(34),
    swift VARCHAR(11),
    saldo_inicial DECIMAL(12,2) DEFAULT 0.00,
    saldo_actual DECIMAL(12,2) DEFAULT 0.00,
    activo BOOLEAN DEFAULT TRUE,
    observaciones TEXT,
    fecha_creacion DATETIME,
    fecha_modificacion DATETIME,

    INDEX idx_codigo (codigo),
    INDEX idx_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS movimientos_banco (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    banco_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    tipo VARCHAR(20), -- INGRESO, GASTO, TRASPASO
    concepto VARCHAR(500),
    importe DECIMAL(12,2) NOT NULL,
    saldo_resultante DECIMAL(12,2),
    factura_id BIGINT,
    factura_compra_id BIGINT,
    conciliado BOOLEAN DEFAULT FALSE,
    fecha_conciliacion DATE,
    observaciones TEXT,
    fecha_creacion DATETIME,

    FOREIGN KEY (banco_id) REFERENCES bancos(id),
    FOREIGN KEY (factura_id) REFERENCES facturas(id),
    FOREIGN KEY (factura_compra_id) REFERENCES facturas_compra(id),
    INDEX idx_banco (banco_id),
    INDEX idx_fecha (fecha),
    INDEX idx_tipo (tipo),
    INDEX idx_conciliado (conciliado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- 5. TESORERÍA - CAJAS
-- ================================================================

CREATE TABLE IF NOT EXISTS cajas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(100) NOT NULL UNIQUE,
    nombre VARCHAR(255) NOT NULL,
    saldo_inicial DECIMAL(12,2) DEFAULT 0.00,
    saldo_actual DECIMAL(12,2) DEFAULT 0.00,
    activa BOOLEAN DEFAULT TRUE,
    observaciones TEXT,
    fecha_creacion DATETIME,
    fecha_modificacion DATETIME,

    INDEX idx_codigo (codigo),
    INDEX idx_activa (activa)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS movimientos_caja (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    caja_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    tipo VARCHAR(20), -- ENTRADA, SALIDA
    concepto VARCHAR(500),
    importe DECIMAL(12,2) NOT NULL,
    saldo_resultante DECIMAL(12,2),
    factura_id BIGINT,
    observaciones TEXT,
    fecha_creacion DATETIME,

    FOREIGN KEY (caja_id) REFERENCES cajas(id),
    FOREIGN KEY (factura_id) REFERENCES facturas(id),
    INDEX idx_caja (caja_id),
    INDEX idx_fecha (fecha),
    INDEX idx_tipo (tipo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- 6. MOVIMIENTOS DE STOCK
-- ================================================================

CREATE TABLE IF NOT EXISTS movimientos_stock (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha DATE NOT NULL,
    tipo VARCHAR(20) NOT NULL, -- ENTRADA, SALIDA, TRASPASO, AJUSTE
    articulo_id BIGINT NOT NULL,
    almacen_origen_id BIGINT,
    almacen_destino_id BIGINT,
    cantidad DECIMAL(10,2) NOT NULL,
    precio_unitario DECIMAL(10,4),
    importe DECIMAL(12,2),
    concepto VARCHAR(500),
    factura_compra_id BIGINT,
    albaran_id BIGINT,
    observaciones TEXT,
    fecha_creacion DATETIME,
    usuario_creacion VARCHAR(100),

    FOREIGN KEY (articulo_id) REFERENCES articulos(id),
    FOREIGN KEY (almacen_origen_id) REFERENCES almacenes(id),
    FOREIGN KEY (almacen_destino_id) REFERENCES almacenes(id),
    FOREIGN KEY (factura_compra_id) REFERENCES facturas_compra(id),
    FOREIGN KEY (albaran_id) REFERENCES albaranes(id),
    INDEX idx_fecha (fecha),
    INDEX idx_tipo (tipo),
    INDEX idx_articulo (articulo_id),
    INDEX idx_almacen_origen (almacen_origen_id),
    INDEX idx_almacen_destino (almacen_destino_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- 7. PRESUPUESTOS
-- ================================================================

CREATE TABLE IF NOT EXISTS presupuestos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero VARCHAR(100) NOT NULL UNIQUE,
    fecha DATE NOT NULL,
    fecha_validez DATE,
    cliente_id BIGINT NOT NULL,
    total DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    estado VARCHAR(20) DEFAULT 'BORRADOR', -- BORRADOR, ENVIADO, ACEPTADO, RECHAZADO, CONVERTIDO
    factura_id BIGINT, -- Si se convirtió a factura
    observaciones TEXT,
    fecha_creacion DATETIME,
    fecha_modificacion DATETIME,
    usuario_creacion VARCHAR(100),

    FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    FOREIGN KEY (factura_id) REFERENCES facturas(id),
    INDEX idx_numero (numero),
    INDEX idx_cliente (cliente_id),
    INDEX idx_fecha (fecha),
    INDEX idx_estado (estado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS presupuestos_lineas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    presupuesto_id BIGINT NOT NULL,
    articulo_id BIGINT,
    descripcion VARCHAR(500),
    cantidad DECIMAL(10,2) NOT NULL,
    precio_unitario DECIMAL(10,4) NOT NULL,
    descuento DECIMAL(5,2),
    tipo_iva DECIMAL(5,2),
    importe DECIMAL(12,2) NOT NULL,
    orden INT,

    FOREIGN KEY (presupuesto_id) REFERENCES presupuestos(id) ON DELETE CASCADE,
    FOREIGN KEY (articulo_id) REFERENCES articulos(id),
    INDEX idx_presupuesto (presupuesto_id),
    INDEX idx_articulo (articulo_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- 8. CONTABILIDAD
-- ================================================================

CREATE TABLE IF NOT EXISTS plan_contable (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(255) NOT NULL,
    tipo VARCHAR(20), -- ACTIVO, PASIVO, PATRIMONIO_NETO, INGRESOS, GASTOS
    nivel INT,
    padre_id BIGINT,
    activa BOOLEAN DEFAULT TRUE,

    FOREIGN KEY (padre_id) REFERENCES plan_contable(id),
    INDEX idx_codigo (codigo),
    INDEX idx_tipo (tipo),
    INDEX idx_padre (padre_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS asientos_contables (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero INT NOT NULL,
    fecha DATE NOT NULL,
    concepto VARCHAR(500),
    tipo VARCHAR(20), -- APERTURA, OPERACION, AJUSTE, REGULARIZACION, CIERRE
    factura_id BIGINT,
    factura_compra_id BIGINT,
    descuadre DECIMAL(12,2) DEFAULT 0.00,
    fecha_creacion DATETIME,
    usuario_creacion VARCHAR(100),

    FOREIGN KEY (factura_id) REFERENCES facturas(id),
    FOREIGN KEY (factura_compra_id) REFERENCES facturas_compra(id),
    INDEX idx_numero (numero),
    INDEX idx_fecha (fecha),
    INDEX idx_tipo (tipo),
    UNIQUE KEY uk_numero_fecha (numero, fecha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS asientos_contables_lineas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asiento_id BIGINT NOT NULL,
    cuenta_id BIGINT NOT NULL,
    concepto VARCHAR(500),
    debe DECIMAL(12,2) DEFAULT 0.00,
    haber DECIMAL(12,2) DEFAULT 0.00,
    orden INT,

    FOREIGN KEY (asiento_id) REFERENCES asientos_contables(id) ON DELETE CASCADE,
    FOREIGN KEY (cuenta_id) REFERENCES plan_contable(id),
    INDEX idx_asiento (asiento_id),
    INDEX idx_cuenta (cuenta_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- DATOS INICIALES
-- ================================================================

-- Bancos de ejemplo
INSERT INTO bancos (codigo, nombre, iban, saldo_inicial, saldo_actual, activo, fecha_creacion)
VALUES
('BANCO001', 'Banco Principal - Cuenta Corriente', 'ES1234567890123456789012', 10000.00, 10000.00, TRUE, NOW()),
('BANCO002', 'Banco Secundario - Ahorro', 'ES9876543210987654321098', 5000.00, 5000.00, TRUE, NOW());

-- Cajas de ejemplo
INSERT INTO cajas (codigo, nombre, saldo_inicial, saldo_actual, activa, fecha_creacion)
VALUES
('CAJA001', 'Caja Principal', 500.00, 500.00, TRUE, NOW()),
('CAJA002', 'Caja Secundaria', 200.00, 200.00, TRUE, NOW());

-- Plan Contable Básico (simplificado)
INSERT INTO plan_contable (codigo, nombre, tipo, nivel, activa) VALUES
-- ACTIVO
('10', 'CAPITAL', 'PATRIMONIO_NETO', 1, TRUE),
('100', 'Capital Social', 'PATRIMONIO_NETO', 2, TRUE),
('12', 'RESULTADOS', 'PATRIMONIO_NETO', 1, TRUE),
('129', 'Resultado del ejercicio', 'PATRIMONIO_NETO', 2, TRUE),
('43', 'CLIENTES', 'ACTIVO', 1, TRUE),
('430', 'Clientes', 'ACTIVO', 2, TRUE),
('57', 'TESORERÍA', 'ACTIVO', 1, TRUE),
('570', 'Caja', 'ACTIVO', 2, TRUE),
('572', 'Bancos', 'ACTIVO', 2, TRUE),
-- PASIVO
('40', 'PROVEEDORES', 'PASIVO', 1, TRUE),
('400', 'Proveedores', 'PASIVO', 2, TRUE),
('47', 'HACIENDA PÚBLICA', 'PASIVO', 1, TRUE),
('472', 'IVA Soportado', 'ACTIVO', 2, TRUE),
('477', 'IVA Repercutido', 'PASIVO', 2, TRUE),
-- GASTOS
('60', 'COMPRAS', 'GASTOS', 1, TRUE),
('600', 'Compras de mercaderías', 'GASTOS', 2, TRUE),
('62', 'SERVICIOS EXTERIORES', 'GASTOS', 1, TRUE),
('621', 'Arrendamientos', 'GASTOS', 2, TRUE),
('623', 'Servicios profesionales', 'GASTOS', 2, TRUE),
('64', 'GASTOS DE PERSONAL', 'GASTOS', 1, TRUE),
('640', 'Sueldos y salarios', 'GASTOS', 2, TRUE),
-- INGRESOS
('70', 'VENTAS', 'INGRESOS', 1, TRUE),
('700', 'Ventas de mercaderías', 'INGRESOS', 2, TRUE);

-- Verificación
SELECT 'TABLAS CREADAS CORRECTAMENTE' as resultado;

SELECT TABLE_NAME, TABLE_ROWS
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'tahona'
AND TABLE_NAME IN (
    'pedidos_compra',
    'facturas_compra',
    'bancos',
    'movimientos_banco',
    'cajas',
    'movimientos_caja',
    'movimientos_stock',
    'presupuestos',
    'plan_contable',
    'asientos_contables'
)
ORDER BY TABLE_NAME;

