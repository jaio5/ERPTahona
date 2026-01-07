-- =====================================================
-- CONFIGURACIÓN DE EMPRESA PARA GRUPO BABO
-- Script para configurar datos de empresa en ERP
-- =====================================================

USE tahona;

-- Eliminar configuración existente si la hay
DELETE FROM empresa_config WHERE id = 1;

-- Insertar configuración de GRUPO BABO
INSERT INTO empresa_config (
    id,
    nombre_empresa,
    nombre_comercial,
    cif,
    direccion,
    ciudad,
    provincia,
    codigo_postal,
    pais,
    telefono,
    email,
    web,
    registro_mercantil,
    registro_sanitario,
    verifactu_habilitado,
    verifactu_nif_emisor,
    verifactu_id_dispositivo,
    verifactu_nombre_sistema,
    verifactu_version_sistema,
    activo,
    fecha_creacion
) VALUES (
    1,
    'GRUPO BABO S.L.',
    'Panadería Tahona',
    'B12345678',  -- ⚠️ ACTUALIZAR con CIF real
    'Calle Real 123',  -- ⚠️ ACTUALIZAR con dirección real
    'Alicante',
    'Alicante',
    '03001',
    'España',
    '965123456',  -- ⚠️ ACTUALIZAR con teléfono real
    'info@grupobabo.es',  -- ⚠️ ACTUALIZAR con email real
    'www.grupobabo.es',  -- ⚠️ ACTUALIZAR si existe web
    'Registro Mercantil de Alicante, Tomo XXX, Folio XXX',  -- ⚠️ ACTUALIZAR si aplica
    'RGSEAA: XX.XXXXX/A',  -- ⚠️ ACTUALIZAR con registro sanitario real
    TRUE,  -- Verifactu habilitado
    'B12345678',  -- ⚠️ Mismo CIF
    'DISPOSITIVO001',  -- ID único del dispositivo/software
    'ERP Tahona v1.0',
    '1.0.0',
    TRUE,
    NOW()
);

-- Verificar que se insertó correctamente
SELECT
    id,
    nombre_empresa,
    cif,
    direccion,
    ciudad,
    telefono,
    email,
    verifactu_habilitado,
    verifactu_nif_emisor,
    verifactu_id_dispositivo
FROM empresa_config
WHERE id = 1;

-- Mensaje final
SELECT 'CONFIGURACIÓN DE EMPRESA COMPLETADA' as Resultado;
SELECT '⚠️ RECUERDA ACTUALIZAR LOS DATOS CON INFORMACIÓN REAL' as Aviso;

