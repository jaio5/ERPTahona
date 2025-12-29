-- ============================================
-- CONFIGURACIÓN DE EMPRESA: GRUPO BABO
-- ============================================
-- Script para configurar los datos de GRUPO BABO
-- necesarios para Verifactu y facturación
-- ============================================

USE tahona;

-- Insertar o actualizar configuración de empresa
INSERT INTO empresa_config (
    -- Datos generales de la empresa
    nombre_empresa,
    cif,
    direccion,
    codigo_postal,
    poblacion,
    provincia,
    telefono,
    email,
    web,

    -- Datos bancarios
    iban,
    banco,

    -- Datos Verifactu específicos (AEAT)
    verifactu_habilitado,
    verifactu_nif_emisor,
    verifactu_nombre_software,
    verifactu_version_software,
    verifactu_num_instalacion,
    verifactu_tipo_comunicacion,

    -- Configuración fiscal
    regimen_iva,
    tipo_iva_defecto,
    retencion_irpf_defecto,

    -- Logo y diseño
    logo_path,
    color_corporativo,

    -- Estado
    activo,
    fecha_creacion,
    fecha_modificacion
) VALUES (
    -- ========== DATOS GENERALES ==========
    'GRUPO BABO, S.Coop.V.L.',           -- nombre_empresa
    'F54059985',                          -- cif

    -- ⚠️ ACTUALIZAR CON DATOS REALES DE GRUPO BABO
    'C/ Ejemplo, 123',                    -- direccion (CAMBIAR)
    '03001',                              -- codigo_postal (CAMBIAR)
    'Alicante',                           -- poblacion (CAMBIAR)
    'Alicante',                           -- provincia (CAMBIAR)
    '965123456',                          -- telefono (CAMBIAR)
    'info@grupobabo.es',                  -- email (CAMBIAR)
    'https://www.grupobabo.es',           -- web (CAMBIAR o NULL)

    -- ========== DATOS BANCARIOS ==========
    'ES00 0000 0000 0000 0000 0000',     -- iban (CAMBIAR o NULL)
    'Banco Ejemplo',                      -- banco (CAMBIAR o NULL)

    -- ========== VERIFACTU (AEAT) ==========
    TRUE,                                 -- verifactu_habilitado (TRUE para activar)
    'F54059985',                          -- verifactu_nif_emisor (mismo que CIF)
    'ERP Panadería Tahona',               -- verifactu_nombre_software
    '1.0.0',                              -- verifactu_version_software
    '001',                                -- verifactu_num_instalacion (único por instalación)
    'WEB_SERVICE',                        -- verifactu_tipo_comunicacion (WEB_SERVICE o API_REST)

    -- ========== CONFIGURACIÓN FISCAL ==========
    'GENERAL',                            -- regimen_iva (GENERAL, SIMPLIFICADO, etc.)
    21.00,                                -- tipo_iva_defecto (21% IVA general)
    0.00,                                 -- retencion_irpf_defecto (0% o el que aplique)

    -- ========== LOGO Y DISEÑO ==========
    '/images/logo_grupo_babo.png',       -- logo_path (o NULL si no hay logo)
    '#2196F3',                            -- color_corporativo (azul por defecto)

    -- ========== ESTADO ==========
    TRUE,                                 -- activo
    NOW(),                                -- fecha_creacion
    NOW()                                 -- fecha_modificacion
)
ON DUPLICATE KEY UPDATE
    -- Si ya existe, actualizar solo ciertos campos
    nombre_empresa = VALUES(nombre_empresa),
    cif = VALUES(cif),
    verifactu_habilitado = VALUES(verifactu_habilitado),
    verifactu_nif_emisor = VALUES(verifactu_nif_emisor),
    verifactu_nombre_software = VALUES(verifactu_nombre_software),
    verifactu_version_software = VALUES(verifactu_version_software),
    fecha_modificacion = NOW();

-- ============================================
-- VERIFICAR CONFIGURACIÓN
-- ============================================

SELECT
    '✅ CONFIGURACIÓN DE EMPRESA' AS resultado,
    nombre_empresa,
    cif,
    CONCAT(direccion, ', ', codigo_postal, ' ', poblacion) AS direccion_completa,
    telefono,
    email,
    CASE
        WHEN verifactu_habilitado = TRUE THEN '✅ Verifactu HABILITADO'
        ELSE '❌ Verifactu DESHABILITADO'
    END AS estado_verifactu,
    verifactu_nif_emisor AS nif_emisor_verifactu,
    verifactu_num_instalacion AS num_instalacion
FROM empresa_config
WHERE activo = TRUE;

-- ============================================
-- VERIFICAR DATOS PARA FACTURACIÓN
-- ============================================

-- Verificar que hay clientes
SELECT
    '✅ VERIFICACIÓN DE DATOS' AS seccion,
    'CLIENTES' AS tipo,
    COUNT(*) AS total,
    CASE
        WHEN COUNT(*) > 0 THEN '✅ OK'
        ELSE '⚠️ Sin clientes - Añade clientes para facturar'
    END AS estado
FROM clientes
WHERE activo = TRUE

UNION ALL

-- Verificar que hay artículos
SELECT
    '✅ VERIFICACIÓN DE DATOS',
    'ARTÍCULOS',
    COUNT(*),
    CASE
        WHEN COUNT(*) > 0 THEN '✅ OK'
        ELSE '⚠️ Sin artículos - Añade productos para facturar'
    END
FROM articulos
WHERE activo = TRUE

UNION ALL

-- Verificar que hay almacenes
SELECT
    '✅ VERIFICACIÓN DE DATOS',
    'ALMACENES',
    COUNT(*),
    CASE
        WHEN COUNT(*) > 0 THEN '✅ OK'
        ELSE '⚠️ Sin almacenes - Crea al menos un almacén'
    END
FROM almacenes;

-- ============================================
-- NOTAS IMPORTANTES
-- ============================================

SELECT
    '📋 PRÓXIMOS PASOS' AS seccion,
    'ACCIÓN' AS columna1,
    'DESCRIPCIÓN' AS columna2

UNION ALL SELECT '', '1. Actualizar datos reales', 'Edita este SQL con la dirección, teléfono y email reales de GRUPO BABO'
UNION ALL SELECT '', '2. Obtener certificado AEAT', 'Solicita certificado digital de la FNMT para Verifactu'
UNION ALL SELECT '', '3. Configurar application.properties', 'Añade la ruta y contraseña del certificado'
UNION ALL SELECT '', '4. Probar en preproducción', 'Usa el entorno de pruebas de la AEAT primero'
UNION ALL SELECT '', '5. Cambiar a producción', 'Cuando todo funcione, cambia al endpoint real';

-- ============================================
-- FIN DEL SCRIPT
-- ============================================

SELECT
    '🎉 CONFIGURACIÓN COMPLETADA' AS resultado,
    'Ahora puedes arrancar la aplicación con: .\\scripts\\iniciar.bat' AS siguiente_paso;

