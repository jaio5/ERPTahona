-- ======================================================
-- FASES 3-8: COMPLETAS - Scripts SQL
-- ERP Panadería Tahona
-- Fecha: 26 de diciembre de 2025
-- ======================================================

-- ==========================================
-- FASE 4: FINANCIERO
-- ==========================================

CREATE TABLE IF NOT EXISTS bancos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    swift_bic VARCHAR(11),
    iban VARCHAR(34) NOT NULL,
    numero_cuenta VARCHAR(20),
    entidad VARCHAR(4),
    oficina VARCHAR(4),
    sucursal VARCHAR(100),
    saldo_actual DECIMAL(12, 2) DEFAULT 0,
    saldo_inicial DECIMAL(12, 2) DEFAULT 0,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    es_principal BOOLEAN DEFAULT FALSE,
    contacto VARCHAR(100),
    telefono VARCHAR(20),
    email VARCHAR(100),
    observaciones TEXT,
    INDEX idx_activo (activo),
    INDEX idx_iban (iban)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS movimientos_bancarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    banco_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    concepto VARCHAR(200),
    importe DECIMAL(12, 2) NOT NULL,
    saldo_anterior DECIMAL(12, 2),
    saldo_posterior DECIMAL(12, 2),
    factura_id BIGINT,
    numero_operacion VARCHAR(50),
    conciliado BOOLEAN DEFAULT FALSE,
    fecha_conciliacion DATE,
    observaciones TEXT,
    FOREIGN KEY (banco_id) REFERENCES bancos(id),
    INDEX idx_banco_fecha (banco_id, fecha),
    INDEX idx_conciliado (conciliado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- FASE 5: CRM
-- ==========================================

CREATE TABLE IF NOT EXISTS leads (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    empresa VARCHAR(200),
    email VARCHAR(100),
    telefono VARCHAR(20),
    estado VARCHAR(30) NOT NULL DEFAULT 'NUEVO',
    origen VARCHAR(50),
    interes VARCHAR(200),
    presupuesto_estimado DECIMAL(12, 2),
    probabilidad_cierre INT,
    fecha_creacion DATE,
    fecha_ultima_interaccion DATE,
    usuario_asignado_id BIGINT,
    notas TEXT,
    activo BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (usuario_asignado_id) REFERENCES usuarios(id),
    INDEX idx_estado (estado),
    INDEX idx_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS oportunidades (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    cliente_id BIGINT,
    lead_id BIGINT,
    etapa VARCHAR(30) NOT NULL DEFAULT 'PROSPECTO',
    valor_estimado DECIMAL(12, 2) NOT NULL,
    probabilidad INT,
    fecha_creacion DATE,
    fecha_cierre_estimada DATE,
    fecha_cierre_real DATE,
    usuario_responsable_id BIGINT,
    motivo_perdida VARCHAR(200),
    notas TEXT,
    FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    FOREIGN KEY (lead_id) REFERENCES leads(id),
    FOREIGN KEY (usuario_responsable_id) REFERENCES usuarios(id),
    INDEX idx_etapa (etapa),
    INDEX idx_cliente (cliente_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- FASE 7: TPV
-- ==========================================

CREATE TABLE IF NOT EXISTS cajas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    usuario_apertura_id BIGINT,
    fecha_apertura DATETIME NOT NULL,
    saldo_inicial DECIMAL(10, 2) DEFAULT 0,
    fecha_cierre DATETIME,
    saldo_final DECIMAL(10, 2),
    total_ventas DECIMAL(10, 2) DEFAULT 0,
    total_efectivo DECIMAL(10, 2) DEFAULT 0,
    total_tarjeta DECIMAL(10, 2) DEFAULT 0,
    numero_tickets INT DEFAULT 0,
    estado VARCHAR(20) NOT NULL DEFAULT 'ABIERTA',
    observaciones TEXT,
    FOREIGN KEY (usuario_apertura_id) REFERENCES usuarios(id),
    INDEX idx_estado (estado),
    INDEX idx_fecha_apertura (fecha_apertura)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS tickets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero VARCHAR(20) NOT NULL,
    caja_id BIGINT NOT NULL,
    fecha DATETIME NOT NULL,
    cliente_id BIGINT,
    total DECIMAL(10, 2) NOT NULL,
    metodo_pago VARCHAR(20) NOT NULL,
    efectivo_recibido DECIMAL(10, 2),
    cambio DECIMAL(10, 2),
    usuario_id BIGINT,
    factura_id BIGINT,
    observaciones VARCHAR(200),
    FOREIGN KEY (caja_id) REFERENCES cajas(id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    INDEX idx_caja (caja_id),
    INDEX idx_fecha (fecha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- FASE 8: COMPRAS
-- ==========================================

CREATE TABLE IF NOT EXISTS pedidos_compra (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero VARCHAR(50) NOT NULL,
    proveedor_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    fecha_entrega_estimada DATE,
    estado VARCHAR(30) NOT NULL DEFAULT 'BORRADOR',
    total DECIMAL(12, 2),
    usuario_creador_id BIGINT,
    observaciones TEXT,
    recibido BOOLEAN DEFAULT FALSE,
    fecha_recepcion DATE,
    FOREIGN KEY (proveedor_id) REFERENCES proveedores(id),
    FOREIGN KEY (usuario_creador_id) REFERENCES usuarios(id),
    INDEX idx_proveedor (proveedor_id),
    INDEX idx_estado (estado),
    INDEX idx_fecha (fecha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS lineas_pedido_compra (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_compra_id BIGINT NOT NULL,
    articulo_id BIGINT NOT NULL,
    cantidad DECIMAL(10, 2) NOT NULL,
    precio_compra DECIMAL(10, 2) NOT NULL,
    descuento DECIMAL(5, 2) DEFAULT 0,
    total DECIMAL(10, 2) NOT NULL,
    recibido DECIMAL(10, 2) DEFAULT 0,
    FOREIGN KEY (pedido_compra_id) REFERENCES pedidos_compra(id),
    FOREIGN KEY (articulo_id) REFERENCES articulos(id),
    INDEX idx_pedido (pedido_compra_id),
    INDEX idx_articulo (articulo_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- ÍNDICES ADICIONALES PARA RENDIMIENTO
-- ==========================================

CREATE INDEX idx_movimientos_tipo ON movimientos_bancarios(tipo);
CREATE INDEX idx_leads_origen ON leads(origen);
CREATE INDEX idx_oportunidades_valor ON oportunidades(valor_estimado);
CREATE INDEX idx_tickets_metodo ON tickets(metodo_pago);

-- ==========================================
-- FIN DEL SCRIPT FASES 3-8
-- ==========================================

SELECT 'Fases 3-8 completadas: Tablas creadas correctamente' AS Resultado;

