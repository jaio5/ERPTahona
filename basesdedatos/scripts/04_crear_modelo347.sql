-- Script para crear tabla modelo347_registros
-- Modelo 347 - Declaración anual de operaciones con terceros

CREATE TABLE IF NOT EXISTS modelo347_registros (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ejercicio INT NOT NULL,
    nif_declarado VARCHAR(20) NOT NULL,
    nombre_declarado VARCHAR(255) NOT NULL,
    tipo_operacion VARCHAR(1),
    clave_operacion VARCHAR(1),
    es_cliente BOOLEAN,
    es_proveedor BOOLEAN,
    importe_t1 DECIMAL(13,2) DEFAULT 0.00,
    importe_t2 DECIMAL(13,2) DEFAULT 0.00,
    importe_t3 DECIMAL(13,2) DEFAULT 0.00,
    importe_t4 DECIMAL(13,2) DEFAULT 0.00,
    importe_total DECIMAL(13,2) DEFAULT 0.00,
    importe_metalico_t1 DECIMAL(13,2) DEFAULT 0.00,
    importe_metalico_t2 DECIMAL(13,2) DEFAULT 0.00,
    importe_metalico_t3 DECIMAL(13,2) DEFAULT 0.00,
    importe_metalico_t4 DECIMAL(13,2) DEFAULT 0.00,
    importe_metalico_total DECIMAL(13,2) DEFAULT 0.00,
    provincia VARCHAR(2),
    pais VARCHAR(2) DEFAULT 'ES',
    numero_operaciones INT DEFAULT 0,
    generado BOOLEAN DEFAULT FALSE,
    observaciones VARCHAR(500),
    INDEX idx_ejercicio (ejercicio),
    INDEX idx_nif (nif_declarado),
    INDEX idx_importe (importe_total),
    UNIQUE KEY uk_ejercicio_nif (ejercicio, nif_declarado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Comentarios
ALTER TABLE modelo347_registros COMMENT = 'Modelo 347 - Declaración anual de operaciones con terceros >3.005,06€';

SELECT 'Tabla modelo347_registros creada correctamente' as Resultado;

