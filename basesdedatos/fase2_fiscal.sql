-- ======================================================
-- FASE 2: FISCAL Y CONTABLE - Scripts SQL
-- ERP Panadería Tahona
-- Fecha: 26 de diciembre de 2025
-- ======================================================

-- ==========================================
-- 1. TABLA TIPOS DE IVA
-- ==========================================
CREATE TABLE IF NOT EXISTS tipos_iva (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    tipo_iva DECIMAL(5, 2) NOT NULL,
    recargo_equivalencia DECIMAL(5, 2),
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    es_por_defecto BOOLEAN DEFAULT FALSE,
    codigo_aeat VARCHAR(10),
    descripcion TEXT,
    orden INT,
    INDEX idx_activo (activo),
    INDEX idx_vigencia (fecha_inicio, fecha_fin)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- 2. TABLA LIBRO FACTURAS EMITIDAS
-- ==========================================
CREATE TABLE IF NOT EXISTS libro_facturas_emitidas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    factura_id BIGINT,
    fecha_expedicion DATE NOT NULL,
    fecha_operacion DATE,
    serie VARCHAR(10) NOT NULL,
    numero VARCHAR(50) NOT NULL,
    numero_factura_completo VARCHAR(100),
    tipo_factura VARCHAR(30),
    cif_destinatario VARCHAR(20),
    nombre_destinatario VARCHAR(200),
    base_imponible DECIMAL(10, 2) NOT NULL,
    tipo_iva DECIMAL(5, 2) NOT NULL,
    cuota_iva DECIMAL(10, 2) NOT NULL,
    tipo_recargo DECIMAL(5, 2),
    cuota_recargo DECIMAL(10, 2),
    total_factura DECIMAL(10, 2) NOT NULL,
    tipo_no_exenta VARCHAR(30),
    regimen_especial VARCHAR(50),
    factura_rectificativa_tipo VARCHAR(2),
    factura_rectificativa_base DECIMAL(10, 2),
    factura_rectificativa_cuota DECIMAL(10, 2),
    numero_registro INT,
    ejercicio INT,
    periodo INT,
    intracomunitaria BOOLEAN DEFAULT FALSE,
    exportacion BOOLEAN DEFAULT FALSE,
    clave_operacion VARCHAR(2),
    observaciones TEXT,
    FOREIGN KEY (factura_id) REFERENCES facturas(id),
    INDEX idx_ejercicio_periodo (ejercicio, periodo),
    INDEX idx_numero_registro (numero_registro),
    INDEX idx_fecha (fecha_expedicion),
    INDEX idx_cif (cif_destinatario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- 3. TABLA LIBRO FACTURAS RECIBIDAS
-- ==========================================
CREATE TABLE IF NOT EXISTS libro_facturas_recibidas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    factura_compra_id BIGINT,
    fecha_expedicion DATE NOT NULL,
    fecha_operacion DATE,
    fecha_contable DATE,
    fecha_registro DATE,
    numero_factura VARCHAR(100) NOT NULL,
    tipo_factura VARCHAR(30),
    cif_emisor VARCHAR(20) NOT NULL,
    nombre_emisor VARCHAR(200) NOT NULL,
    pais_emisor VARCHAR(2),
    base_imponible DECIMAL(10, 2) NOT NULL,
    tipo_iva DECIMAL(5, 2) NOT NULL,
    cuota_iva_soportada DECIMAL(10, 2) NOT NULL,
    cuota_iva_deducible DECIMAL(10, 2),
    porcentaje_deduccion DECIMAL(5, 2),
    total_factura DECIMAL(10, 2) NOT NULL,
    tipo_gasto VARCHAR(50),
    inversion_sujeto_pasivo BOOLEAN DEFAULT FALSE,
    regimen_especial VARCHAR(50),
    factura_rectificativa BOOLEAN DEFAULT FALSE,
    factura_simplificada BOOLEAN DEFAULT FALSE,
    numero_registro INT,
    ejercicio INT,
    periodo INT,
    intracomunitaria BOOLEAN DEFAULT FALSE,
    importacion BOOLEAN DEFAULT FALSE,
    clave_operacion VARCHAR(2),
    deducible_pro_rata BOOLEAN DEFAULT FALSE,
    observaciones TEXT,
    proveedor_id BIGINT,
    FOREIGN KEY (proveedor_id) REFERENCES proveedores(id),
    INDEX idx_ejercicio_periodo (ejercicio, periodo),
    INDEX idx_numero_registro (numero_registro),
    INDEX idx_fecha (fecha_expedicion),
    INDEX idx_cif (cif_emisor)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- 4. TABLA MODELO 347 REGISTROS
-- ==========================================
CREATE TABLE IF NOT EXISTS modelo_347_registro (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ejercicio INT NOT NULL,
    nif_declarante VARCHAR(20) NOT NULL,
    nif_tercero VARCHAR(20) NOT NULL,
    nombre_tercero VARCHAR(200) NOT NULL,
    provincia_tercero VARCHAR(2),
    pais_tercero VARCHAR(2),
    clave_operacion VARCHAR(1) NOT NULL,
    importe_operaciones DECIMAL(12, 2) NOT NULL,
    importe_primer_trimestre DECIMAL(12, 2),
    importe_segundo_trimestre DECIMAL(12, 2),
    importe_tercer_trimestre DECIMAL(12, 2),
    importe_cuarto_trimestre DECIMAL(12, 2),
    seguros_y_capitalizacion BOOLEAN DEFAULT FALSE,
    arrendamiento_local_negocio BOOLEAN DEFAULT FALSE,
    operacion_regimen_simplificado BOOLEAN DEFAULT FALSE,
    operacion_criterio_caja BOOLEAN DEFAULT FALSE,
    operacion_inversion_sujeto_pasivo BOOLEAN DEFAULT FALSE,
    importe_metalico DECIMAL(12, 2),
    numero_facturas INT,
    generado BOOLEAN DEFAULT FALSE,
    incluido_en_declaracion BOOLEAN DEFAULT FALSE,
    INDEX idx_ejercicio (ejercicio),
    INDEX idx_nif_tercero (nif_tercero),
    INDEX idx_generado (generado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- 5. INSERTAR TIPOS DE IVA ESTÁNDAR
-- ==========================================
INSERT INTO tipos_iva (nombre, tipo_iva, recargo_equivalencia, fecha_inicio, activo, es_por_defecto, codigo_aeat, descripcion, orden) VALUES
('General', 21.00, 5.20, '2012-09-01', TRUE, TRUE, 'S1', 'IVA general del 21%', 1),
('Reducido', 10.00, 1.40, '2012-09-01', TRUE, FALSE, 'S1', 'IVA reducido del 10%', 2),
('Superreducido', 4.00, 0.50, '2012-09-01', TRUE, FALSE, 'S1', 'IVA superreducido del 4%', 3),
('Exento', 0.00, 0.00, '2012-09-01', TRUE, FALSE, 'E', 'Operación exenta de IVA', 4);

-- ==========================================
-- 6. ÍNDICES ADICIONALES PARA RENDIMIENTO
-- ==========================================

-- Índices para consultas frecuentes de libros
CREATE INDEX idx_libro_emitidas_clave ON libro_facturas_emitidas(clave_operacion);
CREATE INDEX idx_libro_emitidas_especiales ON libro_facturas_emitidas(intracomunitaria, exportacion);
CREATE INDEX idx_libro_recibidas_especiales ON libro_facturas_recibidas(intracomunitaria, importacion, inversion_sujeto_pasivo);

-- Índices para Modelo 347
CREATE INDEX idx_347_ejercicio_clave ON modelo_347_registro(ejercicio, clave_operacion);
CREATE INDEX idx_347_importe ON modelo_347_registro(importe_operaciones);

-- ==========================================
-- FIN DEL SCRIPT FASE 2
-- ==========================================

SELECT 'Fase 2: Fiscal y Contable - Tablas creadas correctamente' AS Resultado;

