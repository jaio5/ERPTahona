-- Script para añadir campos necesarios a la tabla verifactu_evidence
-- Ejecutar este script en la base de datos tahona

USE tahona;

-- Añadir campo estado
ALTER TABLE verifactu_evidence
ADD COLUMN IF NOT EXISTS estado VARCHAR(50) DEFAULT 'PENDIENTE'
COMMENT 'Estado de la evidencia: PENDIENTE, ENVIADO, ERROR, VERIFICADO';

-- Añadir campo error_message
ALTER TABLE verifactu_evidence
ADD COLUMN IF NOT EXISTS error_message TEXT
COMMENT 'Mensaje de error si el envío falló';

-- Añadir campo fecha_envio
ALTER TABLE verifactu_evidence
ADD COLUMN IF NOT EXISTS fecha_envio DATETIME
COMMENT 'Fecha y hora del envío a AEAT';

-- Añadir campo codigo_respuesta_aeat
ALTER TABLE verifactu_evidence
ADD COLUMN IF NOT EXISTS codigo_respuesta_aeat VARCHAR(100)
COMMENT 'Código de respuesta de la AEAT';

-- Crear índices para mejorar el rendimiento
CREATE INDEX IF NOT EXISTS idx_verifactu_estado ON verifactu_evidence(estado);
CREATE INDEX IF NOT EXISTS idx_verifactu_serie ON verifactu_evidence(serie);
CREATE INDEX IF NOT EXISTS idx_verifactu_fecha_emision ON verifactu_evidence(fecha_emision);
CREATE INDEX IF NOT EXISTS idx_verifactu_hash_anterior ON verifactu_evidence(hash_anterior);

-- Actualizar evidencias existentes para que tengan estado ENVIADO si tienen metadata
UPDATE verifactu_evidence
SET estado = 'ENVIADO'
WHERE estado IS NULL AND metadata IS NOT NULL AND metadata != '{}';

-- Actualizar evidencias sin estado a PENDIENTE
UPDATE verifactu_evidence
SET estado = 'PENDIENTE'
WHERE estado IS NULL;

-- Verificar cambios
SELECT
    'verifactu_evidence' as tabla,
    COUNT(*) as total_registros,
    SUM(CASE WHEN estado = 'PENDIENTE' THEN 1 ELSE 0 END) as pendientes,
    SUM(CASE WHEN estado = 'ENVIADO' THEN 1 ELSE 0 END) as enviados,
    SUM(CASE WHEN estado = 'ERROR' THEN 1 ELSE 0 END) as errores,
    SUM(CASE WHEN estado = 'VERIFICADO' THEN 1 ELSE 0 END) as verificados
FROM verifactu_evidence;

