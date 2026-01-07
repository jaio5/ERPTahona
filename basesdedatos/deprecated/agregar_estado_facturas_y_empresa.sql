-- Script para añadir estado a facturas y tabla de configuración de empresa
-- Ejecutar este script en la base de datos tahona

USE tahona;

-- =====================================================
-- 1. AÑADIR CAMPO ESTADO A FACTURAS
-- =====================================================

-- Añadir campo estado_factura
ALTER TABLE facturas
ADD COLUMN IF NOT EXISTS estado VARCHAR(20) DEFAULT 'BORRADOR'
COMMENT 'Estado: BORRADOR, REVISION, EMITIDA, ANULADA';

-- Añadir campo verifactu_enviada
ALTER TABLE facturas
ADD COLUMN IF NOT EXISTS verifactu_enviada BOOLEAN DEFAULT FALSE
COMMENT 'Indica si ya se envió a Verifactu/AEAT';

-- Añadir campo fecha_emision_verifactu
ALTER TABLE facturas
ADD COLUMN IF NOT EXISTS fecha_emision_verifactu DATETIME
COMMENT 'Fecha y hora de emisión a Verifactu';

-- Añadir campo observaciones_revision
ALTER TABLE facturas
ADD COLUMN IF NOT EXISTS observaciones_revision TEXT
COMMENT 'Observaciones durante la revisión';

-- Crear índice para el estado
CREATE INDEX IF NOT EXISTS idx_facturas_estado ON facturas(estado);

-- Actualizar facturas existentes a EMITIDA (asumimos que ya están emitidas)
UPDATE facturas
SET estado = 'EMITIDA'
WHERE estado IS NULL OR estado = 'BORRADOR';

-- =====================================================
-- 2. CREAR TABLA DE CONFIGURACIÓN DE EMPRESA
-- =====================================================

CREATE TABLE IF NOT EXISTS empresa_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_empresa VARCHAR(255) NOT NULL COMMENT 'Nombre de la empresa',
    nombre_comercial VARCHAR(255) COMMENT 'Nombre comercial/marca',
    cif VARCHAR(20) NOT NULL COMMENT 'CIF/NIF de la empresa',
    direccion TEXT COMMENT 'Dirección completa',
    codigo_postal VARCHAR(10) COMMENT 'Código postal',
    ciudad VARCHAR(100) COMMENT 'Ciudad',
    provincia VARCHAR(100) COMMENT 'Provincia',
    pais VARCHAR(100) DEFAULT 'España' COMMENT 'País',
    telefono VARCHAR(20) COMMENT 'Teléfono de contacto',
    email VARCHAR(255) COMMENT 'Email de contacto',
    web VARCHAR(255) COMMENT 'Página web',

    -- Datos fiscales adicionales
    registro_mercantil VARCHAR(255) COMMENT 'Registro mercantil',
    registro_sanitario VARCHAR(100) COMMENT 'Registro sanitario (para panaderías)',

    -- Configuración Verifactu
    verifactu_habilitado BOOLEAN DEFAULT TRUE COMMENT 'Si está habilitado Verifactu',
    verifactu_nif_emisor VARCHAR(20) COMMENT 'NIF del emisor para Verifactu',
    verifactu_nombre_sistema VARCHAR(100) COMMENT 'Nombre del sistema informático',
    verifactu_version_sistema VARCHAR(50) COMMENT 'Versión del sistema',
    verifactu_id_dispositivo VARCHAR(50) COMMENT 'ID del dispositivo/terminal',

    -- Control
    activo BOOLEAN DEFAULT TRUE COMMENT 'Si está activa (solo debe haber una)',
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_empresa_activa (activo)
) COMMENT 'Configuración de la empresa para facturas y Verifactu';

-- Insertar datos de GRUPO BABO (del albarán de la imagen)
INSERT INTO empresa_config (
    nombre_empresa,
    nombre_comercial,
    cif,
    direccion,
    codigo_postal,
    ciudad,
    provincia,
    telefono,
    registro_sanitario,
    verifactu_nif_emisor,
    verifactu_nombre_sistema,
    verifactu_version_sistema,
    activo
) VALUES (
    'GRUPO BABO, S.Coop.V.L.',
    'LA TAHONA EL ALTET',
    'F54059985',
    'Armada Española, P.2 Nº213',
    '03195',
    'El Altet - ELCHE',
    'Alicante',
    '965 68 73 58',
    'R.G.S. EM-20.05033/A',
    'F54059985',
    'ERP Tahona',
    '1.0.0',
    TRUE
);

-- =====================================================
-- 3. VERIFICAR CAMBIOS
-- =====================================================

-- Ver estados de facturas
SELECT
    'facturas' as tabla,
    COUNT(*) as total,
    SUM(CASE WHEN estado = 'BORRADOR' THEN 1 ELSE 0 END) as borradores,
    SUM(CASE WHEN estado = 'REVISION' THEN 1 ELSE 0 END) as en_revision,
    SUM(CASE WHEN estado = 'EMITIDA' THEN 1 ELSE 0 END) as emitidas,
    SUM(CASE WHEN estado = 'ANULADA' THEN 1 ELSE 0 END) as anuladas,
    SUM(CASE WHEN verifactu_enviada = TRUE THEN 1 ELSE 0 END) as enviadas_verifactu
FROM facturas;

-- Ver configuración de empresa
SELECT * FROM empresa_config WHERE activo = TRUE;

-- Estadísticas de Verifactu
SELECT
    estado,
    COUNT(*) as cantidad
FROM verifactu_evidence
GROUP BY estado;

