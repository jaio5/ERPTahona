-- ======================================================
-- FASE 1: LEGALIZACIÓN - Scripts SQL
-- ERP Panadería Tahona
-- Fecha: 26 de diciembre de 2025
-- ======================================================

-- ==========================================
-- 1. TABLA USUARIOS
-- ==========================================
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    nombre_completo VARCHAR(100) NOT NULL,
    telefono VARCHAR(20),
    rol_id BIGINT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    bloqueado BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ultimo_login DATETIME,
    fecha_cambio_password DATETIME,
    intentos_fallidos INT DEFAULT 0,
    requiere_cambio_password BOOLEAN NOT NULL DEFAULT FALSE,
    token_recuperacion VARCHAR(255),
    fecha_expiracion_token DATETIME,
    observaciones TEXT,
    INDEX idx_usuario_username (username),
    INDEX idx_usuario_email (email),
    INDEX idx_usuario_rol (rol_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- 2. TABLA ROLES
-- ==========================================
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    permisos JSON,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    es_sistema BOOLEAN NOT NULL DEFAULT FALSE,
    INDEX idx_rol_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Foreign key de usuarios a roles
ALTER TABLE usuarios
ADD CONSTRAINT fk_usuario_rol
FOREIGN KEY (rol_id) REFERENCES roles(id);

-- ==========================================
-- 3. TABLA AUDITORIA_ACCIONES
-- ==========================================
CREATE TABLE IF NOT EXISTS auditoria_acciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT,
    usuario_nombre VARCHAR(50),
    tipo_accion VARCHAR(50) NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    entidad_tipo VARCHAR(100),
    entidad_id VARCHAR(50),
    descripcion TEXT,
    modulo VARCHAR(100),
    ip VARCHAR(45),
    user_agent VARCHAR(255),
    valores_anteriores JSON,
    valores_nuevos JSON,
    resultado VARCHAR(20),
    mensaje_error TEXT,
    metadata JSON,
    INDEX idx_auditoria_fecha (fecha),
    INDEX idx_auditoria_usuario (usuario_id),
    INDEX idx_auditoria_entidad (entidad_tipo, entidad_id),
    INDEX idx_auditoria_tipo (tipo_accion),
    INDEX idx_auditoria_modulo (modulo),
    CONSTRAINT fk_auditoria_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- 4. TABLA RGPD_CONSENTIMIENTOS
-- ==========================================
CREATE TABLE IF NOT EXISTS rgpd_consentimientos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT,
    tipo_consentimiento VARCHAR(50) NOT NULL,
    otorgado BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_consentimiento DATETIME NOT NULL,
    fecha_revocacion DATETIME,
    ip_origen VARCHAR(45),
    texto_consentimiento TEXT,
    version_politica VARCHAR(20),
    canal VARCHAR(50),
    email VARCHAR(100),
    nombre VARCHAR(200),
    metadata JSON,
    usuario_registro_id BIGINT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    INDEX idx_rgpd_cliente (cliente_id),
    INDEX idx_rgpd_tipo (tipo_consentimiento),
    INDEX idx_rgpd_fecha (fecha_consentimiento),
    INDEX idx_rgpd_email (email),
    CONSTRAINT fk_rgpd_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE CASCADE,
    CONSTRAINT fk_rgpd_usuario FOREIGN KEY (usuario_registro_id) REFERENCES usuarios(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- 5. TABLA RGPD_ACCESOS_DATOS
-- ==========================================
CREATE TABLE IF NOT EXISTS rgpd_accesos_datos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT,
    cliente_id BIGINT,
    tipo_acceso VARCHAR(50) NOT NULL,
    fecha_acceso DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip VARCHAR(45),
    modulo VARCHAR(100),
    motivo TEXT,
    campos_accedidos JSON,
    metadata JSON,
    INDEX idx_acceso_fecha (fecha_acceso),
    INDEX idx_acceso_usuario (usuario_id),
    INDEX idx_acceso_cliente (cliente_id),
    INDEX idx_acceso_tipo (tipo_acceso),
    CONSTRAINT fk_acceso_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL,
    CONSTRAINT fk_acceso_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- 6. TABLA RGPD_SOLICITUDES
-- ==========================================
CREATE TABLE IF NOT EXISTS rgpd_solicitudes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT,
    email_solicitante VARCHAR(100) NOT NULL,
    nombre_solicitante VARCHAR(200) NOT NULL,
    tipo_derecho VARCHAR(50) NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    fecha_solicitud DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_limite_respuesta DATETIME NOT NULL,
    fecha_respuesta DATETIME,
    descripcion TEXT,
    respuesta TEXT,
    usuario_responsable_id BIGINT,
    ip_origen VARCHAR(45),
    canal VARCHAR(50),
    identidad_verificada BOOLEAN DEFAULT FALSE,
    ruta_archivo_respuesta VARCHAR(500),
    notas_internas TEXT,
    INDEX idx_rgpd_sol_estado (estado),
    INDEX idx_rgpd_sol_fecha (fecha_solicitud),
    INDEX idx_rgpd_sol_limite (fecha_limite_respuesta),
    INDEX idx_rgpd_sol_tipo (tipo_derecho),
    INDEX idx_rgpd_sol_email (email_solicitante),
    CONSTRAINT fk_rgpd_sol_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE CASCADE,
    CONSTRAINT fk_rgpd_sol_responsable FOREIGN KEY (usuario_responsable_id) REFERENCES usuarios(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- 7. AÑADIR CAMPOS A TABLA FACTURAS
-- ==========================================
ALTER TABLE facturas
ADD COLUMN IF NOT EXISTS serie VARCHAR(20) AFTER numero,
ADD COLUMN IF NOT EXISTS fecha_operacion DATE AFTER fecha,
ADD COLUMN IF NOT EXISTS tipo_factura VARCHAR(30) DEFAULT 'ORDINARIA' AFTER estado,
ADD COLUMN IF NOT EXISTS medio_cobro VARCHAR(50) AFTER tipo_factura,
ADD COLUMN IF NOT EXISTS retencion_irpf DECIMAL(10,2) DEFAULT 0.00 AFTER medio_cobro,
ADD COLUMN IF NOT EXISTS porcentaje_retencion DECIMAL(5,2) DEFAULT 0.00 AFTER retencion_irpf,
ADD COLUMN IF NOT EXISTS fecha_vencimiento DATE AFTER porcentaje_retencion,
ADD COLUMN IF NOT EXISTS referencia_pedido VARCHAR(50) AFTER fecha_vencimiento,
ADD COLUMN IF NOT EXISTS numero_albaran VARCHAR(50) AFTER referencia_pedido,
ADD COLUMN IF NOT EXISTS inversion_sujeto_pasivo BOOLEAN DEFAULT FALSE AFTER numero_albaran,
ADD COLUMN IF NOT EXISTS criterio_caja BOOLEAN DEFAULT FALSE AFTER inversion_sujeto_pasivo,
ADD COLUMN IF NOT EXISTS operacion_triangular BOOLEAN DEFAULT FALSE AFTER criterio_caja,
ADD COLUMN IF NOT EXISTS factura_rectificada_numero VARCHAR(50) AFTER operacion_triangular,
ADD COLUMN IF NOT EXISTS factura_rectificada_fecha DATE AFTER factura_rectificada_numero,
ADD COLUMN IF NOT EXISTS motivo_rectificacion VARCHAR(500) AFTER factura_rectificada_fecha,
ADD COLUMN IF NOT EXISTS tipo_rectificacion VARCHAR(20) AFTER motivo_rectificacion,
ADD COLUMN IF NOT EXISTS base_imponible DECIMAL(10,2) DEFAULT 0.00 AFTER tipo_rectificacion,
ADD COLUMN IF NOT EXISTS total_iva DECIMAL(10,2) DEFAULT 0.00 AFTER base_imponible,
ADD COLUMN IF NOT EXISTS total_recargo DECIMAL(10,2) DEFAULT 0.00 AFTER total_iva,
ADD COLUMN IF NOT EXISTS observaciones TEXT AFTER total_recargo,
ADD COLUMN IF NOT EXISTS verifactu_qr TEXT AFTER observaciones_revision,
ADD COLUMN IF NOT EXISTS verifactu_hash VARCHAR(128) AFTER verifactu_qr,
ADD COLUMN IF NOT EXISTS verifactu_hash_anterior VARCHAR(128) AFTER verifactu_hash;

-- Índices para facturas
CREATE INDEX IF NOT EXISTS idx_factura_serie ON facturas(serie);
CREATE INDEX IF NOT EXISTS idx_factura_tipo ON facturas(tipo_factura);
CREATE INDEX IF NOT EXISTS idx_factura_estado ON facturas(estado);
CREATE INDEX IF NOT EXISTS idx_factura_verifactu ON facturas(verifactu_enviada);
CREATE INDEX IF NOT EXISTS idx_factura_fecha_venc ON facturas(fecha_vencimiento);

-- ==========================================
-- 8. INSERTAR ROLES POR DEFECTO
-- ==========================================
INSERT INTO roles (nombre, descripcion, es_sistema, activo, permisos) VALUES
('ADMINISTRADOR', 'Acceso total al sistema', TRUE, TRUE, '{
    "clientes": {"ver": true, "crear": true, "editar": true, "eliminar": true, "exportar": true},
    "proveedores": {"ver": true, "crear": true, "editar": true, "eliminar": true, "exportar": true},
    "articulos": {"ver": true, "crear": true, "editar": true, "eliminar": true, "exportar": true},
    "facturas": {"ver": true, "crear": true, "editar": true, "eliminar": true, "exportar": true, "emitir": true, "anular": true},
    "albaranes": {"ver": true, "crear": true, "editar": true, "eliminar": true, "exportar": true},
    "almacenes": {"ver": true, "crear": true, "editar": true, "eliminar": true},
    "usuarios": {"ver": true, "crear": true, "editar": true, "eliminar": true},
    "configuracion": {"ver": true, "editar": true},
    "verifactu": {"ver": true, "enviar": true},
    "rgpd": {"ver": true, "gestionar": true},
    "auditoria": {"ver": true, "exportar": true}
}'),
('GERENTE', 'Gestión completa excepto configuración crítica', FALSE, TRUE, '{
    "clientes": {"ver": true, "crear": true, "editar": true, "eliminar": false, "exportar": true},
    "proveedores": {"ver": true, "crear": true, "editar": true, "eliminar": false, "exportar": true},
    "articulos": {"ver": true, "crear": true, "editar": true, "eliminar": false, "exportar": true},
    "facturas": {"ver": true, "crear": true, "editar": true, "eliminar": false, "exportar": true, "emitir": true, "anular": false},
    "albaranes": {"ver": true, "crear": true, "editar": true, "eliminar": false, "exportar": true},
    "almacenes": {"ver": true, "crear": true, "editar": true, "eliminar": false},
    "usuarios": {"ver": true, "crear": false, "editar": false, "eliminar": false},
    "configuracion": {"ver": true, "editar": false},
    "verifactu": {"ver": true, "enviar": true},
    "rgpd": {"ver": true, "gestionar": true},
    "auditoria": {"ver": true, "exportar": true}
}'),
('VENDEDOR', 'Acceso a clientes, facturas y artículos', FALSE, TRUE, '{
    "clientes": {"ver": true, "crear": true, "editar": true, "eliminar": false, "exportar": false},
    "proveedores": {"ver": true, "crear": false, "editar": false, "eliminar": false, "exportar": false},
    "articulos": {"ver": true, "crear": false, "editar": false, "eliminar": false, "exportar": false},
    "facturas": {"ver": true, "crear": true, "editar": true, "eliminar": false, "exportar": false, "emitir": false, "anular": false},
    "albaranes": {"ver": true, "crear": true, "editar": true, "eliminar": false, "exportar": false},
    "almacenes": {"ver": true, "crear": false, "editar": false, "eliminar": false},
    "usuarios": {"ver": false, "crear": false, "editar": false, "eliminar": false},
    "configuracion": {"ver": false, "editar": false},
    "verifactu": {"ver": false, "enviar": false},
    "rgpd": {"ver": false, "gestionar": false},
    "auditoria": {"ver": false, "exportar": false}
}'),
('ALMACEN', 'Gestión de almacén y artículos', FALSE, TRUE, '{
    "clientes": {"ver": true, "crear": false, "editar": false, "eliminar": false, "exportar": false},
    "proveedores": {"ver": true, "crear": false, "editar": false, "eliminar": false, "exportar": false},
    "articulos": {"ver": true, "crear": true, "editar": true, "eliminar": false, "exportar": false},
    "facturas": {"ver": true, "crear": false, "editar": false, "eliminar": false, "exportar": false, "emitir": false, "anular": false},
    "albaranes": {"ver": true, "crear": true, "editar": true, "eliminar": false, "exportar": false},
    "almacenes": {"ver": true, "crear": true, "editar": true, "eliminar": false},
    "usuarios": {"ver": false, "crear": false, "editar": false, "eliminar": false},
    "configuracion": {"ver": false, "editar": false},
    "verifactu": {"ver": false, "enviar": false},
    "rgpd": {"ver": false, "gestionar": false},
    "auditoria": {"ver": false, "exportar": false}
}'),
('CONTABLE', 'Acceso a toda la información financiera y fiscal', FALSE, TRUE, '{
    "clientes": {"ver": true, "crear": false, "editar": false, "eliminar": false, "exportar": true},
    "proveedores": {"ver": true, "crear": false, "editar": false, "eliminar": false, "exportar": true},
    "articulos": {"ver": true, "crear": false, "editar": false, "eliminar": false, "exportar": true},
    "facturas": {"ver": true, "crear": false, "editar": false, "eliminar": false, "exportar": true, "emitir": true, "anular": false},
    "albaranes": {"ver": true, "crear": false, "editar": false, "eliminar": false, "exportar": true},
    "almacenes": {"ver": true, "crear": false, "editar": false, "eliminar": false},
    "usuarios": {"ver": false, "crear": false, "editar": false, "eliminar": false},
    "configuracion": {"ver": true, "editar": false},
    "verifactu": {"ver": true, "enviar": true},
    "rgpd": {"ver": false, "gestionar": false},
    "auditoria": {"ver": true, "exportar": true}
}');

-- ==========================================
-- 9. CREAR USUARIO ADMINISTRADOR POR DEFECTO
-- Contraseña: admin123 (CAMBIAR INMEDIATAMENTE EN PRODUCCIÓN)
-- Hash BCrypt de 'admin123'
-- ==========================================
INSERT INTO usuarios (username, password, email, nombre_completo, rol_id, activo, bloqueado, requiere_cambio_password, fecha_creacion)
SELECT
    'admin',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'admin@tahona.com',
    'Administrador del Sistema',
    r.id,
    TRUE,
    FALSE,
    TRUE,  -- Requiere cambio en el primer login
    NOW()
FROM roles r WHERE r.nombre = 'ADMINISTRADOR'
LIMIT 1
ON DUPLICATE KEY UPDATE id=id; -- No hacer nada si ya existe

-- ==========================================
-- 10. POLÍTICA DE PRIVACIDAD POR DEFECTO
-- ==========================================
CREATE TABLE IF NOT EXISTS politicas_privacidad (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version VARCHAR(20) NOT NULL UNIQUE,
    fecha_vigencia DATE NOT NULL,
    texto_html TEXT NOT NULL,
    texto_plano TEXT NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_politica_version (version),
    INDEX idx_politica_activa (activa)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO politicas_privacidad (version, fecha_vigencia, activa, texto_plano) VALUES
('1.0', CURDATE(), TRUE,
'POLÍTICA DE PRIVACIDAD Y PROTECCIÓN DE DATOS

De conformidad con el Reglamento (UE) 2016/679 del Parlamento Europeo y del Consejo, de 27 de abril de 2016, relativo a la protección de las personas físicas en lo que respecta al tratamiento de datos personales y a la libre circulación de estos datos (RGPD) y la Ley Orgánica 3/2018, de 5 de diciembre, de Protección de Datos Personales y garantía de los derechos digitales (LOPDGDD), le informamos sobre el tratamiento de sus datos personales.

RESPONSABLE DEL TRATAMIENTO:
[NOMBRE EMPRESA]
CIF: [CIF]
Dirección: [DIRECCIÓN]
Email: [EMAIL]
Teléfono: [TELÉFONO]

FINALIDAD DEL TRATAMIENTO:
- Gestión comercial y administrativa de la relación contractual
- Emisión de facturas y documentos fiscales
- Gestión de cobros y pagos
- Cumplimiento de obligaciones legales

BASE JURÍDICA:
- Ejecución de un contrato
- Cumplimiento de obligaciones legales
- Consentimiento del interesado (para finalidades adicionales)

CONSERVACIÓN:
Los datos se conservarán durante el tiempo necesario para cumplir con la finalidad para la que se recabaron y para determinar las posibles responsabilidades que se pudieran derivar de dicha finalidad y del tratamiento de los datos. En todo caso, se conservarán durante un mínimo de 6 años de acuerdo con la normativa fiscal vigente.

DERECHOS:
Puede ejercer los siguientes derechos:
- Derecho de acceso: conocer qué datos tratamos sobre usted
- Derecho de rectificación: corregir datos inexactos
- Derecho de supresión: solicitar la eliminación de sus datos
- Derecho de oposición: oponerse al tratamiento
- Derecho de limitación: limitar el tratamiento
- Derecho de portabilidad: recibir sus datos en formato estructurado

Para ejercer estos derechos, puede contactar con nosotros en [EMAIL] o en la dirección indicada.

Asimismo, tiene derecho a presentar una reclamación ante la Agencia Española de Protección de Datos (www.aepd.es).

SEGURIDAD:
Hemos adoptado medidas técnicas y organizativas para garantizar la seguridad de sus datos personales y evitar su alteración, pérdida, tratamiento o acceso no autorizado.
');

-- ==========================================
-- FIN DEL SCRIPT
-- ==========================================

-- Verificar que las tablas se crearon correctamente
SELECT 'USUARIOS' as Tabla, COUNT(*) as Registros FROM usuarios
UNION ALL
SELECT 'ROLES', COUNT(*) FROM roles
UNION ALL
SELECT 'RGPD_CONSENTIMIENTOS', COUNT(*) FROM rgpd_consentimientos
UNION ALL
SELECT 'RGPD_ACCESOS_DATOS', COUNT(*) FROM rgpd_accesos_datos
UNION ALL
SELECT 'RGPD_SOLICITUDES', COUNT(*) FROM rgpd_solicitudes
UNION ALL
SELECT 'AUDITORIA_ACCIONES', COUNT(*) FROM auditoria_acciones
UNION ALL
SELECT 'POLITICAS_PRIVACIDAD', COUNT(*) FROM politicas_privacidad;

