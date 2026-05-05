CREATE TABLE IF NOT EXISTS verifactu_evidence (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  cert_fingerprint VARCHAR(128),
  created_at TIMESTAMP NULL,
  factura_id VARCHAR(100) NOT NULL,
  fecha_emision TIMESTAMP NULL,
  hash VARCHAR(128) NOT NULL,
  hash_anterior VARCHAR(128),
  metadata JSON,
  numero VARCHAR(255),
  serie VARCHAR(255),
  signature LONGBLOB,
  estado VARCHAR(50),
  error_message TEXT,
  fecha_envio TIMESTAMP NULL,
  codigo_respuesta_aeat VARCHAR(100),
  CONSTRAINT uk_verifactu_evidence_factura_id UNIQUE (factura_id),
  CONSTRAINT uk_verifactu_evidence_hash UNIQUE (hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS auditoria_acciones (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  usuario_id BIGINT,
  usuario_nombre VARCHAR(50),
  tipo_accion VARCHAR(50) NOT NULL,
  fecha DATETIME NOT NULL,
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
  CONSTRAINT fk_auditoria_usuario FOREIGN KEY (usuario_id) REFERENCES users(id),
  INDEX idx_auditoria_fecha (fecha),
  INDEX idx_auditoria_usuario (usuario_id),
  INDEX idx_auditoria_entidad (entidad_tipo, entidad_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS factura_series (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  serie VARCHAR(20) NOT NULL,
  ejercicio INT NOT NULL,
  ultimo_numero BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT uk_factura_series_serie_ejercicio UNIQUE (serie, ejercicio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

UPDATE facturas
SET serie = 'GEN'
WHERE serie IS NULL OR TRIM(serie) = '';

ALTER TABLE facturas
ADD CONSTRAINT uk_facturas_serie_numero UNIQUE (serie, numero);
