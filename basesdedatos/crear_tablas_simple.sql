-- Script simplificado para crear todas las tablas necesarias
USE tahona;

-- Facturas de compra
CREATE TABLE IF NOT EXISTS facturas_compra (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero VARCHAR(100) NOT NULL UNIQUE,
    fecha DATE NOT NULL,
    proveedor_id BIGINT NOT NULL,
    base_imponible DECIMAL(12,2) NOT NULL,
    importe_iva DECIMAL(12,2) NOT NULL,
    importe_recargo DECIMAL(12,2) DEFAULT 0.00,
    importe_retencion DECIMAL(12,2) DEFAULT 0.00,
    total DECIMAL(12,2) NOT NULL,
    estado VARCHAR(20) DEFAULT 'PENDIENTE',
    pagada BOOLEAN DEFAULT FALSE,
    fecha_creacion DATETIME,
    FOREIGN KEY (proveedor_id) REFERENCES proveedores(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS facturas_compra_lineas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    factura_compra_id BIGINT NOT NULL,
    articulo_id BIGINT,
    descripcion VARCHAR(500),
    cantidad DECIMAL(10,2) NOT NULL,
    precio_unitario DECIMAL(10,4) NOT NULL,
    importe DECIMAL(12,2) NOT NULL,
    FOREIGN KEY (factura_compra_id) REFERENCES facturas_compra(id) ON DELETE CASCADE,
    FOREIGN KEY (articulo_id) REFERENCES articulos(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Pedidos de compra lineas
CREATE TABLE IF NOT EXISTS pedidos_compra_lineas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_compra_id BIGINT NOT NULL,
    articulo_id BIGINT,
    descripcion VARCHAR(500),
    cantidad DECIMAL(10,2) NOT NULL,
    cantidad_recibida DECIMAL(10,2) DEFAULT 0.00,
    precio_unitario DECIMAL(10,4) NOT NULL,
    importe DECIMAL(12,2) NOT NULL,
    FOREIGN KEY (pedido_compra_id) REFERENCES pedidos_compra(id) ON DELETE CASCADE,
    FOREIGN KEY (articulo_id) REFERENCES articulos(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Bancos
CREATE TABLE IF NOT EXISTS bancos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(100) NOT NULL UNIQUE,
    nombre VARCHAR(255) NOT NULL,
    iban VARCHAR(34),
    saldo_inicial DECIMAL(12,2) DEFAULT 0.00,
    saldo_actual DECIMAL(12,2) DEFAULT 0.00,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS movimientos_banco (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    banco_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    tipo VARCHAR(20),
    concepto VARCHAR(500),
    importe DECIMAL(12,2) NOT NULL,
    saldo_resultante DECIMAL(12,2),
    fecha_creacion DATETIME,
    FOREIGN KEY (banco_id) REFERENCES bancos(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Cajas
CREATE TABLE IF NOT EXISTS cajas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(100) NOT NULL UNIQUE,
    nombre VARCHAR(255) NOT NULL,
    saldo_inicial DECIMAL(12,2) DEFAULT 0.00,
    saldo_actual DECIMAL(12,2) DEFAULT 0.00,
    activa BOOLEAN DEFAULT TRUE,
    fecha_creacion DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS movimientos_caja (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    caja_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    tipo VARCHAR(20),
    concepto VARCHAR(500),
    importe DECIMAL(12,2) NOT NULL,
    saldo_resultante DECIMAL(12,2),
    fecha_creacion DATETIME,
    FOREIGN KEY (caja_id) REFERENCES cajas(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Presupuestos
CREATE TABLE IF NOT EXISTS presupuestos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero VARCHAR(100) NOT NULL UNIQUE,
    fecha DATE NOT NULL,
    cliente_id BIGINT NOT NULL,
    total DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    estado VARCHAR(20) DEFAULT 'BORRADOR',
    fecha_creacion DATETIME,
    FOREIGN KEY (cliente_id) REFERENCES clientes(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS presupuestos_lineas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    presupuesto_id BIGINT NOT NULL,
    articulo_id BIGINT,
    descripcion VARCHAR(500),
    cantidad DECIMAL(10,2) NOT NULL,
    precio_unitario DECIMAL(10,4) NOT NULL,
    importe DECIMAL(12,2) NOT NULL,
    FOREIGN KEY (presupuesto_id) REFERENCES presupuestos(id) ON DELETE CASCADE,
    FOREIGN KEY (articulo_id) REFERENCES articulos(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Movimientos de stock
CREATE TABLE IF NOT EXISTS movimientos_stock (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha DATE NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    articulo_id BIGINT NOT NULL,
    almacen_origen_id BIGINT,
    almacen_destino_id BIGINT,
    cantidad DECIMAL(10,2) NOT NULL,
    precio_unitario DECIMAL(10,4),
    importe DECIMAL(12,2),
    concepto VARCHAR(500),
    fecha_creacion DATETIME,
    FOREIGN KEY (articulo_id) REFERENCES articulos(id),
    FOREIGN KEY (almacen_origen_id) REFERENCES almacenes(id),
    FOREIGN KEY (almacen_destino_id) REFERENCES almacenes(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Plan contable
CREATE TABLE IF NOT EXISTS plan_contable (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(255) NOT NULL,
    tipo VARCHAR(20),
    nivel INT,
    padre_id BIGINT,
    activa BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (padre_id) REFERENCES plan_contable(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Asientos contables
CREATE TABLE IF NOT EXISTS asientos_contables (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero INT NOT NULL,
    fecha DATE NOT NULL,
    concepto VARCHAR(500),
    tipo VARCHAR(20),
    descuadre DECIMAL(12,2) DEFAULT 0.00,
    fecha_creacion DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS asientos_contables_lineas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asiento_id BIGINT NOT NULL,
    cuenta_id BIGINT NOT NULL,
    concepto VARCHAR(500),
    debe DECIMAL(12,2) DEFAULT 0.00,
    haber DECIMAL(12,2) DEFAULT 0.00,
    orden INT,
    FOREIGN KEY (asiento_id) REFERENCES asientos_contables(id) ON DELETE CASCADE,
    FOREIGN KEY (cuenta_id) REFERENCES plan_contable(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insertar datos de ejemplo
INSERT IGNORE INTO bancos (codigo, nombre, iban, saldo_inicial, saldo_actual, activo, fecha_creacion) VALUES
('BANCO001', 'Banco Principal', 'ES1234567890123456789012', 10000.00, 10000.00, TRUE, NOW()),
('BANCO002', 'Banco Secundario', 'ES9876543210987654321098', 5000.00, 5000.00, TRUE, NOW());

INSERT IGNORE INTO cajas (codigo, nombre, saldo_inicial, saldo_actual, activa, fecha_creacion) VALUES
('CAJA001', 'Caja Principal', 500.00, 500.00, TRUE, NOW()),
('CAJA002', 'Caja Secundaria', 200.00, 200.00, TRUE, NOW());

-- Plan contable básico
INSERT IGNORE INTO plan_contable (codigo, nombre, tipo, nivel, activa) VALUES
('10', 'CAPITAL', 'PATRIMONIO_NETO', 1, TRUE),
('43', 'CLIENTES', 'ACTIVO', 1, TRUE),
('430', 'Clientes', 'ACTIVO', 2, TRUE),
('40', 'PROVEEDORES', 'PASIVO', 1, TRUE),
('400', 'Proveedores', 'PASIVO', 2, TRUE),
('57', 'TESORERÍA', 'ACTIVO', 1, TRUE),
('570', 'Caja', 'ACTIVO', 2, TRUE),
('572', 'Bancos', 'ACTIVO', 2, TRUE),
('60', 'COMPRAS', 'GASTOS', 1, TRUE),
('600', 'Compras de mercaderías', 'GASTOS', 2, TRUE),
('70', 'VENTAS', 'INGRESOS', 1, TRUE),
('700', 'Ventas de mercaderías', 'INGRESOS', 2, TRUE),
('472', 'IVA Soportado', 'ACTIVO', 2, TRUE),
('477', 'IVA Repercutido', 'PASIVO', 2, TRUE);

SELECT 'Tablas creadas correctamente' AS Resultado;

