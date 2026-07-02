-- Migración inicial: crear tablas principales necesarias para la aplicación
-- Generado automáticamente por el asistente

CREATE TABLE IF NOT EXISTS roles (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(50) NOT NULL UNIQUE,
  descripcion VARCHAR(255),
  permisos JSON,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  es_sistema BOOLEAN NOT NULL DEFAULT FALSE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  role VARCHAR(20),
  nombre VARCHAR(100),
  email VARCHAR(100),
  fecha_creacion DATETIME,
  fecha_modificacion DATETIME,
  intentos_fallidos INT DEFAULT 0,
  bloqueado BOOLEAN DEFAULT FALSE,
  fecha_bloqueo DATETIME,
  ultimo_acceso DATETIME,
  token_recuperacion VARCHAR(255),
  fecha_expiracion_token DATETIME,
  requiere_cambio_password BOOLEAN DEFAULT FALSE,
  rol_id BIGINT,
  CONSTRAINT fk_users_rol FOREIGN KEY (rol_id) REFERENCES roles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS empresa_config (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre_empresa VARCHAR(255) NOT NULL,
  nombre_comercial VARCHAR(255),
  cif VARCHAR(20) NOT NULL,
  direccion TEXT,
  codigo_postal VARCHAR(10),
  ciudad VARCHAR(100),
  provincia VARCHAR(100),
  pais VARCHAR(100) DEFAULT 'España',
  telefono VARCHAR(20),
  email VARCHAR(255),
  web VARCHAR(255),
  registro_mercantil VARCHAR(255),
  registro_sanitario VARCHAR(100),
  verifactu_habilitado BOOLEAN DEFAULT TRUE,
  verifactu_nif_emisor VARCHAR(20),
  verifactu_nombre_sistema VARCHAR(100),
  verifactu_version_sistema VARCHAR(50),
  verifactu_id_dispositivo VARCHAR(50),
  activo BOOLEAN DEFAULT TRUE,
  fecha_creacion DATETIME,
  fecha_modificacion DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS clientes (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  codigo VARCHAR(50) NOT NULL,
  nombre VARCHAR(255) NOT NULL,
  cif VARCHAR(50),
  telefono VARCHAR(20),
  email VARCHAR(100),
  direccion VARCHAR(255),
  poblacion VARCHAR(100),
  codigo_postal VARCHAR(20),
  provincia VARCHAR(50),
  notas TEXT,
  activo BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS articulos (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  codigo VARCHAR(50) NOT NULL,
  descripcion VARCHAR(255),
  nombre VARCHAR(255),
  codigo_barras VARCHAR(50),
  categoria VARCHAR(100),
  familia VARCHAR(100),
  unidad VARCHAR(20),
  iva DECIMAL(5,2),
  pvp DECIMAL(10,2) DEFAULT 0.00,
  coste DECIMAL(10,2) DEFAULT 0.00,
  stock DECIMAL(10,2),
  stock_minimo DECIMAL(10,2),
  stock_maximo DECIMAL(10,2),
  control_stock BOOLEAN,
  activo BOOLEAN DEFAULT TRUE,
  punto_pedido DECIMAL(10,2)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS almacenes (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  codigo VARCHAR(50) NOT NULL,
  nombre VARCHAR(255),
  activo BOOLEAN DEFAULT TRUE,
  descripcion VARCHAR(1000),
  capacidad DECIMAL(10,2),
  disponible DECIMAL(10,2),
  localidad VARCHAR(255),
  responsable VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS facturas (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  numero VARCHAR(50) NOT NULL,
  fecha DATE,
  cliente_id BIGINT,
  total DECIMAL(10,2) DEFAULT 0.00,
  pagado DECIMAL(10,2) DEFAULT 0.00,
  pagada BOOLEAN NOT NULL DEFAULT FALSE,
  estado VARCHAR(20) DEFAULT 'BORRADOR',
  verifactu_enviada BOOLEAN DEFAULT FALSE,
  fecha_emision_verifactu DATETIME,
  observaciones_revision TEXT,
  serie VARCHAR(20),
  fecha_operacion DATE,
  tipo_factura VARCHAR(30) DEFAULT 'ORDINARIA',
  medio_cobro VARCHAR(50),
  retencion_irpf DECIMAL(10,2) DEFAULT 0.00,
  porcentaje_retencion DECIMAL(5,2) DEFAULT 0.00,
  fecha_vencimiento DATE,
  referencia_pedido VARCHAR(50),
  numero_albaran VARCHAR(50),
  inversion_sujeto_pasivo BOOLEAN DEFAULT FALSE,
  criterio_caja BOOLEAN DEFAULT FALSE,
  operacion_triangular BOOLEAN DEFAULT FALSE,
  factura_rectificada_numero VARCHAR(50),
  factura_rectificada_fecha DATE,
  motivo_rectificacion VARCHAR(500),
  tipo_rectificacion VARCHAR(20),
  base_imponible DECIMAL(10,2) DEFAULT 0.00,
  total_iva DECIMAL(10,2) DEFAULT 0.00,
  tipo_impositivo DECIMAL(5,2),
  total_recargo DECIMAL(10,2) DEFAULT 0.00,
  observaciones TEXT,
  verifactu_qr TEXT,
  verifactu_hash VARCHAR(128),
  verifactu_hash_anterior VARCHAR(128),
  verifactu_procesado BOOLEAN DEFAULT FALSE,
  verifactu_csv VARCHAR(16),
  verifactu_estado VARCHAR(20),
  verifactu_fecha_registro DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS factura_lineas (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  factura_id BIGINT,
  articulo_id BIGINT,
  descripcion VARCHAR(255),
  cantidad DECIMAL(10,2),
  precio_unitario DECIMAL(10,2),
  iva DECIMAL(5,2),
  total DECIMAL(10,2)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Inserts iniciales de datos (solo para entorno de desarrollo)
INSERT INTO roles (nombre, descripcion, activo, es_sistema) VALUES
('ADMIN', 'Administrador del sistema', TRUE, TRUE),
('USUARIO', 'Usuario estándar', TRUE, FALSE)
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre);

INSERT INTO users (username, password, enabled, role, nombre, email, fecha_creacion) VALUES
('admin', 'admin', TRUE, 'ADMIN', 'Administrador', 'admin@example.com', NOW())
ON DUPLICATE KEY UPDATE username=VALUES(username);

INSERT INTO empresa_config (nombre_empresa, cif, verifactu_habilitado, activo, fecha_creacion) VALUES
('ERP Tahona', 'X0000000X', TRUE, TRUE, NOW())
ON DUPLICATE KEY UPDATE nombre_empresa=VALUES(nombre_empresa);
