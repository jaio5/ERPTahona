-- ================================================
-- SCRIPT SQL: Nuevas tablas para funcionalidad completa
-- Módulos: Caja, Contabilidad
-- ================================================

USE tahona;

-- ================================================
-- TABLA: cajas
-- ================================================
CREATE TABLE IF NOT EXISTS cajas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha_apertura DATETIME NOT NULL,
    fecha_cierre DATETIME NULL,
    saldo_inicial DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    saldo_final DECIMAL(12,2) NULL,
    saldo_teorico DECIMAL(12,2) NULL,
    diferencia DECIMAL(12,2) NULL,
    estado VARCHAR(20) DEFAULT 'ABIERTA',
    usuario_apertura_id BIGINT NULL,
    usuario_cierre_id BIGINT NULL,
    observaciones TEXT NULL,
    FOREIGN KEY (usuario_apertura_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (usuario_cierre_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_estado (estado),
    INDEX idx_fecha_apertura (fecha_apertura)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- TABLA: caja_movimientos
-- ================================================
CREATE TABLE IF NOT EXISTS caja_movimientos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    caja_id BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL COMMENT 'INGRESO, GASTO, ARQUEO',
    concepto VARCHAR(255) NOT NULL,
    importe DECIMAL(12,2) NOT NULL,
    fecha DATETIME NOT NULL,
    usuario_id BIGINT NULL,
    forma_pago VARCHAR(50) NULL COMMENT 'EFECTIVO, TARJETA, TRANSFERENCIA',
    referencia VARCHAR(100) NULL,
    observaciones TEXT NULL,
    factura_id BIGINT NULL,
    factura_compra_id BIGINT NULL,
    FOREIGN KEY (caja_id) REFERENCES cajas(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (factura_id) REFERENCES facturas(id) ON DELETE SET NULL,
    INDEX idx_caja (caja_id),
    INDEX idx_tipo (tipo),
    INDEX idx_fecha (fecha),
    INDEX idx_forma_pago (forma_pago)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- TABLA: plan_cuentas (Contabilidad)
-- ================================================
CREATE TABLE IF NOT EXISTS plan_cuentas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(255) NOT NULL,
    tipo VARCHAR(50) NOT NULL COMMENT 'ACTIVO, PASIVO, GASTO, INGRESO, PATRIMONIO',
    nivel INT NOT NULL DEFAULT 1 COMMENT 'Nivel en el árbol contable',
    cuenta_padre_id BIGINT NULL,
    descripcion TEXT NULL,
    activo BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (cuenta_padre_id) REFERENCES plan_cuentas(id) ON DELETE SET NULL,
    INDEX idx_codigo (codigo),
    INDEX idx_tipo (tipo),
    INDEX idx_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- TABLA: asientos_contables
-- ================================================
CREATE TABLE IF NOT EXISTS asientos_contables (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero VARCHAR(50) NOT NULL UNIQUE,
    fecha DATE NOT NULL,
    concepto VARCHAR(255) NOT NULL,
    tipo VARCHAR(50) NOT NULL COMMENT 'APERTURA, CIERRE, TRASPASO, REGULARIZACION, OPERACION',
    debe DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    haber DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    usuario_id BIGINT NULL,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    observaciones TEXT NULL,
    factura_id BIGINT NULL,
    factura_compra_id BIGINT NULL,
    FOREIGN KEY (usuario_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (factura_id) REFERENCES facturas(id) ON DELETE SET NULL,
    INDEX idx_numero (numero),
    INDEX idx_fecha (fecha),
    INDEX idx_tipo (tipo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- TABLA: lineas_asiento
-- ================================================
CREATE TABLE IF NOT EXISTS lineas_asiento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asiento_id BIGINT NOT NULL,
    cuenta_id BIGINT NOT NULL,
    debe DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    haber DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    concepto VARCHAR(255) NULL,
    orden INT DEFAULT 0,
    FOREIGN KEY (asiento_id) REFERENCES asientos_contables(id) ON DELETE CASCADE,
    FOREIGN KEY (cuenta_id) REFERENCES plan_cuentas(id) ON DELETE RESTRICT,
    INDEX idx_asiento (asiento_id),
    INDEX idx_cuenta (cuenta_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- INSERTAR PLAN CONTABLE BÁSICO (PGC España simplificado)
-- ================================================

-- Grupo 1: FINANCIACIÓN BÁSICA
INSERT IGNORE INTO plan_cuentas (codigo, nombre, tipo, nivel) VALUES
('1', 'FINANCIACIÓN BÁSICA', 'PATRIMONIO', 1),
('10', 'CAPITAL', 'PATRIMONIO', 2),
('100', 'Capital Social', 'PATRIMONIO', 3),
('129', 'Resultado del Ejercicio', 'PATRIMONIO', 3);

-- Grupo 4: ACREEDORES Y DEUDORES POR OPERACIONES COMERCIALES
INSERT IGNORE INTO plan_cuentas (codigo, nombre, tipo, nivel) VALUES
('4', 'ACREEDORES Y DEUDORES', 'PASIVO', 1),
('43', 'CLIENTES', 'ACTIVO', 2),
('430', 'Clientes', 'ACTIVO', 3),
('40', 'PROVEEDORES', 'PASIVO', 2),
('400', 'Proveedores', 'PASIVO', 3),
('47', 'ADMINISTRACIONES PÚBLICAS', 'PASIVO', 2),
('477', 'Hacienda Pública IVA Repercutido', 'PASIVO', 3),
('472', 'Hacienda Pública IVA Soportado', 'ACTIVO', 3);

-- Grupo 5: CUENTAS FINANCIERAS
INSERT IGNORE INTO plan_cuentas (codigo, nombre, tipo, nivel) VALUES
('5', 'CUENTAS FINANCIERAS', 'ACTIVO', 1),
('57', 'TESORERÍA', 'ACTIVO', 2),
('570', 'Caja, euros', 'ACTIVO', 3),
('572', 'Bancos e instituciones de crédito', 'ACTIVO', 3);

-- Grupo 6: COMPRAS Y GASTOS
INSERT IGNORE INTO plan_cuentas (codigo, nombre, tipo, nivel) VALUES
('6', 'COMPRAS Y GASTOS', 'GASTO', 1),
('60', 'COMPRAS', 'GASTO', 2),
('600', 'Compras de mercaderías', 'GASTO', 3),
('62', 'SERVICIOS EXTERIORES', 'GASTO', 2),
('621', 'Arrendamientos y cánones', 'GASTO', 3),
('622', 'Reparaciones y conservación', 'GASTO', 3),
('623', 'Servicios de profesionales independientes', 'GASTO', 3),
('624', 'Transportes', 'GASTO', 3),
('626', 'Servicios bancarios', 'GASTO', 3),
('627', 'Publicidad, propaganda y relaciones públicas', 'GASTO', 3),
('628', 'Suministros', 'GASTO', 3),
('629', 'Otros servicios', 'GASTO', 3),
('64', 'GASTOS DE PERSONAL', 'GASTO', 2),
('640', 'Sueldos y salarios', 'GASTO', 3),
('642', 'Seguridad Social a cargo de la empresa', 'GASTO', 3);

-- Grupo 7: VENTAS E INGRESOS
INSERT IGNORE INTO plan_cuentas (codigo, nombre, tipo, nivel) VALUES
('7', 'VENTAS E INGRESOS', 'INGRESO', 1),
('70', 'VENTAS DE MERCADERÍAS', 'INGRESO', 2),
('700', 'Ventas de mercaderías', 'INGRESO', 3),
('705', 'Prestaciones de servicios', 'INGRESO', 3);

-- ================================================
-- ÍNDICES ADICIONALES PARA OPTIMIZACIÓN
-- ================================================

-- Optimizar búsquedas en facturas
CREATE INDEX IF NOT EXISTS idx_facturas_estado ON facturas(estado);
CREATE INDEX IF NOT EXISTS idx_facturas_cliente_fecha ON facturas(cliente_id, fecha);

-- Optimizar búsquedas en artículos
CREATE INDEX IF NOT EXISTS idx_articulos_activo ON articulos(activo);

-- Optimizar búsquedas en clientes
CREATE INDEX IF NOT EXISTS idx_clientes_activo ON clientes(activo);

-- ================================================
-- DATOS DE EJEMPLO PARA TESTING
-- ================================================

-- Insertar caja de ejemplo (solo si no existe ninguna)
INSERT INTO cajas (fecha_apertura, saldo_inicial, estado, usuario_apertura_id)
SELECT NOW(), 100.00, 'ABIERTA', 1
WHERE NOT EXISTS (SELECT 1 FROM cajas WHERE estado = 'ABIERTA');

-- ================================================
-- FIN DEL SCRIPT
-- ================================================

SELECT '✅ Tablas de funcionalidad completa creadas correctamente' AS Resultado;

